package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestBody ItemInDto item
    ) {
        return itemService.createItem(userId, item);
    }

    @PostMapping(value = "/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto comment
    ) {
        return itemService.createCommentForItem(userId, itemId, comment);
    }

    @GetMapping
    public List<ItemDto> getAllUserItems(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId
    ) {
        return itemService.getAllItems(userId);
    }

    @GetMapping(value = "/{itemId}")
    public ItemDto getUserItemById(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId
    ) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping(value = "/search")
    public List<ItemDto> searchItemByName(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(value = "text") String text
    ) {
        return itemService.searchItemByName(userId, text);
    }

    @PatchMapping(value = "/{itemId}")
    public ItemDto updateItem(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemInDto item
    ) {
        return itemService.updateItem(userId, itemId, item);
    }

    @DeleteMapping(value = "/{itemId}")
    public void deleteItem(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId
    ) {
        itemService.deleteItem(userId, itemId);
    }
}
