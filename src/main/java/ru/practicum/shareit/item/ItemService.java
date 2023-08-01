package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

public interface ItemService {

    ItemDto createOrThrow(Integer userId, @Valid ItemDto itemDto);

    List<ItemDto> getAllOrThrow(Integer userId);

    ItemDto getByIdOrThrow(Integer userId, Integer itemId);

    List<ItemDto> searchByNameOrThrow(Integer userId, String namePart);

    ItemDto updateOrThrow(Integer userId, Integer itemId, ItemDto itemDto);

    void delete(Integer userId, Integer itemId);
}
