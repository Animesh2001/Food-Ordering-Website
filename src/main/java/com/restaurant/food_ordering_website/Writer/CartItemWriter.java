package com.restaurant.food_ordering_website.Writer;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartItemWriter implements ItemWriter<Cart> {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public void write(Chunk<? extends Cart> chunk) throws Exception {
        cartRepository.saveAll(chunk.getItems());
    }
}


