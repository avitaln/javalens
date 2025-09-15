package model;

public final class NestedWithers {

    private NestedWithers() {}

    public static Nested withNestedValue(Nested nested, String nestedValue) {
        return new Nested(nestedValue, nested.moreNested());
    }

    public static Nested withMoreNested(Nested nested, MoreNested moreNested) {
        return new Nested(nested.nestedValue(), moreNested);
    }
}
