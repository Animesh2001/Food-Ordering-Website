package com.restaurant.food_ordering_website.Scheduler;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class OrderStatusUpdateScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job updateOrderStatusJob;

    @Scheduled(fixedRate = 10000) // Run every 60 seconds
    public void runJob() throws Exception {
        System.out.println("Batch job started new one");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(updateOrderStatusJob, jobParameters);
    }
}

