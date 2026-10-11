package com.gab.springbatch.firstjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FirstJobConfig {

    @Bean
    Tasklet firstTasklet() {
        return new FirstTasklet();
    }

    @Bean
    Step firstStep(JobRepository jobRepository,
                   PlatformTransactionManager txManager,
                   Tasklet firstTasklet) {
        return new StepBuilder("firstStep", jobRepository)
                .tasklet(firstTasklet, txManager)
                .build();
    }

    @Bean
    Job firstJob(JobRepository jobRepository, Step firstStep) {
        return new JobBuilder("firstJob", jobRepository)
                .start(firstStep)
                .build();
    }
}
