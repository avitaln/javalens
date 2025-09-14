package lib;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract base class for list lens wrappers that return domain lens objects.
 * This provides the common functionality while allowing subclasses to define
 * the specific domain lens type returned by get(int index).
 */
public class ObjectListLensWrapper<A, T, DomainLens> implements Mutations.LensProvider<A, List<T>> {

    protected final Lens<A, List<T>> lens;
    private final BiFunction<Function<A, T>, BiFunction<A, T, A>, DomainLens> lensFactory;
    
    public ObjectListLensWrapper(Function<A, List<T>> getter, BiFunction<A, List<T>, A> setter, 
                                BiFunction<Function<A, T>, BiFunction<A, T, A>, DomainLens> lensFactory) {
        this.lens = Lens.of(getter, setter);
        this.lensFactory = lensFactory;
    }
    
    public ObjectListLensWrapper(Lens<A, List<T>> lens, 
                                BiFunction<Function<A, T>, BiFunction<A, T, A>, DomainLens> lensFactory) {
        this.lens = lens;
        this.lensFactory = lensFactory;
    }
    
    /**
     * Get method that returns a domain lens for the element at the given index.
     * 
     * @param index the list index
     * @return a domain lens object for the element at the given index
     */
    public DomainLens get(int index) {
        return createIndexLens(index, lensFactory);
    }
    
    // Implement LensProvider interface
    @Override
    public Lens<A, List<T>> lens() {
        return lens;
    }
    
    // Delegate common operations
    public A set(A entity, List<T> newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<A, C> andThen(Lens<List<T>, C> that) {
        return lens.andThen(that);
    }
    
    /**
     * Helper method for subclasses to create domain lens objects using list index access.
     * 
     * @param index the list index
     * @param lensFactory function to create the domain lens from getter and setter functions
     * @return the domain lens object
     */
    protected <DL> DL createIndexLens(int index, BiFunction<Function<A, T>, BiFunction<A, T, A>, DL> lensFactory) {
        return lensFactory.apply(
            entity -> lens.andThen(ListLens.index(index)).get(entity),
            (entity, newValue) -> lens.andThen(ListLens.index(index)).set(entity, newValue)
        );
    }
}
