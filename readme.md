# QuizRoom Backend

Spring Boot backend for hosting real-time quiz rooms with WebSocket updates and Google OAuth2 authentication.

## Features
- Google OAuth2 login with JWT cookie authentication
- Room lifecycle (create, join, end) with host validation
- Question broadcast, answer submission, and leaderboard updates
- Real-time events via STOMP over WebSocket (`/ws`)
- PostgreSQL persistence with Spring Data JPA
- Redis-backed room state

## Tech Stack
- Java 21+ (source/target set to 23 with preview enabled in `pom.xml`)
- Spring Boot 4, Spring Security, Spring Data JPA, Spring WebSocket
- PostgreSQL
- Redis

## Configuration
Environment variables (see `src/main/resources/application.yml`):

| Variable | Description |
| --- | --- |
| `PORT` | HTTP port (default: `8080`) |
| `JWT_SECRET` | Secret used to sign JWTs |
| `DB_URL` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `SPRING_REDIS_HOST` | Redis host |
| `SPRING_REDIS_PORT` | Redis port |
| `SPRING_REDIS_PASSWORD` | Redis password |

> Note: CORS, OAuth2 redirect, and WebSocket allowed origins are currently hardcoded to `quizroom.tech`. Update `CorsConfig`, `OAuth2LoginSuccessHandler`, and `WebSocketConfig` (or make them configurable) for local development.

## Run Locally
```bash
mvn test
mvn spring-boot:run
```

Build a jar:
```bash
mvn clean package
java -jar target/*.jar
```

## Docker
```bash
docker build -t quizroom-backend .
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e JWT_SECRET=changeme \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/quizroom \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e GOOGLE_CLIENT_ID=your-client-id \
  -e GOOGLE_CLIENT_SECRET=your-client-secret \
  -e SPRING_REDIS_HOST=host.docker.internal \
  -e SPRING_REDIS_PORT=6379 \
  -e SPRING_REDIS_PASSWORD= \
  quizroom-backend
```

## REST API
Base path: `/api`

**Auth**
- `GET /api/auth/me` — current user profile
- `GET /api/csrf-token` — CSRF token (unauthenticated)

**Rooms**
- `POST /api/rooms/create` — create a room
- `POST /api/rooms/{roomCode}/join` — join a room
- `GET /api/rooms/{roomCode}/validate-host` — host validation
- `GET /api/rooms/{roomCode}/players` — list players
- `GET /api/rooms/{roomCode}/state` — room state
- `POST /api/rooms/{roomCode}/end` — end room

**Quiz**
- `POST /api/rooms/{roomCode}/quiz/question` — broadcast question (host)
- `POST /api/rooms/{roomCode}/quiz/question/{questionId}/end` — end question
- `POST /api/rooms/{roomCode}/quiz/end` — end quiz
- `GET /api/rooms/{roomCode}/quiz/results` — final results

**Answers**
- `POST /api/rooms/{roomCode}/answers/{questionId}` — submit answer

All routes except `/api/csrf-token`, `/oauth2/**`, and `/ws/**` require authentication.

## WebSocket
- Endpoint: `/ws` (SockJS)
- Subscribe: `/topic/room/{roomCode}`
- Payload format:
  ```json
  { "type": "QUESTION|QUESTION_ENDED|LEADERBOARD|CHAT|PLAYER_JOINED|QUIZ_ENDED|ROOM_ENDED", "payload": {} }
  ```

## Tests
```bash
mvn test
```
