package com.restaurant.food_ordering_website.Reader;

import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MerchantItemReader extends AbstractItemStreamItemReader<Merchant> {

    @Autowired
    private MerchantRepository merchantRepository;

    private List<Merchant> merchants;

    private int currentIndex = 0;

    @Override
    public void open(ExecutionContext executionContext) {
        // Reset the state at the start of each batch job
        merchants = merchantRepository.findAll();
        currentIndex = 0;
        System.out.println("Fetched merchants for a new job run.");
    }

    @Override
    public Merchant read() {
        if (merchants == null || currentIndex == 0) {
            merchants = merchantRepository.findAll();
            currentIndex = 0;
            System.out.println("Fetched merchants for a new job run.");
        }

        if (currentIndex < merchants.size()) {
            System.out.println("Reading merchant at index: " + currentIndex);
            return merchants.get(currentIndex++);
        }

        System.out.println("No more merchants to process.");
        return null;
    }
}

