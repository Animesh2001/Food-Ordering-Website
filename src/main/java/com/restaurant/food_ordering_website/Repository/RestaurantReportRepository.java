package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.RestaurantReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantReportRepository extends JpaRepository<RestaurantReport,Integer> {

    RestaurantReport findByRestaurantId(int restaurantId);
}
