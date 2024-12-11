package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.MerchantRestaurantMapping;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import com.restaurant.food_ordering_website.Repository.MerchantRestaurantMappingRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantRestaurantMappingRepository merchantRestaurantMappingRepository;

    @Autowired
    private SummaryService service;

    public ByteArrayOutputStream generateReportForMerchant(Merchant merchant) throws Exception {
        System.out.println("Starting generateReport..!!!!");
        // Create a workbook and sheets
        Workbook workbook = new XSSFWorkbook();
        Sheet summarySheet = workbook.createSheet("Summary");
        Sheet ordersSheet = workbook.createSheet("Orders");

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime endOfYesterday = startOfYesterday.plusDays(1).minusSeconds(1);

        // Convert to milliseconds
        long startOfYesterdayMillis = 1728472955483L;
        long endOfYesterdayMillis = 1728604740000L;

        createSummarySheetForMerchant(summarySheet, merchant, startOfYesterdayMillis, endOfYesterdayMillis);

        createOrdersSheetForMerchant(ordersSheet, merchant, startOfYesterdayMillis, endOfYesterdayMillis);

        // Create Summary Sheet
//        createSummarySheet(summarySheet, carts);

        // Create Orders Sheet
//        createOrdersSheet(ordersSheet, carts);

        // Write to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    private void createSummarySheetForMerchant(Sheet sheet, Merchant merchant, long startOfYesterdayMillis, long endOfYesterdayMillis) {
        System.out.println("Creating summary sheet");
        // Create header row

        int rowIndex = 0;
        Row headerRow = sheet.createRow(rowIndex);
        rowIndex++;
        headerRow.createCell(0).setCellValue("Restaurant Name");
        headerRow.createCell(1).setCellValue("Total Order Placed");
        headerRow.createCell(2).setCellValue("Order with highest amount");
        headerRow.createCell(3).setCellValue("Most Ordered Item");

        System.out.println("Till here merchantRestaurantMappings");
        System.out.println("Start " + startOfYesterdayMillis);
        System.out.println("End " + endOfYesterdayMillis);
        List<MerchantRestaurantMapping> merchantRestaurantMappings = merchantRestaurantMappingRepository.findByMerchantId(merchant.getMerchantId());
        for (MerchantRestaurantMapping merchantRestaurantMapping : merchantRestaurantMappings) { //new row
            System.out.println("Merchant Mappings " + merchantRestaurantMapping.getMerchantId() + " " + merchantRestaurantMapping.getRestaurantId());
            System.out.println("----till here---");
            System.out.println("RestaurantId : " + merchantRestaurantMapping.getRestaurantId());
            List<Cart> carts = cartRepository.findCartsByDateRangeAndRestaurantId(startOfYesterdayMillis, endOfYesterdayMillis, merchantRestaurantMapping.getRestaurantId());
            if (carts.size() == 0) {
                System.out.println("No order placed for restaurantId : " + merchantRestaurantMapping.getRestaurantId());
                continue;
            }
            System.out.println("Before getRestaurantName");
            String restaurantName = service.getRestaurantName(merchantRestaurantMapping.getRestaurantId());
            int totalOrderPlaced = carts.size();
            int orderWithHighestAmount = carts.get(0).getTotalValue();
            String mostOrderedItem = service.getMostOrderedItem(carts);
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.createCell(0).setCellValue(restaurantName);
            dataRow.createCell(1).setCellValue(totalOrderPlaced);
            dataRow.createCell(2).setCellValue(orderWithHighestAmount);
            dataRow.createCell(3).setCellValue(mostOrderedItem);
            System.out.println("Added " + restaurantName + " " + totalOrderPlaced + " " + orderWithHighestAmount + " " + mostOrderedItem);
            rowIndex++;
        }
    }

    private void createOrdersSheetForMerchant(Sheet sheet, Merchant merchant, long startOfYesterdayMillis, long endOfYesterdayMillis) {
        System.out.println("Creating Order Sheet");

        int rowIndex = 0;
        Row headerRow = sheet.createRow(rowIndex);
        rowIndex++;

        headerRow.createCell(0).setCellValue("Restaurant Name");
        headerRow.createCell(1).setCellValue("Order ID");
        headerRow.createCell(2).setCellValue("Order Total Value");
        headerRow.createCell(3).setCellValue("Order Status");

        System.out.println("Till here merchantRestaurantMappings");
        System.out.println("Start " + startOfYesterdayMillis);
        System.out.println("End " + endOfYesterdayMillis);
        List<MerchantRestaurantMapping> merchantRestaurantMappings = merchantRestaurantMappingRepository.findByMerchantId(merchant.getMerchantId());
        for (MerchantRestaurantMapping merchantRestaurantMapping : merchantRestaurantMappings) { //new row
            System.out.println("Merchant Mappings " + merchantRestaurantMapping.getMerchantId() + " " + merchantRestaurantMapping.getRestaurantId());
            System.out.println("----till here---");
            System.out.println("RestaurantId : " + merchantRestaurantMapping.getRestaurantId());
            List<Cart> carts = cartRepository.findCartsByDateRangeAndRestaurantId(startOfYesterdayMillis, endOfYesterdayMillis, merchantRestaurantMapping.getRestaurantId());
            if (carts.size() == 0) {
                System.out.println("No order placed for restaurantId : " + merchantRestaurantMapping.getRestaurantId());
                continue;
            }
            for (Cart cart : carts) {
                System.out.println("Before getRestaurantName");
                String restaurantName = service.getRestaurantName(merchantRestaurantMapping.getRestaurantId());
                int orderId = cart.getOrderId();
                int orderTotalValue = cart.getTotalValue();
                String orderStatus = cart.getStatus();
                Row dataRow = sheet.createRow(rowIndex);
                dataRow.createCell(0).setCellValue(restaurantName);
                dataRow.createCell(1).setCellValue(orderId);
                dataRow.createCell(2).setCellValue(orderTotalValue);
                dataRow.createCell(3).setCellValue(orderStatus);
                System.out.println("Added " + restaurantName + " " + orderId + " " + orderTotalValue + " " + orderStatus);
                rowIndex++;
            }
            rowIndex++;

        }
    }

    public ByteArrayOutputStream generateReportForRestaurant(Restaurant restaurant) throws Exception {
        System.out.println("Starting generateReport..!!!!");
        // Create a workbook and sheets
        Workbook workbook = new XSSFWorkbook();
        Sheet summarySheet = workbook.createSheet("Summary");
        Sheet ordersSheet = workbook.createSheet("Orders");

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime endOfYesterday = startOfYesterday.plusDays(1).minusSeconds(1);

        // Convert to milliseconds
        long startOfYesterdayMillis = 1728472955483L;
        long endOfYesterdayMillis = 1728604740000L;

        createSummarySheetForRestaurant(summarySheet, restaurant, startOfYesterdayMillis, endOfYesterdayMillis);

        createOrdersSheetForRestaurant(ordersSheet, restaurant, startOfYesterdayMillis, endOfYesterdayMillis);

        // Create Summary Sheet
//        createSummarySheet(summarySheet, carts);

        // Create Orders Sheet
//        createOrdersSheet(ordersSheet, carts);

        // Write to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    private void createOrdersSheetForRestaurant(Sheet sheet, Restaurant restaurant, long startOfYesterdayMillis, long endOfYesterdayMillis) {
        System.out.println("Creating Order Sheet for restaurant");

        int rowIndex = 0;
        Row headerRow = sheet.createRow(rowIndex);
        rowIndex++;

        headerRow.createCell(0).setCellValue("Restaurant Name");
        headerRow.createCell(1).setCellValue("Order ID");
        headerRow.createCell(2).setCellValue("Order Total Value");
        headerRow.createCell(3).setCellValue("Order Status");

        System.out.println("----till here---");
        System.out.println("RestaurantId : " + restaurant.getRestaurantId());
        List<Cart> carts = cartRepository.findCartsByDateRangeAndRestaurantId(startOfYesterdayMillis, endOfYesterdayMillis, restaurant.getRestaurantId());
        if (carts.size() == 0) {
            System.out.println("No order placed for restaurantId : " + restaurant.getRestaurantId());
            return;
        }
        for (Cart cart : carts) {
            System.out.println("Before getRestaurantName");
            String restaurantName = restaurant.getName();
            int orderId = cart.getOrderId();
            int orderTotalValue = cart.getTotalValue();
            String orderStatus = cart.getStatus();
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.createCell(0).setCellValue(restaurantName);
            dataRow.createCell(1).setCellValue(orderId);
            dataRow.createCell(2).setCellValue(orderTotalValue);
            dataRow.createCell(3).setCellValue(orderStatus);
            System.out.println("Added " + restaurantName + " " + orderId + " " + orderTotalValue + " " + orderStatus);
            rowIndex++;
        }
    }

    private void createSummarySheetForRestaurant(Sheet sheet, Restaurant restaurnat, long startOfYesterdayMillis, long endOfYesterdayMillis) {
        System.out.println("Creating summary sheet for Restaurant");
        // Create header row

        int rowIndex = 0;
        Row headerRow = sheet.createRow(rowIndex);
        rowIndex++;
        headerRow.createCell(0).setCellValue("Restaurant Name");
        headerRow.createCell(1).setCellValue("Total Order Placed");
        headerRow.createCell(2).setCellValue("Order with highest amount");
        headerRow.createCell(3).setCellValue("Most Ordered Item");

        System.out.println("Start " + startOfYesterdayMillis);
        System.out.println("End " + endOfYesterdayMillis);

        System.out.println("----till here---");
        System.out.println("RestaurantId : " + restaurnat.getRestaurantId());
        List<Cart> carts = cartRepository.findCartsByDateRangeAndRestaurantId(startOfYesterdayMillis, endOfYesterdayMillis, restaurnat.getRestaurantId());
        if (carts.size() == 0) {
            System.out.println("No order placed for restaurantId : " + restaurnat.getRestaurantId());
            return;
        }
        System.out.println("Before getRestaurantName");
        String restaurantName = service.getRestaurantName(restaurnat.getRestaurantId());
        int totalOrderPlaced = carts.size();
        int orderWithHighestAmount = carts.get(0).getTotalValue();
        String mostOrderedItem = service.getMostOrderedItem(carts);
        Row dataRow = sheet.createRow(rowIndex);
        dataRow.createCell(0).setCellValue(restaurantName);
        dataRow.createCell(1).setCellValue(totalOrderPlaced);
        dataRow.createCell(2).setCellValue(orderWithHighestAmount);
        dataRow.createCell(3).setCellValue(mostOrderedItem);
        System.out.println("Added " + restaurantName + " " + totalOrderPlaced + " " + orderWithHighestAmount + " " + mostOrderedItem);
    }


}
