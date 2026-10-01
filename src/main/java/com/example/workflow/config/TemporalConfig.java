package com.example.workflow.config;

import com.example.workflow.temporal.CsvActivitiesImpl;
import com.example.workflow.temporal.CsvBatchWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WorkflowProperties.class)
public class TemporalConfig {

    private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);

    @Bean(destroyMethod = "shutdown")
    WorkflowServiceStubs workflowServiceStubs(WorkflowProperties properties) {
        log.info("Connecting Temporal service target={}", properties.getTemporal().getTarget());
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(properties.getTemporal().getTarget())
                        .build());
    }

    @Bean
    WorkflowClient workflowClient(
            WorkflowServiceStubs serviceStubs,
            WorkflowProperties properties) {
        log.info("Initializing Temporal workflow client namespace={}", properties.getTemporal().getNamespace());
        return WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(properties.getTemporal().getNamespace())
                        .build());
    }

    @Bean(destroyMethod = "shutdown")
    WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            CsvActivitiesImpl activities,
            WorkflowProperties properties) {

        String taskQueue = properties.getTemporal().getTaskQueue();
        log.info("Starting Temporal worker taskQueue={}", taskQueue);

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(taskQueue);

        worker.registerWorkflowImplementationTypes(CsvBatchWorkflowImpl.class);
        worker.registerActivitiesImplementations(activities);

        factory.start();
        return factory;
    }
}
