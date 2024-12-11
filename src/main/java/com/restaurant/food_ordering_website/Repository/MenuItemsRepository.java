package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.MenuItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemsRepository extends JpaRepository<MenuItems,Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE MenuItems m SET m.price = :price WHERE m.id = :id")
    void updatePrice(@Param("id") Integer id, @Param("price") String price);

    @Modifying
    @Transactional
    @Query("UPDATE MenuItems m SET m.quantityAvailable = :quantityAvailable WHERE m.id = :id")
    void updateQuantity(@Param("id") Integer id, @Param("quantityAvailable") int quantity);

    @Modifying
    @Transactional
    @Query("UPDATE MenuItems m SET m.isAvailable = :available WHERE m.id = :id")
    void updateAvailability(@Param("id") Integer id, @Param("available") boolean available);

    List<MenuItems> findByRestaurantId(Integer restaurantId);

    @Query("SELECT r FROM MenuItems r WHERE r.restaurantId = :restaurantId and r.isAvailable = true")
    List<MenuItems> findAllAvailableMenuItems(@Param("restaurantId") Integer restaurantId );

    @Query("SELECT r FROM MenuItems r WHERE  r.restaurantId = :restaurantId  and r.isAvailable = false")
    List<MenuItems> findAllUnAvailableMenuItems(@Param("restaurantId") Integer restaurantId );

    Optional<MenuItems> findById(Integer id);

    @Query("SELECT m FROM MenuItems m WHERE m.itemName = :itemName AND m.restaurantId = :restaurantId")
    Optional<MenuItems> findByItemNameAndRestaurantId(@Param("itemName") String itemName, @Param("restaurantId") Integer restaurantId);
}
