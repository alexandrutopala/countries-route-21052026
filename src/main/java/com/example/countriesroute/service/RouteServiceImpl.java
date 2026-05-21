package com.example.countriesroute.service;

import com.example.countriesroute.exception.RouteNotFoundException;
import com.example.countriesroute.loader.CountryDataLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RouteServiceImpl implements RouteService {

    private final CountryDataLoader countryDataLoader;

    public RouteServiceImpl(CountryDataLoader countryDataLoader) {
        this.countryDataLoader = countryDataLoader;
    }

    @Override
    public List<String> findRoute(String origin, String destination) {
        Map<String, Set<String>> graph = countryDataLoader.getBorderGraph();

        if (!graph.containsKey(origin)) {
            throw new RouteNotFoundException("Unknown country code: " + origin);
        }
        if (!graph.containsKey(destination)) {
            throw new RouteNotFoundException("Unknown country code: " + destination);
        }
        if (origin.equals(destination)) {
            return List.of(origin);
        }

        Deque<List<String>> queue = new ArrayDeque<>();
        queue.add(new ArrayList<>(List.of(origin)));

        Set<String> visited = new HashSet<>();
        visited.add(origin);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String current = path.getLast();

            for (String neighbor : graph.get(current)) {
                if (neighbor.equals(destination)) {
                    List<String> result = new ArrayList<>(path);
                    result.add(neighbor);
                    return result;
                }
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }

        throw new RouteNotFoundException(
                "No land route found between " + origin + " and " + destination);
    }
}
