package ru.practicum.shareit.error;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ErrorResponseTest {

    @Autowired
    JacksonTester<ErrorResponse> jacksonTester;

    @Test
    void shouldSerializeInCorrectFormat() throws IOException {
        ErrorResponse response = new ErrorResponse("test message");

        JsonContent<ErrorResponse> json = jacksonTester.write(response);

        assertThat(json).extractingJsonPathStringValue("$.error").isEqualTo("test message");
    }

}