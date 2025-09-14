package model;

import lib.MapLensWrapper;
import lib.Lens;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DomainEntity$NestedMapLens extends MapLensWrapper<DomainEntity, String, Nested> {
    
    public DomainEntity$NestedMapLens(Function<DomainEntity, Map<String, Nested>> getter, BiFunction<DomainEntity, Map<String, Nested>, DomainEntity> setter) {
        super(getter, setter);
    }
    
    public DomainEntity$NestedLens nested(String key) {
        return new DomainEntity$NestedLens(
            entity -> super.key(key).get(entity),
            (entity, newValue) -> super.key(key).set(entity, newValue)
        );
    }
}
