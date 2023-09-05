package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                null
        );
    }

    public static List<ItemRequestDto> toItemRequestDto(List<ItemRequest> requests) {
        if (requests.isEmpty()) return Collections.emptyList();
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }
}
