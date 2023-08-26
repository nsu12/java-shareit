package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class ItemInDto {
    @NotBlank(message = "Название вещи должно быть задано")
    private String name;
    @NotBlank(message = "Описание вещи должно быть задано")
    private String description;
    @NotNull(message = "Статус 'доступно' должен быть задан")
    private Boolean available;
}
