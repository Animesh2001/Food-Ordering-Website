package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MerchantCreditService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;


    @Transactional
    public void creditMerchantsForTodayOrders() {
        try {
            // Get today's date as timestamp (assuming the 'date' field is stored as a Unix timestamp in seconds)
//            Long todayStart = Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond();

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay(now.getZone());
            ZonedDateTime endOfYesterday = startOfYesterday.plusDays(1).minusSeconds(1);

            // Convert to milliseconds
            long startOfYesterdayMillis = startOfYesterday.toInstant().toEpochMilli();
            long endOfYesterdayMillis = endOfYesterday.toInstant().toEpochMilli();

            // Fetch all cart entries placed today
            List<Cart> todayOrders = cartRepository.findCartsPlacedYesterday(startOfYesterdayMillis,endOfYesterdayMillis);
            System.out.println("Got carts of size : ["+todayOrders.size()+"] placed today");
            // Map to accumulate total value for each merchant
            Map<Integer, Integer> merchantTotalValueMap = new HashMap<>();

            // Process each cart entry
            for (Cart cart : todayOrders) {
                // Get the restaurant for this order
                Restaurant restaurant = restaurantRepository.findByRestaurantId(cart.getRestaurantId());
                if (restaurant == null) {
                    continue;  // Skip if restaurant is not found
                }

                // Get the merchant ID for this restaurant
                Integer merchantId = restaurant.getMerchantId();

                // Accumulate the total value for this merchant
                merchantTotalValueMap.put(merchantId, merchantTotalValueMap.getOrDefault(merchantId, 0) + cart.getTotalValue());
            }

            // Credit the merchants' accounts
            for (Map.Entry<Integer, Integer> entry : merchantTotalValueMap.entrySet()) {
                Integer merchantId = entry.getKey();
                Integer totalValue = entry.getValue();

                // Fetch the merchant and update their credit
                Merchant merchant = merchantRepository.findByMerchantId(merchantId);
                if (merchant != null) {
                    merchant.setCredit(merchant.getCredit() + totalValue);
                    merchantRepository.save(merchant);  // Persist the updated merchant
                }

                System.out.println("Merchant Id : [" + merchantId +"] "+ "Credit for today : ["+ totalValue+"]");
            }
        } catch (Exception e) {
            log.info("Credit Merchant Account the order is failed");
            throw e;
        }
    }
}
