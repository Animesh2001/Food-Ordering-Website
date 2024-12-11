package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant,Integer> {

    Optional<Merchant> findByEmail(String email);

    Merchant findByMerchantId(int merchantId);



}
