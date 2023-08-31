package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
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
        return ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.findAllByRequester_Id(userId, Sort.by("created").descending())
        );
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        return ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.findAllByRequester_IdNot(
                        userId,
                        PageRequest.of(from / size, size, Sort.by("created").descending())
                ).toList()
        );
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        return ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.findById(requestId)
                        .orElseThrow(() -> new EntryNotFoundException(
                                        String.format("запрос с указанным id (%d) не существует", requestId)
                                )
                        )
        );
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntryNotFoundException(
                                String.format("пользователь с указанным id (%d) не существует", userId)
                        )
                );
    }
}
