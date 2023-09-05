package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private final CommentDto commentDto = new CommentDto(
            1L,
            "some text",
            "some author",
            LocalDateTime.now()
    );

    private final List<ItemDto> itemDtoList = List.of(
            new ItemDto(
                    1L,
                    "first item",
                    "first item desc",
                    true,
                    null,
                    null,
                    List.of(commentDto),
                    null
            ),
            new ItemDto(
                    2L,
                    "second item",
                    "some desc",
                    true,
                    null,
                    null,
                    Collections.emptyList(),
                    null
            )
    );

    @Test
    void shouldCreateItem() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(itemDtoList.get(0));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(new ObjectMapper().writeValueAsString(
                                new ItemInDto("first item", "first item desc", true, null)
                        ))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoList.get(0).getName())))
                .andExpect(jsonPath("$.description", is(itemDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoList.get(0).getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(itemDtoList.get(0).getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(itemDtoList.get(0).getNextBooking())))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(
                        commentDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME)
                )))
                .andExpect(jsonPath("$.requestId", is(itemDtoList.get(0).getRequestId()), Long.class));
    }

    @Test
    void shouldCreateComment() throws Exception {
        when(itemService.createCommentForItem(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(new ObjectMapper().writeValueAsString(
                                new CommentInDto("some text")
                        ))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(
                        commentDto.getCreated().format(DateTimeFormatter.ISO_DATE_TIME)
                )));
    }

    @Test
    void shouldGetAllUserItems() throws Exception {
        when(itemService.getAllItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(itemDtoList);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoList.get(0).getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking", is(itemDtoList.get(0).getLastBooking())))
                .andExpect(jsonPath("$[0].nextBooking", is(itemDtoList.get(0).getNextBooking())))
                .andExpect(jsonPath("$[0].comments", hasSize(itemDtoList.get(0).getComments().size())))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoList.get(0).getRequestId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDtoList.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDtoList.get(1).getName())))
                .andExpect(jsonPath("$[1].description", is(itemDtoList.get(1).getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDtoList.get(1).getAvailable())))
                .andExpect(jsonPath("$[1].lastBooking", is(itemDtoList.get(1).getLastBooking())))
                .andExpect(jsonPath("$[1].nextBooking", is(itemDtoList.get(1).getNextBooking())))
                .andExpect(jsonPath("$[1].comments", hasSize(itemDtoList.get(1).getComments().size())))
                .andExpect(jsonPath("$[1].requestId", is(itemDtoList.get(1).getRequestId()), Long.class));
    }

    @Test
    void shouldGetUserItemById() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDtoList.get(0));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoList.get(0).getName())))
                .andExpect(jsonPath("$.description", is(itemDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoList.get(0).getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(itemDtoList.get(0).getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(itemDtoList.get(0).getNextBooking())))
                .andExpect(jsonPath("$.comments", hasSize(itemDtoList.get(0).getComments().size())))
                .andExpect(jsonPath("$.requestId", is(itemDtoList.get(0).getRequestId()), Long.class));
    }

    @Test
    void shouldSearchItemByName() throws Exception {
        when(itemService.searchItemByName(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDtoList.get(0)));

        mockMvc.perform(get("/items/search?text=text")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoList.get(0).getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking", is(itemDtoList.get(0).getLastBooking())))
                .andExpect(jsonPath("$[0].nextBooking", is(itemDtoList.get(0).getNextBooking())))
                .andExpect(jsonPath("$[0].comments", hasSize(itemDtoList.get(0).getComments().size())))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoList.get(0).getRequestId()), Long.class));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any()))
                .thenReturn(itemDtoList.get(0));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(
                                "{ \"available\" : true }"
                        )
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoList.get(0).getName())))
                .andExpect(jsonPath("$.description", is(itemDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoList.get(0).getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(itemDtoList.get(0).getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(itemDtoList.get(0).getNextBooking())))
                .andExpect(jsonPath("$.comments", hasSize(itemDtoList.get(0).getComments().size())))
                .andExpect(jsonPath("$.requestId", is(itemDtoList.get(0).getRequestId()), Long.class));
    }

    @Test
    void shouldDeleteItem() throws Exception {

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk());
    }
}