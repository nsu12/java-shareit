package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import javax.validation.Valid;
import java.util.List;

@Transactional(readOnly = true)
public interface ItemService {

    @Transactional
    ItemDto createItem(Long userId, @Valid ItemInDto itemDto);

    @Transactional
    CommentDto createCommentForItem(Long userId, Long itemId, @Valid CommentDto commentDto);

    List<ItemDto> getAllItems(Long userId);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> searchItemByName(Long userId, String namePart);

    @Transactional
    ItemDto updateItem(Long userId, Long itemId, ItemInDto itemDto);

    @Transactional
    void deleteItem(Long userId, Long itemId);
}
