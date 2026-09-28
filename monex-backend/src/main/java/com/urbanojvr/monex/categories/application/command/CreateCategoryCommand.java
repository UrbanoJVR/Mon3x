package com.urbanojvr.monex.categories.application.command;

import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryDescription;
import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryName;
import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public record CreateCategoryCommand(
        CategoryName name,
        @Nullable CategoryDescription description,
        CategoryType type
) {

    public CreateCategoryCommand {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
    }
}
