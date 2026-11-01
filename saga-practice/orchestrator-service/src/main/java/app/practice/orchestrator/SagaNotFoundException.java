package app.practice.orchestrator;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SagaNotFoundException extends RuntimeException {

    public SagaNotFoundException(String sagaId) {
        super("Saga not found: " + sagaId);
    }
}
