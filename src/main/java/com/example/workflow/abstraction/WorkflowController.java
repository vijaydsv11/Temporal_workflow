package com.example.workflow.abstraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowEngine workflowEngine;

    public WorkflowController(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @PostMapping
    public String start(@RequestBody WorkflowRequest request) {
        log.info("Starting workflow request workflowType={} input={}", request.workflowType(), request.input());
        String workflowId = workflowEngine.start(
                request.workflowType(),
                request.input());
        log.info("Workflow started workflowId={}", workflowId);
        return workflowId;
    }

    @GetMapping("/{workflowId}")
    public String status(@PathVariable String workflowId) {
        log.info("Checking workflow status workflowId={}", workflowId);
        String status = workflowEngine.getStatus(workflowId);
        log.info("Workflow status response workflowId={} status={}", workflowId, status);
        return status;
    }
}
