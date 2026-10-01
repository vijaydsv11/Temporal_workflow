package com.example.workflow.temporal;

import io.temporal.activity.ActivityInterface;

import java.util.List;

@ActivityInterface
public interface CsvActivities {

    List<CsvRecord> readCsv(String fileKey);

    void processBatch(List<CsvRecord> records);
}
