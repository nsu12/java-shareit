package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

@Data
@Builder
public class Item {
    private int id;   // уникальный идентификатор вещи;
    private String name;    // краткое название;
    private String description; // развёрнутое описание;
    private boolean available;  // статус о том, доступна или нет вещь для аренды;
    private int owner;    // владелец вещи;
    private ItemRequest request; /* если вещь была создана по запросу другого пользователя, то в этом
                                    поле будет храниться ссылка на соответствующий запрос
                                  */
}
