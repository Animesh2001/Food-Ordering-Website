package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.OrderCount;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.OrderCountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AsyncProcessExecute {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderCountRepository orderCountRepository;


    public void startStatusUpdate(Integer orderId) {
        Cart cart = cartRepository.findByOrderId(orderId).get();
        while (!cart.getStatus().equals("Delivered")) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - cart.getDate();
            String lastStatus = cart.getStatus();
            if (elapsedTime >= 2 * 60 * 1000 && !lastStatus.equals("Delivered")) {
                System.out.println("updated cart id : " + cart.getOrderId() + " status to Delivered");
                cart.setStatus("Delivered");
            } else if (elapsedTime >= 60 * 1000 && elapsedTime< 2 * 60 * 1000 && !lastStatus.equals("Out for Delivery")) {
                System.out.println("updated cart id : " + cart.getOrderId() + " status to Out For Delivery");
                cart.setStatus("Out for Delivery");
                decreaseOrderCount(cart.getRestaurantId());
                checkIfAnyOrderIsWTA(cart.getRestaurantId());
            } else if (elapsedTime >= 30 * 1000 && elapsedTime < 60 * 1000 && !lastStatus.equals("Ready")) {
                System.out.println("updated cart id : " + cart.getOrderId() + " status to Ready");
                cart.setStatus("Ready");
            }
            cartRepository.save(cart);
        }
    }

    @Async
    private void checkIfAnyOrderIsWTA(int restId) {
        List<Cart> carts = cartRepository.findByRestaurantIdAndStatusOrderByDateAsc(restId, "Waiting to Accepted");
        long currTime = System.currentTimeMillis();
        for (Cart cart : carts) {
            System.out.println("Updated by :- "+ currTime);
            Cart currentCart = cartRepository.findByOrderId(cart.getOrderId()).get();
            if(currentCart.getStatus().equals("Waiting to Accepted")){
                cart.setStatus("Accepted");
                cartRepository.save(cart);
                if (increaseOrderCount(cart.getRestaurantId())) {
                    log.info("WTA processing for OrderId: [{}]",cart.getOrderId());
                    startStatusUpdate(cart.getOrderId());
                }
            }
        }
    }

    private boolean increaseOrderCount(Integer restaurantId) {
        OrderCount orderCount = orderCountRepository.findById(restaurantId).get();
        int currentCountOfOrders = orderCount.getTotalOrdersProcessing()+1;
        if(currentCountOfOrders<=3){
            orderCount.setTotalOrdersProcessing(currentCountOfOrders);
            log.info("Increased  restaurantId: [{}] total count to : [{}]",restaurantId, orderCount.getTotalOrdersProcessing());
            orderCountRepository.save(orderCount);
            return true;
        }else{
            return false;
        }
    }

    public void decreaseOrderCount(int restaurantId) {
        OrderCount orderCount = orderCountRepository.findById(restaurantId).get();
        int currentCountOfOrders = orderCount.getTotalOrdersProcessing()-1;
        if(currentCountOfOrders>0){
            orderCount.setTotalOrdersProcessing(currentCountOfOrders);
        }else{
            orderCount.setTotalOrdersProcessing(0);
        }
        log.info("Decreased restaurantId: [{}] total count to : [{}] ",restaurantId, orderCount.getTotalOrdersProcessing());
        orderCountRepository.save(orderCount);
    }
}
