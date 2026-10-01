package com.example.workflow.abstraction;

public record WorkflowRequest(
        String workflowType,
        String input
) {}
