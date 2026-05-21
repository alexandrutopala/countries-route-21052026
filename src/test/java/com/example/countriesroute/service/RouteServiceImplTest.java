package com.example.countriesroute.service;

import com.example.countriesroute.exception.RouteNotFoundException;
import com.example.countriesroute.loader.CountryDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RouteServiceImpl using a small synthetic border graph.
 *
 * Synthetic topology (bidirectional):
 *
 *   AAA -- BBB -- CCC
 *               |
 *              DDD -- EEE
 *
 *   FFF (island, no borders)
 *   GGG -- HHH (connected pair, isolated from the rest)
 */
@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    @Mock
    private CountryDataLoader countryDataLoader;

    private RouteServiceImpl routeService;

    @BeforeEach
    void setUp() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("AAA", Set.of("BBB"));
        graph.put("BBB", Set.of("AAA", "CCC"));
        graph.put("CCC", Set.of("BBB", "DDD"));
        graph.put("DDD", Set.of("CCC", "EEE"));
        graph.put("EEE", Set.of("DDD"));
        graph.put("FFF", Set.of());
        graph.put("GGG", Set.of("HHH"));
        graph.put("HHH", Set.of("GGG"));

        when(countryDataLoader.getBorderGraph()).thenReturn(graph);
        routeService = new RouteServiceImpl(countryDataLoader);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Direct neighbors return a two-element route")
    void directNeighbors() {
        List<String> route = routeService.findRoute("AAA", "BBB");

        assertThat(route).containsExactly("AAA", "BBB");
    }

    @Test
    @DisplayName("Two-hop route returns correct intermediate country")
    void twoHopRoute() {
        List<String> route = routeService.findRoute("AAA", "CCC");

        assertThat(route).containsExactly("AAA", "BBB", "CCC");
    }

    @Test
    @DisplayName("Multi-hop route traverses the full chain")
    void multiHopRoute() {
        List<String> route = routeService.findRoute("AAA", "EEE");

        assertThat(route).containsExactly("AAA", "BBB", "CCC", "DDD", "EEE");
    }

    @Test
    @DisplayName("Route from a middle node to an end node")
    void middleToEnd() {
        List<String> route = routeService.findRoute("CCC", "EEE");

        assertThat(route).containsExactly("CCC", "DDD", "EEE");
    }

    @Test
    @DisplayName("Reverse direction returns shortest route")
    void reverseDirection() {
        List<String> route = routeService.findRoute("EEE", "AAA");

        assertThat(route).containsExactly("EEE", "DDD", "CCC", "BBB", "AAA");
    }

    // -------------------------------------------------------------------------
    // Same origin and destination
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Origin equals destination returns single-element list")
    void sameOriginAndDestination() {
        List<String> route = routeService.findRoute("CCC", "CCC");

        assertThat(route).containsExactly("CCC");
    }

    @Test
    @DisplayName("Origin equals destination for island country returns single-element list")
    void sameOriginAndDestinationIsland() {
        List<String> route = routeService.findRoute("FFF", "FFF");

        assertThat(route).containsExactly("FFF");
    }

    // -------------------------------------------------------------------------
    // No land route
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Island country as origin throws RouteNotFoundException")
    void islandAsOrigin() {
        assertThatThrownBy(() -> routeService.findRoute("FFF", "AAA"))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    @DisplayName("Island country as destination throws RouteNotFoundException")
    void islandAsDestination() {
        assertThatThrownBy(() -> routeService.findRoute("AAA", "FFF"))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    @DisplayName("Disconnected subgraph throws RouteNotFoundException")
    void disconnectedSubgraph() {
        assertThatThrownBy(() -> routeService.findRoute("AAA", "GGG"))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @Test
    @DisplayName("Connected pair isolated from main graph throws RouteNotFoundException toward main")
    void isolatedPairToMainGraph() {
        assertThatThrownBy(() -> routeService.findRoute("GGG", "EEE"))
                .isInstanceOf(RouteNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Unknown country codes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Unknown origin code throws RouteNotFoundException")
    void unknownOrigin() {
        assertThatThrownBy(() -> routeService.findRoute("ZZZ", "AAA"))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("ZZZ");
    }

    @Test
    @DisplayName("Unknown destination code throws RouteNotFoundException")
    void unknownDestination() {
        assertThatThrownBy(() -> routeService.findRoute("AAA", "ZZZ"))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("ZZZ");
    }

    @Test
    @DisplayName("Both origin and destination unknown throws RouteNotFoundException for origin first")
    void bothUnknown() {
        assertThatThrownBy(() -> routeService.findRoute("XXX", "YYY"))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("XXX");
    }

    // -------------------------------------------------------------------------
    // Route correctness: BFS guarantees shortest path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("BFS returns the shortest path, not a longer one")
    void shortestPath() {
        List<String> route = routeService.findRoute("AAA", "DDD");

        // Shortest: AAA -> BBB -> CCC -> DDD (length 4)
        assertThat(route).hasSize(4);
        assertThat(route).startsWith("AAA");
        assertThat(route).endsWith("DDD");
    }

    @Test
    @DisplayName("Route always starts with origin and ends with destination")
    void routeStartsAndEnds() {
        List<String> route = routeService.findRoute("BBB", "EEE");

        assertThat(route.getFirst()).isEqualTo("BBB");
        assertThat(route.getLast()).isEqualTo("EEE");
    }

    @Test
    @DisplayName("Route contains no duplicate country codes")
    void noDuplicatesInRoute() {
        List<String> route = routeService.findRoute("AAA", "EEE");

        assertThat(route).doesNotHaveDuplicates();
    }
}
