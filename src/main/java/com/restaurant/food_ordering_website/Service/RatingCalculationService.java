package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.PriorityTracker;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Repository.PriorityTrackerRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingCalculationService {


    @Autowired
    private PriorityTrackerRepository priorityTrackerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;


    public void calculateRating() {
        deleteAndSetDefaultSizePriority();
        int priority = 1;
        List<PriorityTracker> priorityTrackerList = priorityTrackerRepository.findAllOrderedByOrderAndProfit();
        for(PriorityTracker priorityTracker : priorityTrackerList){
            int restaurantId = priorityTracker.getRestaurant_id();
            Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId);
            restaurant.setPriority(priority);
            restaurantRepository.save(restaurant);
            priority ++;
        }
        priorityTrackerRepository.deleteAll();
    }

    private void deleteAndSetDefaultSizePriority() {
        int count = (int)restaurantRepository.count();
        restaurantRepository.clearAllPriorities();
        restaurantRepository.updateAllPriorities(count);
    }

}
