package com.restaurant.food_ordering_website.Scheduler;

import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.MerchantReport;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Model.RestaurantReport;
import com.restaurant.food_ordering_website.Repository.MerchantReportRepository;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantReportRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import com.restaurant.food_ordering_website.Service.AWSService;
import com.restaurant.food_ordering_website.Service.ReportService;
import com.restaurant.food_ordering_website.Service.S3Service;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
public class ReportScheduler {

    @Autowired
    private ReportService reportService;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MerchantReportRepository merchantReportRepository;

    @Autowired
    private RestaurantReportRepository restaurantReportRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AWSService awsService;

    @Value("${s3.file.path}")
    private String filePath;

//    @PostConstruct
//    public void init() {
//        generateDailyReportForRestaurantLevel();
////        generateDailyReportForMerchantLevel();
//    }

    private void generateDailyReportForRestaurantLevel() {
        try{
            List<Restaurant> restaurants = restaurantRepository.findAll();
            for(Restaurant restaurant : restaurants){
                System.out.println("Generating report for restaurantId : [{}]" + restaurant.getRestaurantId());
                ByteArrayOutputStream reportContent = reportService.generateReportForRestaurant(restaurant);

                String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String restaurantId = String.valueOf(restaurant.getRestaurantId());
                String reportName = "Daily_Report_Restaurant_" +restaurantId + "_"+ timestamp + ".xlsx";

                // Create the full path for the report
//                String reportFilePath = Paths.get(rootDirectory, reportName).toString();
//
//                try (FileOutputStream fos = new FileOutputStream(reportFilePath)) {
//                    reportContent.writeTo(fos);  // Write the byte array to the file
//                }
//                reportContent.close();
                String bucketName = "foodorderingsummary";
                ByteArrayInputStream reportStream = new ByteArrayInputStream(reportContent.toByteArray());
                long contentLength = reportContent.toByteArray().length;  // Set the size of the report

                awsService.uploadReport(bucketName, reportStream, reportName, contentLength);

                RestaurantReport restaurantReport = restaurantReportRepository.findByRestaurantId(restaurant.getRestaurantId());
                if(restaurantReport!=null){
                    restaurantReport.setFilePath(filePath+reportName);
                    restaurantReportRepository.save(restaurantReport);
                }else{
                    RestaurantReport restaurantReport1 = new RestaurantReport();
                    restaurantReport1.setRestaurantId(restaurant.getRestaurantId());
                    restaurantReport1.setFilePath(filePath+reportName);
                    restaurantReportRepository.save(restaurantReport1);
                }
            }
        }catch(Exception e){
            log.error("Error while saving the report for restaurant, message : [{}]", e.getMessage(),e);
            e.printStackTrace(); // Handle exceptions appropriately
        }

    }


    //    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void generateDailyReportForMerchantLevel() {
        try {
            List <Merchant> merchants = merchantRepository.findAll();
//            String rootDirectory = "files";
//            File directory = new File(rootDirectory);
//            if (!directory.exists()) {
//                boolean created = directory.mkdirs(); // Use mkdirs() instead of mkdir()
//                if (created) {
//                    System.out.println("Directory is created");
//                } else {
//                    System.out.println("Failed to create directory");
//                    return; // Exit the method if directory creation fails
//                }
//            } else {
//                // Delete all existing files in the directory
//                File[] files = directory.listFiles();
//                if (files != null) {
//                    for (File file : files) {
//                        if (file.isFile()) {
//                            boolean deleted = file.delete();
//                            if (deleted) {
//                                System.out.println("Deleted file: " + file.getName());
//                            } else {
//                                System.out.println("Failed to delete file: " + file.getName());
//                            }
//                        }
//                    }
//                }
//            }
            for(Merchant merchant : merchants){
                System.out.println("Generating report for merchantId : [{}]" + merchant.getMerchantId());
                ByteArrayOutputStream reportContent = reportService.generateReportForMerchant(merchant);

                String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String merchantId = String.valueOf(merchant.getMerchantId());
                String reportName = "Daily_Report_Merchant_" +merchantId + "_"+ timestamp + ".xlsx";

                // Create the full path for the report
//                String reportFilePath = Paths.get(rootDirectory, reportName).toString();
//
//                try (FileOutputStream fos = new FileOutputStream(reportFilePath)) {
//                    reportContent.writeTo(fos);  // Write the byte array to the file
//                }
//                reportContent.close();
                String bucketName = "foodorderingsummary";
                ByteArrayInputStream reportStream = new ByteArrayInputStream(reportContent.toByteArray());
                long contentLength = reportContent.toByteArray().length;  // Set the size of the report

                awsService.uploadReport(bucketName, reportStream, reportName, contentLength);

                MerchantReport merchantReport = merchantReportRepository.findByMerchantId(merchant.getMerchantId());
                if(merchantReport!=null){
                    merchantReport.setFilePath(filePath+reportName);
                    merchantReportRepository.save(merchantReport);
                }else{
                    MerchantReport merchantReport1 = new MerchantReport();
                    merchantReport1.setMerchantId(merchant.getMerchantId());
                    merchantReport1.setFilePath(filePath+reportName);
                    merchantReportRepository.save(merchantReport1);
                }
            }
//            s3Service.uploadReport(reportName, reportContent);
        } catch (Exception e) {
            log.error("Error while saving the report for Merchant, message : [{}]", e.getMessage(),e);
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }
}
