package com.restaurant.food_ordering_website.Processor;

import com.restaurant.food_ordering_website.Configuration.OrderProcess;
import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.OrderCount;
import com.restaurant.food_ordering_website.Model.WaitingRecord;
import com.restaurant.food_ordering_website.Repository.OrderCountRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import com.restaurant.food_ordering_website.Repository.WaitingRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
public class CartItemProcessor implements ItemProcessor<Cart, Cart> {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private WaitingRecordRepository waitingRecordRepository;

    @Autowired
    private OrderCountRepository orderCountRepository;

    @Autowired
    private OrderProcess orderProcess;

    @Override
    public Cart process(Cart cart) throws Exception {
        System.out.println("Cart id is " + cart.getOrderId() + " String value " + cart.toString());
        long currentTime = System.currentTimeMillis();
//        long elapsedTime = (currentTime - cart.getDate()) / (60 * 1000); // Convert to minutes
        long elapsedTime = (currentTime - cart.getDate()) ;

        List<WaitingRecord> waitingRecords = waitingRecordRepository.findByRestaurantId(cart.getRestaurantId());
        if(cart.getStatus().equals("Cancelled")){
            return cart;
        }
        if (!waitingRecords.isEmpty()) {

            if (contains(cart.getOrderId(), waitingRecords)) {
                if (cart.getStatus().equals("Waiting to Accepted")) {
                    if(elapsedTime>6 * 60 * 1000){
                        cart.setStatus("Cancelled");
                        orderProcess.cancelAndRevertOrder(cart, true);
                        waitingRecordRepository.deleteByOrderId(cart.getOrderId());
                        return cart;
                    }
                    cart.setStatus("Accepted");
                    cart.setDate(ZonedDateTime.now().toInstant().toEpochMilli());
                } else {
                    if (elapsedTime >= 30000) {
                        System.out.println("Cart id " + cart.getOrderId() + " status set to Delivered");
                        cart.setStatus("Delivered");
                        waitingRecordRepository.deleteByOrderId(cart.getOrderId());
                    } else if (elapsedTime >= 20000) {
                        System.out.println("Cart id " + cart.getOrderId() + " status set to Out for Delivery");
                        cart.setStatus("Out for Delivery");
                        decreaseOrderCount(cart.getRestaurantId());
                    } else if (elapsedTime >= 10000) {
                        System.out.println("Cart id " + cart.getOrderId() + " status set to Ready");
                        cart.setStatus("Ready");
                    }
                }
            } else {
                cart.setDate(ZonedDateTime.now().toInstant().toEpochMilli());
            }

        } else {
            if (cart.getStatus().equals("Waiting to Accepted")) {
                boolean isRestaurantAvailable = restaurantRepository.findByRestaurantId(cart.getRestaurantId()).isAvailable();
//                if (elapsedTime > 360 && !isRestaurantAvailable) {
//                    cart.setStatus("Cancelled");
//                    orderProcess.cancelAndRevertOrder(cart, true);
//                }
                if (isRestaurantAvailable && elapsedTime <= 6 * 60 * 1000 && checkProcessingCount(cart.getRestaurantId())) {
                    cart.setStatus("Accepted");
                    increaseOrderCount(cart.getRestaurantId());
                }
            } else if (elapsedTime >= 30000) {
                System.out.println("Cart id " + cart.getOrderId() + " status set to Delivered");
                cart.setStatus("Delivered");
            } else if (elapsedTime >= 20000) {
                System.out.println("Cart id " + cart.getOrderId() + " status set to Out for Delivery");
                cart.setStatus("Out for Delivery");
                decreaseOrderCount(cart.getRestaurantId());
            } else if (elapsedTime >= 10000) {
                System.out.println("Cart id " + cart.getOrderId() + " status set to Ready");
                cart.setStatus("Ready");
            }
        }
        return cart;
    }

    private void increaseOrderCount(Integer restaurantId) {
        OrderCount orderCount = orderCountRepository.findById(restaurantId).get();
        int currentCountOfOrders = orderCount.getTotalOrdersProcessing()+1;
        orderCount.setTotalOrdersProcessing(currentCountOfOrders);
        log.info("increased restaurantId: [{}] total count to : [{}] ",restaurantId, orderCount.getTotalOrdersProcessing());
        orderCountRepository.save(orderCount);
    }

    private boolean checkProcessingCount(Integer restaurantId) {
        return orderCountRepository.findById(restaurantId).get().getTotalOrdersProcessing()<3;
    }

    private boolean contains(Integer orderId, List<WaitingRecord> waitingRecords) {
        for (WaitingRecord waitingRecord : waitingRecords) {
            if (waitingRecord.getOrderId() == orderId) {
                return true;
            }
        }
        return false;
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

