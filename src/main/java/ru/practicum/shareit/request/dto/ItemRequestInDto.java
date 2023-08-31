package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@RequiredArgsConstructor
@Getter
public class ItemRequestInDto {
    @NotBlank(message = "Описание вещи должно быть задано")
    private final String description;
}
