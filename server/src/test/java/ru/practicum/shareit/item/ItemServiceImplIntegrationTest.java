package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@Transactional
@SpringBootTest
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private EntityManager em;
    private List<User> users;
    private List<Item> sourceItems;

    @BeforeEach
    void setUp() {
        users = List.of(
                makeUser("John Doe", "john.doe@gmail.com"),
                makeUser("The Owner", "owner@gmail.com")
        );

        sourceItems = List.of(
                makeItem("first", "first item", users.get(1)),
                makeItem("second", "second item", users.get(1))
        );

        em.flush();
    }

    @Test
    void shouldGetAllItems() {

        Comment sourceComment = makeComment("text", sourceItems.get(0), users.get(0));
        Booking lastBooking = makeBooking(
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), sourceItems.get(0), users.get(0)
        );
        Booking nextBooking = makeBooking(
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), sourceItems.get(0), users.get(0)
        );
        em.flush();

        List<ItemDto> targetItemsDto = itemService.getAllItems(users.get(1).getId(), 0, 10);

        var sourceItemsDto = ItemMapper.toItemDto(sourceItems);
        sourceItemsDto.get(0).setComments(List.of(CommentMapper.toCommentDto(sourceComment)));
        sourceItemsDto.get(0).setNextBooking(BookingMapper.toBookingShortDto(nextBooking));
        sourceItemsDto.get(0).setLastBooking(BookingMapper.toBookingShortDto(lastBooking));

        ItemServiceImplTest.checkResult(targetItemsDto, sourceItemsDto);
    }

    @Test
    void shouldSearchItemByText() {

        List<ItemDto> targetItemsDto = itemService.searchItemByName(users.get(0).getId(), "first", 0, 10);

        ItemServiceImplTest.checkResult(targetItemsDto, ItemMapper.toItemDto(List.of(sourceItems.get(0))));
    }

    @Test
    void shouldDeleteItem() {

        itemService.deleteItem(users.get(1).getId(), sourceItems.get(0).getId());

        TypedQuery<Item> query = em.createQuery("SELECT item FROM Item item", Item.class);
        List<Item> result = query.getResultList();

        assertThat(result, notNullValue());
        assertThat(result, hasSize(1));

        ItemServiceImplTest.checkResult(ItemMapper.toItemDto(result.get(0)), ItemMapper.toItemDto(sourceItems.get(1)));
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private Item makeItem(String name, String desc, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(desc);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(null);
        em.persist(item);
        return item;
    }

    private Comment makeComment(String text, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        em.persist(comment);
        return comment;
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        em.persist(booking);
        return booking;
    }
}