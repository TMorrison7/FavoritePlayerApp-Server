package app.dto.userdto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavoritePlayersRequestTest {

    private static Validator validator;
    private static AutoCloseable validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory::close;
    }

    @AfterAll
    static void closeValidatorFactory() throws Exception {
        validatorFactory.close();
    }

    @Test
    void acceptsNonEmptyListOfPlayerIds() {
        assertTrue(validator.validate(new FavoritePlayersRequest(List.of(10L, 20L))).isEmpty());
    }

    @Test
    void rejectsEmptyList() {
        assertEquals(1, validator.validate(new FavoritePlayersRequest(List.of())).size());
    }

    @Test
    void rejectsNullPlayerId() {
        var request = new FavoritePlayersRequest(Arrays.asList(10L, null));

        assertEquals(1, validator.validate(request).size());
    }
}
