package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select comment " +
           "from Comment comment " +
           "where comment.item in ?1 " +
           "order by comment.created DESC")
    List<Comment> findCommentsFor(List<Item> items);

    List<Comment> findByItem_Id(Long itemId);
}
