package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
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
        UserDto userDto = new UserDto(1L, "John Doe", "john.doe@gmail.com");

        //
        when(userRepository.save(any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        UserDto resultDto = userService.createUser(userDto);

        checkResult(userDto, resultDto);
    }

    @Test
    void shouldGetUserById() {
        User user = makeUser(1L, "John Doe", "john.doe@gmail.com");

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        UserDto resultDto = userService.getUserById(1L);

        checkResult(UserMapper.toUserDto(user), resultDto);
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
        List<User> userList = List.of(
                makeUser(1L, "John Doe", "john.doe@gmail.com"),
                makeUser(2L, "Will Smith", "will.smith@gmail.com")
        );

        when(userRepository.findAll()).thenReturn(userList);

        List<UserDto> resultList = userService.getAllUsers();

        checkResult(UserMapper.toUserDto(userList), resultList);
    }

    @Test
    void shouldUpdateUser() {
        UserDto updatedUserDto = new UserDto(1L, "new name", "new@email.com");

        when(userRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(makeUser(1L, "John Doe", "john.doe@gmail.com")));

        UserDto resultDto = userService.updateUser(1L, updatedUserDto);

        checkResult(updatedUserDto, resultDto);
    }

    @Test
    void shouldThrowOnUpdateUserWhenWrongEmail() {

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(makeUser(1L, "John Doe", "john.doe@gmail.com")));

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.updateUser(1L, new UserDto(1L, "", "bad email.com"))
        );

        assertThat(exception.getMessage(), is(notNullValue()));
    }

    @Test
    void shouldThrowOnUpdateUserWhenNoUserExist() {

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        final EntryNotFoundException exception = assertThrows(
                EntryNotFoundException.class,
                () -> userService.updateUser(99L, new UserDto(1L, "", "bad email.com"))
        );

        assertThat(exception.getMessage(), is(notNullValue()));
    }

    static void checkResult(UserDto userDto, UserDto resultDto) {
        assertThat(resultDto, is(notNullValue()));
        assertThat(resultDto.getId(), is(userDto.getId()));
        assertThat(resultDto.getName(), is(userDto.getName()));
        assertThat(resultDto.getEmail(), is(userDto.getEmail()));
    }

    static void checkResult(List<UserDto> sourceUsers, List<UserDto> targetUsers) {
        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (var sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", equalTo(sourceUser.getId())),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    private User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}