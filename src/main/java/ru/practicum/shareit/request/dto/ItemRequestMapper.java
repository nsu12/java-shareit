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
                Collections.emptyList()
        );
    }

    public static List<ItemRequestDto> toItemRequestDto(List<ItemRequest> requests) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setCreated(itemRequestDto.getCreated());
        return itemRequest;
    }

    public static List<ItemRequest> toItemRequest(List<ItemRequestDto> itemRequestDtoList) {
        return itemRequestDtoList.stream()
                .map(ItemRequestMapper::toItemRequest)
                .collect(Collectors.toList());
    }
}
