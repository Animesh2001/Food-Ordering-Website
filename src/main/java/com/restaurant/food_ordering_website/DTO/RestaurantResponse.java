package com.restaurant.food_ordering_website.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse<T> {
    private String message;
    private List<T> restaurants;
}
