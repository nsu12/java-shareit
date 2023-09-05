package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static List<CommentDto> toCommentDto(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return Collections.emptyList();
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public static Comment toComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setCreated(commentDto.getCreated());
        return comment;
    }

    public static List<Comment> toComment(List<CommentDto> commentDtoList) {
        if (commentDtoList == null || commentDtoList.isEmpty()) return Collections.emptyList();
        return commentDtoList.stream()
                .map(CommentMapper::toComment)
                .collect(Collectors.toList());
    }
}
