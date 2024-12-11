package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.PriorityTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityTrackerRepository extends JpaRepository<PriorityTracker,Integer> {

    @Query("SELECT pt FROM PriorityTracker pt ORDER BY pt.order DESC, pt.profit DESC")
    List<PriorityTracker> findAllOrderedByOrderAndProfit();

    @Query("SELECT pt FROM PriorityTracker pt WHERE pt.restaurant_id = :restaurantId")
    PriorityTracker findByRestaurantId(int restaurantId);

}
