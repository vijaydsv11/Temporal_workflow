package com.example.workflow.abstraction;

public interface WorkflowEngine {

    String start(String workflowType, String input);

    String getStatus(String workflowId);
}
