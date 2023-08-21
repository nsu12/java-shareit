package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_IdAndEndDateIsBefore(Long bookerId, LocalDateTime date, Sort sort);

    @Query("select booking " +
            "from Booking booking " +
            "where booking.booker.id = ?1 and booking.startDate < ?2 and booking.endDate > ?2")
    List<Booking> findAllCurrentForBooker(Long userId, LocalDateTime now, Sort sort);

    List<Booking> findByBooker_IdAndStartDateIsAfter(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findByBooker_Id(Long userId, Sort sort);

    List<Booking> findByBooker_IdAndStatusIs(Long userId, BookingStatus status, Sort sort);

    List<Booking> findByItem_Owner_IdAndEndDateIsBefore(Long userId, LocalDateTime date, Sort sort);

    @Query("select booking " +
            "from Booking booking " +
            "where booking.item.owner.id = ?1 and booking.startDate < ?2 and booking.endDate > ?2")
    List<Booking> findAllCurrentForOwner(Long userId, LocalDateTime date, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartDateIsAfter(Long userId, LocalDateTime date, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatusIs(Long userId, BookingStatus status, Sort sort);

    List<Booking> findByItem_Owner_Id(Long userId, Sort sort);

    @Query(
            "select booking " +
            "from Booking booking " +
            "where booking.item in ?1 and booking.startDate > ?2 and booking.status = 'APPROVED' " +
            "order by booking.item.id DESC, booking.startDate ASC"
    )
    List<Booking> findNextBookingsFor(List<Item> items, LocalDateTime date);

    @Query(
            "select booking " +
            "from Booking booking " +
            "where booking.item in ?1 and booking.startDate < ?2 and booking.status = 'APPROVED'" +
            "order by booking.item.id DESC, booking.startDate DESC"
    )
    List<Booking> findLastBookingsFor(List<Item> items, LocalDateTime date);

    List<Booking> findByBooker_IdAndItem_Id(Long userId, Long itemId, Sort sort);
}
