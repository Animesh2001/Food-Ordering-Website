package com.restaurant.food_ordering_website.Controller;

import com.restaurant.food_ordering_website.DTO.SignInUser;
import com.restaurant.food_ordering_website.Model.MenuItems;
import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authentication")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(value = "/sign-up/customer")
    public ResponseEntity<String> userSignUpCustomer(@RequestBody User user) {

        try {
            authenticationService.addCustomer(user);
            return ResponseEntity.ok("User added successfully");
        } catch (Exception e) {
            log.error("Error while saving user: [{}]", user, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save user "+ e.getMessage());
        }
    }

    @PostMapping(value = "/sign-up/merchant")
    public ResponseEntity<String> userSignUpMerchant(@RequestBody SignInUser user) {

        try {
            authenticationService.addMerchant(user);
            return ResponseEntity.ok("Merchant added successfully");
        } catch (Exception e) {
            log.error("Error while saving user: [{}]", user, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save user "+ e.getMessage());
        }
    }

    @PostMapping(value = "/sign-in/customer")
    public ResponseEntity<?> userSignInCustomer(@RequestBody SignInUser user) {

        try {
            User signInUser =authenticationService.getCustomer(user);
            Map<String,String> map = new HashMap();
            map.put("customerId",String.valueOf(signInUser.getUserId()));
            map.put("city",signInUser.getCity());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            log.error("Error while fetching user: [{}]", user, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch user "+ e.getMessage());
        }
    }

    @PostMapping(value = "/sign-in/merchant")
    public ResponseEntity<?> userSignInMerchant(@RequestBody SignInUser user) {

        try {
            Merchant signInMerchant =authenticationService.getMerchant(user);
            Map<String,String> map = new HashMap();
            map.put("merchantId",String.valueOf(signInMerchant.getMerchantId()));
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            log.error("Error while fetching user: [{}]", user, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch user "+ e.getMessage());
        }
    }
}
