package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByOwner_Id(Long userId, PageRequest pageRequest);

    @Query("select i from Item i " +
           "where i.available = TRUE and " +
           "(upper(i.name) like upper(concat('%', ?1, '%')) " +
           "or upper(i.description) like upper(concat('%', ?1, '%')))")
    Page<Item> searchAvailable(String text, PageRequest of);

    @Query("select i from Item i " +
           "where i.request in ?1 " +
           "order by i.id ASC")
    List<Item> findItemsFor(List<ItemRequest> requests);

    List<Item> findAllByRequest_Id(Long requestId);
}
