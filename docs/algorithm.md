# Land Route Algorithm

## Problem

Given a dataset of countries, each identified by a `cca3` code and a list of bordering country codes,
find the shortest sequence of border crossings to travel from an origin country to a destination country.

## Graph Model

- **Nodes**: each country, identified by its `cca3` code (e.g. `CZE`, `AUT`, `ITA`)
- **Edges**: undirected — if country A lists country B in its `borders`, they share a land border
- **Weight**: uniform (every crossing costs 1), so BFS yields the shortest path

The graph is represented as `Map<String, Set<String>>` (adjacency list), built once at application startup
by loading the countries JSON from:

```
https://raw.githubusercontent.com/mledoze/countries/master/countries.json
```

## Algorithm: Breadth-First Search (BFS)

BFS is optimal for this problem because:
- All edges have equal weight → BFS finds the shortest path in `O(V + E)` time
- V ≈ 250 countries, E ≈ 1 000 border pairs → negligible cost per request
- No need for Dijkstra or A*; their overhead is unwarranted for a uniform-weight graph

### Pseudocode

```
findRoute(origin, destination):
    if origin not in graph → throw RouteNotFoundException
    if destination not in graph → throw RouteNotFoundException
    if origin == destination → return [origin]

    queue ← deque([[origin]])
    visited ← {origin}

    while queue is not empty:
        path ← queue.popleft()
        current ← path.last()

        for each neighbor in graph[current]:
            if neighbor == destination:
                return path + [neighbor]
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(path + [neighbor])

    throw RouteNotFoundException   // no land route exists
```

### Key design decisions

| Decision | Choice | Reason |
|---|---|---|
| Queue element | Full path (list of codes) | Allows returning the route directly without backtracking |
| Visited set | Per-search `HashSet` | Prevents revisiting nodes; O(1) lookup |
| Graph build | Once at startup (`@PostConstruct`) | Amortises the HTTP fetch and JSON parse cost |
| Thread safety | Graph map is read-only after build | Safe for concurrent requests without locking |

### HTTP 400 conditions

The endpoint must return HTTP 400 when:
1. `origin` is not a known `cca3` code
2. `destination` is not a known `cca3` code
3. No land path exists between the two countries (e.g. island nations, separated continents)
