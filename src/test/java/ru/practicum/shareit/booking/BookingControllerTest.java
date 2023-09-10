package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.error.ItemNotAvailableException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

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

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;


    private final List<ItemDto> itemDtoList = List.of(
            new ItemDto(
                    1L,
                    "thing",
                    "thing",
                    false,
                    null,
                    new BookingShortDto(2L, 2L),
                    Collections.emptyList(),
                    null
            ),
            new ItemDto(
                    2L,
                    "another thing",
                    "thing 2",
                    false,
                    null,
                    null,
                    Collections.emptyList(),
                    null
            )
    );

    private final List<UserDto> userDtoList = List.of(
            new UserDto(1L, "John Doe", "john.doe@gmail.com"),
            new UserDto(2L, "Will Smith", "will.smith@gmail.com"),
            new UserDto(3L, "The Owner", "owner@gmail.com")
    );

    private final List<BookingDto> bookingDtoList = List.of(
            new BookingDto(
                    1L,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1),
                    BookingStatus.APPROVED,
                    itemDtoList.get(0),
                    userDtoList.get(0)
            ),
            new BookingDto(
                    2L,
                    LocalDateTime.now().plusDays(2),
                    LocalDateTime.now().plusDays(3),
                    BookingStatus.APPROVED,
                    itemDtoList.get(0),
                    userDtoList.get(1)
            ),
            new BookingDto(
                    3L,
                    LocalDateTime.now().plusDays(2),
                    LocalDateTime.now().plusDays(3),
                    BookingStatus.APPROVED,
                    itemDtoList.get(1),
                    userDtoList.get(1)
            )
    );

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.createBooking(anyLong(), any()))
                .thenReturn(bookingDtoList.get(0));

        var result = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsString(
                                        new BookingInDto(
                                                1L,
                                                bookingDtoList.get(0).getStart(),
                                                bookingDtoList.get(0).getEnd()
                                        )
                                ))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        checkOneBooking(result, bookingDtoList.get(0));
    }

    @Test
    void shouldGetErrorOnCreateBookingWhenNoItemAvailable() throws Exception {
        when(bookingService.createBooking(anyLong(), any()))
                .thenThrow(ItemNotAvailableException.class);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsString(
                                        new BookingInDto(
                                                99L,
                                                LocalDateTime.now().plusSeconds(10),
                                                LocalDateTime.now().plusDays(1)
                                        )
                                ))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    private void checkOneBooking(ResultActions result, BookingDto bookingDto) throws Exception {
        result
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME)))
                )
                .andExpect(jsonPath("$.end",
                        is(bookingDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME)))
                )
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())))
                .andExpect(jsonPath("$.item").isNotEmpty())
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker").isNotEmpty())
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingDto.getBooker().getEmail())));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDtoList.get(0));

        var result = mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L)
                )
                .andExpect(status().isOk());
        checkOneBooking(result, bookingDtoList.get(0));
    }

    @Test
    void shouldGetErrorOnGetBookingByIdWhenNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(EntryNotFoundException.class);

        mockMvc.perform(get("/bookings/99")
                        .header("X-Sharer-User-Id", 3L)
                )
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void shouldGetUserBookings() throws Exception {
        when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDtoList.get(1), bookingDtoList.get(2)));

        var result = mockMvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 2L)
                )
                .andExpect(status().isOk());

        checkListOfBookings(result, List.of(bookingDtoList.get(1), bookingDtoList.get(2)));
    }

    @Test
    void shouldGetErrorIfWrongStateWhenGetUserBookings() throws Exception {
        when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDtoList.get(1), bookingDtoList.get(2)));

        mockMvc.perform(get("/bookings?state=WRONG_STATE")
                        .header("X-Sharer-User-Id", 2L)
                )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error", is("Unknown state: WRONG_STATE")));
    }

    private static void checkListOfBookings(ResultActions result, List<BookingDto> bookingDtoList) throws Exception {
        result.andExpect(jsonPath("$", hasSize(bookingDtoList.size())));
        for (int i = 0; i < bookingDtoList.size(); i++) {

            BookingDto bookingDto = bookingDtoList.get(i);
            result
                    .andExpect(jsonPath(String.format("$[%d].id", i), is(bookingDto.getId()), Long.class))
                    .andExpect(jsonPath(
                                    String.format("$[%d].start", i),
                                    is(bookingDto.getStart().format(DateTimeFormatter.ISO_DATE_TIME))
                            )
                    )
                    .andExpect(jsonPath(
                                    String.format("$[%d].end", i),
                                    is(bookingDto.getEnd().format(DateTimeFormatter.ISO_DATE_TIME))
                            )
                    )
                    .andExpect(jsonPath(
                                    String.format("$[%d].status", i),
                                    is(bookingDto.getStatus().name())
                            )
                    )
                    .andExpect(jsonPath(String.format("$[%d].item", i)).isNotEmpty())
                    .andExpect(jsonPath(
                                    String.format("$[%d].item.id", i),
                                    is(bookingDto.getItem().getId()),
                                    Long.class
                            )
                    )
                    .andExpect(jsonPath(
                                    String.format("$[%d].item.name", i),
                                    is(bookingDto.getItem().getName())
                            )
                    )
                    .andExpect(jsonPath(String.format("$[%d].booker", i)).isNotEmpty())
                    .andExpect(jsonPath(
                                    String.format("$[%d].booker.id", i),
                                    is(bookingDto.getBooker().getId()),
                                    Long.class
                            )
                    )
                    .andExpect(jsonPath(
                                    String.format("$[%d].booker.name", i),
                                    is(bookingDto.getBooker().getName())
                            )
                    )
                    .andExpect(jsonPath(
                                    String.format("$[%d].booker.email", i),
                                    is(bookingDto.getBooker().getEmail())
                            )
                    );
        }
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(bookingDtoList);

        var result = mockMvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", 3L)
                )
                .andExpect(status().isOk());

        checkListOfBookings(result, bookingDtoList);
    }

    @Test
    void shouldGetErrorIfWrongStateWhenGetOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(bookingDtoList);

        mockMvc.perform(get("/bookings/owner?state=WRONG_STATE")
                        .header("X-Sharer-User-Id", 3L)
                )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error", is("Unknown state: WRONG_STATE")));
    }

    @Test
    void shouldSetBookingApproveStatus() throws Exception {
        when(bookingService.setBookingApproveStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDtoList.get(0));

        var result = mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L)
                )
                .andExpect(status().isOk());
        checkOneBooking(result, bookingDtoList.get(0));
    }
}