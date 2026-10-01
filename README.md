# Workflow Abstraction Service

A Spring Boot service that abstracts workflow orchestration behind a small HTTP API and uses Temporal for long-running, resilient processing.

## Purpose

This project acts as a workflow façade for batch-style processing. The service exposes a simple API to start a workflow and query its status, while the actual orchestration and execution logic is delegated to Temporal.

The design keeps the app flexible by separating:

- API layer
- workflow abstraction contract
- Temporal-specific implementation
- activity implementation for external calls
- configuration-backed runtime settings

## High-level architecture

```text
Client
  |
  v
WorkflowController (/workflows)
  |
  v
WorkflowEngine abstraction
  |
  v
TemporalWorkflowEngine
  |
  v
Temporal workflow worker
  |
  +--> CsvBatchWorkflowImpl
          |
          +--> CsvActivities.readCsv(...)
          |
          +--> CsvActivities.processBatch(...)
```

## Key components

### 1. API Layer

- `WorkflowController`
  - `POST /workflows` starts a workflow
  - `GET /workflows/{workflowId}` returns workflow status

- `WorkflowRequest`
  - Carries the workflow type and input payload

### 2. Workflow abstraction

- `WorkflowEngine`
  - Common interface for workflow orchestration implementations

- `TemporalWorkflowEngine`
  - Uses Temporal `WorkflowClient` to create a workflow stub
  - Starts the workflow with a configured task queue
  - Returns workflow IDs and status

### 3. Temporal workflow

- `CsvBatchWorkflow`
  - Declares the workflow contract

- `CsvBatchWorkflowImpl`
  - Implements the workflow steps
  - Reads CSV-like records
  - Splits them into batches
  - Calls activity methods for each batch
  - Maintains a status state such as `READING_FILE`, `PROCESSING_BATCH_1`, and `COMPLETED`

### 4. Temporal activities

- `CsvActivities`
  - Contract for task execution methods

- `CsvActivitiesImpl`
  - Loads data and calls an external service using `RestClient`
  - Example endpoint: `/batches/process`

## Workflow flow

1. A client calls `POST /workflows` with a workflow type and input.
2. `WorkflowController` delegates to `WorkflowEngine.start(...)`.
3. `TemporalWorkflowEngine` creates a Temporal workflow stub using the configured task queue.
4. The Temporal worker starts `CsvBatchWorkflowImpl.process(fileKey)`.
5. The workflow marks its status while reading data and processing batches.
6. Each batch is sent to an activity through `CsvActivities.processBatch(...)`.
7. The client can call `GET /workflows/{workflowId}` to query the status.

## Example request

```http
POST /workflows
Content-Type: application/json

{
  "workflowType": "CSV_BATCH",
  "input": "sample-file-key"
}
```

Example response:

```text
3b8b9db5-4c97-4f76-a0bd-9c6d44e7f0d3
```

## Configuration

The project reads runtime settings from `src/main/resources/application.yml` and supports environment overrides.

```yaml
server:
  port: 8080

workflow:
  temporal:
    target: ${TEMPORAL_TARGET:127.0.0.1:7233}
    namespace: ${TEMPORAL_NAMESPACE:default}
    task-queue: ${TEMPORAL_TASK_QUEUE:CSV_BATCH_TASK_QUEUE}
  external:
    base-url: ${BATCH_SERVICE_URL:http://localhost:8081}
```

## Production considerations

This project is structured for extension toward production by:

- isolating workflow logic from external HTTP calls
- using configuration values instead of hard-coded endpoints
- validating configuration with Spring Boot property binding
- using explicit Temporal activity timeout settings
- supporting environment-based deployment configuration

## Suggested future enhancements

- add a persistence layer for workflow metadata
- add retry and dead-letter handling for external batch calls
- add metrics and health endpoints
- add integration tests with a test Temporal cluster
- add structured logging and correlation IDs
- separate workflow-specific domain classes from runtime integration code

## Build and run

```bash
mvn clean package
java -jar target/workflow-abstraction-service-0.0.1-SNAPSHOT.jar
```

> Note: the application expects a Temporal server to be reachable at the configured target before the workflow worker can fully start.

podman compose -f podman-compose.yml up --build
