# countries-route

A Spring Boot REST service that calculates the shortest land route between two countries using BFS over a border-adjacency graph loaded from public country data.

## Running the application

### Option 1 — Published Docker image

**Prerequisites:** Docker

```bash
docker run -p 8080:8080 --name contries-route -d --rm alexandrutopala/countries-route:latest
```

### Option 2 — Local build

**Prerequisites:** Java 25+, Git (the `./mvnw` wrapper handles Maven — no local install required)

```bash
./mvnw package -DskipTests && java -jar target/countries-route-0.0.1-SNAPSHOT.jar
```

---

The service starts on port `8080` and fetches country data from GitHub on startup. An internet connection is required.

## Sample request

```bash
curl http://localhost:8080/routing/CZE/ITA
```

```json
{
  "route": ["CZE", "AUT", "ITA"]
}
```

## Clean up

Stop the Docker container, if used

```bash
docker stop contries-route
```

## Building the Docker image

### Single platform (native)

```bash
docker build -t countries-route .
```

### Targeting aarch64 explicitly (e.g. Apple Silicon or ARM servers)

Requires [Docker Buildx](https://docs.docker.com/buildx/working-with-buildx/), which ships with Docker Desktop by default.

```bash
docker buildx build --platform linux/arm64 -t countries-route .
```

The builder stage always runs on your native machine so Maven never executes under emulation — only the final JRE layer targets `linux/arm64`.

### Multi-platform image (amd64 + arm64)

```bash
docker buildx build --platform linux/amd64,linux/arm64 -t countries-route .
```

To push a multi-platform image directly to a registry, add `--push`:

```bash
docker buildx build --platform linux/amd64,linux/arm64 -t <your-registry>/countries-route:latest --push .
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
