package com.example.workflow.temporal;

import com.example.workflow.abstraction.WorkflowEngine;
import com.example.workflow.config.WorkflowProperties;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TemporalWorkflowEngine implements WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(TemporalWorkflowEngine.class);

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    public TemporalWorkflowEngine(
            WorkflowClient workflowClient,
            WorkflowProperties properties) {
        this.workflowClient = workflowClient;
        this.taskQueue = properties.getTemporal().getTaskQueue();
        log.info("Temporal workflow engine initialized for taskQueue={}", this.taskQueue);
    }

    @Override
    public String start(String workflowType, String input) {

        if (!"CSV_BATCH".equals(workflowType)) {
            log.error("Unsupported workflow type requested workflowType={}", workflowType);
            throw new IllegalArgumentException(
                    "Unsupported workflow type: " + workflowType);
        }

        String workflowId = "csv-" + UUID.randomUUID();
        log.info("Starting Temporal workflow workflowType={} workflowId={} input={}", workflowType, workflowId, input);

        CsvBatchWorkflow workflow = workflowClient.newWorkflowStub(
                CsvBatchWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(taskQueue)
                        .build());

        WorkflowExecution execution = WorkflowClient.start(
                workflow::process,
                input);

        log.info("Temporal workflow started workflowId={} executionId={}", execution.getWorkflowId(),
                execution.getRunId());
        return execution.getWorkflowId();
    }

    @Override
    public String getStatus(String workflowId) {
        log.info("Fetching workflow status workflowId={}", workflowId);

        CsvBatchWorkflow workflow = workflowClient.newWorkflowStub(
                CsvBatchWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(taskQueue)
                        .build());

        try {
            String status = workflow.getStatus();
            log.info("Workflow status retrieved workflowId={} status={}", workflowId, status);
            return status;
        } catch (Exception e) {
            log.warn("Unable to fetch workflow status workflowId={} reason={}", workflowId, e.getMessage());
            return "UNKNOWN";
        }
    }
}
