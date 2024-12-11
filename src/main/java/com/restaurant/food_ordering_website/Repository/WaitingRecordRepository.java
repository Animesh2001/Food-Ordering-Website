package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.WaitingRecord;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaitingRecordRepository extends JpaRepository<WaitingRecord, Integer> {

    List<WaitingRecord> findByRestaurantId(int restaurantId);

    void deleteByOrderId(int orderId);
}
