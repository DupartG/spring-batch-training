package com.gab.springbatch.firstjob;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

class FirstTaskletTest {

    @Test
    void executeFinishesInOneCall() {
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        StepContribution contribution = new StepContribution(stepExecution);
        ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

        RepeatStatus status = new FirstTasklet().execute(contribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }
}
