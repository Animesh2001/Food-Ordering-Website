package com.restaurant.food_ordering_website.Configuration;

import com.restaurant.food_ordering_website.Model.Cart;
import com.restaurant.food_ordering_website.Processor.CartItemProcessor;
import com.restaurant.food_ordering_website.Reader.CartItemReader;
import com.restaurant.food_ordering_website.Writer.CartItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class OrderStatusUpdateBatchConfig {

    @Autowired
    private CartItemReader cartItemReader;

    @Autowired
    private CartItemProcessor cartItemProcessor;

    @Autowired
    private CartItemWriter cartItemWriter;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job updateOrderStatusJob(JobRepository jobRepository) {
        return new JobBuilder("updateOrderStatusJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(updateOrderStatusStep())
                .end()
                .build();
    }

    @Bean
    public Step updateOrderStatusStep() {
        return new StepBuilder("updateOrderStatusStep", jobRepository)
                .<Cart, Cart>chunk(10, transactionManager)
                .reader(cartItemReader.databaseItemReader())
                .processor(cartItemProcessor)
                .writer(cartItemWriter)
                .build();
    }
}
