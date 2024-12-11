package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.SummaryData;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import jakarta.mail.MessagingException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class ExcelService {

    @Autowired
    private AWSService awsService;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private EmailService merchantEmailService;

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Restaurant Name");
        header.createCell(1).setCellValue("Total Orders of the Month");
        header.createCell(2).setCellValue("Total Profit");
    }
    public void writeToExcel(String merchantId, List<SummaryData> restaurantData) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Restaurant Monthly Data");
        createHeader(sheet);

        int rowNum = 1; // Start from the second row

            for(SummaryData summaryData: restaurantData){
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(summaryData.getRestaurantName());
                row.createCell(1).setCellValue(summaryData.getTotalOrders());
                row.createCell(2).setCellValue(summaryData.getTotalValue());
            }
            Merchant merchant = merchantRepository.findByMerchantId(Integer.valueOf(merchantId));
            System.out.println("Sheet created now sending to merchant");
            sendToMerchant(merchant,workbook);
    }

    private void sendToMerchant(Merchant merchant, Workbook workbook) {
        InputStream inputStream = getInputStream(workbook);
        try {
            System.out.println("Sending mail to Merchant");
            merchantEmailService.sendEmailWithAttachment(merchant,inputStream);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private InputStream getInputStream( Workbook workbook) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            ByteArrayInputStream reportStream = new ByteArrayInputStream(outputStream.toByteArray());
            return reportStream;
        } catch (IOException e) {
            // Handle exception
            throw new RuntimeException(e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                // Handle exception
            }
        }
    }
}
