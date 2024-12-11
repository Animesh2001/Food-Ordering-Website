package com.restaurant.food_ordering_website.Scheduler;

import jakarta.annotation.PostConstruct;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WeeklyCustomerCheckScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job customerCheckJob;

//    @PostConstruct
//    public void init() {
//        runWeeklyCustomerCheckJob();
//    }

//    @Scheduled(fixedRate = 30000) // Run every Sunday at midnight
    public void runWeeklyCustomerCheckJob() {
        System.out.println("Starting batch job for lazy customers");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(customerCheckJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
