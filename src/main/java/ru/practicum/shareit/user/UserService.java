package ru.practicum.shareit.user;

import javax.validation.Valid;
import java.util.List;

interface UserService {
    User createOrThrow(@Valid User user);

    User getOrThrow(Integer id);

    List<User> getAll();

    User updateOrThrow(Integer id, User user);

    void delete(Integer id);
}
