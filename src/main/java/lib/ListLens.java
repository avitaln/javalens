package lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ListLens {

    private ListLens() {}

    public static <T> Lens<List<T>, T> index(int i) {
        return Lens.of(
                list -> list.get(i),
                (list, newValue) -> {
                    Objects.checkIndex(i, list.size());
                    List<T> copy = new ArrayList<>(list);
                    copy.set(i, newValue);
                    return List.copyOf(copy);
                }
        );
    }
}


