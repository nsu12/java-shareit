package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping(value = "/{userId}")
    public User get(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PatchMapping(value = "/{userId}")
    public User update(@PathVariable Integer userId, @RequestBody User user) {
        return userService.updateUser(userId, user);
    }

    @DeleteMapping(value = "/{userId}")
    public void delete(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }
}
