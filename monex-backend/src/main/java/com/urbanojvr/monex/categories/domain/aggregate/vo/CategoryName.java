package com.urbanojvr.monex.categories.domain.aggregate.vo;

import com.urbanojvr.monex.categories.domain.exception.InvalidCategoryNameException;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record CategoryName(String value) {

    private static final int MAX_LENGTH = 30;

    public CategoryName {
        Objects.requireNonNull(value);

        if (value.isBlank() || value.length() > MAX_LENGTH) {
            throw new InvalidCategoryNameException(value);
        }
    }
}
