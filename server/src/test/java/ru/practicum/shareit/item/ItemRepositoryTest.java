package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepository itemRepository;
    private List<ItemRequest> itemRequests;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        List<User> users = List.of(
                makeUser("John Doe", "john.doe@gmail.com"),
                makeUser("Will Smith", "will.smith@gmail.com"),
                makeUser("Owner", "owner@gmail.com")
        );

        itemRequests = List.of(
                makeItemRequest("first", users.get(0)),
                makeItemRequest("second", users.get(0))
        );

        items = List.of(
                makeItem("item 1", "some desc", users.get(2), itemRequests.get(0)),
                makeItem("thing 2", "item desc", users.get(2), itemRequests.get(0)),
                makeItem("item 3", "item 3 desc", users.get(2), null)
        );

        items.get(2).setAvailable(false);

        em.flush();
    }

    @Test
    void shouldSearchAvailableItems() {

        var result = itemRepository.searchAvailable("item", PageRequest.of(0, 10));

        assertThat(result, notNullValue());

        var targetItems = result.toList();
        var sourceItems = List.of(items.get(0), items.get(1)); // available only 2 first items

        ItemServiceImplTest.checkResult(ItemMapper.toItemDto(targetItems), ItemMapper.toItemDto(sourceItems));
    }

    @Test
    void shouldFindItemsForRequests() {

        var targetItems = itemRepository.findItemsFor(itemRequests);
        var sourceItems = List.of(items.get(0), items.get(1)); // only 2 first items has requests

        ItemServiceImplTest.checkResult(ItemMapper.toItemDto(targetItems), ItemMapper.toItemDto(sourceItems));
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

    private Item makeItem(String name, String desc, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(desc);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        em.persist(item);
        return item;
    }
}