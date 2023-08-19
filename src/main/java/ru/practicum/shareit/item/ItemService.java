package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@Transactional(readOnly = true)
public interface ItemService {

    ItemDto createItem(Long userId, @Valid ItemDto itemDto);

    List<ItemDto> getAllItems(Long userId);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> searchItemByName(Long userId, String namePart);

    @Transactional
    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    @Transactional
    void deleteItem(Long userId, Long itemId);
}
