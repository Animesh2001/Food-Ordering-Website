package com.restaurant.food_ordering_website.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInUser {

    private String email;
    private String password;
}
