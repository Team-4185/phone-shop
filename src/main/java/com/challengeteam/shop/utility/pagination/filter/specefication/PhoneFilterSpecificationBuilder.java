package com.challengeteam.shop.utility.pagination.filter.specefication;

import com.challengeteam.shop.entity.phone.Phone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builder class for constructing JPA Specifications to filter {@link Phone} entities.
 * <p>
 * This builder provides a fluent API for dynamically creating complex filter specifications
 * that can be combined using logical AND or OR operations. It supports both single-value
 * and collection-based filters, automatically handling null checks and empty collections.
 * </p>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * <pre>{@code
 * Specification<Phone> spec = PhoneFilterSpecificationBuilder.build()
 *     .add(brand, b -> (root, query, builder) -> builder.equal(root.get(Phone_.BRAND), b))
 *     .add(brands, bs -> (root, query, builder) -> root.get(Phone_.BRAND).in(bs))
 *     .add(minPrice, min -> (root, query, builder) -> builder.greaterThanOrEqualTo(root.get(Phone_.PRICE), min))
 *     .buildAnd();
 * }</pre>
 *
 * @see Phone
 * @see Specification
 * @see org.springframework.data.jpa.domain.Specification
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PhoneFilterSpecificationBuilder {

    /**
     * Internal list that accumulates all specifications added through the builder methods.
     */
    private final List<Specification<Phone>> specifications = new ArrayList<>();

    /**
     * Creates and returns a new instance of {@link PhoneFilterSpecificationBuilder}.
     * <p>
     * This is the entry point for building filter specifications using the fluent API.
     * </p>
     *
     * @return a new {@link PhoneFilterSpecificationBuilder} instance
     */
    public static PhoneFilterSpecificationBuilder build() {
        return new PhoneFilterSpecificationBuilder();
    }

    /**
     * Adds a specification to the builder for a single non-null value.
     * <p>
     * If the provided value is {@code null}, the specification is not added and the builder
     * is returned unchanged. This allows for clean chaining without explicit null checks
     * in client code.
     * </p>
     *
     * @param <T>         the type of the filter value
     * @param value       the filter value; if {@code null}, no specification is added
     * @param specFactory a function that creates a {@link Specification} from the given value
     * @return this builder instance for method chaining
     */
    public <T> PhoneFilterSpecificationBuilder add(
            T value, Function<T, Specification<Phone>> specFactory) {
        if (value != null) {
            log.debug("Add single value specification: {}", specFactory.apply(value));
            specifications.add(specFactory.apply(value));
        }
        return this;
    }

    /**
     * Adds a specification to the builder for a collection of values.
     * <p>
     * If the provided collection is {@code null} or empty, the specification is not added
     * and the builder is returned unchanged. This is useful for filters like "brand IN (brand1, brand2)"
     * where you want to ignore empty filter lists.
     * </p>
     *
     * @param <T>         the type of the collection, must extend {@link Collection}
     * @param value       the collection of filter values; if {@code null} or empty, no specification is added
     * @param specFactory a function that creates a {@link Specification} from the given collection
     * @return this builder instance for method chaining
     */
    public <T extends Collection<?>> PhoneFilterSpecificationBuilder add(
            T value, Function<T, Specification<Phone>> specFactory) {
        if (value != null && !value.isEmpty()) {
            log.debug("Add list of specifications: {}", specFactory.apply(value));
            specifications.add(specFactory.apply(value));
        }
        return this;
    }

    /**
     * Conditionally adds a specification to the builder based on a boolean flag.
     * <p>
     * If the provided flag is {@code true}, the specification produced by the supplier
     * is added to the builder. If the flag is {@code false} or {@code null}, no specification
     * is added and the builder is returned unchanged. This is useful for optional filters
     * that should only apply when explicitly enabled.
     * </p>
     *
     * @param flag        the boolean flag; the specification is only added if this is {@code true}
     * @param specFactory a supplier that creates a {@link Specification} when invoked
     * @return this builder instance for method chaining
     */
    public PhoneFilterSpecificationBuilder add(
            Boolean flag, Supplier<Specification<Phone>> specFactory) {
        if (Boolean.TRUE.equals(flag)) {
            specifications.add(specFactory.get());
        }
        return this;
    }

    /**
     * Builds and returns a {@link Specification} that combines all added specifications using logical AND.
     * <p>
     * All specifications added to this builder will be combined such that a {@link Phone} entity
     * must satisfy <strong>all</strong> of them to be included in the result set.
     * If no specifications were added, this returns a specification that matches all entities.
     * </p>
     *
     * @return a {@link Specification} representing the logical AND of all added specifications
     */
    public Specification<Phone> buildAnd() {
        log.debug("Build and specification: {}", specifications);
        return Specification.allOf(specifications);
    }

    /**
     * Builds and returns a {@link Specification} that combines all added specifications using logical OR.
     * <p>
     * All specifications added to this builder will be combined such that a {@link Phone} entity
     * must satisfy <strong>at least one</strong> of them to be included in the result set.
     * If no specifications were added, this returns a specification that matches all entities.
     * </p>
     *
     * @return a {@link Specification} representing the logical OR of all added specifications
     */
    public Specification<Phone> buildOr() {
        log.debug("Build or specification: {}", specifications);
        return Specification.anyOf(specifications);
    }
}