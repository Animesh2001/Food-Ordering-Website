package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Integer> {

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.restaurantId = :restaurantId ORDER BY o.groupOrderId ASC")
    List<Order> findOrdersByUserIdAndRestaurantId(@Param("userId") Integer userId, @Param("restaurantId") int restaurantId);

    List<Order> findByGroupOrderId(Integer groupOrderId);
}
