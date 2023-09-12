package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemRequestInDto itemRequest
    ) {
        return itemRequestClient.createItemRequest(userId, itemRequest);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId
    ) {
        return itemRequestClient.getRequestsByOwner(userId);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping(value = "/{requestId}")
    public ResponseEntity<Object> getOneRequest(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId
    ) {
        return itemRequestClient.getRequestById(userId, requestId);
    }
}
