package ru.practicum.shareit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceProvider {

    private final CheckConsistencyService checkConsistencyService;

    @Autowired
    public ServiceProvider(CheckConsistencyService checkConsistencyService) {
        this.checkConsistencyService = checkConsistencyService;
    }

    public CheckConsistencyService getChecker() {
        return checkConsistencyService;
    }
}