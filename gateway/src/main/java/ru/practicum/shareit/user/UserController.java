package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid UserDto user) {
        return userClient.createUser(user);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return userClient.getAllUsers();
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> get(@PathVariable Long userId) {
        return userClient.getUserById(userId);
    }

    @PatchMapping(value = "/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId, @RequestBody UserDto user) {
        if (user.getEmail() != null) {
            checkEmail(user.getEmail());
        }
        return userClient.updateUser(userId, user);
    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        return userClient.deleteUser(userId);
    }

    private void checkEmail(String email) {
        // using validation from https://www.baeldung.com/java-email-validation-regex
        Pattern pattern = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9\\\\+_-]+(\\.[A-Za-z0-9\\\\+_-]+)*@"
                + "[^-][A-Za-z0-9\\\\+-]+(\\.[A-Za-z0-9\\\\+-]+)*(\\.[A-Za-z]{2,})$");
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new ValidationException("Адрес должен иметь формат электронной почты");
        }
    }
}
