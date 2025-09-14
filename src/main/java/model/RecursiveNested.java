package model;

import java.util.Optional;

public record RecursiveNested(
    String value,
    Optional<RecursiveNested> child
) {
}
