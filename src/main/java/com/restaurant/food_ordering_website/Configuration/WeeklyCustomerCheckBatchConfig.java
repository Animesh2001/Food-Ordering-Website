package com.restaurant.food_ordering_website.Configuration;

import com.restaurant.food_ordering_website.Model.User;
import com.restaurant.food_ordering_website.Processor.UserEmailProcessor;
import com.restaurant.food_ordering_website.Reader.UserReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class WeeklyCustomerCheckBatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserReader userReader;

    @Autowired
    private UserEmailProcessor userEmailProcessor;



    @Bean
    public Job customerCheckJob() {
        return new JobBuilder("customerCheckJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(customerCheckStep())
                .end()
                .build();
    }

    @Bean
    public Step customerCheckStep() {
        return new StepBuilder("customerCheckStep", jobRepository)
                .<User, User>chunk(10, transactionManager)
                .reader(userReader)
                .processor(userEmailProcessor)
                .writer(noOpWriter())
                .build();
    }

    @Bean
    public ItemWriter<User> noOpWriter() {
        return items -> {
            // Do nothing
        };
    }

}

