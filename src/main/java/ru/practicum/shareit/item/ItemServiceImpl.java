package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.error.InvalidRequestParamsException;
import ru.practicum.shareit.exception.AccessViolationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto createItem(Long userId, @Valid ItemInDto itemDto) {
        User user = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public CommentDto createCommentForItem(Long userId, Long itemId, @Valid CommentDto commentDto) {
        User user = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        List<Booking> bookings = bookingRepository.findByBooker_IdAndItem_Id(
                userId, itemId, Sort.by(Sort.Direction.ASC, "startDate")
        );
        if (bookings.isEmpty() || bookings.get(0).getStartDate().isAfter(LocalDateTime.now())) {
            throw new InvalidRequestParamsException(
                    String.format("пользователь %d еще не брал вещь %d в аренду", userId, itemId)
            );
        }
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        getUserOrThrow(userId);
        return makeItemDtosWithBookingsAndComments(itemRepository.findAllByOwner_Id(userId));
    }

    private List<ItemDto> makeItemDtosWithBookingsAndComments(List<Item> items) {
        Map<Long, BookingShortDto> nextBookingsForItems =
                bookingRepository.findNextBookingsFor(items, LocalDateTime.now()).stream()
                        .collect(Collectors.toMap(
                                booking -> booking.getItem().getId(),
                                BookingMapper::toBookingShortDto,
                                (existing, replacement) -> existing)
                        );
        Map<Long, BookingShortDto> lastBookingsForItems =
                bookingRepository.findLastBookingsFor(items, LocalDateTime.now()).stream()
                        .collect(Collectors.toMap(
                                booking -> booking.getItem().getId(),
                                BookingMapper::toBookingShortDto,
                                (existing, replacement) -> existing)
                        );
        Map<Long, List<Comment>> commentsForItems = commentRepository.findCommentsFor(items).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        return ItemMapper.toItemDto(items).stream()
                .peek(itemDto -> {
                    itemDto.setLastBooking(lastBookingsForItems.get(itemDto.getId()));
                    itemDto.setNextBooking(nextBookingsForItems.get(itemDto.getId()));
                    itemDto.setComments(CommentMapper.toCommentDto(commentsForItems.get(itemDto.getId())));
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        if (item.getOwner().getId().equals(userId)) {
            return makeItemDtosWithBookingsAndComments(List.of(item)).get(0);
        } else {
            List<Comment> commentsForItem = commentRepository.findByItem_Id(itemId);
            ItemDto itemDto = ItemMapper.toItemDto(item);
            itemDto.setComments(CommentMapper.toCommentDto(commentsForItem));
            return itemDto;
        }
    }

    @Override
    public List<ItemDto> searchItemByName(Long userId, String text) {
        if (text.isBlank()) return Collections.emptyList();
        return ItemMapper.toItemDto(itemRepository.searchAvailable(text));
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemInDto itemDto) {
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
