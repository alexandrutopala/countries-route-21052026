package com.example.countriesroute.loader;

import com.example.countriesroute.model.Country;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CountryDataLoader {

    private static final Logger log = LoggerFactory.getLogger(CountryDataLoader.class);

    private final RestClient restClient;
    private final String countriesDataUrl;

    private Map<String, Set<String>> borderGraph = Collections.emptyMap();

    public CountryDataLoader(@Value("${countries.data.url}") String countriesDataUrl, RestClient restClient) {
        this.restClient = restClient;
        this.countriesDataUrl = countriesDataUrl;
    }

    @PostConstruct
    void loadCountries() {
        log.info("Loading country data from {}", countriesDataUrl);

        List<Country> countries = restClient.get()
                .uri(countriesDataUrl)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (countries == null || countries.isEmpty()) {
            log.warn("No country data received; border graph will be empty");
            return;
        }

        Map<String, Set<String>> graph = new HashMap<>(countries.size() * 2);
        for (Country country : countries) {
            if (country.cca3() == null) continue;
            graph.computeIfAbsent(country.cca3(), k -> new HashSet<>());
            if (country.borders() != null) {
                for (String neighbor : country.borders()) {
                    graph.computeIfAbsent(country.cca3(), k -> new HashSet<>()).add(neighbor);
                    graph.computeIfAbsent(neighbor, k -> new HashSet<>()).add(country.cca3());
                }
            }
        }

        this.borderGraph = Collections.unmodifiableMap(graph);
        log.info("Border graph built: {} countries, {} border entries", graph.size(),
                graph.values().stream().mapToInt(Set::size).sum() / 2);
    }

    public Map<String, Set<String>> getBorderGraph() {
        return borderGraph;
    }
}
