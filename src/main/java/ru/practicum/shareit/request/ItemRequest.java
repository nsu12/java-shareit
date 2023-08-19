package ru.practicum.shareit.request;

import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.user.User;

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
    private User requester; // пользователь, создавший запрос;

    private LocalDateTime created; // дата и время создания запроса.
}
