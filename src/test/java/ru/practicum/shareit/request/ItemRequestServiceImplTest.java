package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest
class ItemRequestServiceImplTest {

    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    private final List<ItemRequestDto> sourceItemRequests = List.of(
            new ItemRequestDto(
                    1L,
                    "thing one request",
                    LocalDateTime.now(),
                    Collections.emptyList()
            ),
            new ItemRequestDto(
                    1L,
                    "thing two request",
                    LocalDateTime.now(),
                    Collections.emptyList()
            )
    );

    @BeforeEach
    void setUp() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
    }

    @Test
    void shouldCreateItemRequest() {
        ItemRequestDto sourceItemRequest = sourceItemRequests.get(0);

        when(itemRequestRepository.save(any()))
                .thenReturn(ItemRequestMapper.toItemRequest(sourceItemRequest));

        var targetItemRequest = itemRequestService.createItemRequest(
                1L, new ItemRequestInDto("some thing")
        );

        checkResult(sourceItemRequest, targetItemRequest);
    }

    private static void checkResult(ItemRequestDto sourceItemRequest, ItemRequestDto targetItemRequest) {
        assertThat(targetItemRequest, notNullValue());
        assertThat(targetItemRequest.getId(), equalTo(sourceItemRequest.getId()));
        assertThat(targetItemRequest.getDescription(), equalTo(sourceItemRequest.getDescription()));
        assertThat(targetItemRequest.getCreated(), equalTo(sourceItemRequest.getCreated()));
        assertThat(targetItemRequest.getItems(), equalTo(sourceItemRequest.getItems()));
    }

    @Test
    void shouldGetRequestsByOwner() {

        when(itemRepository.findItemsFor(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestRepository.findAllByRequester_Id(anyLong(), any()))
                .thenReturn(ItemRequestMapper.toItemRequest(sourceItemRequests));

        var targetItemRequests = itemRequestService.getRequestsByOwner(1L);

        checkResult(targetItemRequests, sourceItemRequests);
    }

    private void checkResult(List<ItemRequestDto> targetItemRequests, List<ItemRequestDto> sourceItemRequests) {
        assertThat(targetItemRequests, hasSize(sourceItemRequests.size()));
        for (var sourceItemRequest : sourceItemRequests) {
            assertThat(targetItemRequests, hasItem(allOf(
                    hasProperty("id", equalTo(sourceItemRequest.getId())),
                    hasProperty("description", equalTo(sourceItemRequest.getDescription())),
                    hasProperty("created", equalTo(sourceItemRequest.getCreated())),
                    hasProperty("items", equalTo(sourceItemRequest.getItems()))
            )));
        }
    }

    @Test
    void shouldGetAllRequests() {

        when(itemRepository.findItemsFor(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestRepository.findAllByRequester_IdNot(anyLong(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            ItemRequestMapper.toItemRequest(sourceItemRequests),
                            pageRequest,
                            sourceItemRequests.size()
                    );
                });

        var targetItemRequests = itemRequestService.getAllRequests(1L, 0, 20);

        checkResult(targetItemRequests, sourceItemRequests);
    }

    @Test
    void shouldGetRequestById() {

        ItemRequestDto sourceItemRequest = sourceItemRequests.get(0);

        when(itemRepository.findAllByRequest_Id(anyLong()))
                .thenReturn(Collections.emptyList());
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(ItemRequestMapper.toItemRequest(sourceItemRequest)));

        var targetItemRequest = itemRequestService.getRequestById(1L, 1L);

        checkResult(targetItemRequest, sourceItemRequest);
    }
}