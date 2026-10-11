# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

A learning repository for Spring Batch, built while following the Udemy course
*Spring Batch Framework for Beginners* (Abhay Redkar). Each course topic is
implemented as a self-contained job so it can be run and inspected in isolation.

This is a scratchpad for learning, not a production system. Favour clarity and
explicitness over abstraction — the point is to see the framework's mechanics,
not to hide them behind helpers.

## Stack

- Java 21
- Spring Boot 3.x / Spring Batch 5
- Maven
- MySQL (default profile, via `compose.yaml`) for persistent job metadata;
  H2 in-memory for tests only (`test` profile, `src/test/resources/application-test.yml`)
- JUnit 5 + `spring-batch-test`

## Critical: Spring Batch 5 API only

The course was recorded against Spring Batch 4 and has a migration section
bolted on at the end. **Never** write Spring Batch 4 code here, even if it
matches what is on screen in the videos.

Do not use:

- `JobBuilderFactory` / `StepBuilderFactory` — removed
- `@EnableBatchProcessing` — not needed with Spring Boot 3 autoconfiguration,
  and adding it actively disables that autoconfiguration
- `extends DefaultBatchConfigurer` — removed

Use instead:

- `new JobBuilder("jobName", jobRepository)`
- `new StepBuilder("stepName", jobRepository)`
- Inject `JobRepository` and `PlatformTransactionManager` into the `@Bean`
  methods; every chunk or tasklet step builder requires the transaction manager
  explicitly.

Reference shape:

```java
@Bean
Step importStep(JobRepository jobRepository,
                PlatformTransactionManager txManager,
                ItemReader<Product> reader,
                ItemProcessor<Product, Product> processor,
                ItemWriter<Product> writer) {
    return new StepBuilder("importStep", jobRepository)
            .<Product, Product>chunk(100, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
}
```

Other 5.x details worth remembering: `ItemWriter.write` takes a `Chunk<T>`, not
a `List<T>`; the metadata tables use `BATCH_` prefixed DDL shipped in
`spring-batch-core` under `org/springframework/batch/core/schema-*.sql`.

## Layout

```
src/main/java/<base>/
  <topic>/              one package per course topic (readers, writers,
                        processors, flows, listeners, errorhandling, ...)
    <Topic>JobConfig.java
    supporting classes (reader/writer/processor/listener)
  domain/               shared POJOs (Product, etc.)
src/main/resources/
  application.yml
  data/                 sample CSV / JSON input
src/test/java/          one test per job
```

Each job config class owns its own `Job`, `Step`s and components. Do not
extract shared base classes across topics — duplication between topics is
intentional so each one reads standalone.

## Conventions

- Bean names and job names are explicit strings, matching the package topic
  (`flatFileImportJob`, not `job`).
- `spring.batch.job.enabled=false` in `application.yml`. Jobs are launched
  deliberately, never on context startup.
- Launch a specific job with `--spring.batch.job.enabled=true
  --spring.batch.job.name=<jobName>` when running from the command line (the
  `enabled` override is required, `job.name` alone does nothing), or via a
  `JobLauncher` in a test.
- Every job gets a unique `JobParameters` (e.g. a `run.date` or run id) —
  otherwise reruns fail with `JobInstanceAlreadyCompleteException`.
- Readers and writers that hold state are `@StepScope`.
- Sample data files live in `src/main/resources/data/` and are read by path
  from configuration, not hardcoded in Java.

## Commands

```bash
mvn clean verify                                  # build + tests
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=importJob"
mvn test -Dtest=ImportJobTest                     # single job test
```

MySQL must be up before running the app (tests use H2 and do not need it):

```bash
docker compose up -d                              # MySQL 8.4, data in volume batch-mysql-data
docker compose down -v                            # stop and wipe job metadata
```

Job tests (`@SpringBootTest`) must carry `@ActiveProfiles("test")`.

## Testing

Use `JobLauncherTestUtils` and `JobRepositoryTestUtils`. Assert on
`BatchStatus`, `ExitStatus`, and read/write/skip counts from the
`StepExecution` — not just on "it did not throw".

```java
JobExecution execution = jobLauncherTestUtils.launchJob(uniqueParams());
assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
assertThat(stepExecution.getWriteCount()).isEqualTo(50);
assertThat(stepExecution.getSkipCount()).isEqualTo(2);
```

Clear metadata between tests so job instances do not collide.

## Working with me

I am a Java backend developer with around five years of experience (Java EE,
Spring REST, event-driven systems). Skip explanations of Java, Maven, Spring DI
and REST basics. Explain Spring Batch specifics — scopes, restart semantics,
transaction boundaries around chunks, skip/retry interaction — where they are
genuinely non-obvious.

Answer directly. Do not pad responses with summaries of what was just done,
and do not regenerate files unless I ask.