package com.example.workflow.temporal;

import com.example.workflow.config.WorkflowProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CsvActivitiesImpl implements CsvActivities {

    private final RestClient restClient;

    public CsvActivitiesImpl(RestClient.Builder builder, WorkflowProperties properties) {
        this.restClient = builder
                .baseUrl(properties.getExternal().getBaseUrl())
                .build();
    }

    @Override
    public List<CsvRecord> readCsv(String fileKey) {

        // POC only. In production, read the CSV from S3/object storage.
        return List.of(
                new CsvRecord("1", "John", 100),
                new CsvRecord("2", "David", 200),
                new CsvRecord("3", "Ravi", 150));
    }

    @Override
    public void processBatch(List<CsvRecord> records) {

        restClient.post()
                .uri("/batches/process")
                .body(records)
                .retrieve()
                .toBodilessEntity();
    }
}
