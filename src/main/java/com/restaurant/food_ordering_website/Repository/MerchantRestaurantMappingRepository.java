package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.MerchantRestaurantMapping;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRestaurantMappingRepository extends JpaRepository<MerchantRestaurantMapping, Integer> {

    List<MerchantRestaurantMapping> findByMerchantId(Integer merchantId);

}
