package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, @Valid ItemRequestInDto itemRequestDto) {
        User user = getUserOrThrow(userId);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getRequestsByOwner(Long userId) {
        getUserOrThrow(userId);
        return makeItemRequestDtosWithItems(
                itemRequestRepository.findAllByRequester_Id(
                        userId, Sort.by("created").descending()
                )
        );
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, @PositiveOrZero Integer from, @Positive Integer size) {
        getUserOrThrow(userId);
        return makeItemRequestDtosWithItems(
                itemRequestRepository.findAllByRequester_IdNot(
                    userId,
                    PageRequest.of(from / size, size, Sort.by("created").descending())
            ).toList()
        );
    }

    private List<ItemRequestDto> makeItemRequestDtosWithItems(List<ItemRequest> requests) {
        Map<Long, List<Item>> itemsForRequests = itemRepository.findItemsFor(requests).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return ItemRequestMapper.toItemRequestDto(requests).stream()
                .peek(itemRequestDto -> itemRequestDto.setItems(
                        ItemMapper.toItemDto(itemsForRequests.get(itemRequestDto.getId())))
                )
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntryNotFoundException(
                                String.format("запрос с указанным id (%d) не существует", requestId)
                        )
                );
        List<Item> items = itemRepository.findAllByRequest_Id(requestId);
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(itemRequest);
        dto.setItems(ItemMapper.toItemDto(items));
        return dto;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntryNotFoundException(
                                String.format("пользователь с указанным id (%d) не существует", userId)
                        )
                );
    }
}
