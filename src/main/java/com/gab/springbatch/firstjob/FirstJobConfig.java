package com.gab.springbatch.firstjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * {@code @StepScope} makes this bean a proxy: the real tasklet is created at the start of
     * each step execution, which is when {@code jobParameters} exists and the SpEL expression
     * can be evaluated. Without it, the expression would be evaluated at context startup and fail.
     */
    @Bean
    @StepScope
    Tasklet secondTasklet(@Value("#{jobParameters['source']}") String source) {
        return new SecondTasklet(source);
    }

    @Bean
    Step secondStep(JobRepository jobRepository,
                    PlatformTransactionManager txManager,
                    Tasklet secondTasklet) {
        return new StepBuilder("secondStep", jobRepository)
                .tasklet(secondTasklet, txManager)
                .build();
    }

    @Bean
    Job firstJob(JobRepository jobRepository, Step firstStep, Step secondStep) {
        return new JobBuilder("firstJob", jobRepository)
                .start(firstStep)
                .next(secondStep)
                .build();
    }
}
