package ru.practicum.shareit.request.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ItemRequestInDto {
    @NotBlank(message = "Описание вещи должно быть задано")
    private String description;
}
