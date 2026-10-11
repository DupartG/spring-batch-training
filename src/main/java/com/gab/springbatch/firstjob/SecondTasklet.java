package com.gab.springbatch.firstjob;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Plain POJO: the job parameter is injected through the constructor by the
 * {@code @StepScope} bean definition in {@link FirstJobConfig}, so this class
 * never reads {@code JobParameters} itself.
 */
public class SecondTasklet implements Tasklet {

    private final String source;

    public SecondTasklet(String source) {
        this.source = source;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        System.out.println("source = " + source);
        return RepeatStatus.FINISHED;
    }
}
