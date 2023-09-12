package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingInDto {
    @NotNull(message = "ID вещи должен быть задан")
    private Long itemId;
    @NotNull(message = "Дата начала бронирования должна быть задана")
    @FutureOrPresent(message = "Дата начала бронирования не должна быть в прошлом")
    private LocalDateTime start;
    @NotNull(message = "Дата окончания бронирования должна быть задана")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;
}
