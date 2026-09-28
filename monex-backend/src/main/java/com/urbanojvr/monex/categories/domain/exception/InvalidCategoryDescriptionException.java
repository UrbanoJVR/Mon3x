package com.urbanojvr.monex.categories.domain.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class InvalidCategoryDescriptionException extends RuntimeException {

    public InvalidCategoryDescriptionException(String description) {
        super("Invalid category description: '%s'".formatted(description));
    }
}
