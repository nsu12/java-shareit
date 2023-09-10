package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService itemRequestService;
    private final List<ItemRequestDto> itemRequestDtoList = List.of(
            new ItemRequestDto(
                    1L,
                    "thing one request",
                    LocalDateTime.now(),
                    null
            ),
            new ItemRequestDto(
                    1L,
                    "thing two request",
                    LocalDateTime.now(),
                    null
            )
    );

    @Test
    void shouldCreateItemRequest() throws Exception {
        when(itemRequestService.createItemRequest(anyLong(), any()))
                .thenReturn(itemRequestDtoList.get(0));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(new ObjectMapper().writeValueAsString(new ItemRequestInDto("something")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDtoList.get(0).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.items", is(itemRequestDtoList.get(0).getItems())));
    }

    @Test
    void shouldGetUserRequests() throws Exception {
        when(itemRequestService.getRequestsByOwner(anyLong()))
                .thenReturn(List.of(itemRequestDtoList.get(0)));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDtoList.get(0).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", is(itemRequestDtoList.get(0).getItems())));
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(itemRequestDtoList);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequestDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDtoList.get(0).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", is(itemRequestDtoList.get(0).getItems())))
                .andExpect(jsonPath("$[1].id", is(itemRequestDtoList.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequestDtoList.get(1).getDescription())))
                .andExpect(jsonPath("$[1].created", is(itemRequestDtoList.get(1).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[1].items", is(itemRequestDtoList.get(1).getItems())));
    }

    @Test
    void shouldGetOneRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestDtoList.get(0));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDtoList.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDtoList.get(0).getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDtoList.get(0).getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.items", is(itemRequestDtoList.get(0).getItems())));
    }
}