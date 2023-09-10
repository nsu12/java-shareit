package ru.practicum.shareit.booking.model;

import lombok.Data;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "bookings", schema = "public")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // уникальный идентификатор бронирования;

    @Column(name = "start_date")
    private LocalDateTime startDate; // дата и время начала бронирования;

    @Column(name = "end_date")
    private LocalDateTime endDate; // дата и время конца бронирования;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;  // вещь, которую пользователь бронирует;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id")
    private User booker; // пользователь, который осуществляет бронирование;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status; // статус бронирования.
}
