package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new UserAlreadyExistsException("Пользователь с E-mail=" + user.getEmail() + " уже существует!");
        }

        if (isValidUser(user)) {
            if (user.getId() == null) {
                user.setId(++currentId);
            }
            users.put(user.getId(), user);
        }
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID=" + user.getId() + " не найден!");
        }

        User existingUser = users.get(user.getId());

        if (user.getName() == null) {
            user.setName(existingUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(existingUser.getEmail());
        }

        if (users.values().stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .anyMatch(u -> !u.getId().equals(user.getId()))) {
            throw new UserAlreadyExistsException("Пользователь с E-mail=" + user.getEmail() + " уже существует!");
        }

        if (isValidUser(user)) {
            users.put(user.getId(), user);
        }

        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return users.get(userId);
    }

    @Override
    public User delete(Long userId) {
        if (userId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return users.remove(userId);
    }

    private boolean isValidUser(User user) {
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный e-mail пользователя: " + user.getEmail());
        }
        if (user.getName().isEmpty()) {
            throw new ValidationException("Некорректный логин пользователя: " + user.getName());
        }
        return true;
    }
}
