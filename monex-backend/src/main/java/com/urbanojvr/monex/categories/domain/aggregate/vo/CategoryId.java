package com.urbanojvr.monex.categories.domain.aggregate.vo;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

@NullMarked
public record CategoryId(UUID value) {

    public CategoryId {
        Objects.requireNonNull(value);
    }
}
