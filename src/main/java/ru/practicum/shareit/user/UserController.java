package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.service.CheckConsistencyService;
import ru.practicum.shareit.user.dto.UserDto;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public UserController(UserService userService, CheckConsistencyService checkConsistencyService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @ResponseBody
    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Получен POST-запрос к эндпоинту: '/users' на добавление пользователя");
        return userService.create(userDto);
    }

    @ResponseBody
    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto, @PathVariable Long userId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users' на обновление пользователя с ID={}", userId);
        return userService.update(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public UserDto delete(@PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/users' на удаление пользователя с ID={}", userId);
        UserDto userDto = userService.delete(userId);
        itemService.deleteItemsByOwner(userId);
        return userDto;
    }
}