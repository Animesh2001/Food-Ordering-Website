package com.restaurant.food_ordering_website.Processor;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import com.restaurant.food_ordering_website.Service.EmailService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserEmailProcessor implements ItemProcessor<User, User> {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public User process(User user) throws Exception {
        long startOfWeek = ZonedDateTime.now().minusWeeks(1).toInstant().toEpochMilli();
        long endOfWeek = ZonedDateTime.now().toInstant().toEpochMilli();
        System.out.println("Gonna process for start : "+startOfWeek+ " end: "+ endOfWeek);

        // Check if the user placed an order in the last week
        List<Cart> carts = cartRepository.findCartsByUserIdAndDateRange(startOfWeek, endOfWeek, user.getUserId());
        if (carts.isEmpty()) {
            // Fetch top 5 restaurants based on priority
            List<Restaurant> topRestaurants = restaurantRepository.findAllAvailableRestaurants()
                    .stream()
                    .sorted(Comparator.comparing(Restaurant::getPriority))
                    .limit(5)
                    .collect(Collectors.toList());

            // Send email with top restaurants
            emailService.sendEmailWithTopRestaurants(user, topRestaurants);
        }
        return user;
    }
}

