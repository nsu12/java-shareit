package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestBody BookingInDto booking
    ) {
        return bookingService.createBooking(userId, booking);
    }

    @GetMapping(value = "/{bookingId}")
    public BookingDto getBookingById(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state
    ) {
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping(value = "/owner")
    public List<BookingDto> getOwnerBookings(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state
    ) {
        return bookingService.getOwnerBookings(userId, state);
    }

    @PatchMapping(value = "/{bookingId}")
    public BookingDto setBookingApproveStatus(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam(value = "approved") Boolean approved
    ) {
        return bookingService.setBookingApproveStatus(userId, bookingId, approved);
    }
}
