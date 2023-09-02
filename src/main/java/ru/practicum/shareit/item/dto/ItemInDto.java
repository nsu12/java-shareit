package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemInDto {
    @NotBlank(message = "Название вещи должно быть задано")
    private String name;
    @NotBlank(message = "Описание вещи должно быть задано")
    private String description;
    @NotNull(message = "Статус 'доступно' должен быть задан")
    private Boolean available;
    private Long requestId;
}
