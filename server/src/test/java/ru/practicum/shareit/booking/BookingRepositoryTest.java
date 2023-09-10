package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepository bookingRepository;
    private List<User> users;
    private List<Item> items;
    private List<Booking> bookings;

    @BeforeEach
    void setUp() {
        users = List.of(
                makeUser("John Doe", "john.doe@gmail.com"),
                makeUser("Will Smith", "will.smith@gmail.com"),
                makeUser("The Owner", "owner@gmail.com")
        );

        items = List.of(
                makeItem("thing", "desc", users.get(2)),
                makeItem("another thing", "desc", users.get(2))
        );

        bookings = List.of(
                // current for item(0) and user(0)
                makeBooking(
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusHours(2),
                        items.get(0),
                        users.get(0)
                ),
                // next to for item(0) and user(0)
                makeBooking(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        items.get(0),
                        users.get(0)
                ),
                // current for item(1) and user(1)
                makeBooking(
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusDays(1),
                        items.get(1),
                        users.get(1)
                ),
                // next to for item(0) and user(1) not approved
                makeBooking(
                        LocalDateTime.now().plusDays(3),
                        LocalDateTime.now().plusDays(4),
                        items.get(0),
                        users.get(1)
                )
        );
        bookings.get(3).setStatus(BookingStatus.WAITING);

        em.flush();
    }

    @Test
    void shouldFindAllCurrentForBooker() {

        var result = bookingRepository.findAllCurrentForBooker(
                users.get(0).getId(), LocalDateTime.now(), PageRequest.of(0, 10)
        );

        assertThat(result, notNullValue());

        var targetBookings = result.toList();
        var sourceBookings = List.of(bookings.get(0));

        BookingServiceImplTest.checkResult(
                BookingMapper.toBookingDto(targetBookings), BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldFindAllCurrentForOwner() {

        var result = bookingRepository.findAllCurrentForOwner(
                users.get(2).getId(), LocalDateTime.now(), PageRequest.of(0, 10)
        );

        assertThat(result, notNullValue());

        var targetBookings = result.toList();
        var sourceBookings = List.of(bookings.get(0), bookings.get(2));

        BookingServiceImplTest.checkResult(
                BookingMapper.toBookingDto(targetBookings), BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldFindNextBookingsForItems() {

        var targetBookings = bookingRepository.findNextBookingsFor(items, LocalDateTime.now());

        var sourceBookings = List.of(bookings.get(1));

        BookingServiceImplTest.checkResult(
                BookingMapper.toBookingDto(targetBookings), BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldFindLastBookingsForItems() {

        var targetBookings = bookingRepository.findLastBookingsFor(items, LocalDateTime.now());

        var sourceBookings = List.of(bookings.get(0), bookings.get(2));

        BookingServiceImplTest.checkResult(
                BookingMapper.toBookingDto(targetBookings), BookingMapper.toBookingDto(sourceBookings));
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