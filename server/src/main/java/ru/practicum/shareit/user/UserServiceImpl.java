package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserDto> getUsers() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> user = repository.findById(id);
        return user.map(UserMapper::toUserDto).orElse(null);
    }

    @Override
    public UserDto create(UserDto userDto) {
        try {
            return UserMapper.toUserDto(repository.save(UserMapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Пользователь с E-mail=" +
                    userDto.getEmail() + " уже существует!");
        }
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + id + " не найден!"));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if ((userDto.getEmail() != null) && (!userDto.getEmail().equals(user.getEmail()))) {
            if (!repository.findByEmail(userDto.getEmail())
                    .stream()
                    .filter(u -> u.getEmail().equals(userDto.getEmail()))
                    .allMatch(u -> u.getId().equals(userDto.getId()))) {

                throw new UserAlreadyExistsException("Пользователь с E-mail=" + userDto.getEmail() + " уже существует!");
            }
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(repository.save(user));
    }

    @Override
    public void delete(Long userId) {
        try {
            repository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
    }

    @Override
    public User findUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + id + " не найден!"));
    }
}