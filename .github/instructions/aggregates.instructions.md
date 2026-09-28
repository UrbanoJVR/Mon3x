---
applyTo: "src/main/java/**/domain/**/*.java"
---

# Aggregate and Value Object Instructions

These instructions define the mandatory conventions for aggregates, aggregate roots, child entities, and value objects in the domain layer.

The domain model must use modern Java and remain completely independent from frameworks, persistence technologies, transport models, and infrastructure concerns.

## General principles

- Prefer rich domain models over anemic models.
- Follow Tell, Don't Ask.
- Put as much business behavior as reasonably possible inside the aggregate.
- Methods must express domain intent rather than technical state mutation.
- Keep aggregates readable and cohesive. Do not turn aggregate roots into god objects.
- Extract a domain service or domain factory when domain logic becomes sufficiently complex that keeping it inside the aggregate harms readability or maintainability.
- Use English for all domain names, methods, types, fields, and exceptions.
- Avoid technical or generic behavior names such as `update`, `setData`, `process`, `modify`, or similar names when a domain-specific verb exists.

Good:

```java
account.rename(name);
account.close(closedAt);
category.deactivate();
transaction.cancel(cancelledAt);
```

Avoid:

```java
account.updateName(name);
account.setName(name);
category.updateStatus(status);
transaction.process();
```

## Package structure

Aggregates and their value objects follow this package layout inside each bounded context:

```text
com.urbanojvr.monex.<context>.domain.aggregate      -> aggregate roots and child entities
com.urbanojvr.monex.<context>.domain.aggregate.vo   -> value objects and domain enums
com.urbanojvr.monex.<context>.domain.exception      -> domain exceptions
```

Example:

```text
com.urbanojvr.monex.categories.domain.aggregate.Category
com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryId
com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryName
com.urbanojvr.monex.categories.domain.aggregate.vo.CategoryType
```

Never place value objects in other packages such as `domain.valueobject`, `domain.model`, or directly in `domain.aggregate`.

## Framework independence

Domain code must be pure Java.

The only non-JDK dependency allowed in domain model classes is JSpecify for nullness annotations.

Never use:

- Spring annotations or APIs
- Jakarta Persistence / JPA annotations
- Hibernate APIs
- Lombok
- Jackson annotations
- API DTOs
- persistence entities
- infrastructure-specific types

Aggregates must always be persistence-agnostic.

Persistence entities belong to the persistence adapter and must be converted to and from domain objects using dedicated mappers or converters.

## Nullness

Use JSpecify.

Domain classes should normally be annotated with:

```java
@NullMarked
```

at class level.

Null is forbidden by default.

A field may only be nullable when absence is a meaningful and explicit part of the domain model.

Never use `Optional` as:

- an aggregate field
- an entity field
- a value object field
- a method parameter representing stored domain state

`Optional` is appropriate as a return type when absence is expected, for example from repository ports:

```java
Optional<Account> findById(AccountId id);
```

When a domain attribute may be absent, the value object reference itself may be nullable.

Good:

```java
private final @Nullable Description description;
```

with:

```java
public record Description(String value) {
    public Description {
        Objects.requireNonNull(value);
    }
}
```

Never represent absence by creating a value object whose internal value is null.

Forbidden:

```java
new Description(null);
```

Value objects must never contain null values unless there is an exceptional domain reason for doing so.

Such a design must not be introduced automatically. It requires explicit confirmation before implementation.

## Construction

Aggregate constructors are public.

The constructor is the canonical reconstruction mechanism and must always enforce the aggregate's structural invariants.

This allows infrastructure mappers and converters to reconstruct an aggregate directly while still executing all constructor validations.

Example:

```java
@NullMarked
public final class Account {

    private final AccountId id;
    private final AccountName name;
    private final AccountStatus status;

    public Account(
            AccountId id,
            AccountName name,
            AccountStatus status
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
    }
}
```

Do not create separate reconstruction methods such as:

```java
restore(...)
fromPersistence(...)
rehydrate(...)
```

unless explicitly requested.

Reconstructing an object from persistence is not a business creation operation.

## Business creation

New aggregates created as part of business behavior must normally use a named static factory method such as:

```java
create(...)
```

Example:

```java
public static Account create(
        AccountId id,
        AccountName name,
        Instant createdAt
) {
    return new Account(
            id,
            name,
            AccountStatus.ACTIVE,
            createdAt
    );
}
```

`create(...)` represents business creation and may:

- establish initial state
- apply creation-specific business rules
- calculate derived initial values
- prevent callers from choosing internal initial state that should be controlled by the domain

Do not use `create(...)` when loading an existing aggregate from persistence. Use the public constructor instead.

## External values and providers

Aggregates must never depend directly on providers or infrastructure abstractions such as:

- `Clock`
- `ClockProvider`
- UUID generators
- random generators
- repositories
- external clients

The application layer is responsible for obtaining external values and passing the resolved values into the domain.

Example:

```java
var accountId = accountIdProvider.generate();
var createdAt = clock.instant();

var account = Account.create(
        this.accountId,
        command.name(),
        this.createdAt
);
```

The aggregate receives:

```java
Instant createdAt
```

not:

```java
Clock clock
```

and not:

```java
ClockProvider clockProvider
```

The same rule applies to UUIDs and other externally generated values.

## Immutability

All aggregate fields must be `final`.

Aggregates must not expose setters.

State-changing domain operations return a new aggregate instance rather than mutating fields.

Prefer records when their semantics fit naturally.

Classes are also allowed when they improve readability or modeling.

Example:

```java
public Account rename(AccountName name) {
    return this.toBuilder()
            .name(name)
            .build();
}
```

Do not write:

```java
public void setName(AccountName name) {
    this.name = name;
}
```

## Builders

Aggregates must provide a manually implemented builder and `toBuilder()`.

Do not use Lombok builders or any generated builder implementation.

The builder primarily exists to simplify internal reconstruction of immutable aggregate instances during domain operations.

Example:

```java
public Account rename(AccountName name) {
    return toBuilder()
            .name(name)
            .build();
}
```

The builder must eventually create the aggregate through its public constructor so that constructor validations are always executed.

Conceptually:

```java
public Account build() {
    return new Account(
            id,
            name,
            status,
            createdAt
    );
}
```

### Builder visibility and usage

Builders are an internal implementation convenience for aggregates.

Code outside the aggregate must not normally construct domain objects through the builder.

External callers must use:

- the public constructor when reconstructing a complete existing aggregate
- a business factory such as `create(...)` when creating a new aggregate through business behavior
- domain methods when changing an existing aggregate

This is intentional.

Using constructors from mappers provides compile-time failures when the aggregate constructor changes, forcing mappings to be reviewed.

Avoid:

```java
Account.builder()
        .id(entity.id())
        .name(entity.name())
        .build();
```

Prefer:

```java
new Account(
        entity.id(),
        entity.name(),
        entity.status()
);
```

The builder should therefore not become an alternative public construction API used throughout the codebase.

## Domain operations

Never expose generic setters.

Operations that result in a changed aggregate must describe business intent.

Good:

```java
rename(...)
activate(...)
deactivate(...)
close(...)
cancel(...)
assignCategory(...)
registerTransaction(...)
```

Avoid:

```java
setName(...)
setActive(...)
setStatus(...)
update(...)
patch(...)
```

Domain operations should validate any business rule they are responsible for before producing the new aggregate.

When an operation does not modify the aggregate, use domain-oriented query methods such as:

```java
isActive()
canBeDeleted()
hasTransactions()
isClosed()
```

Prefer these over making callers inspect raw state and reproduce business rules externally.

## Getters

Aggregate state must be readable because application services, mappers, converters, and other legitimate collaborators may need it.

Use record-style accessor names.

Good:

```java
public AccountId id() {
    return id;
}

public AccountName name() {
    return name;
}

public AccountStatus status() {
    return status;
}
```

Avoid JavaBean-style accessors:

```java
getId()
getName()
getStatus()
```

When using records, use their native accessors.

## Value Objects

Use value objects aggressively.

Avoid primitive obsession.

Domain concepts should normally have dedicated types rather than being represented directly with primitives or generic JDK types.

Prefer:

```java
AccountId
AccountName
CategoryName
Amount
Percentage
TransactionId
```

over passing raw values such as:

```java
UUID
String
BigDecimal
int
long
```

through the domain model.

Enums are acceptable directly when they correctly represent a closed domain concept and a dedicated wrapper would add no value.

Other already meaningful non-primitive domain types may also be used directly when introducing another wrapper would not improve the model.

### Value Objects must be records

Value objects should be implemented as Java records.

Example:

```java
@NullMarked
public record AccountName(String value) {

    public AccountName {
        Objects.requireNonNull(value);

        if (value.isBlank()) {
            throw new InvalidAccountNameException();
        }
    }
}
```

When a value object wraps a single value, the record component must be named:

```java
value
```

Good:

```java
public record AccountName(String value) {}
public record AccountId(UUID value) {}
public record Amount(BigDecimal value) {}
```

Avoid:

```java
public record AccountName(String name) {}
public record AccountId(UUID id) {}
```

A value object containing several domain values should instead use descriptive component names:

```java
public record Money(
        BigDecimal amount,
        Currency currency
) {}
```

### Value Object nullness

A value object's internal values must never be null.

Use:

```java
Objects.requireNonNull(value);
```

Do not add redundant custom messages to `Objects.requireNonNull`.

Prefer:

```java
Objects.requireNonNull(value);
```

over:

```java
Objects.requireNonNull(value, "value cannot be null");
```

The stack trace and parameter context are normally sufficient.

## Validation and invariants

Structural validity must be enforced by the type that owns the state.

Examples:

- a value object validates whether its own value is valid
- an aggregate validates consistency between its members
- a domain operation validates whether that operation is allowed

Do not rely on controllers, DTO validation, persistence constraints, or application services as the only protection for domain invariants.

Application-layer validation may exist for faster feedback, but the domain must remain valid independently.

Prefer `Objects.requireNonNull(...)` for mandatory references.

Use domain-specific exceptions for meaningful business-rule violations.

Prefer exceptions that are specific enough to communicate the violated business concept without creating excessively granular exception hierarchies.

Example:

```java
throw new AccountAlreadyClosedException();
```

or, when several related rules can reasonably share one type:

```java
throw new InvalidAccountStateException();
```

Do not create one exception class for every minor validation detail unless it has real value to callers.

## DTO boundaries

DTOs, commands, API requests, persistence entities, and other transport representations must never enter the aggregate.

Forbidden:

```java
Account.create(CreateAccountRequest request);
```

Forbidden:

```java
Account.create(CreateAccountCommand command);
```

Prefer application-layer mapping:

```java
var account = Account.create(
        accountId,
        new AccountName(command.name()),
        createdAt
);
```

The application layer is responsible for translating commands or DTOs into domain concepts.

The aggregate only accepts domain-relevant values.

## Persistence mapping

Persistence adapters reconstruct aggregates using their public constructors.

Example:

```java
return new Account(
        new AccountId(entity.id()),
        new AccountName(entity.name()),
        AccountStatus.valueOf(entity.status()),
        entity.createdAt()
);
```

Do not expose persistence concerns inside the aggregate merely to simplify ORM mapping.

Do not annotate aggregates with persistence annotations.

Do not let persistence entity types leak into constructors or domain methods.

## Equality

Do not manually implement `equals()` or `hashCode()` unless there is a clear domain need.

Prefer records when value-based equality is naturally correct.

Do not generate boilerplate equality methods by default.

When aggregate identity semantics require special equality behavior that cannot be represented naturally by the chosen Java type, stop and evaluate the requirement rather than automatically introducing custom `equals()` and `hashCode()` implementations.

## Collections

Aggregates may internally use mutable collections when this simplifies business operations.

However, collection ownership must remain inside the aggregate.

Never expose a mutable internal collection directly.

Read access must return a safe representation such as an immutable copy.

Example:

```java
public List<Transaction> transactions() {
    return List.copyOf(transactions);
}
```

External callers must not be able to modify aggregate state through a returned collection.

Changes to aggregate-owned collections must happen through domain operations.

Good:

```java
account.registerTransaction(transaction);
```

Avoid:

```java
account.transactions().add(transaction);
```

## Child entities

As a general rule, child entities belonging to an aggregate should be modified through the aggregate root.

The aggregate root is responsible for maintaining aggregate-wide consistency.

However, this is not an absolute rule.

Direct interaction with child entities is acceptable when the domain model clearly benefits from it and aggregate invariants remain protected.

Do not mechanically force every child-entity operation through the root when doing so makes the model less expressive or significantly harder to maintain.

## Domain services and factories

Prefer behavior inside aggregates and value objects.

Introduce a domain service or external domain factory when:

- the behavior naturally spans multiple aggregates
- no single aggregate clearly owns the rule
- the amount of logic would make the aggregate difficult to read
- extracting the logic meaningfully improves domain clarity

Do not create domain services merely to avoid putting behavior in aggregates.

An aggregate should contain the maximum reasonable amount of behavior that belongs to it.

## Modern Java

Use modern Java features whenever they make the model clearer.

Target modern Java, currently Java 21 or newer.

Prefer:

- records for value objects
- records for aggregates when appropriate
- switch expressions
- pattern matching
- sealed types when modeling closed hierarchies
- immutable data structures where suitable
- concise modern Java constructs

Avoid legacy Java patterns and unnecessary boilerplate.

Do not imitate Java 8-era conventions when modern Java expresses the model more clearly.

## Aggregate shape example

A typical immutable aggregate should resemble:

```java
@NullMarked
public final class Account {

    private final AccountId id;
    private final AccountName name;
    private final AccountStatus status;
    private final Instant createdAt;

    public Account(
            AccountId id,
            AccountName name,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Account create(
            AccountId id,
            AccountName name,
            Instant createdAt
    ) {
        return new Account(
                id,
                name,
                AccountStatus.ACTIVE,
                createdAt
        );
    }

    public Account rename(AccountName name) {
        Objects.requireNonNull(name);

        return this.toBuilder()
                .name(name)
                .build();
    }

    public Account close(Instant closedAt) {
        Objects.requireNonNull(closedAt);

        if (!this.isActive()) {
            throw new AccountAlreadyClosedException();
        }

        return this.toBuilder()
                .status(AccountStatus.CLOSED)
                .build();
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public boolean canBeDeleted() {
        return this.isActive();
    }

    public AccountId id() {
        return this.id;
    }

    public AccountName name() {
        return this.name;
    }

    public AccountStatus status() {
        return this.status;
    }

    public Instant createdAt() {
        return this.createdAt;
    }

    private Builder toBuilder() {
        return new Builder(this);
    }

    private static final class Builder {

        private final AccountId id;
        private AccountName name;
        private AccountStatus status;
        private final Instant createdAt;

        private Builder(Account source) {
            this.id = source.id;
            this.name = source.name;
            this.status = source.status;
            this.createdAt = source.createdAt;
        }

        private Builder name(AccountName name) {
            this.name = name;
            return this;
        }

        private Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        private Account build() {
            return new Account(
                    this.id,
                    this.name,
                    this.status,
                    this.createdAt
            );
        }
    }
}
```

The exact structure may vary according to the domain, but generated aggregates must preserve the principles in this document.

## Before generating or modifying an aggregate

Always verify:

1. Whether each domain concept should be represented by a value object.
2. Whether nullable state is actually meaningful in the domain.
3. Whether behavior belongs inside the aggregate rather than in the application layer.
4. Whether the proposed operation has a domain-specific name.
5. Whether externally generated values are being resolved by the application layer.
6. Whether persistence or transport types are leaking into the domain.
7. Whether constructor validation is sufficient to guarantee a structurally valid aggregate.
8. Whether all fields can remain final.
9. Whether `toBuilder()` can simplify immutable state transitions.
10. Whether a domain service is genuinely justified rather than being used to create an anemic aggregate.