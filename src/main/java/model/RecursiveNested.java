package model;

import java.util.Optional;

/**
 * Example of a recursive nested structure that contains a reference to itself.
 * This would cause infinite loops with eager initialization.
 */
public record RecursiveNested(
    String value,
    Optional<RecursiveNested> child
) {
}
