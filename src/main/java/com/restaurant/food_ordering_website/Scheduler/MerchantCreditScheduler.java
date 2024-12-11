package com.restaurant.food_ordering_website.Scheduler;

import com.restaurant.food_ordering_website.Service.MerchantCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MerchantCreditScheduler {

    @Autowired
    private MerchantCreditService merchantCreditService;

//    @Scheduled(cron = "0 0 0 * * ?") // This will run every day at midnight
    public void processDailyMerchantCredit() {
        System.out.println("Starting processDailyMerchantCredit");
        merchantCreditService.creditMerchantsForTodayOrders();
    }
}
