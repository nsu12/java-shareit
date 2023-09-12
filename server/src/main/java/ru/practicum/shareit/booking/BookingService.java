package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStateFilter;

import java.util.List;

@Transactional(readOnly = true)
public interface BookingService {
    BookingDto getBookingById(Long userId, Long bookingId);

    @Transactional
    BookingDto createBooking(Long userId, BookingInDto booking);

    @Transactional
    BookingDto setBookingApproveStatus(Long userId, Long bookingId, Boolean approved);

    List<BookingDto> getUserBookings(Long userId, BookingStateFilter stateFilter,
                                     Integer from, Integer size);

    List<BookingDto> getOwnerBookings(Long userId, BookingStateFilter stateFilter,
                                      Integer from, Integer size);
}
