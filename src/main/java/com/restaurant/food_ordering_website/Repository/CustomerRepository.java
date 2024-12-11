package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<User,Integer> {

    Optional<User> findByEmail(String email);

}
