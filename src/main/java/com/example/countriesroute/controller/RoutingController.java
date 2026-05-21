package com.example.countriesroute.controller;

import com.example.countriesroute.dto.RouteResponse;
import com.example.countriesroute.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingController implements RoutingApi {

    private final RouteService routeService;

    public RoutingController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    public ResponseEntity<RouteResponse> getRoute(String origin, String destination) {
        return ResponseEntity.ok(new RouteResponse(routeService.findRoute(origin, destination)));
    }
}
