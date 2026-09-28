package com.urbanojvr.monex.categories.domain.aggregate.vo;

import com.urbanojvr.monex.categories.domain.exception.InvalidCategoryDescriptionException;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record CategoryDescription(String value) {

	private static final int MAX_LENGTH = 200;

	public CategoryDescription {
		Objects.requireNonNull(value);

		value = value.trim();

		if (value.isBlank() || value.length() > MAX_LENGTH) {
			throw new InvalidCategoryDescriptionException(value);
		}
	}
}
