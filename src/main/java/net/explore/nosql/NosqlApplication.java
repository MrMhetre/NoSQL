package net.explore.nosql;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ResourceUtils;

@SpringBootApplication
@EnableAsync
public class NosqlApplication implements CommandLineRunner {
	Logger logger = LoggerFactory.getLogger(NosqlApplication.class);

	@Autowired
	DocumentGenerator documentGenerator;

	@Autowired
	DocumentWriter documentWriter;

	@Value("${batch-size}")
	int batchSize;

	@Value("${number-of-documents}")
	long numberOfDocuments;

	@Value("${template-file}")
	String templateFileName;

	public static void main(String[] args) {
		SpringApplication.run(NosqlApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		File templateFile = ResourceUtils.getFile("classpath:" + templateFileName);
		logger.info("Template file to use:" + templateFile.getName());
		logger.info("Number of documents: " + numberOfDocuments);
		logger.info("Batch size: " + batchSize);
		logger.info("Document Generator: " + documentGenerator.getClass().getName());
		logger.info("Document Writer: " + documentWriter.getClass().getName());

		long startTime = System.currentTimeMillis();
		long pendingCount = numberOfDocuments;
		ArrayList<CompletableFuture> completableFutures = new ArrayList<>();
		while(pendingCount > 0) {
			int chunkSize = (int) (pendingCount <= batchSize ? pendingCount : batchSize);
			CompletableFuture<ArrayList<String>> generatorFuture = documentGenerator.generateDocuments(templateFile, chunkSize)
					.thenApply(documentsToWrite -> {
						CompletableFuture<ArrayList<String>> writerFuture = documentWriter.WriteDocuments(documentsToWrite)
								.thenApply(documentIdentifiers -> {
									for (String documentIdentifier : documentIdentifiers) {
										logger.trace(documentIdentifier);
									}
									return documentIdentifiers;
								});

						completableFutures.add(writerFuture);
						return documentsToWrite;
					});

			completableFutures.add(generatorFuture);
			pendingCount = pendingCount - chunkSize;
			logger.debug("Pending docs: " + pendingCount);
		}

		logger.info("Waiting for all tasks to complete...");
		CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).get();
		long endTime = System.currentTimeMillis();

		logger.info("Time taken: " + ((endTime - startTime)/1000) + "s");

		System.exit(0);
	}

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("DataGenerator-");
		executor.setRejectedExecutionHandler(
				new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r,
												  ThreadPoolExecutor executor) {
						logger.info("Task rejected. Waiting for thread to be available.");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							logger.error (e.getMessage(), e);
						}
						executor.execute(r);
					}
				}
		);
		executor.initialize();
		return executor;
	}
}
