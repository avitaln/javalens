package model;

public final class MoreNestedWithers {

    private MoreNestedWithers() {}

    public static MoreNested withMoreNestedValue(MoreNested moreNested, String moreNestedValue) {
        return new MoreNested(moreNestedValue);
    }
}
