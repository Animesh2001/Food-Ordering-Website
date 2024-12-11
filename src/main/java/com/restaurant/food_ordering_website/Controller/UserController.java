package com.restaurant.food_ordering_website.Controller;

import com.restaurant.food_ordering_website.DTO.OrderHistoryResponse;
import com.restaurant.food_ordering_website.DTO.RestaurantResponse;
import com.restaurant.food_ordering_website.DTO.OrderItem;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/users")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final ConcurrentHashMap<Integer, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    @Autowired
    private UserService userService;


    @GetMapping("/getCredit/{userId}")
    private ResponseEntity<?> getCredit(@PathVariable Integer userId ){
        try{

            User user = userService.findUserById(userId);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            Map<String, Integer> response = new HashMap<>();
            response.put("credit", user.getCredit());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving credit of user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get credit of userId : "+ userId);
        }
    }


    @GetMapping("/get-restaurant/{city}")
    private ResponseEntity<RestaurantResponse<Restaurant>> getRestaurantByCity(@PathVariable String city ){
        try {
            List<Restaurant> listOfRestaurants = userService.findRestaurantByCity(city);
            String message = "Retrieved " + listOfRestaurants.size() + " restaurants.";
            RestaurantResponse<Restaurant> response = new RestaurantResponse<>(message, listOfRestaurants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestaurantResponse<>("Failed to retrieve restaurants: " + e.getMessage(), null));
        }
    }

    @PostMapping("/order/{restaurantId}/{userId}")
    private ResponseEntity<?> placeOrder(@PathVariable Integer restaurantId, @PathVariable Integer userId, @RequestBody List<OrderItem> orderedItem){
        try {
           Integer groupOrderId = userService.placeOrder(orderedItem,restaurantId,userId);
            Map<String,String> map = new HashMap<>();
            map.put("orderId",String.valueOf(groupOrderId));
            map.put("status","200");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            log.error("Error while retrieving restaurant data", e);
            Map<String,String> map = new HashMap<>();
            map.put("status","500");
            map.put("message",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(map);
        }
    }

    @GetMapping("/order/history/{restaurantId}/{userId}")
    private ResponseEntity<?> getOrderHistoryOfUser(@PathVariable Integer restaurantId, @PathVariable Integer userId){
        try {
           List<OrderHistoryResponse> orderHistoryList  = userService.getOrderHistory(userId,restaurantId);
            return ResponseEntity.ok(orderHistoryList);
        } catch (Exception e) {
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch order history");
        }
    }

    @GetMapping("/order/{orderId}")
    private ResponseEntity<?> getOrderOfUser(@PathVariable Integer orderId){
        try {
            OrderHistoryResponse orderHistoryList  = userService.getOrderOfUser(orderId);
            return ResponseEntity.ok(orderHistoryList);
        } catch (Exception e) {
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch order of user");
        }
    }

    @GetMapping("/order/date/{orderId}")
    private ResponseEntity<?> getDateOfOrder(@PathVariable Integer orderId){
        try{
            Long date  = userService.getDate(orderId);
            String formattedDate = userService.getFormattedDate(date);
            String formattedTime = userService.getFormattedTime(date); ;
            Map<String,String> map = new HashMap<>();
            map.put("Date",formattedDate);
            map.put("Time",formattedTime);
            return ResponseEntity.ok(map);
        }catch(Exception e){
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch date of order");
        }

    }

    @GetMapping("/order/status/{orderId}")
    private ResponseEntity<?> getStatusOfOrder(@PathVariable Integer orderId){
        try{
            String status  = userService.getStatus(orderId);
            Map<String,String> map = new HashMap<>();
            map.put("status",status);
            return ResponseEntity.ok(map);
        }catch(Exception e){
            log.error("Error while retrieving restaurant data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch status of order");
        }

    }

    @GetMapping(value = "/status/stream/{orderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderStatus(@PathVariable Integer orderId) {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                while (true) {
                    String status = userService.getStatus(orderId);

                    // Send the status update
                    emitter.send(SseEmitter.event().name("status-update").data(status));

                    // Check for updates periodically (you may customize this logic)
                    Thread.sleep(5000); // Send every 5 seconds, or on actual status change
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

//    @GetMapping(value = "/order/status/subscribe/{orderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter subscribeToOrderStatus(@PathVariable Integer orderId) {
//        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
//        sseEmitters.put(orderId, emitter);
//
//        emitter.onCompletion(() -> sseEmitters.remove(orderId));
//        emitter.onTimeout(() -> sseEmitters.remove(orderId));
//        emitter.onError(e -> sseEmitters.remove(orderId));
//
//        return emitter;
//    }

//    @GetMapping("/order/status/{orderId}")
//    public ResponseEntity<?> getStatusOfOrder(@PathVariable Integer orderId) {
//        try {
//            // Fetch status from userService
//            String status = userService.getStatus(orderId);
//
//            // Push status to any active SSE emitters
//            SseEmitter emitter = sseEmitters.get(orderId);
//            if (emitter != null) {
//                try {
//                    executorService.submit(() -> {
//                        try {
//                            emitter.send(SseEmitter.event().name("statusUpdate").data(status));
//                        } catch (IOException e) {
//                            emitter.completeWithError(e);
//                        }
//                    });
//                } catch (Exception e) {
//                    emitter.completeWithError(e);
//                }
//            }
//
//            Map<String, String> map = new HashMap<>();
//            map.put("status", status);
//            return ResponseEntity.ok(map);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Unable to fetch status of order");
//        }
//    }

    @PutMapping("/cancel-order/{orderId}")
    private ResponseEntity<?> cancelOrder(@PathVariable  Integer orderId){
        try{
            userService.cancelTheOrder(orderId);
            return ResponseEntity.ok("Order Cancelled");
        }catch(Exception e){
            log.error("Error while cancelling the order : [{}]",orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to cancel the order due to "+ e.getMessage());
        }
    }

//    @GetMapping("/old-order/history/{restaurantId}/{userId}")
//    private ResponseEntity<?> getOldOrderHistoryOfUser(@PathVariable Integer restaurantId, @PathVariable Integer userId){
//        try {
//            Map<Integer, List<Order>> orderHistory  = userService.getOrdersGroupedByGroupOrderId(userId,restaurantId);
//            return ResponseEntity.ok(orderHistory);
//        } catch (Exception e) {
//            log.error("Error while retrieving restaurant data", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Unable to fetch order history");
//        }
//    }



}
