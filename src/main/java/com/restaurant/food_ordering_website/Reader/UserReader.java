package com.restaurant.food_ordering_website.Reader;

import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Repository.CustomerRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserReader extends AbstractItemStreamItemReader<User> {

    @Autowired
    private CustomerRepository customerRepository;

    private List<User> customers;
    private int currentIndex = 0;

    @Override
    public void open(ExecutionContext executionContext) {
        // Reset the state at the start of each batch job
        customers = customerRepository.findAll();
        currentIndex = 0;
        System.out.println("Fetched customers for a new job run.");
    }

    @Override
    public User read() {
        if (customers == null) {
            customers = customerRepository.findAll();
        }

        if (currentIndex < customers.size()) {
            return customers.get(currentIndex++);
        } else {
            return null; // All users processed
        }
    }
}

