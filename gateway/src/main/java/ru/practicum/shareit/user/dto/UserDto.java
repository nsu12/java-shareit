package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "логин не может быть пустым")
    private String name;
    @NotNull(message = "email должен быть задан")
    @Email
    private String email;
}
