package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static List<UserDto> toUserDto(List<User> users) {
        if (users == null || users.isEmpty()) return Collections.emptyList();
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public static User toUser(UserDto userDto) {
         User user = new User();
         user.setId(userDto.getId());
         user.setName(userDto.getName());
         user.setEmail(userDto.getEmail());
         return user;
    }

    public static List<User> toUser(List<UserDto> userDtoList) {
        if (userDtoList.isEmpty()) return Collections.emptyList();
        return userDtoList.stream()
                .map(UserMapper::toUser)
                .collect(Collectors.toList());
    }
}
