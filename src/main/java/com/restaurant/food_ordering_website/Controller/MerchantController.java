package com.restaurant.food_ordering_website.Controller;


import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.food_ordering_website.DTO.MerchantOrderView;
import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.MenuItems;
import com.restaurant.food_ordering_website.Model.Order;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.DTO.RestaurantResponse;
import com.restaurant.food_ordering_website.Service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/merchants")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class MerchantController {

    @Autowired
    private RestaurantService restaurantService;

    //tested
//    @PostMapping(value = "/addRestaurant", consumes = {org.springframework.http.MediaType.ALL_VALUE})
//    public ResponseEntity<String> addRestaurant(@RequestPart("restaurant") Restaurant restaurant,
//                                                @RequestPart("file") MultipartFile file) {
//
//        try {
//            int restaurantId = restaurantService.saveRestaurant(restaurant);
//            restaurantService.saveMenu(file, restaurantId);
//            return ResponseEntity.ok("Restaurant added successfully with ID: " + restaurantId);
//        } catch (Exception e) {
//            log.error("Error while saving restaurant data for restaurant [{}]", restaurant, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to add the restaurant: " + e.getMessage());
//        }
//    }

    @PostMapping(value = "/addRestaurant", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addRestaurant(
            @RequestPart("restaurantDetails") String restaurantDetails, // Handle JSON data as a String
            @RequestPart("menuFile") MultipartFile menuFile // Handle file upload
    ) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Restaurant restaurant = objectMapper.readValue(restaurantDetails, Restaurant.class);

            System.out.println("Restaurant Name: " + restaurant.getName());
            System.out.println("Uploaded File: " + menuFile.getOriginalFilename());

            Restaurant restaurant1 = restaurantService.saveRestaurant(restaurant);
            restaurantService.saveMerchantRestaurantMapping(restaurant.getMerchantId(), restaurant1.getRestaurantId());
            restaurantService.saveMenu(menuFile, restaurant1.getRestaurantId());
            List<Restaurant> restaurants = new ArrayList<>();
            restaurants.add(restaurant1);
            return ResponseEntity.ok(new RestaurantResponse<Restaurant>("Restaurant added successfully with id : " + restaurant1.getRestaurantId(), restaurants));

        } catch (Exception e) {
            log.error("Error while saving restaurant data for restaurant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add the restaurant: " + e.getMessage());
        }
    }

    //tested
    @PostMapping(value = "/item/update-price")
    public ResponseEntity<String> updatePrice(@RequestBody MenuItems item) {

        try {
            restaurantService.updatePrice(item);
            return ResponseEntity.ok("price updated to " + item.getPrice() + " for item with id " + item.getId());
        } catch (Exception e) {
            log.error("Error while updating price for item: [{}]", item, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update the price of item with id: " + item.getId() + " " + e.getMessage());
        }
    }


    //tested
    @PostMapping(value = "/item/update-quantity")
    public ResponseEntity<String> updateQuantity(@RequestBody MenuItems item) {

        try {
            restaurantService.updateQuantity(item);
            return ResponseEntity.ok("quantity updated for item with id " + item.getId());
        } catch (Exception e) {
            log.error("Error  while updating quantity for item: [{}]", item, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update the item with id: " + item.getId() + " " + e.getMessage());
        }
    }

    @PutMapping(value = "/update/item")
    public ResponseEntity<String> updateMenu(@RequestBody List<MenuItems> updatedMenu) {

        try {
            restaurantService.updateMenu(updatedMenu);
            return ResponseEntity.ok("Items updated");
        } catch (Exception e) {
            log.error("Error  while updating the items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update the items due to : " + e.getMessage());
        }
    }

    //tested
    @PutMapping(value = "/update/restaurant")
    public ResponseEntity<String> updateRestaurantAvailability(@RequestBody Restaurant restaurant) {

        try {
            restaurantService.updateRestaurant(restaurant);
            return ResponseEntity.ok("Availability updated for restaurant with id: " + restaurant.getRestaurantId() + " to " + restaurant.isAvailable());
        } catch (Exception e) {
            log.error("Error while updating restaurant availability for restaurant: [{}]", restaurant, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update availability of restaurant with id: " + restaurant.getRestaurantId() + " " + e.getMessage());
        }
    }

    //tested
    @PostMapping(value = "/item/available")
    public ResponseEntity<String> updateItemAvailability(@RequestBody MenuItems item) {

        try {
            restaurantService.updateItemAvailability(item);
            return ResponseEntity.ok("Availability updated for item with id: " + item.getId() + " to " + item.isAvailable());
        } catch (Exception e) {
            log.error("Error while updating item availability for item: [{}]", item, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update availability of item with id: " + item.getId() + " " + e.getMessage());
        }
    }

    //tested
    @GetMapping("/restaurants")
    public ResponseEntity<RestaurantResponse<Restaurant>> getRestaurants() {
        try {
            List<Restaurant> listOfRestaurants = restaurantService.getAllRestaurants();
            String message = "Retrieved " + listOfRestaurants.size() + " restaurants.";
            RestaurantResponse<Restaurant> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve restaurants: " + e.getMessage(), null));
        }
    }

    //tested
    @GetMapping("/restaurants/available")
    public ResponseEntity<RestaurantResponse<Restaurant>> getRestaurantsAvailable() {
        try {
            List<Restaurant> listOfRestaurants = restaurantService.getAllRestaurantsAvailable();
            String message = "Retrieved " + listOfRestaurants.size() + " restaurants available.";
            RestaurantResponse<Restaurant> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving available restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve available restaurants: " + e.getMessage(), null));
        }
    }

    @GetMapping("/restaurants/unavailable")
    public ResponseEntity<RestaurantResponse<Restaurant>> getRestaurantsUnavailable() {
        try {
            List<Restaurant> listOfRestaurants = restaurantService.getAllRestaurantsUnavailable();
            String message = "Retrieved " + listOfRestaurants.size() + " restaurants unavailable.";
            RestaurantResponse<Restaurant> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving unavailable restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve unavailable restaurants: " + e.getMessage(), null));
        }
    }


    //tested
    @GetMapping("/items/{restaurantId}")
    public ResponseEntity<RestaurantResponse<MenuItems>> getItems(@PathVariable Integer restaurantId) {
        try {
            List<MenuItems> listOfItems = restaurantService.getAllItems(restaurantId);
            String message = "Retrieved " + listOfItems.size() + " items.";
            RestaurantResponse<MenuItems> response = new RestaurantResponse<>(message, listOfItems);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving items for restaurant Id: [{}]", restaurantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve unavailable restaurants: " + e.getMessage(), null));
        }
    }

    //tested
    @GetMapping("/items/available/{restaurantId}")
    public ResponseEntity<RestaurantResponse<MenuItems>> getItemsAvailable(@PathVariable Integer restaurantId) {
        try {
            List<MenuItems> listOfRestaurants = restaurantService.getAllItemsAvailable(restaurantId);
            String message = "Retrieved " + listOfRestaurants.size() + " items available.";
            RestaurantResponse<MenuItems> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving available items data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve available items: " + e.getMessage(), null));
        }
    }

    //tested
    @GetMapping("/items/unavailable/{restaurantId}")
    public ResponseEntity<RestaurantResponse<MenuItems>> getItemsUnAvailable(@PathVariable Integer restaurantId) {
        try {
            List<MenuItems> listOfRestaurants = restaurantService.getAllItemsUnavailable(restaurantId);
            String message = "Retrieved " + listOfRestaurants.size() + " items unavailable.";
            RestaurantResponse<MenuItems> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving unavailable items for restaurantId : [{}]", restaurantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve unavailable items: " + e.getMessage(), null));
        }
    }


    //fetch restaurant by merchantId
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponse<Restaurant>> getRestaurantsByMerchantId(@PathVariable Integer id) {
        try {
            List<Restaurant> listOfRestaurants = restaurantService.getRestaurantsByMerchantId(id);
            String message = "Retrieved " + listOfRestaurants.size() + " restaurants.";
            RestaurantResponse<Restaurant> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve restaurants: " + e.getMessage(), null));
        }
    }


    @GetMapping("/daily-report/merchant/{id}")
    public ResponseEntity<?> getReportForMerchant(@PathVariable Integer id) {
        try {
            // Fetch the file from S3
            S3Object s3Object = restaurantService.getReportOfMerchant(id);

            // Get the input stream of the file from the S3Object
            InputStream inputStream = s3Object.getObjectContent();

            // Get the filename from the S3Object (you can customize this as needed)
            String fileName = s3Object.getKey().substring(s3Object.getKey().lastIndexOf("/") + 1);

            // Set headers to indicate file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // Return the file content in the response
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(inputStream.readAllBytes());
        } catch (Exception e) {
            log.error("Error while retrieving merchant report file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve the file: " + e.getMessage());
        }
    }


    @GetMapping("/daily-report/restaurant/{id}")
    public ResponseEntity<?> getReportForRestaurant(@PathVariable Integer id) {
        try {
            // Fetch the file from S3
            S3Object s3Object = restaurantService.getReportOfRestaurant(id);

            // Get the input stream of the file from the S3Object
            InputStream inputStream = s3Object.getObjectContent();

            // Get the filename from the S3Object (you can customize this as needed)
            String fileName = s3Object.getKey().substring(s3Object.getKey().lastIndexOf("/") + 1);

            // Set headers to indicate file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // Return the file content in the response
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(inputStream.readAllBytes());
        } catch (Exception e) {
            log.error("Error while retrieving restaurant report file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve the file: " + e.getMessage());
        }
    }

    @GetMapping("/get-orders/restaurant/{restaurantId}")
    public ResponseEntity<?> getOrderOfRestaurant(@PathVariable Integer restaurantId) {
        try {
            List<Cart> carts = restaurantService.getOrdersOfRestaurant(restaurantId);
            String message = "Retrieved " + carts.size() + " orders in accepted status.";
            RestaurantResponse<Cart> response = new RestaurantResponse<>(message, carts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving orders for restaurantId : [{}]", restaurantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve orders " + e.getMessage(), null));
        }
    }

    @GetMapping("/get-order/items/restaurant/{orderID}")
    public ResponseEntity<?> getItemsOfOrder(@PathVariable Integer orderID) {
        try{
            Cart cart = restaurantService.getCartofOrder(orderID);
            List<Order> orders = restaurantService.getItemsOfCart(orderID);
            MerchantOrderView merchantOrderView = new MerchantOrderView();
            merchantOrderView.setOrders(orders);
            merchantOrderView.setTotalValue(cart.getTotalValue());
            return  ResponseEntity.ok(merchantOrderView);
        }catch(Exception e){
            log.error("Error while retrieving items of an order with orderId [{}]",orderID);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve items for order " + e.getMessage(), null));
        }
    }

    @PutMapping("/cancel-order/{orderId}")
    private ResponseEntity<?> cancelOrder(@PathVariable Integer orderId){
        try{
            restaurantService.cancelTheOrder(orderId);
            return ResponseEntity.ok("Order Cancelled");
        }catch(Exception e){
            log.error("Error while cancelling the order : [{}]",orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to cancel the order due to "+ e.getMessage());
        }
    }

}
