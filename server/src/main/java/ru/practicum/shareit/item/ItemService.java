package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import java.util.List;

@Transactional(readOnly = true)
public interface ItemService {

    @Transactional
    ItemDto createItem(Long userId, ItemInDto itemDto);

    @Transactional
    CommentDto createCommentForItem(Long userId, Long itemId, CommentInDto commentDto);

    List<ItemDto> getAllItems(Long userId, Integer from, Integer size);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> searchItemByName(Long userId, String namePart, Integer from, Integer size);

    @Transactional
    ItemDto updateItem(Long userId, Long itemId, ItemInDto itemDto);

    @Transactional
    void deleteItem(Long userId, Long itemId);
}
