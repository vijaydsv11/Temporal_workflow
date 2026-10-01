package com.example.workflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkflowPropertiesTest {

    @Test
    void shouldNotHardcodeWorkflowDefaultsInJava() {
        WorkflowProperties properties = new WorkflowProperties();

        assertNull(properties.getTemporal().getTarget());
        assertNull(properties.getTemporal().getNamespace());
        assertNull(properties.getTemporal().getTaskQueue());
        assertNull(properties.getExternal().getBaseUrl());
    }

    @Test
    void shouldBindWorkflowProperties() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "workflow.temporal.target", "temporal.example.com:7233",
                "workflow.temporal.namespace", "production",
                "workflow.temporal.task-queue", "prod-csv-queue",
                "workflow.external.base-url", "https://api.example.com")));

        WorkflowProperties properties = binder.bind("workflow", Bindable.of(WorkflowProperties.class))
                .orElseThrow(() -> new IllegalStateException("Workflow properties were not bound"));

        assertEquals("temporal.example.com:7233", properties.getTemporal().getTarget());
        assertEquals("production", properties.getTemporal().getNamespace());
        assertEquals("prod-csv-queue", properties.getTemporal().getTaskQueue());
        assertEquals("https://api.example.com", properties.getExternal().getBaseUrl());
    }
}
