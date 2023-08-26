package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingStateFilter {
    ALL,
    PAST,
    CURRENT,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingStateFilter> fromString(String string) {
        for (BookingStateFilter state: BookingStateFilter.values()) {
            if (state.name().equals(string))
                return Optional.of(state);
        }
        return Optional.empty();
    }
}
