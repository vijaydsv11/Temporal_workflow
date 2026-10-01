package com.example.workflow.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "workflow")
public class WorkflowProperties {

    private final Temporal temporal = new Temporal();
    private final External external = new External();

    public Temporal getTemporal() {
        return temporal;
    }

    public External getExternal() {
        return external;
    }

    public static class Temporal {
        @NotBlank
        private String target;

        @NotBlank
        private String namespace;

        @NotBlank
        private String taskQueue;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getTaskQueue() {
            return taskQueue;
        }

        public void setTaskQueue(String taskQueue) {
            this.taskQueue = taskQueue;
        }
    }

    public static class External {
        @NotBlank
        private String baseUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
