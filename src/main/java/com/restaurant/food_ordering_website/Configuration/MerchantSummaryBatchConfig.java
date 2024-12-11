package com.restaurant.food_ordering_website.Configuration;

import com.restaurant.food_ordering_website.Model.Merchant;
import com.restaurant.food_ordering_website.Model.MerchantSummaryData;
import com.restaurant.food_ordering_website.Processor.MerchantItemProcessor;
import com.restaurant.food_ordering_website.Reader.MerchantItemReader;
import com.restaurant.food_ordering_website.Repository.CartRepository;
import com.restaurant.food_ordering_website.Repository.MerchantRepository;
import com.restaurant.food_ordering_website.Repository.RestaurantRepository;
import com.restaurant.food_ordering_website.Service.ExcelService;
import com.restaurant.food_ordering_website.Writer.RestaurantDataWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@EnableBatchProcessing
public class MerchantSummaryBatchConfig {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private  RestaurantRepository restaurantRepository;

    @Autowired
    private  CartRepository cartRepository;

    @Autowired
    private ExcelService excelService;
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private MerchantItemReader merchantItemReader;

    @Autowired
    private MerchantItemProcessor merchantItemProcessor;

    @Autowired
    private RestaurantDataWriter restaurantDataWriter;

    @Autowired
    private PlatformTransactionManager transactionManager;


    @Bean
    public Job processMerchantJob(JobRepository jobRepository) {
        return new JobBuilder("processMerchantJob",jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(processMerchantStep())
                .end()
                .build();
    }

    @Bean
    public Step processMerchantStep() {
        return new StepBuilder("processMerchantStep",jobRepository)
                .<Merchant, MerchantSummaryData>chunk(10, transactionManager)
                .reader(merchantItemReader)
                .processor(merchantItemProcessor)
                .writer(restaurantDataWriter)
                .build();
    }

}

