package com.restaurant.food_ordering_website.Reader;

import com.restaurant.food_ordering_website.Model.Cart;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CartItemReader {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public JpaPagingItemReader<Cart> databaseItemReader() {
        return new JpaPagingItemReaderBuilder<Cart>()
                .name("cartItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Cart c WHERE c.status NOT IN ('Delivered', 'Cancelled')")
                .pageSize(10)
                .build();
    }
}

