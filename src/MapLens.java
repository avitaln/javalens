import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MapLens {

    private MapLens() {}

    public static <K, V> Lens<Map<K, V>, V> key(K key) {
        return Lens.of(
                map -> map.get(key),
                (map, newValue) -> {
                    Objects.requireNonNull(key, "key");
                    Map<K, V> copy = new HashMap<>(map);
                    copy.put(key, newValue);
                    return Map.copyOf(copy);
                }
        );
    }
}


