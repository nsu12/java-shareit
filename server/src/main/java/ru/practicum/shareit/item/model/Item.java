package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "items", schema = "public")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // уникальный идентификатор вещи;

    @Column(nullable = false)
    private String name;    // краткое название;

    private String description; // развёрнутое описание;

    @Column(name = "is_available")
    private boolean available;  // статус о том, доступна или нет вещь для аренды;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;    // владелец вещи;

    @OneToOne(fetch = FetchType.LAZY)
    private ItemRequest request; /* если вещь была создана по запросу другого пользователя, то в этом
                                    поле будет храниться ссылка на соответствующий запрос
                                  */
}
