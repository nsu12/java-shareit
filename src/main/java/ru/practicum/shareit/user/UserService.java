package ru.practicum.shareit.user;

import javax.validation.Valid;
import java.util.List;

interface UserService {
    User createUser(@Valid User user);

    User getUserById(Integer id);

    List<User> getAllUsers();

    User updateUser(Integer id, User user);

    void deleteUser(Integer id);
}
