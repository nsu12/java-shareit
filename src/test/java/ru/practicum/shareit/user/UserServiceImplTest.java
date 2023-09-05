package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;


import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // given
        UserDto userDto = new UserDto( 1L, "John Doe", "john.doe@gmail.com");
        when(userRepository.save(any())).thenReturn(UserMapper.toUser(userDto));

        //
        UserDto resultDto = userService.createUser(userDto);

        checkResult(userDto, resultDto);
    }

    @Test
    void shouldGetUserById() {
        UserDto userDto = new UserDto( 1L, "John Doe", "john.doe@gmail.com");

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));

        UserDto resultDto = userService.getUserById(1L);

        checkResult(userDto, resultDto);
    }

    @Test
    void shouldThrowIfUserNotExists() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        assertThat(exception.getMessage(), is(notNullValue()));
    }

    @Test
    void shouldGetAllUsers() {
        List<UserDto> userDtoList = List.of(
                new UserDto(1L, "John Doe", "john.doe@gmail.com"),
                new UserDto(2L, "Will Smith", "will.smith@gmail.com")
        );

        when(userRepository.findAll()).thenReturn(UserMapper.toUser(userDtoList));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(userDtoList.size()));

        for (int i = 0; i < userDtoList.size(); i++) {
            assertThat(result.get(i).getId(), is(userDtoList.get(i).getId()));
            assertThat(result.get(i).getName(), is(userDtoList.get(i).getName()));
            assertThat(result.get(i).getEmail(), is(userDtoList.get(i).getEmail()));
        }
    }

    @Test
    void shouldUpdateUser() {
        UserDto userDto = new UserDto( 1L, "John Doe", "john.doe@gmail.com");

        when(userRepository.save(any())).thenReturn(UserMapper.toUser(userDto));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));

        UserDto resultDto = userService.updateUser(1L, userDto);

        checkResult(userDto, resultDto);
    }

    private static void checkResult(UserDto userDto, UserDto resultDto) {
        assertThat(resultDto, is(notNullValue()));
        assertThat(resultDto.getId(), is(userDto.getId()));
        assertThat(resultDto.getName(), is(userDto.getName()));
        assertThat(resultDto.getEmail(), is(userDto.getEmail()));
    }
}