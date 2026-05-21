package com.example.countriesroute.exception;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(String message) {
        super(message);
    }
}
