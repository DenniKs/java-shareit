package ru.practicum.shareit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserForbiddenException;
import ru.practicum.shareit.item.ItemService;

import ru.practicum.shareit.user.UserService;

@Service
public class CheckConsistencyService {
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public CheckConsistencyService(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    public void isExistUser(Long userId) {
        if (userService.getUserById(userId) == null) {
            throw new UserForbiddenException("Пользователь с ID " + userId + " не найден!");
        }
    }

    public boolean isAvailableItem(Long itemId) {
        return itemService.findItemById(itemId).getAvailable();
    }

    public boolean isItemOwner(Long itemId, Long userId) {

        return itemService.getItemsByOwner(userId, 0, null).stream()
                .anyMatch(i -> i.getId().equals(itemId));
    }
}