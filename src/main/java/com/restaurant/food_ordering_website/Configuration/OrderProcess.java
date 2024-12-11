package com.restaurant.food_ordering_website.Configuration;

import com.restaurant.food_ordering_website.Model.*;
import com.restaurant.food_ordering_website.Repository.*;
import com.restaurant.food_ordering_website.Service.AsyncProcessExecute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class OrderProcess {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderCountRepository orderCountRepository;

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    @Autowired
    private PriorityTrackerRepository priorityTrackerRepository;

    @Autowired
    private AsyncProcessExecute asyncProcessExecute;
    private Integer orderId;

    @Async
    public void updateOrderStatus(Integer orderId) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Cart cart = cartRepository.findByOrderId(orderId).get();
        if(cart.getStatus().equals("Cancelled")){
            log.info("Order id : [{}] got cancelled",orderId);
           cancelAndRevertOrder(cart,true);
        }else{
            asyncProcessExecute.startStatusUpdate(orderId);
        }
    }

    public void cancelAndRevertOrder(Cart cart, boolean amountCredit) {
        if(amountCredit){
            amountCreditedBackToCustomer(cart);
        }
        revertQuantities(cart);
        deleteOrderDetails(cart);
        updateOrderCount(cart);
        priorityTrackerUpdate(cart);
        cart.setStatus("Cancelled");
        cartRepository.save(cart);
    }

    private void priorityTrackerUpdate(Cart cart) {
        PriorityTracker priorityTracker = priorityTrackerRepository.findByRestaurantId(cart.getRestaurantId());
        int prevOrder = priorityTracker.getOrder();
        priorityTracker.setOrder(prevOrder-1);
        int cartProfit = cart.getTotalValue();
        int currProfit = priorityTracker.getProfit() - cartProfit;
        priorityTracker.setProfit(currProfit);
        priorityTrackerRepository.save(priorityTracker);
    }

    private void revertQuantities(Cart cart) {
        List<Order> orders = orderRepository.findByGroupOrderId(cart.getOrderId());
        for(Order order: orders){
            MenuItems menuItems = menuItemsRepository.findByItemNameAndRestaurantId(order.getItemName(),order.getRestaurantId()).get();
            int quantity = menuItems.getQuantityAvailable() + order.getItemQuantity();
            menuItems.setQuantityAvailable(quantity);
            menuItemsRepository.save(menuItems);
        }
    }

    private void updateOrderCount(Cart cart) {
        OrderCount orderCount = orderCountRepository.findById(cart.getRestaurantId()).get();
        int availableCount = orderCount.getTotalOrdersProcessing()-1;
        if(availableCount>0){
            orderCount.setTotalOrdersProcessing(availableCount);
        }else{
            orderCount.setTotalOrdersProcessing(0);
        }
        log.info("CANCELLED restaurant id [{}] total order count updated to [{}]",cart.getRestaurantId(),orderCount.getTotalOrdersProcessing());
        orderCountRepository.save(orderCount);
    }

    private void deleteOrderDetails(Cart cart) {
        int id = cart.getOrderId();
        List<Order> orders = orderRepository.findByGroupOrderId(id);
        for(Order order : orders){
            orderRepository.delete(order);
        }
    }

    private void amountCreditedBackToCustomer(Cart cart) {
        int creditAmount = cart.getTotalValue();
        User user = userRepository.findById(cart.getUserId()).get();
        int totalAmount = user.getCredit() + creditAmount;
        user.setCredit(totalAmount);
        userRepository.save(user);
    }


    public void processExecute(Integer orderId) {
        Cart cart = cartRepository.findByOrderId(orderId).get();
            while(!cart.getStatus().equals("Delivered")) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - cart.getDate();
                System.out.println("current time :" + currentTime);
                System.out.println("cart date: " + cart.getDate());
                System.out.println("elapsed time : " + elapsedTime);
                if (elapsedTime >= 45 * 1000 && !cart.getStatus().equals("Delivered")) { // 30 seconds
                    System.out.println("updated cart id : " + cart.getOrderId() + " status to Delivered");
                    cart.setStatus("Delivered");
                } else if (elapsedTime >= 35 * 1000 && !cart.getStatus().equals("Out for Delivery")) { // 20 seconds
                    System.out.println("updated cart id : " + cart.getOrderId() + " status to Out For Delivery");
                    cart.setStatus("Out for Delivery");
                } else if (elapsedTime >= 25 * 1000 && !cart.getStatus().equals("Ready")) { // 10 seconds
                    System.out.println("updated cart id : " + cart.getOrderId() + " status to Ready");
                    cart.setStatus("Ready");
                }
                cartRepository.save(cart);
                System.out.println("--------");
                System.out.println("----------");
            }
    }
}
