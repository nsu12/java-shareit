package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStateFilter;
import ru.practicum.shareit.error.InvalidRequestParamsException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestBody @Valid BookingInDto booking
    ) {
        if (!booking.getEnd().isAfter(booking.getStart())) {
            throw new InvalidRequestParamsException("дата окончания бронирования должна быть после даты начала");
        }
        return bookingClient.createBooking(userId, booking);
    }

    @GetMapping(value = "/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return bookingClient.getUserBookings(
                userId,
                BookingStateFilter.fromString(state).orElseThrow(
                        () -> new InvalidRequestParamsException(String.format("Unknown state: %s", state))
                ),
                from,
                size
        );
    }

    @GetMapping(value = "/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return bookingClient.getOwnerBookings(
                userId,
                BookingStateFilter.fromString(state).orElseThrow(
                        () -> new InvalidRequestParamsException(String.format("Unknown state: %s", state))
                ),
                from,
                size
        );
    }

    @PatchMapping(value = "/{bookingId}")
    public ResponseEntity<Object> setBookingApproveStatus(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam(value = "approved") Boolean approved
    ) {
        return bookingClient.setBookingApproveStatus(userId, bookingId, approved);
    }
}
