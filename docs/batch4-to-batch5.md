# Pense-bête Spring Batch 4 → 5

La formation montre du Batch 4 (Boot 2). Le projet tourne en Batch 5 (Boot 3).
Une ligne par équivalence rencontrée, ajoutée au fil des modules.

## Configuration générale

| Batch 4 (formation) | Batch 5 (projet) | Pourquoi |
|---|---|---|
| `@EnableBatchProcessing` sur la classe main | Rien. Supprimer l'annotation | Boot 3 autoconfigure Batch. L'annotation **désactive** cette autoconfiguration (plus d'init des tables `BATCH_*`, plus de lancement via `spring.batch.job.*`) |
| Jobs et steps déclarés dans la classe main | Une classe `@Configuration` par sujet (`firstjob/FirstJobConfig`) | Convention du projet, pas une contrainte Batch 5 |

## Job et Step

| Batch 4 (formation) | Batch 5 (projet) | Pourquoi |
|---|---|---|
| `@Autowired JobBuilderFactory jobBuilderFactory;` | Injecter `JobRepository` en paramètre de la méthode `@Bean` | Les factories sont supprimées en 5.0. Elles ne faisaient que cacher le `JobRepository` |
| `jobBuilderFactory.get("job1")` | `new JobBuilder("firstJob", jobRepository)` | Idem |
| `@Autowired StepBuilderFactory stepBuilderFactory;` | Injecter `JobRepository` **et** `PlatformTransactionManager` | Idem |
| `stepBuilderFactory.get("step1")` | `new StepBuilder("firstStep", jobRepository)` | Idem |
| `.tasklet(tasklet)` | `.tasklet(tasklet, txManager)` | Le transaction manager est explicite : chaque `execute()` de tasklet tourne dans une transaction |
| `.start(firstStep())` (appel de méthode) | `.start(firstStep)` (bean injecté en paramètre) | Les deux marchent dans une `@Configuration`. L'injection rend la dépendance explicite |

## Tasklet

| Batch 4 (formation) | Batch 5 (projet) | Pourquoi |
|---|---|---|
| `new Tasklet() { public RepeatStatus execute(...) {...} }` inline dans le step | Classe dédiée `FirstTasklet implements Tasklet`, exposée par un `@Bean` | Pas un changement Batch 5, une bonne pratique : la classe porte un nom métier, reçoit ses dépendances et se teste unitairement. Une lambda reste acceptable pour du trivial |

## Lancement

| Batch 4 (formation) | Batch 5 (projet) | Pourquoi |
|---|---|---|
| Tous les jobs se lancent au démarrage de l'app | `spring.batch.job.enabled=false`, lancement explicite | Convention du projet. Boot 3 refuse aussi de lancer au démarrage s'il y a plusieurs jobs sans `spring.batch.job.name` |
| — | `java -jar target/*.jar --spring.batch.job.enabled=true --spring.batch.job.name=firstJob` | Il faut réactiver le runner, sinon `job.name` seul n'a aucun effet |
