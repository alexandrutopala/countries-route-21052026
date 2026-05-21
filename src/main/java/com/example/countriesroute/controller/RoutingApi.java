package com.example.countriesroute.controller;

import com.example.countriesroute.dto.ErrorResponse;
import com.example.countriesroute.dto.RouteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Routing", description = "Land route calculation between countries")
@RequestMapping("/routing")
public interface RoutingApi {

    @Operation(
            summary = "Find land route between two countries",
            description = "Returns the shortest sequence of border crossings to travel by land from the origin country to the destination country."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Route found",
                    content = @Content(schema = @Schema(implementation = RouteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No land route exists or country code is unknown",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{origin}/{destination}")
    ResponseEntity<RouteResponse> getRoute(
            @Parameter(description = "cca3 code of the origin country", example = "CZE")
            @PathVariable String origin,
            @Parameter(description = "cca3 code of the destination country", example = "ITA")
            @PathVariable String destination
    );
}
