package com.urbanojvr.monex.categories.domain.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class InvalidCategoryNameException extends RuntimeException {

    public InvalidCategoryNameException(String name) {
        super("Invalid category name: '%s'".formatted(name));
    }
}
