package lib;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A generic wrapper around a Lens<A, List<T>> that provides direct access to list operations.
 * This allows for syntax like: lens.get(0) instead of lens.andThen(ListLens.index(0))
 */
public class ListLensWrapper<A, T> implements Mutations.LensProvider<A, List<T>> {

    private final Lens<A, List<T>> lens;

    public ListLensWrapper(Function<A, List<T>> getter, BiFunction<A, List<T>, A> setter) {
        this.lens = Lens.of(getter, setter);
    }
    
    public ListLensWrapper(Lens<A, List<T>> lens) {
        this.lens = lens;
    }
    
    // Direct access to list element by index
    public Lens<A, T> get(int index) {
        return lens.andThen(ListLens.index(index));
    }
    
    // Delegate lens methods to the wrapped lens
    public List<T> get(A entity) {
        return lens.get(entity);
    }
    
    public A set(A entity, List<T> newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<A, C> andThen(Lens<List<T>, C> that) {
        return lens.andThen(that);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<A, List<T>> lens() {
        return lens;
    }
}
