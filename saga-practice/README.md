# Distributed Saga Practice

This is a small, Kafka-free, orchestrated Saga that runs beside the original application. The services are separate Spring Boot processes with separate MySQL schemas:

- `orchestrator-service` owns saga state in `saga_orchestrator`.
- `team-service` owns teams in `saga_teams`.
- `player-service` owns players and assignment history in `saga_players`.

The workflow creates a team, then assigns each player in its own committed Player Service transaction. If a later assignment fails, the orchestrator asks Player Service to restore all earlier team assignments and then asks Team Service to delete the new team. There are no cross-service database transactions or foreign keys. HTTP carries commands; Kafka is deliberately not involved.

## Run the exercise

From this directory, start all services and the three local databases:

```shell
docker compose up --build
```

The local-only MySQL password is `practice-only`; do not reuse it outside this exercise. MySQL is exposed on port `3307`. The services run on ports `8080` (orchestrator), `8081` (team), and `8082` (player).

Create a couple of players:

```shell
curl -X POST http://localhost:8082/players -H "Content-Type: application/json" -d "{\"firstName\":\"Maya\",\"lastName\":\"Moore\",\"sport\":\"Basketball\"}"
curl -X POST http://localhost:8082/players -H "Content-Type: application/json" -d "{\"firstName\":\"Jordan\",\"lastName\":\"Lee\",\"sport\":\"Football\"}"
curl http://localhost:8082/players
```

Use the returned player IDs to start the Saga:

```shell
curl -X POST http://localhost:8080/sagas/team-assignments -H "Content-Type: application/json" -d "{\"team\":{\"name\":\"Example City\",\"city\":\"Example City\",\"state\":\"CA\",\"division\":\"Practice\",\"colors\":[\"blue\",\"gold\"],\"teamType\":\"PRO\"},\"playerIds\":[1,2]}"
```

The response includes the saga ID, status, and created team ID. Check it with `GET /sagas/team-assignments/{sagaId}`; check player assignments with `GET http://localhost:8082/players`.

## Practice compensation

Use one existing player ID followed by a nonexistent ID in `playerIds`, for example `[1,999999]`. The team creation and first player assignment commit, the second assignment fails, and the orchestrator compensates player assignments in reverse step order before deleting the team. Verify that player 1 has its original `teamId` using `GET /players` and that the created team is gone using `GET http://localhost:8081/internal/teams/by-saga/{sagaId}` (which returns 404 after compensation).

The player service records each prior `teamId` under the saga ID. Repeated commands are idempotent, so `POST /sagas/team-assignments/{sagaId}/resume` can retry a workflow after an orchestrator interruption. If a compensation endpoint is unavailable, the saga remains `COMPENSATION_FAILED`; retry with `POST /sagas/team-assignments/{sagaId}/retry-compensation`.

## Follow the implementation

1. `TeamAssignmentSagaController` creates the durable saga record and invokes the executor.
2. `TeamAssignmentSagaExecutor` calls Team Service, then Player Service once per player.
3. Each service commits only to its own schema. Team Service uses the saga ID as an idempotency key; Player Service records prior assignments for compensation.
4. On failure, the executor restores player assignments and deletes the team. The Saga database records `COMPLETED`, `FAILED`, or `COMPENSATION_FAILED`.

Run all module tests with `mvn test` from this directory. The original application and the separate Kafka practice branch are unchanged.
