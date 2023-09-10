package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStateFilter;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Transactional(readOnly = true)
public interface BookingService {
    BookingDto getBookingById(Long userId, Long bookingId);

    @Transactional
    BookingDto createBooking(Long userId, @Valid BookingInDto booking);

    @Transactional
    BookingDto setBookingApproveStatus(Long userId, Long bookingId, Boolean approved);

    List<BookingDto> getUserBookings(Long userId, BookingStateFilter stateFilter,
                                     @PositiveOrZero Integer from, @Positive Integer size);

    List<BookingDto> getOwnerBookings(Long userId, BookingStateFilter stateFilter,
                                      @PositiveOrZero Integer from, @Positive Integer size);
}
