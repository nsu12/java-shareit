package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {

    /**
     * @param user добавляет пользователя в хранилище
     * @return модифицированный объект User в случае успешного добавления, либо null в случае ошибки
     */
    User add(User user);

    User getByIdOrNull(int id);

    User getByEmailOrNull(String email);

    List<User> getAll();

    void update(User user);

    void delete(int id);
}
