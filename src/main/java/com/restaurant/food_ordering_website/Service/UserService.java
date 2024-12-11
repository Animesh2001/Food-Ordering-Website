package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Configuration.OrderProcess;
import com.restaurant.food_ordering_website.DTO.OrderHistory;
import com.restaurant.food_ordering_website.DTO.OrderHistoryResponse;
import com.restaurant.food_ordering_website.DTO.OrderItem;
import com.restaurant.food_ordering_website.Model.*;
import com.restaurant.food_ordering_website.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    @Autowired
    private PriorityTrackerRepository priorityTrackerRepository;

    @Autowired
    private OrderProcess orderProcess;

    @Autowired
    private OrderCountRepository orderCountRepository;

    public User findUserById(Integer userId) {
        try {
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching User by Id " + e.getMessage());
        }
    }

    public List<Restaurant> findRestaurantByCity(String city) {
        try {
            return restaurantRepository.findByAddressOrderByPriorityAsc(city);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching User by Id " + e.getMessage());
        }
    }

    public Integer placeOrder(List<OrderItem> orderedItems, Integer restaurantId, Integer userId) {
        try{
            int totalValue = getTotalValueOfTheOrder(orderedItems,userId);
            if(checkCustomerHasSufficientCredit(userId,totalValue)){
                int groupOrderId = insertOrderDetails(orderedItems,userId,restaurantId);
                Cart cart = addToCart(groupOrderId,restaurantId,userId, totalValue);
                makeEntryToPriorityTracker(restaurantId, totalValue);
                deductAmountFromCustomerAccount(userId,totalValue,cart);
                System.out.println("Order placed at date "+ getFormattedDate(cart.getDate()) + "at time " + getFormattedTime(cart.getDate()) + "of id: "+ groupOrderId);
                return groupOrderId;
            }else{
                throw new RuntimeException("Customer do not have sufficient credit");
            }
        }catch(Exception e){
            log.error("Exception while placing the order : [{}]",e.getMessage(),e);
            throw e;
        }
    }

    private void makeEntryToPriorityTracker(int restaurantId, int totalValue) {
        PriorityTracker priorityTracker = priorityTrackerRepository.findByRestaurantId(restaurantId);
        if(priorityTracker!=null){
            int totalOrderTillNow = priorityTracker.getOrder();
            priorityTracker.setOrder(totalOrderTillNow+1);
            int totalProfitTillNow = priorityTracker.getProfit();
            priorityTracker.setProfit(totalProfitTillNow+totalValue);
        }else{
            priorityTracker = new PriorityTracker();
            priorityTracker.setRestaurant_id(restaurantId);
            priorityTracker.setOrder(1);
            priorityTracker.setProfit(totalValue);
        }
        priorityTrackerRepository.save(priorityTracker);
    }

    private boolean checkCustomerHasSufficientCredit(int userId, int totalValue) {
        User user = userRepository.findById(userId).get();
        return user.getCredit()>=totalValue;
    }

    private int insertOrderDetails(List<OrderItem> orderedItems, int userId, int restaurantId) {

        Order firstOrder = new Order();

        //fetch the item on the basis of id.
        int itemId = orderedItems.get(0).getId();
        MenuItems menuItem = menuItemsRepository.findById(itemId).get();
        int remainingQuantity = menuItem.getQuantityAvailable() - orderedItems.get(0).getItemQuantity();
        if(remainingQuantity == 0){
            menuItem.setAvailable(false);
        }
        menuItem.setQuantityAvailable(remainingQuantity);
        menuItemsRepository.save(menuItem);

        int currItemPrice = 0;
        firstOrder.setUserId(userId);
        firstOrder.setRestaurantId(restaurantId);
        firstOrder.setItemName(orderedItems.get(0).getItemName());
        firstOrder.setItemPrice(orderedItems.get(0).getItemPrice());
        firstOrder.setItemQuantity(orderedItems.get(0).getItemQuantity());
        firstOrder.setRestaurantId(restaurantId);
        firstOrder.setUserId(userId);
        currItemPrice += (orderedItems.get(0).getItemPrice() * orderedItems.get(0).getItemQuantity());
        firstOrder.setItemTotal(currItemPrice);
        Order savedOrder = orderRepository.save(firstOrder);
        Integer groupOrderId = savedOrder.getOrderId();

        savedOrder.setGroupOrderId(groupOrderId);
        orderRepository.save(savedOrder);


        for (int i = 1; i < orderedItems.size(); i++) {
            Order order = new Order();
            order.setUserId(userId);
            order.setRestaurantId(restaurantId);
            order.setItemName(orderedItems.get(i).getItemName());
            order.setItemPrice(orderedItems.get(i).getItemPrice());
            order.setItemQuantity(orderedItems.get(i).getItemQuantity());
            order.setGroupOrderId(groupOrderId);
            order.setRestaurantId(restaurantId);
            order.setUserId(userId);
            int currTotalPrice = (orderedItems.get(i).getItemPrice()*orderedItems.get(i).getItemQuantity());
            order.setItemTotal(currTotalPrice);
            menuItem = menuItemsRepository.findById(orderedItems.get(i).getId()).get();
            remainingQuantity = menuItem.getQuantityAvailable() - orderedItems.get(i).getItemQuantity();
            menuItem.setQuantityAvailable(remainingQuantity);
            if(remainingQuantity == 0){
                menuItem.setAvailable(false);
            }
            menuItemsRepository.save(menuItem);
            orderRepository.save(order);
        }
        return groupOrderId;
    }



    private int getTotalValueOfTheOrder(List<OrderItem>orderedItems, Integer userId) {
        int totalValue = 0;
        for (int i = 0; i < orderedItems.size(); i++) {
            int currTotalPrice = (orderedItems.get(i).getItemPrice()*orderedItems.get(i).getItemQuantity());
            totalValue+=currTotalPrice;
        }
        return totalValue;
    }

    private void updateOrderStatus(Cart cart) {
        if(!checkRestaurantAvailable(cart.getRestaurantId())){
            cart.setStatus("Waiting to Accepted");
        }
        else if(updateOrderCountOfRestaurant(cart.getRestaurantId())){
            cart.setStatus("Waiting to Accepted");
        }else{
            cart.setStatus("Accepted");
        }
    }

    private boolean checkRestaurantAvailable(Integer restaurantId) {
       Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
       return restaurant.isAvailable();
    }

    private boolean updateOrderCountOfRestaurant(Integer restaurantId) {
        if(orderCountRepository.findById(restaurantId).isPresent()){
            OrderCount orderCount = orderCountRepository.findById(restaurantId).get();
            if(orderCount.getTotalOrdersProcessing()>=3){
                return true;
            }else{
                orderCount.setTotalOrdersProcessing(orderCount.getTotalOrdersProcessing()+1);
                orderCountRepository.save(orderCount);
                return false;
            }
        }else{
            OrderCount orderCount = new OrderCount();
            orderCount.setRestaurantId(restaurantId);
            orderCount.setTotalOrdersProcessing(1);
            orderCountRepository.save(orderCount);
            return false;
        }
    }

    private void deductAmountFromCustomerAccount(Integer userId, int totalValue, Cart cart) {
        User user = userRepository.findById(userId).get();
        if(checkAmountAvailable(totalValue,user.getCredit())){
            user.setCredit(user.getCredit()-totalValue);
            userRepository.save(user);
        }else{
            log.error("Insufficient amount in user account to place the order");
            orderProcess.cancelAndRevertOrder(cart,false);
            throw new RuntimeException("Sufficient amount is not available in user account");
        }
    }

    private boolean checkAmountAvailable(int totalValue, int credit) {
        return credit - totalValue >=0;
    }

    public Cart addToCart(Integer groupOrderId,Integer restaurantId, Integer userId, int totalValue){
        Cart cart = new Cart();
        cart.setOrderId(groupOrderId);
        cart.setUserId(userId);
        cart.setRestaurantId(restaurantId);
        cart.setTotalValue(totalValue);
        updateOrderStatus(cart);
        cart.setDate(ZonedDateTime.now().toInstant().toEpochMilli());
        cartRepository.save(cart);
        return cart;
    }

    public List<OrderHistoryResponse> getOrderHistory(int userId, int restaurantId) throws Exception {
        Map<Integer,List<Order>> map = getOrdersGroupedByGroupOrderId(userId,restaurantId);
        List<OrderHistoryResponse> orderHistoryResponses = new ArrayList<>();
        for(int i: map.keySet()){
            OrderHistoryResponse orderHistoryResponse = new OrderHistoryResponse();
            List<Order> list = map.get(i);
            List<OrderHistory> childOrderHistory = new ArrayList<>();
            for(Order order: list){
                OrderHistory orderHistory = new OrderHistory();
                orderHistory.setItemName(order.getItemName());
                orderHistory.setItemQuantity(order.getItemQuantity());
                orderHistory.setItemPrice(order.getItemPrice());
                orderHistory.setItemTotalPrice(order.getItemTotal());
                childOrderHistory.add(orderHistory);
            }
            orderHistoryResponse.setOrderItems(childOrderHistory);
            Cart cart = getCartByOrderId(i);
            orderHistoryResponse.setDate(getFormattedDate(cart.getDate()));
            orderHistoryResponse.setTime(getFormattedTime(cart.getDate()));
            orderHistoryResponse.setDateInEpochTimeMillis(cart.getDate());
            orderHistoryResponse.setStatus(cart.getStatus());
            orderHistoryResponse.setTotalValue(cart.getTotalValue());
            orderHistoryResponses.add(orderHistoryResponse);
        }

        return sortOrderHistoryResponsesByDate(orderHistoryResponses);
    }


    public List<OrderHistoryResponse> sortOrderHistoryResponsesByDate(List<OrderHistoryResponse> orderHistoryResponses) {
        orderHistoryResponses.sort(Comparator.comparing(OrderHistoryResponse::getDateInEpochTimeMillis).reversed());
        return orderHistoryResponses;
    }

    public Cart getCartByOrderId(Integer orderId) throws Exception {
        return cartRepository.findByOrderId(orderId)
                .orElseThrow(() -> new Exception("Cart not found for orderId: " + orderId));
    }


    public  Map<Integer, List<Order>>  getOrdersGroupedByGroupOrderId(Integer userId, int restaurantId) {
        List<Order> orders =  orderRepository.findOrdersByUserIdAndRestaurantId(userId, restaurantId);
        List<Order> deliveredOrders = new ArrayList<>();
        for(Order order: orders){
            int orderId = order.getGroupOrderId();
            if(cartRepository.findByOrderId(orderId).get().getStatus().equals("DELIVERED")){
                deliveredOrders.add(order);
            }
        }

        return deliveredOrders.stream().collect(Collectors.groupingBy(Order::getGroupOrderId));
    }

    public Long getDate(Integer orderId){
        return cartRepository.findByOrderId(orderId)
                .map(Cart::getDate)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for orderId: " + orderId));
    }

    public String getFormattedDate(long timestamp){
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return zonedDateTime.format(dateFormatter);
    }


    public String getFormattedTime(long timestamp){
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return zonedDateTime.format(timeFormatter);
    }

    public String getStatus(Integer orderId){
        return cartRepository.findByOrderId(orderId)
                .map(Cart::getStatus)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for orderId: " + orderId));
    }

    public OrderHistoryResponse getOrderOfUser(int orderId) throws Exception {
        List<Order> groupOrder = getOrdersByGroupOrderId(orderId);
        List<OrderHistory> list = new ArrayList<>();
        for(Order order : groupOrder){
            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setItemPrice(order.getItemPrice());
            orderHistory.setItemName(order.getItemName());
            orderHistory.setItemQuantity(order.getItemQuantity());
            orderHistory.setItemTotalPrice(order.getItemTotal());
            list.add(orderHistory);
        }
        OrderHistoryResponse orderHistoryResponse = new OrderHistoryResponse();
        orderHistoryResponse.setOrderItems(list);

        Cart cart = getCartByOrderId(orderId);
        orderHistoryResponse.setDate(getFormattedDate(cart.getDate()));
        orderHistoryResponse.setTime(getFormattedTime(cart.getDate()));
        orderHistoryResponse.setDateInEpochTimeMillis(cart.getDate());
        orderHistoryResponse.setStatus(cart.getStatus());
        orderHistoryResponse.setTotalValue(cart.getTotalValue());

        return orderHistoryResponse;

    }

    public List<Order> getOrdersByGroupOrderId(Integer groupOrderId) {
        return orderRepository.findByGroupOrderId(groupOrderId);
    }

    public void updateOrderCount(int restaurantId) {
        OrderCount orderCount = orderCountRepository.findById(restaurantId).get();
        orderCount.setTotalOrdersProcessing(orderCount.getTotalOrdersProcessing()-1);
        orderCountRepository.save(orderCount);
    }

    public void cancelTheOrder(int orderId){
        try{
            Cart cart = cartRepository.findByOrderId(orderId).get();
            if(!cart.getStatus().equals("Accepted")){
                throw new RuntimeException("Order is not in Accepted state");
            }
            cart.setStatus("Cancelled");
            orderProcess.cancelAndRevertOrder(cart, true);
            cartRepository.save(cart);
        }catch(Exception e){
            log.error("Exception while cancelling the order : [{}]",orderId);
            throw e;
        }

    }
}
