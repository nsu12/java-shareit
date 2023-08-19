package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.exception.AccessViolationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(Long userId, @Valid ItemDto itemDto) {
        User user = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        getUserOrThrow(userId);
        return ItemMapper.toItemDto(itemRepository.findAllByOwner_Id(userId));
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        return ItemMapper.toItemDto(getItemOrThrow(itemId));
    }

    @Override
    public List<ItemDto> searchItemByName(Long userId, String text) {
        if (text.isBlank()) return Collections.emptyList();
        return ItemMapper.toItemDto(itemRepository.searchAvailable(text));
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
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

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public void deleteItem(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        throwIfUserCantEditItem(userId, item);
        itemRepository.delete(item);
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntryNotFoundException(
                                String.format("вещь с id = %d не найдена", itemId)
                        )
                );
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntryNotFoundException(
                                String.format("пользователь с указанным id (%d) не существует", userId)
                        )
                );
    }

    private void throwIfUserCantEditItem(Long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessViolationException("пользователь не может изменить чужую вещь");
        }
    }
}
