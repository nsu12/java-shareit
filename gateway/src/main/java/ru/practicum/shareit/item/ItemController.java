package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RequiredArgsConstructor
@Controller
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemInDto item
    ) {
        return itemClient.createItem(userId, item);
    }

    @PostMapping(value = "/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid CommentInDto comment
    ) {
        return itemClient.createCommentForItem(userId, itemId, comment);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping(value = "/{itemId}")
    public ResponseEntity<Object> getUserItemById(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId
    ) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> searchItemByName(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(value = "text") String text,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return itemClient.searchItemByName(userId, text, from, size);
    }

    @PatchMapping(value = "/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemInDto item
    ) {
        return itemClient.updateItem(userId, itemId, item);
    }

    @DeleteMapping(value = "/{itemId}")
    public ResponseEntity<Object> deleteItem(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId
    ) {
        return itemClient.deleteItem(userId, itemId);
    }
}
