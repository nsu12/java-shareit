package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private EntityManager em;

    private List<User> users;
    private List<ItemRequest> sourceItemRequests;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        users = List.of(
                makeUser("John Doe", "john.doe@gmail.com"),
                makeUser("Will Smith", "will.smith@gmail.com"),
                makeUser("Owner", "owner@gmail.com")
        );

        sourceItemRequests = List.of(
                makeItemRequest("first", users.get(0)),
                makeItemRequest("second", users.get(0))
        );

        items = List.of(
                makeItem("item 1", users.get(2), sourceItemRequests.get(0)),
                makeItem("item 2", users.get(2), sourceItemRequests.get(0))
        );

        em.flush();
    }

    @Test
    void shouldGetAllRequests() {

        var targetItemRequestDtos = itemRequestService.getAllRequests(users.get(1).getId(), 0, 10);
        var sourceItemRequestDtos = ItemRequestMapper.toItemRequestDto(sourceItemRequests);
        sourceItemRequestDtos.get(0).setItems(ItemMapper.toItemDto(items));

        ItemRequestServiceImplTest.checkResult(targetItemRequestDtos, sourceItemRequestDtos);
    }

    @Test
    void shouldGetAllOwnerRequests() {
        var targetItemRequestDtos = itemRequestService.getRequestsByOwner(users.get(0).getId());
        var sourceItemRequestDtos = ItemRequestMapper.toItemRequestDto(sourceItemRequests);
        sourceItemRequestDtos.get(0).setItems(ItemMapper.toItemDto(items));

        ItemRequestServiceImplTest.checkResult(targetItemRequestDtos, sourceItemRequestDtos);
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private ItemRequest makeItemRequest(String desc, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(desc);
        itemRequest.setRequester(requester); // booker
        itemRequest.setCreated(LocalDateTime.now());
        em.persist(itemRequest);
        return itemRequest;
    }

    private Item makeItem(String name, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(name);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        em.persist(item);
        return item;
    }
}