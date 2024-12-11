package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.Order;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.OrderRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SummaryService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;


    //getting restaurant name
    public String getRestaurantName(int restId) {
        return restaurantRepository.findByRestaurantId(restId).getName();
    }

    public int getOrderWithHighestValue() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime endOfYesterday = startOfYesterday.plusDays(1).minusSeconds(1);

        // Convert to milliseconds
        long startOfYesterdayMillis = startOfYesterday.toInstant().toEpochMilli();
        long endOfYesterdayMillis = endOfYesterday.toInstant().toEpochMilli();

        Cart cart = cartRepository.findCartWithHighestTotalValue(startOfYesterdayMillis, endOfYesterdayMillis).get();
        return cart.getTotalValue();
    }

    public String getMostOrderedItem(List<Cart> carts) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime endOfYesterday = startOfYesterday.plusDays(1).minusSeconds(1);

        // Convert to milliseconds
        long startOfYesterdayMillis = startOfYesterday.toInstant().toEpochMilli();
        long endOfYesterdayMillis = endOfYesterday.toInstant().toEpochMilli();

//        List<Cart> carts = cartRepository.findCartsPlacedYesterday(startOfYesterdayMillis, endOfYesterdayMillis,restaurantId);

        Map<String, Integer> map = new HashMap<>();
        int max = Integer.MIN_VALUE;
        String maxOrderedItem = null;
        for (Cart cart : carts) {
            List<Order> orders = orderRepository.findByGroupOrderId(cart.getOrderId());
            for (Order order : orders) {
                String itemName = order.getItemName();
                int itemQuantity = order.getItemQuantity();
                map.put(itemName, map.getOrDefault(itemName, 0) + itemQuantity);
                if (map.get(itemName) > max) {
                    max = map.get(itemName);
                    maxOrderedItem = itemName;
                }
            }
        }

        return maxOrderedItem;


    }


}
