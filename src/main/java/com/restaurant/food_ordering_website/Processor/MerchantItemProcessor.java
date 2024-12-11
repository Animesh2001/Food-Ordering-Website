package com.restaurant.food_ordering_website.Processor;

import com.restaurant.food_ordering_website.Model.*;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MerchantItemProcessor implements ItemProcessor<Merchant, MerchantSummaryData> {


    @Autowired
    private  RestaurantRepository restaurantRepository;

    @Autowired

    private  CartRepository cartRepository;




    @Override
    public MerchantSummaryData process(Merchant merchant) {
        List<Restaurant> restaurants = restaurantRepository.findByMerchantId(merchant.getMerchantId());

        // For each restaurant, get cart details for the current month with status "Delivered"
        MerchantSummaryData merchantSummaryData = new MerchantSummaryData();
        List<SummaryData> sheetData = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            List<Cart> carts = cartRepository.findByRestaurantIdAndStatus(restaurant.getRestaurantId(), "Delivered");
            // Process cart data (calculate total orders and total profit)
            int totalOrders = carts.size();
            int totalProfit = carts.stream().mapToInt(Cart::getTotalValue).sum();

            SummaryData summaryData = new SummaryData();
            // Store the results
            summaryData.setRestaurantName(restaurant.getName());
            summaryData.setTotalOrders(totalOrders);
            summaryData.setTotalValue(totalProfit);
            sheetData.add(summaryData);
        }
        merchantSummaryData.setList(sheetData);
        merchantSummaryData.setMerchantId(merchant.getMerchantId());
        return merchantSummaryData;
    }
}
