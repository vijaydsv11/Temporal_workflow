package com.example.workflow.temporal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.QueryMethod;

@WorkflowInterface
public interface CsvBatchWorkflow {

    @WorkflowMethod
    void process(String fileKey);

    @QueryMethod
    String getStatus();
}
