package com.urbanojvr.monex.categories.domain.aggregate;

import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryDescription;
import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryId;
import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryName;
import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public final class Category {

	private final CategoryId id;
	private final CategoryName name;
	private final @Nullable CategoryDescription description;
	private final CategoryType type;

	public Category(CategoryId id, CategoryName name, @Nullable CategoryDescription description, CategoryType type) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
		this.description = description;
		this.type = Objects.requireNonNull(type);
	}

	public static Category create(CategoryId id, CategoryName name, @Nullable CategoryDescription description,
			CategoryType type) {
		return new Category(id, name, description, type);
	}

	public Category rename(CategoryName name) {
		Objects.requireNonNull(name);

		return this.toBuilder().name(name).build();
	}

	public Category changeDescription(CategoryDescription description) {
		Objects.requireNonNull(description);

		return this.toBuilder().description(description).build();
	}

	public Category removeDescription() {
		return this.toBuilder().description(null).build();
	}

	public CategoryId id() {
		return this.id;
	}

	public CategoryName name() {
		return this.name;
	}

	public @Nullable CategoryDescription description() {
		return this.description;
	}

	public CategoryType type() {
		return this.type;
	}

	private Builder toBuilder() {
		return new Builder(this);
	}

	private static final class Builder {

		private final CategoryId id;
		private CategoryName name;
		private @Nullable CategoryDescription description;
		private final CategoryType type;

		private Builder(Category source) {
			this.id = source.id;
			this.name = source.name;
			this.description = source.description;
			this.type = source.type;
		}

		private Builder name(CategoryName name) {
			this.name = name;
			return this;
		}

		private Builder description(@Nullable CategoryDescription description) {
			this.description = description;
			return this;
		}

		private Category build() {
			return new Category(this.id, this.name, this.description, this.type);
		}
	}
}