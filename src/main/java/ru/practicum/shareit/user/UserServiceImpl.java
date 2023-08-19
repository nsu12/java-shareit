package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.error.EntryNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import javax.validation.ValidationException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(@Valid UserDto user) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(user)));
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntryNotFoundException(String.format("Пользователь с id = %d не найден", id)));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new EntryNotFoundException(String.format("Пользователь с id = %d не найден", id)));

        if (userDto.getName() != null) {
            userToUpdate.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            checkEmail(userDto.getEmail());
            userToUpdate.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userRepository.save(userToUpdate));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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
