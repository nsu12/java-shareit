package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingStateFilter;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private EntityManager em;
    private List<User> users;
    private List<Booking> sourceBookings;

    @BeforeEach
    void setUp() {
        users = List.of(
                makeUser("John Doe", "john.doe@gmail.com"),
                makeUser("Will Smith", "will.smith@gmail.com"),
                makeUser("The Owner", "owner@gmail.com")
        );

        sourceBookings = List.of(
                makeBooking(
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        makeItem("thing", "desc", users.get(2)),
                        users.get(0)
                ),
                makeBooking(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusDays(1),
                        makeItem("another thing", "desc", users.get(2)),
                        users.get(0)
                )
        );
        em.flush();
    }

    @Test
    void shouldGetAllUserBookings() {

        var targetBookings = bookingService.getUserBookings(
                users.get(0).getId(), BookingStateFilter.ALL, 0, 20);

        BookingServiceImplTest.checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
    }

    @Test
    void shouldGetAllOwnerBookings() {

        var targetBookings = bookingService.getOwnerBookings(
                users.get(2).getId(), BookingStateFilter.ALL, 0, 20);

        BookingServiceImplTest.checkResult(targetBookings, BookingMapper.toBookingDto(sourceBookings));
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
        booking.setStatus(BookingStatus.WAITING);
        em.persist(booking);
        return booking;
    }
}