package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.exception.AccessViolationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto createItem(Integer userId, @Valid ItemDto itemDto) {
        throwIfUserNotExists(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        return ItemMapper.toItemDto(itemStorage.add(item));
    }

    @Override
    public List<ItemDto> getAllItems(Integer userId) {
        throwIfUserNotExists(userId);
        return itemStorage.getAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Integer userId, Integer itemId) {
        Item item = getItemOrThrow(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> searchItemByName(Integer userId, String namePart) {
        return itemStorage.searchByName(namePart).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto updateItem(Integer userId, Integer itemId, ItemDto itemDto) {
        Item item = getItemOrThrow(itemId);
        throwIfUserCantEditItem(userId, item);

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        itemStorage.update(item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public void deleteItem(Integer userId, Integer itemId) {
        Item item = getItemOrThrow(itemId);
        throwIfUserCantEditItem(userId, item);
        itemStorage.delete(itemId);
    }

    private Item getItemOrThrow(Integer itemId) {
        Item item = itemStorage.getByIdOrNull(itemId);
        if (item == null) {
            throw new EntryNotFoundException(
                    String.format("вещь с id = %d не найдена", itemId)
            );
        }
        return item;
    }

    private void throwIfUserNotExists(Integer userId) {
        if (userStorage.getByIdOrNull(userId) == null) {
            throw new EntryNotFoundException(
                    String.format("пользователь с указанным id (%d) не существует", userId)
            );
        }
    }

    private void throwIfUserCantEditItem(Integer userId, Item item) {
        if (item.getOwner() != userId) {
            throw new AccessViolationException("пользователь не может изменить чужую вещь");
        }
    }
}
