package lib;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract base class for map lens wrappers that return domain lens objects.
 * This provides the common functionality while allowing subclasses to define
 * the specific domain lens type returned by get(K key).
 */
public class ObjectMapLensWrapper<A, K, V, DomainLens> implements Mutations.LensProvider<A, Map<K, V>> {

    protected final Lens<A, Map<K, V>> lens;
    private final BiFunction<Function<A, V>, BiFunction<A, V, A>, DomainLens> lensFactory;
    
    public ObjectMapLensWrapper(Function<A, Map<K, V>> getter, BiFunction<A, Map<K, V>, A> setter,
                               BiFunction<Function<A, V>, BiFunction<A, V, A>, DomainLens> lensFactory) {
        this.lens = Lens.of(getter, setter);
        this.lensFactory = lensFactory;
    }
    
    public ObjectMapLensWrapper(Lens<A, Map<K, V>> lens,
                               BiFunction<Function<A, V>, BiFunction<A, V, A>, DomainLens> lensFactory) {
        this.lens = lens;
        this.lensFactory = lensFactory;
    }
    
    /**
     * Get method that returns a domain lens for the value at the given key.
     * 
     * @param key the map key
     * @return a domain lens object for the value at the given key
     */
    public DomainLens get(K key) {
        return createKeyLens(key, lensFactory);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<A, Map<K, V>> lens() {
        return lens;
    }
    
    // Delegate common operations
    public A set(A entity, Map<K, V> newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<A, C> andThen(Lens<Map<K, V>, C> that) {
        return lens.andThen(that);
    }
    
    // Direct access to map key (like the original key method)
    public Lens<A, V> key(K key) {
        return lens.andThen(MapLens.key(key));
    }
    
    /**
     * Helper method for subclasses to create domain lens objects using map key access.
     * 
     * @param key the map key
     * @param lensFactory function to create the domain lens from getter and setter functions
     * @return the domain lens object
     */
    protected <DL> DL createKeyLens(K key, BiFunction<Function<A, V>, BiFunction<A, V, A>, DL> lensFactory) {
        return lensFactory.apply(
            entity -> lens.andThen(MapLens.key(key)).get(entity),
            (entity, newValue) -> lens.andThen(MapLens.key(key)).set(entity, newValue)
        );
    }
}
