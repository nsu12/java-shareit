package ru.practicum.shareit.request;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    public static final long REQUESTER_USER_ID1 = 1L;
    public static final long REQUESTER_USER_ID2 = 2L;
    public static final long OWNER_USER_ID = 3L;
    private List<User> users;
    private List<ItemRequest> sourceItemRequests;

    private List<Item> items;

    @BeforeEach
    void setUp() {
        users = List.of(
                makeUser(REQUESTER_USER_ID1, "John Doe", "john.doe@gmail.com"),
                makeUser(REQUESTER_USER_ID2, "Will Smith", "will.smith@gmail.com"),
                makeUser(OWNER_USER_ID, "Owner", "owner@gmail.com")
        );

        sourceItemRequests = List.of(
                makeItemRequest(1L, "first", users.get(0)),
                makeItemRequest(2L, "second", users.get(0))
        );

        items = List.of(
                makeItem(1L, "item 1", users.get(2), sourceItemRequests.get(0)),
                makeItem(2L, "item 2", users.get(2), sourceItemRequests.get(0))
        );

        when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long userId = invocationOnMock.getArgument(0);
                    return users.stream().filter(user -> user.getId().equals(userId)).findFirst();
                });
    }

    @Test
    void shouldCreateItemRequest() {
        ItemRequest sourceItemRequest = sourceItemRequests.get(0);

        when(itemRequestRepository.save(any()))
                .thenReturn(sourceItemRequest);

        var targetItemRequest = itemRequestService.createItemRequest(
                REQUESTER_USER_ID1, new ItemRequestInDto(sourceItemRequest.getDescription())
        );

        checkResult(ItemRequestMapper.toItemRequestDto(sourceItemRequest), targetItemRequest);
    }

    private static void checkResult(ItemRequestDto sourceItemRequest, ItemRequestDto targetItemRequest) {
        assertThat(targetItemRequest, notNullValue());
        assertThat(targetItemRequest.getId(), equalTo(sourceItemRequest.getId()));
        assertThat(targetItemRequest.getDescription(), equalTo(sourceItemRequest.getDescription()));
        assertThat(targetItemRequest.getCreated(), equalTo(sourceItemRequest.getCreated()));
        assertThat(targetItemRequest.getItems(), equalTo(sourceItemRequest.getItems()));
    }

    @Test
    void shouldThrowOnCreateItemRequestIfUserNotExists() {

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> itemRequestService.createItemRequest(
                        99L, new ItemRequestInDto("some decs")
                )
        );

        assertThat(exception.getMessage(), is(Matchers.notNullValue()));
    }

    @Test
    void shouldGetRequestsByOwner() {

        when(itemRepository.findItemsFor(anyList())).thenReturn(items);
        when(itemRequestRepository.findAllByRequester_Id(anyLong(), any()))
                .thenReturn(sourceItemRequests);

        var targetItemRequestDtos = itemRequestService.getRequestsByOwner(REQUESTER_USER_ID1);
        var sourceItemRequestDtos = ItemRequestMapper.toItemRequestDto(sourceItemRequests);
        sourceItemRequestDtos.get(0).setItems(ItemMapper.toItemDto(items));

        checkResult(targetItemRequestDtos, sourceItemRequestDtos);
    }

    static void checkResult(List<ItemRequestDto> targetItemRequests, List<ItemRequestDto> sourceItemRequests) {
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

        List<ItemRequest> otherRequests = List.of(
                makeItemRequest(3L, "other 1", users.get(1)),
                makeItemRequest(4L, "other 2", users.get(2))
        );

        when(itemRepository.findItemsFor(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestRepository.findAllByRequester_IdNot(anyLong(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            otherRequests,
                            pageRequest,
                            otherRequests.size()
                    );
                });

        var targetItemRequestDtos = itemRequestService.getAllRequests(REQUESTER_USER_ID1, 0, 20);

        checkResult(targetItemRequestDtos, ItemRequestMapper.toItemRequestDto(otherRequests));
    }

    @Test
    void shouldGetRequestById() {

        ItemRequest sourceItemRequest = sourceItemRequests.get(0);
        var sourceItemRequestDto = ItemRequestMapper.toItemRequestDto(sourceItemRequest);
        sourceItemRequestDto.setItems(ItemMapper.toItemDto(items));

        when(itemRepository.findAllByRequest_Id(anyLong()))
                .thenReturn(items);
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(sourceItemRequest));

        var targetItemRequestDto = itemRequestService.getRequestById(1L, 1L);

        checkResult(targetItemRequestDto, sourceItemRequestDto);
    }

    private User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemRequest makeItemRequest(Long id, String desc, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(id);
        itemRequest.setDescription(desc);
        itemRequest.setRequester(requester); // booker
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private Item makeItem(Long id, String name, User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(name);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}