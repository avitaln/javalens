package model;

import lib.ListLensWrapper;
import lib.Lens;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$NestedListLens extends ListLensWrapper<DomainEntity, Nested> {
    
    public DomainEntity$NestedListLens(Function<DomainEntity, List<Nested>> getter, BiFunction<DomainEntity, List<Nested>, DomainEntity> setter) {
        super(getter, setter);
    }
    
    public DomainEntity$NestedLens nested(int index) {
        return new DomainEntity$NestedLens(
            entity -> super.get(index).get(entity),
            (entity, newValue) -> super.get(index).set(entity, newValue)
        );
    }
}
