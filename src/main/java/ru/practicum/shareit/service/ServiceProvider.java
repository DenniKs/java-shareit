package ru.practicum.shareit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceProvider {

    private static CheckConsistencyService checkConsistencyService;

    @Autowired
    public ServiceProvider(CheckConsistencyService checkConsistencyService) {
        ServiceProvider.checkConsistencyService = checkConsistencyService;
    }

    public static CheckConsistencyService getChecker() {
        return checkConsistencyService;
    }
}