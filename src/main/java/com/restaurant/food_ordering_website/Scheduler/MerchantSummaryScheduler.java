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
public class MerchantSummaryScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private  Job processMerchantJob;

//    @PostConstruct
//    public void init() {
//        runBatchJob();
//    }

//    @Scheduled(fixedRate = 30000) // 60000 milliseconds = 1 minute
    public void runBatchJob() {
        try {
            System.out.println("Batch job started");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(processMerchantJob, jobParameters);
        } catch (Exception e) {
            // Handle exception (e.g., log the error)
            e.printStackTrace();
        }
    }
}
