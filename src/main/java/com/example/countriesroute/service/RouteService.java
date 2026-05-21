package com.example.countriesroute.service;

import java.util.List;

public interface RouteService {

    /**
     * Finds the shortest land route from {@code origin} to {@code destination}.
     *
     * @param origin      the cca3 code of the starting country
     * @param destination the cca3 code of the target country
     * @return ordered list of cca3 codes representing the route, including both endpoints
     * @throws com.example.countriesroute.exception.RouteNotFoundException if no land route exists
     *         or either country code is unknown
     */
    List<String> findRoute(String origin, String destination);
}
