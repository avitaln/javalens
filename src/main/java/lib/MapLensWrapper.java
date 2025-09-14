package lib;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A generic wrapper around a Lens<A, Map<K, V>> that provides direct access to map operations.
 * This allows for syntax like: lens.key("key") instead of lens.andThen(MapLens.key("key"))
 */
public class MapLensWrapper<A, K, V> implements Mutations.LensProvider<A, Map<K, V>> {

    private final Lens<A, Map<K, V>> lens;

    public MapLensWrapper(Function<A, Map<K, V>> getter, BiFunction<A, Map<K, V>, A> setter) {
        this.lens = Lens.of(getter, setter);
    }
    
    public MapLensWrapper(Lens<A, Map<K, V>> lens) {
        this.lens = lens;
    }
    
    // Direct access to map key
    public Lens<A, V> key(K key) {
        return lens.andThen(MapLens.key(key));
    }
    
    // Delegate lens methods to the wrapped lens
    public Map<K, V> get(A entity) {
        return lens.get(entity);
    }
    
    public A set(A entity, Map<K, V> newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<A, C> andThen(Lens<Map<K, V>, C> that) {
        return lens.andThen(that);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<A, Map<K, V>> lens() {
        return lens;
    }
}
