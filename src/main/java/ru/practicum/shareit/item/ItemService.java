package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

public interface ItemService {

    ItemDto createItem(Integer userId, @Valid ItemDto itemDto);

    List<ItemDto> getAllItems(Integer userId);

    ItemDto getItemById(Integer userId, Integer itemId);

    List<ItemDto> searchItemByName(Integer userId, String namePart);

    ItemDto updateItem(Integer userId, Integer itemId, ItemDto itemDto);

    void deleteItem(Integer userId, Integer itemId);
}
