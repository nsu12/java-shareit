package ru.practicum.shareit.request.model;

import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "requests", schema = "public")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // уникальный идентификатор запроса;

    @Column(nullable = false)
    private String description; // текст запроса, содержащий описание требуемой вещи;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // пользователь, создавший запрос;

    @Column(name = "create_date")
    private LocalDateTime created; // дата и время создания запроса.
}
