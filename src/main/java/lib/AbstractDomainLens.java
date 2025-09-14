package lib;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractDomainLens<A, B> implements Mutations.LensProvider<A, B> {

    protected final Lens<A, B> lens;
    
    public AbstractDomainLens(Function<A, B> getter, BiFunction<A, B, A> setter) {
        this.lens = Lens.of(getter, setter);
    }
    
    protected AbstractDomainLens(Lens<A, B> lens) {
        this.lens = lens;
    }
    
    public B get(A entity) {
        return lens.get(entity);
    }
    
    public A set(A entity, B newValue) {
        return lens.set(entity, newValue);
    }
    
    public <C> Lens<A, C> andThen(Lens<B, C> that) {
        return lens.andThen(that);
    }
    
    @Override
    public Lens<A, B> lens() {
        return lens;
    }
}
