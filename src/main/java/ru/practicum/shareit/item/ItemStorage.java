package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item add(Item item);

    Item getByIdOrNull(int itemId);

    List<Item> getAllByOwnerId(int ownerId);

    List<Item> searchByName(String text);

    void update(Item item);

    void delete(int itemId);
}
