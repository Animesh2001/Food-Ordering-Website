package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.Restaurant;
import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import jakarta.mail.internet.MimeMessage;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private AWSService awsService;

    @Autowired
    private JavaMailSender javaMailSender;

//    @PostConstruct
//    public void init() {
//        sendMonthlySummaries();
//    }


    public void sendMonthlySummaries() {
        List<Merchant> merchants = merchantRepository.findAll();

        for (Merchant merchant : merchants) {
            // Fetch the file from S3
            InputStream fileInputStream = awsService.getFileFromS3("s3://foodorderingsummary/Daily_Report_Restaurant_10_2024-10-12_18-48-42.xlsx").getObjectContent();
            if (fileInputStream != null) {
                try {
                    // Send email with the .xlsx attachment
                    sendEmailWithAttachment(merchant, fileInputStream);
                } catch (MessagingException e) {
                    // Handle exception (log it and continue)
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendEmailWithAttachment(Merchant merchant, InputStream fileInputStream) throws MessagingException, jakarta.mail.MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("sendtoanimeshjaiswal@gmail.com");
        helper.setTo(merchant.getEmail());
        helper.setSubject("Your Monthly Summary");
        helper.setText("Dear " + merchant.getEmail() + ",\n\nPlease find attached your monthly summary.\n\nBest regards,\nYour Restaurant Team");

        // Attach the .xlsx file
        helper.addAttachment("Monthly_Report_" + merchant.getMerchantId() + ".xlsx", new ByteArrayResource(fileInputStream.readAllBytes()));

        // Send the email
        javaMailSender.send(message);
        System.out.println("Successfully send email to "+ merchant.getEmail());
    }

    public void sendEmailWithTopRestaurants(User user, List<Restaurant> topRestaurants) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("sendtoanimeshjaiswal@gmail.com");
        helper.setTo(user.getEmail());
        helper.setSubject("Top Restaurants for You this week!");

        // Prepare the content of the email
        StringBuilder emailContent = new StringBuilder();

        // Add greeting
        emailContent.append("Hello,")
                .append("\n\n"); // Add one line space after the comma

        // Add restaurant list with each on a new line
        emailContent.append("Here are the top 5 restaurants for you this week:\n");
        for (Restaurant restaurant : topRestaurants) {
            emailContent.append(restaurant.getName()).append("\n")  // Restaurant name
                    .append("(").append(restaurant.getAddress()).append(")").append("\n\n"); // Restaurant address inside brackets
        }

        // Add final line
        emailContent.append("\nVisit our app to order now!"); // Ensure this comes at the end

        // Set email content
        helper.setText(emailContent.toString());

        // Send the email
        javaMailSender.send(message);
        System.out.println("Successfully send email to "+ user.getEmail());
    }
}

