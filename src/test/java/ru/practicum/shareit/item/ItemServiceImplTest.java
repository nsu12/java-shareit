package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessViolationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

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

    private final List<ItemDto> sourceItems = List.of(
            new ItemDto(
                    1L,
                    "first item",
                    "first item desc",
                    true,
                    null,
                    null,
                    Collections.emptyList(),
                    null
            ),
            new ItemDto(
                    2L,
                    "second item",
                    "some desc",
                    true,
                    null,
                    null,
                    Collections.emptyList(),
                    null
            )

    );

    @BeforeEach
    void setUp() {
        when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long userId = invocationOnMock.getArgument(0);
                    return Optional.of(
                            UserMapper.toUser(new UserDto(userId, "user", "user@gmail.com"))
                    );
                });
        when(bookingRepository.findNextBookingsFor(anyList(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findLastBookingsFor(anyList(), any())).thenReturn(Collections.emptyList());
        when(commentRepository.findCommentsFor(anyList())).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldCreateItem() {
        ItemDto sourceItem = sourceItems.get(0);

        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(sourceItem));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        var targetItem = itemService.createItem(1L, new ItemInDto(
                        sourceItem.getName(), sourceItem.getDescription(), sourceItem.getAvailable(), null
                )
        );

        checkResult(sourceItem, targetItem);
    }

    private static void checkResult(ItemDto sourceItem, ItemDto targetItem) {
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
    void shouldCreateCommentForItem() {
        UserDto bookerDto = new UserDto(1L, "user", "user@gmail.com");

        CommentDto sourceComment = new CommentDto(
                1L,
                "some text",
                bookerDto.getName(),
                LocalDateTime.now()
        );

        ItemDto sourceItem = new ItemDto(
                1L,
                "item with commit",
                "item desc",
                true,
                null,
                null,
                List.of(sourceComment),
                null
        );

        BookingDto bookingDto = new BookingDto(
                1L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                BookingStatus.APPROVED,
                sourceItem,
                bookerDto
        );

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(ItemMapper.toItem(sourceItem)));
        when(bookingRepository.findByBooker_IdAndItem_Id(anyLong(), anyLong(), any()))
                .thenReturn(List.of(BookingMapper.toBooking(bookingDto)));
        when(commentRepository.save(any()))
                .thenAnswer(invocationOnMock -> {
                    Comment comment = CommentMapper.toComment(sourceComment);
                    comment.setAuthor(UserMapper.toUser(bookerDto));
                    return comment;
                });


        var targetComment = itemService.createCommentForItem(
                1L, 1L, new CommentInDto(sourceComment.getText())
        );

        assertThat(targetComment, notNullValue());
        assertThat(targetComment.getId(), equalTo(sourceComment.getId()));
        assertThat(targetComment.getText(), equalTo(sourceComment.getText()));
        assertThat(targetComment.getAuthorName(), equalTo(sourceComment.getAuthorName()));
        assertThat(targetComment.getCreated(), equalTo(sourceComment.getCreated()));
    }

    @Test
    void shouldGetAllItems() {

        when(itemRepository.findAllByOwner_Id(anyLong(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            ItemMapper.toItem(sourceItems),
                            pageRequest,
                            sourceItems.size()
                    );
                });

        var targetItems = itemService.getAllItems(1L, 0, 20);

        checkResult(targetItems, sourceItems);
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
        ItemDto sourceItem = sourceItems.get(0);

        when(itemRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Item item = ItemMapper.toItem(sourceItem);
                    item.setOwner(UserMapper.toUser(new UserDto(1L, "name", "name@gmail.com")));
                    return Optional.of(item);
                });
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(Collections.emptyList());

        var targetItem = itemService.getItemById(2L, 1L);

        checkResult(sourceItem, targetItem);
    }

    @Test
    void shouldGetItemByIdWhenOwner() {
        ItemDto sourceItem = sourceItems.get(0);

        when(itemRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Item item = ItemMapper.toItem(sourceItem);
                    item.setOwner(UserMapper.toUser(new UserDto(1L, "name", "name@gmail.com")));
                    return Optional.of(item);
                });

        var targetItem = itemService.getItemById(1L, 1L);

        checkResult(sourceItem, targetItem);
    }

    @Test
    void searchItemByNotEmptyName() {
        when(itemRepository.searchAvailable(anyString(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            ItemMapper.toItem(sourceItems),
                            pageRequest,
                            sourceItems.size()
                    );
                });

        var targetItems = itemService.searchItemByName(1L, "text", 0, 20);

        checkResult(targetItems, sourceItems);
    }

    @Test
    void searchItemByEmptyName() {

        var targetItems = itemService.searchItemByName(1L, "", 0, 20);

        assertThat(targetItems, notNullValue());
        assertThat(targetItems, hasSize(0));
    }

    @Test
    void shouldUpdateItemWhenOwner() {

        ItemDto sourceItem = sourceItems.get(0);
        ItemInDto updatedItem = new ItemInDto(
                "new name", "new desc", !sourceItem.getAvailable(), null
        );

        when(itemRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Item item = ItemMapper.toItem(sourceItem);
                    item.setOwner(UserMapper.toUser(new UserDto(1L, "name", "name@gmail.com")));
                    return Optional.of(item);
                });
        when(itemRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Item.class));

        var targetItem = itemService.updateItem(1L, 1L, updatedItem);

        assertThat(targetItem, notNullValue());
        assertThat(targetItem.getId(), equalTo(sourceItem.getId()));
        assertThat(targetItem.getName(), equalTo(updatedItem.getName()));
        assertThat(targetItem.getDescription(), equalTo(updatedItem.getDescription()));
        assertThat(targetItem.getAvailable(), equalTo(updatedItem.getAvailable()));
        assertThat(targetItem.getLastBooking(), equalTo(sourceItem.getLastBooking()));
        assertThat(targetItem.getNextBooking(), equalTo(sourceItem.getNextBooking()));
        assertThat(targetItem.getComments(), equalTo(sourceItem.getComments()));
        assertThat(targetItem.getRequestId(), equalTo(sourceItem.getRequestId()));
    }

    @Test
    void shouldNotUpdateItemWhenNotOwner() {

        ItemDto sourceItem = sourceItems.get(0);

        when(itemRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Item item = ItemMapper.toItem(sourceItem);
                    item.setOwner(UserMapper.toUser(new UserDto(1L, "name", "name@gmail.com")));
                    return Optional.of(item);
                });
        when(itemRepository.save(any())).thenReturn(ItemMapper.toItem(sourceItem));

        final AccessViolationException exception = assertThrows(
                AccessViolationException.class,
                () -> itemService.updateItem(2L, 1L, new ItemInDto(
                                "", "", !sourceItem.getAvailable(), null
                        )
                )
        );

        assertThat(exception.getMessage(), notNullValue());
    }
}