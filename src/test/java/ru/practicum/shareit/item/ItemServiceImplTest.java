package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.error.InvalidRequestParamsException;
import ru.practicum.shareit.error.AccessViolationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
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
class ItemServiceImplTest {

    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private CommentRepository commentRepository;
    @Autowired
    private ItemService itemService;

    public static final long BOOKER_USER_ID = 1L;
    public static final long OWNER_USER_ID = 2L;

    private List<User> users;
    private Item sourceItem;
    private List<Item> sourceItems;
    private Comment sourceComment;
    private Booking lastBooking;
    private Booking nextBooking;


    @BeforeEach
    void setUp() {
        users = List.of(
                makeUser(BOOKER_USER_ID, "John Doe", "john.doe@gmail.com"),
                makeUser(OWNER_USER_ID, "The Owner", "owner@gmail.com")
        );

        sourceItem = makeItem(1L, "first", "first item", users.get(1));
        sourceItems = List.of(
                sourceItem,
                makeItem(2L, "second", "second item", users.get(1))
        );

        sourceComment = makeComment(1L, "text", sourceItem, users.get(0));

        lastBooking = makeBooking(
                1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), sourceItem, users.get(0)
        );
        nextBooking = makeBooking(
                2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), sourceItem, users.get(0)
        );

        when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long userId = invocationOnMock.getArgument(0);
                    return users.stream().filter(user -> user.getId().equals(userId)).findFirst();
                });

        when(bookingRepository.findNextBookingsFor(anyList(), any())).thenReturn(List.of(nextBooking));
        when(bookingRepository.findLastBookingsFor(anyList(), any())).thenReturn(List.of(lastBooking));
        when(commentRepository.findCommentsFor(anyList())).thenReturn(List.of(sourceComment));
    }

    @Test
    void shouldCreateItem() {

        when(itemRepository.save(any())).thenReturn(sourceItem);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        var targetItem = itemService.createItem(OWNER_USER_ID, new ItemInDto(
                        sourceItem.getName(), sourceItem.getDescription(), sourceItem.isAvailable(), null
                )
        );

        checkResult(targetItem, ItemMapper.toItemDto(sourceItem));
    }

    @Test
    void shouldCreateItemOnRequest() {

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("desc");
        itemRequest.setRequester(users.get(0)); // booker
        itemRequest.setCreated(LocalDateTime.now());

        sourceItem.setRequest(itemRequest);

        when(itemRepository.save(any())).thenReturn(sourceItem);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));

        var targetItem = itemService.createItem(OWNER_USER_ID, new ItemInDto(
                        sourceItem.getName(),
                        sourceItem.getDescription(),
                        sourceItem.isAvailable(),
                        1L
                )
        );

        checkResult(targetItem, ItemMapper.toItemDto(sourceItem));
    }

    private static void checkResult(ItemDto targetItem, ItemDto sourceItem) {
        assertThat(targetItem, notNullValue());
        assertThat(targetItem.getId(), equalTo(sourceItem.getId()));
        assertThat(targetItem.getName(), equalTo(sourceItem.getName()));
        assertThat(targetItem.getAvailable(), equalTo(sourceItem.getAvailable()));
        assertThat(targetItem.getLastBooking(), equalTo(sourceItem.getLastBooking()));
        assertThat(targetItem.getNextBooking(), equalTo(sourceItem.getNextBooking()));
        assertThat(targetItem.getComments(), equalTo(sourceItem.getComments()));
        assertThat(targetItem.getRequestId(), equalTo(sourceItem.getRequestId()));
    }

    @Test
    void shouldThrowOnCreateItemWhenRequestNotFound() {

        when(itemRepository.save(any())).thenReturn(sourceItem);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> itemService.createItem(BOOKER_USER_ID, new ItemInDto(
                                sourceItem.getName(), sourceItem.getDescription(), sourceItem.isAvailable(), 1L
                        )
                )
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnCreateItemWhenUserNotFound() {

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> itemService.createItem(99L, new ItemInDto(
                                sourceItem.getName(), sourceItem.getDescription(), sourceItem.isAvailable(), 1L
                        )
                )
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldCreateCommentForItem() {

        var sourceCommentDto = CommentMapper.toCommentDto(sourceComment);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceItem));
        when(bookingRepository.findByBooker_IdAndItem_Id(anyLong(), anyLong(), any()))
                .thenReturn(List.of(lastBooking));
        when(commentRepository.save(any())).thenReturn(sourceComment);

        var targetCommentDto = itemService.createCommentForItem(
                BOOKER_USER_ID, sourceItem.getId(), new CommentInDto(sourceComment.getText())
        );

        assertThat(targetCommentDto, notNullValue());
        assertThat(targetCommentDto.getId(), equalTo(sourceCommentDto.getId()));
        assertThat(targetCommentDto.getText(), equalTo(sourceCommentDto.getText()));
        assertThat(targetCommentDto.getAuthorName(), equalTo(sourceCommentDto.getAuthorName()));
        assertThat(targetCommentDto.getCreated(), equalTo(sourceCommentDto.getCreated()));
    }

    @Test
    void shouldThrowOnCreateCommentForItemWhenNoItem() {

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> itemService.createCommentForItem(
                        BOOKER_USER_ID, 99L, new CommentInDto(sourceComment.getText())
                )
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnCreateCommentForItemWhenNoBooking() {

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceItem));
        when(bookingRepository.findByBooker_IdAndItem_Id(anyLong(), anyLong(), any()))
                .thenReturn(Collections.emptyList());
        when(commentRepository.save(any())).thenReturn(sourceComment);

        final InvalidRequestParamsException exception = assertThrows(
                InvalidRequestParamsException.class,
                () -> itemService.createCommentForItem(
                        BOOKER_USER_ID, sourceItem.getId(), new CommentInDto(sourceComment.getText())
                )
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldGetAllItems() {

        var sourceItemDtos = ItemMapper.toItemDto(sourceItems);
        sourceItemDtos.get(0).setComments(List.of(CommentMapper.toCommentDto(sourceComment)));
        sourceItemDtos.get(0).setNextBooking(BookingMapper.toBookingShortDto(nextBooking));
        sourceItemDtos.get(0).setLastBooking(BookingMapper.toBookingShortDto(lastBooking));

        when(itemRepository.findAllByOwner_Id(anyLong(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            sourceItems,
                            pageRequest,
                            sourceItems.size()
                    );
                });

        var targetItemDtos = itemService.getAllItems(OWNER_USER_ID, 0, 20);

        checkResult(targetItemDtos, sourceItemDtos);
    }

    private void checkResult(List<ItemDto> targetItems, List<ItemDto> sourceItems) {
        assertThat(targetItems, hasSize(sourceItems.size()));
        for (var sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", equalTo(sourceItem.getId())),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable())),
                    hasProperty("lastBooking", equalTo(sourceItem.getLastBooking())),
                    hasProperty("nextBooking", equalTo(sourceItem.getNextBooking())),
                    hasProperty("comments", equalTo(sourceItem.getComments())),
                    hasProperty("requestId", equalTo(sourceItem.getRequestId()))
            )));
        }
    }

    @Test
    void shouldGetItemByIdWhenNotOwner() {

        var sourceItemDto = ItemMapper.toItemDto(sourceItem);
        sourceItemDto.setComments(List.of(CommentMapper.toCommentDto(sourceComment)));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceItem));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of(sourceComment));

        var targetItemDto = itemService.getItemById(BOOKER_USER_ID, 1L);

        checkResult(targetItemDto, sourceItemDto);
    }

    @Test
    void shouldGetItemByIdWhenOwner() {

        var sourceItemDto = ItemMapper.toItemDto(sourceItem);
        sourceItemDto.setComments(List.of(CommentMapper.toCommentDto(sourceComment)));
        sourceItemDto.setNextBooking(BookingMapper.toBookingShortDto(nextBooking));
        sourceItemDto.setLastBooking(BookingMapper.toBookingShortDto(lastBooking));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceItem));

        var targetItemDto = itemService.getItemById(OWNER_USER_ID, 1L);

        checkResult(targetItemDto, sourceItemDto);
    }

    @Test
    void searchItemByNotEmptyName() {
        when(itemRepository.searchAvailable(anyString(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            sourceItems,
                            pageRequest,
                            sourceItems.size()
                    );
                });

        var targetItems = itemService.searchItemByName(BOOKER_USER_ID, "text", 0, 20);

        checkResult(targetItems, ItemMapper.toItemDto(sourceItems));
    }

    @Test
    void searchItemByEmptyName() {

        var targetItems = itemService.searchItemByName(BOOKER_USER_ID, "", 0, 20);

        assertThat(targetItems, notNullValue());
        assertThat(targetItems, hasSize(0));
    }

    @Test
    void shouldUpdateItemWhenOwner() {

        ItemInDto updatedItem = new ItemInDto(
                "new name", "new desc", !sourceItem.isAvailable(), null
        );

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceItem));
        when(itemRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Item.class));

        var targetItemDto = itemService.updateItem(OWNER_USER_ID, 1L, updatedItem);
        var sourceItemDto = ItemMapper.toItemDto(sourceItem);

        assertThat(targetItemDto, notNullValue());
        assertThat(targetItemDto.getId(), equalTo(sourceItemDto.getId()));
        assertThat(targetItemDto.getName(), equalTo(updatedItem.getName()));
        assertThat(targetItemDto.getDescription(), equalTo(updatedItem.getDescription()));
        assertThat(targetItemDto.getAvailable(), equalTo(updatedItem.getAvailable()));
        assertThat(targetItemDto.getLastBooking(), equalTo(sourceItemDto.getLastBooking()));
        assertThat(targetItemDto.getNextBooking(), equalTo(sourceItemDto.getNextBooking()));
        assertThat(targetItemDto.getComments(), equalTo(sourceItemDto.getComments()));
        assertThat(targetItemDto.getRequestId(), equalTo(sourceItemDto.getRequestId()));
    }

    @Test
    void shouldNotUpdateItemWhenNotOwner() {

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceItem));

        final AccessViolationException exception = assertThrows(
                AccessViolationException.class,
                () -> itemService.updateItem(BOOKER_USER_ID, 1L, new ItemInDto(
                                "", "", !sourceItem.isAvailable(), null
                        )
                )
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    private User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeItem(Long id, String name, String desc, User owner) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(desc);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(null);
        return item;
    }

    private Comment makeComment(Long id, String text, Item item, User author) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private Booking makeBooking(Long id, LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }
}