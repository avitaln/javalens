package model;

import java.util.Optional;

public final class RecursiveNestedWithers {

    private RecursiveNestedWithers() {}

    public static RecursiveNested withValue(RecursiveNested recursiveNested, String value) {
        return new RecursiveNested(value, recursiveNested.child());
    }

    public static RecursiveNested withChild(RecursiveNested recursiveNested, Optional<RecursiveNested> child) {
        return new RecursiveNested(recursiveNested.value(), child);
    }
}
