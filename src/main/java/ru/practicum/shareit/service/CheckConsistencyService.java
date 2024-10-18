package ru.practicum.shareit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

@Service
public class CheckConsistencyService {
    private final UserService userService;

    @Autowired
    public CheckConsistencyService(UserService userService, ItemService itemService) {
        this.userService = userService;
    }

    public void isExistUser(Long userId) {
        if (userService.getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
    }
}