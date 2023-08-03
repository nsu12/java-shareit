package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.EntryAlreadyExistsException;
import ru.practicum.shareit.error.EntryNotFoundException;

import javax.validation.Valid;
import javax.validation.ValidationException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public User createUser(@Valid User user) {
        if (userStorage.getByEmailOrNull(user.getEmail()) != null) {
            throw new EntryAlreadyExistsException(
                    String.format("Пользователь с email %s уже существует", user.getEmail())
            );
        }
        return userStorage.add(user);
    }

    @Override
    public User getUserById(Integer id) {
        User user = userStorage.getByIdOrNull(id);
        if (user == null) {
            throw new EntryNotFoundException(
                    String.format("Пользователь с id = %d не найден", id)
            );
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    @Override
    public User updateUser(Integer id, User userDto) {
        User userToUpdate = userStorage.getByIdOrNull(id);
        if (userToUpdate == null) {
            throw new EntryNotFoundException(
                    String.format("Пользователь с id = %d не найден", id)
            );
        }

        if (userDto.getName() != null) {
            userToUpdate.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            checkEmail(userDto.getEmail());
            User userWithEmail = userStorage.getByEmailOrNull(userDto.getEmail());
            if (userWithEmail != null && userWithEmail.getId() != userToUpdate.getId()) {
                throw new EntryAlreadyExistsException(
                        String.format("Email %s уже используется", userDto.getEmail())
                );
            }
            userToUpdate.setEmail(userDto.getEmail());
        }

        userStorage.update(userToUpdate);

        return userToUpdate;
    }

    @Override
    public void deleteUser(Integer id) {
        userStorage.delete(id);
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
