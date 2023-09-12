package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@Transactional
@SpringBootTest
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager em;

    @Test
    void shouldCreateUser() {

        UserDto createdUser = userService.createUser(
                new UserDto(1L, "John Doe", "john.doe@gmail.com")
        );

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User savedUser = query.setParameter("id", createdUser.getId()).getSingleResult();

        UserServiceImplTest.checkResult(createdUser, UserMapper.toUserDto(savedUser));
    }

    @Test
    void shouldGetUserById() {

        User sourceUser = makeUser("John Doe", "john.doe@gmail.com");

        UserDto resultUserDto = userService.getUserById(sourceUser.getId());

        UserServiceImplTest.checkResult(UserMapper.toUserDto(sourceUser), resultUserDto);
    }

    @Test
    void shouldGetAllUsers() {
        List<User> sourceUsers = List.of(
                makeUser("John Doe", "john.doe@gmail.com"),
                makeUser("Will Smith", "will.smith@gmail.com")
        );

        var targetUserDtos = userService.getAllUsers();

        UserServiceImplTest.checkResult(UserMapper.toUserDto(sourceUsers), targetUserDtos);
    }

    @Test
    void deleteUser() {

        User user = makeUser("John Doe", "john.doe@gmail.com");

        userService.deleteUser(user.getId());

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        List<User> result = query.getResultList();

        assertThat(result, notNullValue());
        assertThat(result, hasSize(0));
    }

    private User makeUser(String name, String desc) {
        User user = new User();
        user.setName(name);
        user.setEmail(desc);
        em.persist(user);
        em.flush();
        return user;
    }
}