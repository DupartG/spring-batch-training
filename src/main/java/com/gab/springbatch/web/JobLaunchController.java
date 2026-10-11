package com.gab.springbatch.web;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/jobs")
public class JobLaunchController {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobsByName;

    /**
     * {@code List<Job>} is a collection injection: Spring resolves it by type and passes every
     * bean of type {@link Job} in the context (each {@code @Bean Job} method, typed from its
     * return type). The bean definitions are already registered before this constructor runs;
     * the parameter only queries them, and makes Spring instantiate the jobs before this
     * controller. Spring does not populate {@code jobsByName}; it is built here from that list.
     *
     */
    public JobLaunchController(JobLauncher jobLauncher, List<Job> jobs) {
        this.jobLauncher = jobLauncher;
        // Keyed by Job.getName() (the name stored in BATCH_JOB_INSTANCE), not by bean name.
        this.jobsByName = jobs.stream().collect(Collectors.toMap(Job::getName, Function.identity()));
    }

    /**
     * Every request param becomes an identifying String job parameter.
     * A run.id timestamp is added so that each call creates a new JobInstance.
     */
    @PostMapping("/{jobName}")
    public JobLaunchResponse launch(@PathVariable String jobName,
                                    @RequestParam Map<String, String> params)
            throws JobExecutionAlreadyRunningException, JobRestartException,
                   JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        Job job = jobsByName.get(jobName);
        if (job == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown job: " + jobName);
        }

        JobParametersBuilder builder = new JobParametersBuilder();
        params.forEach(builder::addString);
        builder.addLong("run.id", System.currentTimeMillis());
        JobParameters jobParameters = builder.toJobParameters();

        // The auto-configured JobLauncher uses a SyncTaskExecutor: this call blocks until the job ends.
        JobExecution execution = jobLauncher.run(job, jobParameters);

        return new JobLaunchResponse(
                execution.getId(),
                job.getName(),
                execution.getStatus().name(),
                execution.getExitStatus().getExitCode());
    }

    public record JobLaunchResponse(Long executionId, String jobName, String status, String exitCode) {
    }
}
