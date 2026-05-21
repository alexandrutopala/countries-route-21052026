# countries-route

A Spring Boot REST service that calculates the shortest land route between two countries using BFS over a border-adjacency graph loaded from public country data.

## Prerequisites

- Java 25+
- Maven 3.9+ (or use the included `./mvnw` wrapper — no local Maven install required)
- Docker (optional, for containerised deployment)

## Build

```bash
./mvnw package
```

The fat JAR is produced at `target/countries-route-0.0.1-SNAPSHOT.jar`.

To also run the test suite:

```bash
./mvnw verify
```

## Run

```bash
java -jar target/countries-route-0.0.1-SNAPSHOT.jar
```

The service starts on port `8080` and fetches country data from GitHub on startup. An internet connection is required on first boot.

## Docker

Build the image:

```bash
docker build -t countries-route .
```

Run the container:

```bash
docker run -p 8080:8080 countries-route
```

## API

### Find land route

```
GET /routing/{origin}/{destination}
```

| Parameter     | Description                              | Example |
|---------------|------------------------------------------|---------|
| `origin`      | cca3 country code of the starting point  | `CZE`   |
| `destination` | cca3 country code of the end point       | `ITA`   |

**200 OK — route found**

```json
{
  "route": ["CZE", "AUT", "ITA"]
}
```

**400 Bad Request — no land route or unknown country code**

```json
{
  "message": "No land route found between AUS and ESP"
}
```

### Sample request

```bash
curl http://localhost:8080/routing/CZE/ITA
```

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

## Algorithm

Country border data is fetched from:

```
https://raw.githubusercontent.com/mledoze/countries/master/countries.json
```

Countries are identified by their `cca3` code. The border graph is built once at startup as an adjacency list (`Map<String, Set<String>>`). Route calculation uses **Breadth-First Search (BFS)**, which is optimal for this unweighted graph and runs in O(V + E) time (V ≈ 250 countries, E ≈ 1 000 border pairs).

See [`docs/algorithm.md`](docs/algorithm.md) for a detailed description.
