package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestInDto itemRequest
    ) {
        return itemRequestService.createItemRequest(userId, itemRequest);
    }

    @GetMapping
    public List<ItemRequestDto> getYourRequests(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId
    ) {
        return itemRequestService.getRequestsByOwner(userId);
    }

    @GetMapping(value = "/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping(value = "/{requestId}")
    public ItemRequestDto getOneRequest(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId
    ) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}
