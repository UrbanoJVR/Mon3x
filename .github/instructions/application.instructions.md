---
applyTo: "src/main/java/**/application/**/*.java"
---

# Application Layer Instructions

These instructions define the mandatory conventions for the application layer: commands, queries, use cases, and any other application object.

The application layer orchestrates the domain. It depends on the domain, but never on infrastructure.

## Dependency direction

The allowed dependency direction is:

```text
infrastructure  ->  application  ->  domain
```

Application objects may know and use domain types:

- aggregates
- value objects
- domain enums
- domain exceptions
- domain services and factories
- repository and provider ports

Application objects must never know infrastructure types.

Never use in the application layer:

- REST request or response DTOs
- OpenAPI generated models (for example, anything under `com.urbanojvr.monex.<context>.model`)
- persistence entities
- Spring Web, JPA, or Hibernate types
- Jackson annotations
- external client models

Even when a generated REST type looks identical to a domain type, it is a transport type and must not cross into the application layer.

Forbidden:

```java
import com.urbanojvr.monex.categories.model.CategoryType;

public record CreateCategoryCommand(
        String name,
        CategoryType type
) {}
```

Required:

```java
import com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryType;

public record CreateCategoryCommand(
        CategoryName name,
        CategoryType type
) {}
```

## Conversion happens in infrastructure

Transport representations are always converted before reaching the application layer.

The infrastructure adapter (REST controller, message listener, scheduler, etc.) is responsible for translating its input into domain value objects and building the command or query.

Example in a REST controller:

```java
var command = new CreateCategoryCommand(
        new CategoryName(request.getName()),
        request.getDescription() != null
                ? new CategoryDescription(request.getDescription())
                : null,
        CategoryType.valueOf(request.getType().name())
);

createCategoryUseCase.execute(command);
```

Complex or repeated conversions should be extracted into a dedicated mapper in the infrastructure layer.

Results returned by use cases are also converted to transport representations by the infrastructure adapter, never by the application layer.

## Commands and queries

Commands and queries are immutable Java records.

Their components must be domain value objects or domain enums, not primitives or generic JDK types.

Good:

```java
@NullMarked
public record CreateCategoryCommand(
        CategoryName name,
        @Nullable CategoryDescription description,
        CategoryType type
) {}
```

Avoid:

```java
public record CreateCategoryCommand(
        String name,
        String description,
        String type
) {}
```

Because value objects validate themselves on construction, a command built from value objects is structurally valid by definition. Do not duplicate value object validation inside commands or use cases.

Commands must not contain values that the application layer is responsible for resolving, such as generated identifiers or the current time.

## Nullness

Use JSpecify. Annotate application classes with `@NullMarked`.

Null is forbidden by default. A component may be `@Nullable` only when absence is meaningful, following the same rules as the domain layer:

- the value object reference may be nullable
- a value object must never be created with a null internal value
- never use `Optional` as a command or query component

Mandatory record components should be checked with `Objects.requireNonNull(...)` in the compact constructor, without custom messages.

## Use cases

Use cases orchestrate; they do not contain business rules.

A use case typically:

1. receives a command or query
2. resolves external values (identifiers, current time, etc.) through providers
3. loads aggregates through repository ports
4. invokes domain factories or domain operations
5. persists the result through repository ports
6. returns a result expressed in domain or application types

Example:

```java
public void execute(CreateCategoryCommand command) {
    final var category = Category.create(
            categoryIdProvider.generate(),
            command.name(),
            command.description(),
            command.type()
    );

    categoryRepository.save(category);
}
```

Business rules belong in aggregates, value objects, or domain services. If a use case starts making business decisions, move that logic into the domain, if possible.

Commands, queries, and other application objects must never be passed into aggregates. The use case unpacks them and passes only domain values.

Forbidden:

```java
Category.create(command);
```

## External values

The application layer obtains externally generated values and passes the resolved values to the domain.

Use ports for:

- identifier generation
- the current time (`Clock` or a clock provider)
- random values

The domain never receives these providers; it receives the resolved values.

## Before generating or modifying an application object

Always verify:

1. No infrastructure or REST/OpenAPI generated type is imported.
2. Command and query components are domain value objects or domain enums.
3. Conversion from transport types happens in the infrastructure adapter.
4. Externally generated values are resolved in the use case, not received in the command.
5. Business rules live in the domain, not in the use case.
6. No command, query, or DTO is passed into an aggregate.
