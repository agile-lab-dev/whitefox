package io.whitefox.core.types.predicates;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;

public class PredicateParsingException extends PredicateException{

    private final JsonProcessingException cause;

    public PredicateParsingException(JsonProcessingException cause) {
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return "Parsing of predicate failed due to: " + cause.getMessage() + "\n Stack trace: " + Arrays.toString(cause.getStackTrace());
    }
}

