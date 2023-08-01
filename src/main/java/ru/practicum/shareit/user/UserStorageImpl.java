package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStorageImpl implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User add(User newUser) {
        newUser.setId(getId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User getByIdOrNull(int id) {
        return users.get(id);
    }

    @Override
    public User getByEmailOrNull(String email) {
        List<User> result = users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .collect(Collectors.toList());
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void update(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    private int getId() {
        return nextId++;
    }
}
