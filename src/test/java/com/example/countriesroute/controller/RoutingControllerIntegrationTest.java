package com.example.countriesroute.controller;

import com.example.countriesroute.exception.RouteNotFoundException;
import com.example.countriesroute.service.RouteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoutingController.class)
class RoutingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouteService routeService;

    // -------------------------------------------------------------------------
    // HTTP 200 — valid routes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /routing/CZE/ITA returns 200 with correct route")
    void validRoute_czechToItaly() throws Exception {
        when(routeService.findRoute("CZE", "ITA"))
                .thenReturn(List.of("CZE", "AUT", "ITA"));

        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.route").isArray())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[1]").value("AUT"))
                .andExpect(jsonPath("$.route[2]").value("ITA"))
                .andExpect(jsonPath("$.route.length()").value(3));
    }

    @Test
    @DisplayName("GET /routing/AAA/AAA returns 200 with single-element route for same country")
    void validRoute_sameCountry() throws Exception {
        when(routeService.findRoute("AAA", "AAA"))
                .thenReturn(List.of("AAA"));

        mockMvc.perform(get("/routing/AAA/AAA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.route").isArray())
                .andExpect(jsonPath("$.route.length()").value(1))
                .andExpect(jsonPath("$.route[0]").value("AAA"));
    }

    @Test
    @DisplayName("GET /routing/AAA/BBB returns 200 with direct-neighbor route")
    void validRoute_directNeighbors() throws Exception {
        when(routeService.findRoute("AAA", "BBB"))
                .thenReturn(List.of("AAA", "BBB"));

        mockMvc.perform(get("/routing/AAA/BBB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.length()").value(2))
                .andExpect(jsonPath("$.route[0]").value("AAA"))
                .andExpect(jsonPath("$.route[1]").value("BBB"));
    }

    @Test
    @DisplayName("Response body contains only the 'route' key")
    void responseBodyShape() throws Exception {
        when(routeService.findRoute("CZE", "ITA"))
                .thenReturn(List.of("CZE", "AUT", "ITA"));

        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // HTTP 400 — no land route
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /routing/AUS/ESP returns 400 when no land route exists")
    void noLandRoute_returns400() throws Exception {
        when(routeService.findRoute("AUS", "ESP"))
                .thenThrow(new RouteNotFoundException("No land route found between AUS and ESP"));

        mockMvc.perform(get("/routing/AUS/ESP"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.message").value("No land route found between AUS and ESP"));
    }

    // -------------------------------------------------------------------------
    // HTTP 400 — unknown country codes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /routing/ZZZ/ITA returns 400 for unknown origin")
    void unknownOrigin_returns400() throws Exception {
        when(routeService.findRoute("ZZZ", "ITA"))
                .thenThrow(new RouteNotFoundException("Unknown country code: ZZZ"));

        mockMvc.perform(get("/routing/ZZZ/ITA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Unknown country code: ZZZ"));
    }

    @Test
    @DisplayName("GET /routing/CZE/ZZZ returns 400 for unknown destination")
    void unknownDestination_returns400() throws Exception {
        when(routeService.findRoute("CZE", "ZZZ"))
                .thenThrow(new RouteNotFoundException("Unknown country code: ZZZ"));

        mockMvc.perform(get("/routing/CZE/ZZZ"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Unknown country code: ZZZ"));
    }

    @Test
    @DisplayName("GET /routing/XXX/YYY returns 400 when both codes are unknown")
    void bothUnknown_returns400() throws Exception {
        when(routeService.findRoute("XXX", "YYY"))
                .thenThrow(new RouteNotFoundException("Unknown country code: XXX"));

        mockMvc.perform(get("/routing/XXX/YYY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown country code: XXX"));
    }

    // -------------------------------------------------------------------------
    // Swagger UI availability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Swagger UI is reachable at /swagger-ui/index.html")
    void swaggerUiAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
