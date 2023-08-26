package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

interface UserService {
    UserDto createUser(@Valid UserDto user);

    UserDto getUserById(Long id);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UserDto user);

    void deleteUser(Long id);
}
