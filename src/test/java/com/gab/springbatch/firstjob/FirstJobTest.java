package com.gab.springbatch.firstjob;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class FirstJobTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    @Qualifier("firstJob")
    Job firstJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(firstJob);
    }

    @AfterEach
    void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void firstJobCompletes() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(execution.getStepExecutions()).hasSize(1);

        StepExecution stepExecution = execution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getStepName()).isEqualTo("firstStep");
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
