package ru.practicum.shareit.error;

public class EntryAlreadyExistsException extends RuntimeException {
    public EntryAlreadyExistsException(String message) {
        super(message);
    }
}
