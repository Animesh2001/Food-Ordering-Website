package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Restaurant r SET r.isAvailable = :available WHERE r.restaurantId = :restaurantId")
    void updateAvailability(@Param("restaurantId") int restaurantId, @Param("available") boolean available);


    @Modifying
    @Transactional
    @Query("UPDATE Restaurant r SET r.priority = NULL")
    void clearAllPriorities();

    @Modifying
    @Transactional
    @Query(value = "UPDATE restaurant SET priority = :priorityValue", nativeQuery = true)
    void updateAllPriorities(@Param("priorityValue") int priorityValue);

    List<Restaurant> findAll();

    @Query("SELECT r FROM Restaurant r WHERE r.isAvailable = true")
    List<Restaurant> findAllAvailableRestaurants();

    @Query("SELECT r FROM Restaurant r WHERE r.isAvailable = false")
    List<Restaurant> findAllUnAvailableRestaurants();

    List<Restaurant> findByMerchantId(int merchantId);

    Restaurant findByRestaurantId(int restaurantId);


    List<Restaurant> findByAddressOrderByPriorityAsc(String address);

    long count();
}
