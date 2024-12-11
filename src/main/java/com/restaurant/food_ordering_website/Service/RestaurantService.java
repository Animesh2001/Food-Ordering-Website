package com.restaurant.food_ordering_website.Service;

import com.amazonaws.services.s3.model.S3Object;
import com.restaurant.food_ordering_website.Configuration.OrderProcess;
import com.restaurant.food_ordering_website.Model.*;
import com.restaurant.food_ordering_website.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderCountRepository orderCountRepository;

    @Autowired
    private MerchantRestaurantMappingRepository merchantRestaurantMappingRepository;

    @Autowired
    private MerchantReportRepository merchantReportRepository;

    @Autowired
    private RestaurantReportRepository restaurantReportRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WaitingRecordRepository waitingRecordRepository;

    @Autowired
    private OrderProcess orderProcess;

    @Autowired
    private AWSService awsService;

    @Autowired
    private AsyncProcessExecute asyncProcessExecute;

    public Restaurant saveRestaurant(Restaurant restaurant) {
        try {
            int priority = (int) restaurantRepository.count();
            restaurant.setPriority(priority);
            restaurantRepository.save(restaurant);
            return restaurant;
        } catch (Exception e) {
            log.error("Exception while saving restaurant: [{}]", restaurant, e);
            throw new RuntimeException("Unable to save restaurant");
        }
    }

    public void saveMenu(MultipartFile file, int restaurantId) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {


            Sheet sheet = workbook.getSheetAt(0);


            Row headerRow = sheet.getRow(0);


            List<MenuItems> menuItemsList = new ArrayList<>();


            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null || getCellValueAsString(row.getCell(0)) == null) {
                    break; // Stop reading the sheet
                }

                if (row != null) {
                    MenuItems menuItem = new MenuItems();
                    menuItem.setRestaurantId(restaurantId);

                    menuItem.setItemName(getCellValueAsString(row.getCell(0)));
                    menuItem.setDescription(getCellValueAsString(row.getCell(1)));
                    menuItem.setQuantityAvailable(getCellValueAsInt(row.getCell(2)));
                    menuItem.setQuantityUnit(getCellValueAsString(row.getCell(3)));
                    menuItem.setPrice(getCellValueAsString(row.getCell(4)));
                    menuItem.setAdditionalCustomizations(getCellValueAsString(row.getCell(5)));
                    menuItem.setAvailableTime(getCellValueAsString(row.getCell(6)));
                    menuItem.setMaxQuantityPerOrder(getCellValueAsInt(row.getCell(7)));

                    menuItemsList.add(menuItem);
                }
            }


            menuItemsRepository.saveAll(menuItemsList);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save menu: " + e.getMessage(), e);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return null;
    }

    private Integer getCellValueAsInt(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue(); // Cast to int for quantity fields
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue()); // Try to parse the string value to an int
            } catch (NumberFormatException e) {
                return null; // Handle parsing errors if the value is not an integer
            }
        }
        return null; // Return null if not applicable
    }

    public void updatePrice(MenuItems item) {
        try {
            menuItemsRepository.updatePrice(item.getId(), item.getPrice());
            log.info("price updated successfully.");
        } catch (Exception e) {
            log.error("Exception while updating price of item: [{}]", item, e);
            throw new RuntimeException("Unable to update price of item");
        }
    }

    public void updateQuantity(MenuItems item) {
        try {
            menuItemsRepository.updateQuantity(item.getId(), item.getQuantityAvailable());
        } catch (Exception e) {
            log.error("Exception while updating quantity of item: [{}]", item, e);
            throw new RuntimeException("Unable to update quantity of item");
        }
    }

    public void updateMenu(List<MenuItems> items) {
        for (MenuItems item : items) {
            try {
                // Fetch existing MenuItems from the database
                MenuItems existingItem = menuItemsRepository.findById(item.getId())
                        .orElseThrow(() -> new RuntimeException("Menu item not found for ID: " + item.getId()));

                // Apply changes from the incoming item to the existing item
                existingItem.setPrice(item.getPrice());
                existingItem.setQuantityAvailable(item.getQuantityAvailable());
                existingItem.setAvailable(item.isAvailable());
                existingItem.setItemName(item.getItemName());
                existingItem.setDescription(item.getDescription());
                existingItem.setQuantityUnit(item.getQuantityUnit());
                existingItem.setAdditionalCustomizations(item.getAdditionalCustomizations());
                existingItem.setAvailableTime(item.getAvailableTime());
                existingItem.setMaxQuantityPerOrder(item.getMaxQuantityPerOrder());

                // Save the updated entity
                menuItemsRepository.save(existingItem);
            } catch (Exception e) {
                log.error("Exception while updating item: [{}]", item, e);
                throw new RuntimeException("Unable to update item with ID: " + item.getId());
            }
        }
    }

    // old update restaurant logic
//    public void updateRestaurant(Restaurant restaurant) {
//        try {
//            Restaurant restaurantPrevious = restaurantRepository.findById(restaurant.getRestaurantId()).get();
//            if (!restaurantPrevious.isAvailable() && restaurant.isAvailable()) {
//                List<Cart> carts = cartRepository.findByRestaurantIdAndStatusOrderByDateAsc(restaurant.getRestaurantId(), "Waiting to Accepted");
//                log.info("Restaurant with id : [{}] just got available", restaurant.getRestaurantId());
//                processWaitingCarts(carts);
//            }
//            restaurantRepository.save(restaurant);
//        } catch (Exception e) {
//            log.error("Exception while updating availability of restaurant: [{}]", restaurant, e);
//            throw new RuntimeException("Unable to update restaurant");
//        }
//    }

    public void updateRestaurant(Restaurant restaurant) {
        try {
            Restaurant restaurantPrevious = restaurantRepository.findById(restaurant.getRestaurantId()).get();
            if (!restaurantPrevious.isAvailable() && restaurant.isAvailable()) {
                log.info("Restaurant with id : [{}] just got available", restaurant.getRestaurantId());
                List<Cart> carts = cartRepository.findByRestaurantIdAndStatusOrderByDateAsc(restaurant.getRestaurantId(), "Waiting to Accepted");
                if(carts.isEmpty()){
                    log.info("No cart is Waiting to Accepted status for restaurantID : [{}]",restaurant.getRestaurantId());
                }else{
                    storeInWaitingRecords(carts);
                }
            }
            restaurantRepository.save(restaurant);
        } catch (Exception e) {
            log.error("Exception while updating availability of restaurant: [{}]", restaurant, e);
            throw new RuntimeException("Unable to update restaurant");
        }
    }

    private void storeInWaitingRecords(List<Cart> carts) {
        for(Cart cart: carts){
            WaitingRecord waitingRecord = new WaitingRecord();
            waitingRecord.setRestaurantId(cart.getRestaurantId());
            waitingRecord.setOrderId(cart.getOrderId());
            waitingRecordRepository.save(waitingRecord);
        }
    }

    private void processWaitingCarts(List<Cart> carts) {
        for (Cart cart : carts) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - cart.getDate();
            if (elapsedTime >= 2 * 60 * 1000) {
                log.info("Order Id : [{}] has crossed 2 min , so it is cancelled", cart.getOrderId());
                orderProcess.cancelAndRevertOrder(cart, true);
            } else {
                log.info("Order Id : [{}] is changed from Waiting to Accepted to Accepted and being processed", cart.getOrderId());
                cart.setStatus("Accepted");
                cartRepository.save(cart);
                orderProcess.updateOrderStatus(cart.getOrderId());
            }
        }
    }

    public void updateItemAvailability(MenuItems item) {
        try {
            menuItemsRepository.updateAvailability(item.getId(), item.isAvailable());
        } catch (Exception e) {
            log.error("Exception while updating availability of item : [{}]", item, e);
            throw new RuntimeException("Unable to update availability of item");
        }
    }

    public List<Restaurant> getAllRestaurants() {
        try {
            return restaurantRepository.findAll();
        } catch (Exception e) {
            log.error("Exception while fetching restaurants", e);
            throw new RuntimeException("Unable to fetch restaurants");
        }
    }

    public List<Restaurant> getRestaurantsByMerchantId(Integer merchantId) {
        try {
            return restaurantRepository.findByMerchantId(merchantId);
        } catch (Exception e) {
            log.error("Exception while fetching restaurants", e);
            throw new RuntimeException("Unable to fetch restaurants");
        }
    }

    public List<Restaurant> getAllRestaurantsAvailable() {
        try {
            return restaurantRepository.findAllAvailableRestaurants();
        } catch (Exception e) {
            log.error("Exception while fetching restaurants which are available", e);
            throw new RuntimeException("Unable to fetch available restaurants");
        }
    }

    public List<Restaurant> getAllRestaurantsUnavailable() {
        try {
            return restaurantRepository.findAllUnAvailableRestaurants();
        } catch (Exception e) {
            log.error("Exception while fetching restaurants which are unavailable", e);
            throw new RuntimeException("Unable to fetch unavailable restaurants");
        }
    }


    public List<MenuItems> getAllItems(Integer restaurantId) {
        try {
            return menuItemsRepository.findByRestaurantId(restaurantId);
        } catch (Exception e) {
            log.error("Exception while fetching items for restaurant id : [{}]", restaurantId, e);
            throw new RuntimeException("Unable to fetch items for restaurant with id : " + restaurantId);
        }
    }

    public List<MenuItems> getAllItemsAvailable(Integer restaurantId) {
        try {
            return menuItemsRepository.findAllAvailableMenuItems(restaurantId);
        } catch (Exception e) {
            log.error("Exception while fetching items which are available for restaurant with Id : [{}]", restaurantId, e);
            throw new RuntimeException("Unable to fetch available items for restaurantID " + restaurantId);
        }
    }

    public List<MenuItems> getAllItemsUnavailable(Integer restaurantId) {
        try {
            return menuItemsRepository.findAllUnAvailableMenuItems(restaurantId);
        } catch (Exception e) {
            log.error("Exception while fetching items which are unavailable for restaurant with id:[{}}", restaurantId, e);
            throw new RuntimeException("Unable to fetch unavailable items for restaurantID " + restaurantId);
        }
    }

    public void saveMerchantRestaurantMapping(Integer merchantId, int restaurantId) {
        MerchantRestaurantMapping merchantRestaurantMapping = new MerchantRestaurantMapping();
        merchantRestaurantMapping.setMerchantId(merchantId);
        merchantRestaurantMapping.setRestaurantId(restaurantId);
        merchantRestaurantMappingRepository.save(merchantRestaurantMapping);
    }

    public S3Object getReportOfMerchant(Integer id) {
        MerchantReport merchantReport = merchantReportRepository.findByMerchantId(id);
        if (merchantReport == null) {
            log.error("Report not available for merchant Id : [{}]", id);
            throw new RuntimeException("Report not available for merchantID");
        }
        S3Object s3Object = awsService.getFileFromS3(merchantReport.getFilePath());
        return s3Object;
    }

    public S3Object getReportOfRestaurant(Integer id) {
        RestaurantReport restaurantReport = restaurantReportRepository.findByRestaurantId(id);
        if (restaurantReport == null) {
            log.error("Report not available for restaurant Id : [{}]", id);
            throw new RuntimeException("Report not available for restaurantId");
        }
        S3Object s3Object = awsService.getFileFromS3(restaurantReport.getFilePath());
        return s3Object;
    }

    public List<Cart> getOrdersOfRestaurant(Integer id) {
        List<Cart> carts = cartRepository.findByRestaurantIdAndStatus(id, "Delivered");
        return carts;
    }

    public Cart getCartofOrder(Integer id) {
        Cart cart = cartRepository.findByOrderId(id).get();
        return cart;
    }

    public List<Order> getItemsOfCart(Integer id) {
        List<Order> orders = orderRepository.findByGroupOrderId(id);
        return orders;
    }

    public void cancelTheOrder(int orderId){
        try{
            Cart cart = cartRepository.findByOrderId(orderId).get();
            if(!cart.getStatus().equals("Accepted")){
                throw new RuntimeException("Order is not in Accepted state");
            }
            cart.setStatus("Cancelled");
            cartRepository.save(cart);
        }catch(Exception e){
            log.error("Exception while cancelling the order : [{}]",orderId);
            throw e;
        }

    }
}
