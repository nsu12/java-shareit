package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker())
        );
    }

    public static List<BookingDto> toBookingDto(List<Booking> bookings)  {
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        return new BookingShortDto(
                booking.getId(), booking.getBooker().getId()
        );
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStartDate(bookingDto.getStart());
        booking.setEndDate(bookingDto.getEnd());
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }

    public static List<Booking> toBooking(List<BookingDto> bookingDtoList) {
        return bookingDtoList.stream()
                .map(BookingMapper::toBooking)
                .collect(Collectors.toList());
    }
}
