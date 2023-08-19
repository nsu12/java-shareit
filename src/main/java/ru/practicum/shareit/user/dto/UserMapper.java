package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(UserDto userDto) {
         User user = new User();
         user.setId(userDto.getId());
         user.setName(userDto.getName());
         user.setEmail(userDto.getEmail());
         return user;
    }
}
