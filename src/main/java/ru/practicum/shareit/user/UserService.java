package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }

    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(userStorage.getUserById(id));
    }

    public UserDto create(UserDto userDto) {
        return UserMapper.toUserDto(userStorage.create(UserMapper.toUser(userDto)));
    }

    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        return UserMapper.toUserDto(userStorage.update(UserMapper.toUser(userDto)));
    }

    public UserDto delete(Long userId) {
        return UserMapper.toUserDto(userStorage.delete(userId));
    }
}