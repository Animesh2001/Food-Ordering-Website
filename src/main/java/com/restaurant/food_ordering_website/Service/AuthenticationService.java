package com.restaurant.food_ordering_website.Service;

import com.restaurant.food_ordering_website.DTO.SignInUser;
import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Repository.CustomerRepository;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    @Autowired
    private CustomerRepository authenticationRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    public void addCustomer(User user) {
        try {
            authenticationRepository.save(user);
            log.info("user added successfully.");
        } catch (Exception e) {
            log.error("Exception while saving user: [{}]", user, e);
            throw new RuntimeException("Unable to save user");
        }
    }


    public User getCustomer(SignInUser signInUser) {
        if(authenticationRepository.findByEmail(signInUser.getEmail()).isPresent()){
            return authenticationRepository.findByEmail(signInUser.getEmail()).get();
        }else{
            throw new RuntimeException("No User found");
        }
    }

    public void addMerchant(SignInUser user) {
        try {
            Merchant merchant = new Merchant();
            merchant.setEmail(user.getEmail());
            merchant.setPassword(user.getPassword());
            merchantRepository.save(merchant);
            log.info("merchant added successfully.");
        } catch (Exception e) {
            log.error("Exception while saving user: [{}]", user, e);
            throw new RuntimeException("Unable to save user");
        }

    }

    public Merchant getMerchant(SignInUser user) {
        if(merchantRepository.findByEmail(user.getEmail()).isPresent()){
            return merchantRepository.findByEmail(user.getEmail()).get();
        }else{
            throw new RuntimeException("No Merchant found");
        }
    }
}
