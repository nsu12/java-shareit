package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ItemStorageImpl implements ItemStorage {

    private final Map<Integer, Item> items = new HashMap<>();
    private int nextId = 1;

    @Override
    public Item add(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getByIdOrNull(int itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllByOwnerId(int ownerId) {
       return items.values().stream()
                .filter(item -> item.getOwner() == ownerId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchByName(String text) {
        if (text.isBlank()) return Collections.emptyList();
        String lcText = text.toLowerCase();
        return items.values().stream()
                .filter(
                        item -> item.isAvailable() &&
                                (item.getName().toLowerCase().contains(lcText) ||
                                        item.getDescription().toLowerCase().contains(lcText))
                )
                .collect(Collectors.toList());
    }

    @Override
    public void update(Item item) {
        items.put(item.getId(), item);
    }

    @Override
    public void delete(int itemId) {
        items.remove(itemId);
    }

    private int getId() {
        return nextId++;
    }
}
