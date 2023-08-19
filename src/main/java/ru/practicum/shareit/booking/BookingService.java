package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;

import javax.validation.Valid;
import java.util.List;

@Transactional(readOnly = true)
public interface BookingService {
    BookingDto getBookingById(Long userId, Long bookingId);

    @Transactional
    BookingDto createBooking(Long userId, @Valid BookingInDto booking);

    @Transactional
    BookingDto setBookingApproveStatus(Long userId, Long bookingId, Boolean approved);

    List<BookingDto> getUserBookings(Long userId, String stateFilter);

    List<BookingDto> getOwnerBookings(Long userId, String stateFilter);
}
