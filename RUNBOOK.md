# Runbook: Local Podman Setup for Workflow Abstraction Service

This runbook explains the exact sequence to start the application with Temporal and the downstream batch service in a local Podman environment.

## 1. Prerequisites

Make sure the following are available on your machine:

- Podman installed and working
- Maven installed
- Java 21 installed
- Dockerfile and compose files are present in the project root

## 2. Build the Java application

From the project root:

```bash
mvn clean package
```

This creates the executable jar in:

```text
target/workflow-abstraction-service-0.0.1-SNAPSHOT.jar
```

## 3. Build the mock batch service image

```bash
podman build -f batch-service.Dockerfile -t batch-service .
```

## 4. Build the app image

```bash
podman build -t workflow-abstraction-service .
```

## 5. Copy the sample environment file

```bash
cp .env.example .env
```

If needed, edit `.env` and ensure the values match the container network names:

```env
TEMPORAL_TARGET=temporal:7233
TEMPORAL_NAMESPACE=default
TEMPORAL_TASK_QUEUE=CSV_BATCH_TASK_QUEUE
BATCH_SERVICE_URL=http://batch-service:8081
```

## 6. Start the full stack

From the project root:

```bash
podman compose -f podman-compose.yml up --build
```

This will start:

- Postgres
- Temporal
- batch-service
- workflow-abstraction-service

## 7. Verify the services are running

Check the containers:

```bash
podman ps
```

You should see containers for:

- `temporal-postgres`
- `temporal`
- `batch-service`
- `workflow-abstraction-service`

## 8. Verify Temporal is accessible

From the host, check the Temporal gRPC endpoint:

```bash
telnet localhost 7233
```

If telnet is not available, use a tool like `nc` or simply confirm it is listening through container logs.

## 9. Trigger a workflow

Start the service endpoint with a POST request:

```bash
curl -X POST http://localhost:8080/workflows \
  -H "Content-Type: application/json" \
  -d '{
    "workflowType": "CSV_BATCH",
    "input": "sample-file-key"
  }'
```

The response should be a workflow ID.

## 10. Check workflow status

```bash
curl http://localhost:8080/workflows/<workflow-id>
```

Expected behavior:

- status begins with a workflow state like `STARTED`
- then moves through `READING_FILE` and `PROCESSING_BATCH_*`
- ultimately becomes `COMPLETED`

## 11. Container networking rule

Inside Podman’s bridge network, use service names instead of localhost:

```text
TEMPORAL_TARGET=temporal:7233
BATCH_SERVICE_URL=http://batch-service:8081
```

Do not use `localhost` for service-to-service calls inside containers.

## 12. Stop the stack

```bash
podman compose -f podman-compose.yml down
```

To remove persisted database data too:

```bash
podman compose -f podman-compose.yml down -v
```

## 13. Troubleshooting

### Temporal not starting

- Check the Postgres container is healthy
- Confirm the Postgres credentials in compose match the Temporal env values
- Inspect logs:

```bash
podman logs temporal
```

### App cannot connect to Temporal

- Verify `TEMPORAL_TARGET` is set to `temporal:7233`
- Ensure the app and Temporal are on the same network
- Check app logs:

```bash
podman logs workflow-abstraction-service
```

### Batch service call fails

- Verify the batch service is running
- Confirm `BATCH_SERVICE_URL=http://batch-service:8081`
- Test directly:

```bash
curl http://localhost:8081
```

## 14. Notes

This setup is meant for local validation and team development. For production, you would normally replace the local container-based setup with:

- managed Postgres
- managed Temporal cluster
- managed secrets/config
- proper ingress and health checks
