package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Integer> {

    Optional<Cart> findByOrderId(Integer orderId);

    List<Cart> findByRestaurantIdAndStatus(Integer restaurantId, String status);

//    @Query(value = "SELECT c FROM customer_cart_details c WHERE c.date BETWEEN :startOfDay AND :endOfDay AND c.restaurant_Id = :restaurantId ORDER BY c.total_value DESC", nativeQuery = true)
//    List<Cart> findCartsByDateRange(@Param("startOfDay") long startOfDay, @Param("endOfDay") long endOfDay);

    @Query(value = "SELECT * FROM customer_cart_details c WHERE c.date BETWEEN :startDate AND :endDate AND c.restaurant_id = :restaurantId ORDER BY c.total_value DESC", nativeQuery = true)
    List<Cart> findCartsByDateRangeAndRestaurantId(@Param("startDate") long startDate,
                                                   @Param("endDate") long endDate,
                                                   @Param("restaurantId") Integer restaurantId);

    @Query(value = "SELECT * FROM customer_cart_details c WHERE c.date BETWEEN :startDate AND :endDate", nativeQuery = true)
    List<Cart> findCartsByDateRange(@Param("startDate") long startDate,
                                                   @Param("endDate") long endDate);


    @Query(value = "SELECT c FROM customer_cart_details c WHERE c.date BETWEEN :startOfDay AND :endOfDay", nativeQuery = true)
    List<Cart> findCartsPlacedYesterday(@Param("startOfDay") long startOfDay, @Param("endOfDay") long endOfDay);

    List<Cart> findByDate(Long date);

    List<Cart> findByRestaurantIdAndStatusOrderByDateAsc(Integer restaurantId, String status);

    // Use a native query to fetch only the cart with the highest totalValue
    @Query(value = "SELECT * FROM customer_cart_details c WHERE c.date BETWEEN :startOfDay AND :endOfDay ORDER BY c.total_value DESC LIMIT 1", nativeQuery = true)
    Optional<Cart> findCartWithHighestTotalValue(@Param("startOfDay") long startOfDay, @Param("endOfDay") long endOfDay);

    @Query(value = "SELECT * FROM customer_cart_details c WHERE c.date BETWEEN :startDate AND :endDate AND c.user_id = :userId", nativeQuery = true)
    List<Cart> findCartsByUserIdAndDateRange(@Param("startDate") long startDate,
                                             @Param("endDate") long endDate,
                                             @Param("userId") Integer userId);


}
