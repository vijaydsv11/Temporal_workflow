package com.example.workflow.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class CsvBatchWorkflowImpl implements CsvBatchWorkflow {

    private final CsvActivities activities = Workflow.newActivityStub(
            CsvActivities.class,
            ActivityOptions.newBuilder()
                    .setScheduleToCloseTimeout(Duration.ofMinutes(10))
                    .setStartToCloseTimeout(Duration.ofMinutes(5))
                    .build());

    private String status = "STARTED";

    @Override
    public void process(String fileKey) {

        status = "READING_FILE";
        var records = activities.readCsv(fileKey);

        int batchSize = 100;

        for (int i = 0; i < records.size(); i += batchSize) {

            int end = Math.min(i + batchSize, records.size());

            var batch = records.subList(i, end);

            status = "PROCESSING_BATCH_" + ((i / batchSize) + 1);

            activities.processBatch(batch);
        }

        status = "COMPLETED";
    }

    @Override
    public String getStatus() {
        return status;
    }
}
