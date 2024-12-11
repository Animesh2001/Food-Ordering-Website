package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.OrderCount;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCountRepository extends JpaRepository<OrderCount, Integer> {

}
