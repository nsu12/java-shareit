package ru.practicum.shareit.request;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import java.util.List;

@Transactional(readOnly = true)
public interface ItemRequestService {
    @Transactional
    ItemRequestDto createItemRequest(Long userId, ItemRequestInDto itemRequestDto);

    List<ItemRequestDto> getRequestsByOwner(Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);

    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);
}
