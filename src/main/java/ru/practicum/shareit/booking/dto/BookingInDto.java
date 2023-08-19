package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.validator.NotInPast;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BookingInDto {
    @NotNull(message = "ID вещи должен быть задан")
    private Long itemId;
    @NotNull(message = "Дата начала бронирования должна быть задана")
    @NotInPast(message = "Дата начала бронирования не должна быть в прошлом")
    private LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования должна быть задана")
    @NotInPast(message = "Дата окончания бронирования не должна быть в прошлом")
    private LocalDateTime end;
}
