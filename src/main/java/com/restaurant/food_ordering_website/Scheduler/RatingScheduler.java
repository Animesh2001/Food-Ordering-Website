package com.restaurant.food_ordering_website.Scheduler;

import com.restaurant.food_ordering_website.Service.MerchantCreditService;
import com.restaurant.food_ordering_website.Service.RatingCalculationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RatingScheduler {

    @Autowired
    private RatingCalculationService ratingCalculationService;

//    @PostConstruct
//    public void init() {
//        processRatingOfRestaurants();
//    }

    public void processRatingOfRestaurants() {
        System.out.println("Starting ratingCalculation");
       ratingCalculationService.calculateRating();
    }
}
