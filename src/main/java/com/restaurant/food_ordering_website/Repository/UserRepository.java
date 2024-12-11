package com.restaurant.food_ordering_website.Repository;

import com.restaurant.food_ordering_website.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
}
