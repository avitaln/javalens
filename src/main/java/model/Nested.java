package model;

public record Nested(
    String nestedValue,
    MoreNested moreNested
) {
}
