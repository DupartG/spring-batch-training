Spring Batch is for handling huge load of data that does not need human interaction. It is only applied for treatment, and doesn't not provide scheduling features.

A batch unit of work is called a Job, composed of Steps. Each Step is either a
tasklet or a chunk-oriented step.

A tasklet is a single `execute()` call in one transaction used for a one-off
action in the chain (cleanup, unzip, truncate, flag).

A chunk-oriented step is built from an ItemReader, an optional ItemProcessor and an ItemWriter. The framework reads and processes items one at a time, writes them in groups of N, and commits after each group. Use it to iterate over independent items. The per-chunk commit is what gives restartability, read/write/skip counts, and skip/retry support, none of which a tasklet can provide, since its loop is opaque to the framework.

Spring Batch with spring boot embedded multiple database tables (JobRepository) that are created and updated automatically
JobInstance = Job+JobParameters. Only run jobs are registered in the batch_shema, that also means changing a job's name won't update it name.

A JobInstance is a job + parameters, hence it can have multiple executions like a restart or a retry. 