package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingStateFilter;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.error.InvalidRequestParamsException;
import ru.practicum.shareit.error.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookingServiceImplTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private ItemRepository itemRepository;

    @Autowired
    private BookingService bookingService;

    public static final long BOOKER_USER_ID1 = 1L;
    public static final long BOOKER_USER_ID2 = 2L;
    public static final long OWNER_USER_ID = 3L;

    private List<User> users;
    private Booking sourceBooking;
    private List<Booking> sourceBookings;

    @BeforeEach
    void setUp() {
         users = List.of(
                makeUser(BOOKER_USER_ID1, "John Doe", "john.doe@gmail.com"),
                makeUser(BOOKER_USER_ID2, "Will Smith", "will.smith@gmail.com"),
                makeUser(OWNER_USER_ID, "The Owner", "owner@gmail.com")
        );

        sourceBooking = makeBooking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                makeItem(1L, "thing", "desc", users.get(2)),
                users.get(0)
        );

        sourceBookings = List.of(
                sourceBooking,
                makeBooking(
                        2L,
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusDays(1),
                        makeItem(2L, "another thing", "desc", users.get(2)),
                        users.get(0)
                )
        );

        when(userRepository.findById(anyLong()))
                .thenAnswer(invocationOnMock -> {
                    Long userId = invocationOnMock.getArgument(0);
                    return users.stream().filter(user -> user.getId().equals(userId)).findFirst();
                });
    }

    @Test
    void shouldGetBookingByIdForOwner() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(sourceBooking));

        var targetBooking = bookingService.getBookingById(OWNER_USER_ID, 1L);

        checkResult(BookingMapper.toBookingDto(sourceBooking), targetBooking);
    }

    @Test
    void shouldGetBookingByIdForBooker() {

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(sourceBooking));

        var targetBooking = bookingService.getBookingById(BOOKER_USER_ID1, 1L);

        checkResult(BookingMapper.toBookingDto(sourceBooking), targetBooking);
    }

    private static void checkResult(BookingDto sourceBooking, BookingDto targetBooking) {
        assertThat(targetBooking, notNullValue());
        assertThat(targetBooking.getId(), is(sourceBooking.getId()));
        assertThat(targetBooking.getStart(), equalTo(sourceBooking.getStart()));
        assertThat(targetBooking.getEnd(), equalTo(sourceBooking.getEnd()));
        assertThat(targetBooking.getStatus(), equalTo(sourceBooking.getStatus()));
        assertThat(targetBooking.getItem(), equalTo(sourceBooking.getItem()));
        assertThat(targetBooking.getBooker(), equalTo(sourceBooking.getBooker()));
    }

    @Test
    void shouldThrowWhenInvalidUserOnGetBookingById() {

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(sourceBooking));

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> bookingService.getBookingById(BOOKER_USER_ID2, 1L)
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnGetBookingByIdWhenBookingNotFound() {

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> bookingService.getBookingById(BOOKER_USER_ID1, 1L)
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldCreateBooking() {

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(sourceBooking.getItem()));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(sourceBooking));
        when(bookingRepository.save(any())).thenReturn(sourceBooking);

        var targetBooking = bookingService.createBooking(BOOKER_USER_ID1,
                new BookingInDto(
                        sourceBooking.getId(), sourceBooking.getStartDate(), sourceBooking.getEndDate()
                ));

        checkResult(BookingMapper.toBookingDto(sourceBooking), targetBooking);
    }

    @Test
    void shouldThrowOnCreateBookingWhenUserNotFound() {

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> bookingService.createBooking(99L,
                        new BookingInDto(
                                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1)
                        ))
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnCreateBookingWhenItemNotFound() {

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> bookingService.createBooking(1L,
                        new BookingInDto(
                                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1)
                        ))
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnCreateBookingWhenItemNotAvailable() {
        Item item = makeItem(1L, "thing", "desc", users.get(2));
        item.setAvailable(false);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final ItemNotAvailableException exception = assertThrows(
                ItemNotAvailableException.class,
                () -> bookingService.createBooking(BOOKER_USER_ID1,
                        new BookingInDto(
                                item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1)
                        ))
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnCreateBookingWhenUserIsOwner() {
        Item item = makeItem(1L, "thing", "desc", users.get(2));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> bookingService.createBooking(OWNER_USER_ID,
                        new BookingInDto(
                                item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1)
                        ))
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnCreateBookingWhenStartDateAfter() {
        Item item = makeItem(1L, "thing", "desc", users.get(2));

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final InvalidRequestParamsException exception = assertThrows(
                InvalidRequestParamsException.class,
                () -> bookingService.createBooking(BOOKER_USER_ID1,
                        new BookingInDto(
                                item.getId(), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(5)
                        ))
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldSetBookingApproveStatus() {

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(sourceBooking));

        var targetBooking = bookingService.setBookingApproveStatus(OWNER_USER_ID, sourceBooking.getId(), true);

        assertThat(targetBooking, notNullValue());
        assertThat(targetBooking.getId(), equalTo(sourceBooking.getId()));
        assertThat(targetBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void shouldThrowOnSetBookingApproveStatusWhenNotOwner() {

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(sourceBooking));

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> bookingService.setBookingApproveStatus(BOOKER_USER_ID1, sourceBooking.getId(), true)
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldThrowOnSetBookingApproveStatusWhenAlreadyApproved() {
        sourceBooking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong()))
                .thenReturn(Optional.of(sourceBooking));

        final ItemNotAvailableException exception = assertThrows(
                ItemNotAvailableException.class,
                () -> bookingService.setBookingApproveStatus(OWNER_USER_ID, sourceBooking.getId(), true)
        );

        assertThat(exception.getMessage(), notNullValue());
    }

    @Test
    void shouldGetAllUserBookings() {

        when(bookingRepository.findByBooker_Id(anyLong(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getUserBookings(
                BOOKER_USER_ID1, BookingStateFilter.ALL, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetPastUserBookings() {

        when(bookingRepository.findByBooker_IdAndEndDateIsBefore(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getUserBookings(
                BOOKER_USER_ID1, BookingStateFilter.PAST, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetCurrentUserBookings() {

        when(bookingRepository.findAllCurrentForBooker(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getUserBookings(
                BOOKER_USER_ID1, BookingStateFilter.CURRENT, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetFutureUserBookings() {

        when(bookingRepository.findByBooker_IdAndStartDateIsAfter(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getUserBookings(
                BOOKER_USER_ID1, BookingStateFilter.FUTURE, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    static void checkResult(List<BookingDto> targetBookings, List<BookingDto> sourceBookings) {
        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (var sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus())),
                    hasProperty("item", equalTo(sourceBooking.getItem())),
                    hasProperty("booker", equalTo(sourceBooking.getBooker()))
            )));
        }
    }

    @Test
    void shouldGetRejectedAndWaitingUserBookings() {

        when(bookingRepository.findByBooker_IdAndStatusIs(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getUserBookings(
                OWNER_USER_ID, BookingStateFilter.REJECTED, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));

        targetBookings = bookingService.getUserBookings(
                OWNER_USER_ID, BookingStateFilter.WAITING, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetAllOwnerBookings() {

        when(bookingRepository.findByItem_Owner_Id(anyLong(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(1, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getOwnerBookings(
                OWNER_USER_ID, BookingStateFilter.ALL, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetPastOwnerBookings() {

        when(bookingRepository.findByItem_Owner_IdAndEndDateIsBefore(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getOwnerBookings(
                OWNER_USER_ID, BookingStateFilter.PAST, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetCurrentOwnerBookings() {

        when(bookingRepository.findAllCurrentForOwner(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getOwnerBookings(
                OWNER_USER_ID, BookingStateFilter.CURRENT, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetFutureOwnerBookings() {

        when(bookingRepository.findByItem_Owner_IdAndStartDateIsAfter(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getOwnerBookings(
                OWNER_USER_ID, BookingStateFilter.FUTURE, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetRejectedAndWaitingOwnerBookings() {

        when(bookingRepository.findByItem_Owner_IdAndStatusIs(anyLong(), any(), any()))
                .thenAnswer(invocationOnMock -> {
                    PageRequest pageRequest = invocationOnMock.getArgument(2, PageRequest.class);
                    return new PageImpl<>(
                            sourceBookings,
                            pageRequest,
                            sourceBookings.size()
                    );
                });

        var targetBookings = bookingService.getOwnerBookings(
                OWNER_USER_ID, BookingStateFilter.REJECTED, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));

        targetBookings = bookingService.getOwnerBookings(
                OWNER_USER_ID, BookingStateFilter.WAITING, 0, 20);

        checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
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

    private Booking makeBooking(Long id, LocalDateTime start, LocalDateTime end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}