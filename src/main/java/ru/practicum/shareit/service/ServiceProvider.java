package ru.practicum.shareit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceProvider {

    private CheckConsistencyService checkConsistencyService;

    @Autowired
    public ServiceProvider() {
    }

    public CheckConsistencyService getChecker() {
        return checkConsistencyService;
    }
}