import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.DomainEntity;
import model.DomainEntityLens;
import model.Nested;
import model.NestedLens;
import model.MoreNested;
import model.MoreNestedLens;
import lib.Lens;

public class DomainEntityLensTest {
    
    private DomainEntity testEntity;
    
    @BeforeEach
    void setUp() {
        MoreNested moreNested = new MoreNested("moreNestedValue");
        Nested nested = new Nested("nestedValue", moreNested);
        
        MoreNested optionalMoreNested = new MoreNested("optionalMoreNestedValue");
        Nested optionalNestedValue = new Nested("optionalNestedValue", optionalMoreNested);
        
        testEntity = new DomainEntity(
            "hello",
            Optional.of("optional"),
            List.of("a", "b", "c"),
            Map.of("str1", "value1", "str2", "value2"),
            nested,
            Optional.of(optionalNestedValue)
        );
    }
    
    // PRIMITIVE LENS TESTS
    
    @Test
    void testStringValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringValue, "updated")
            .apply();
        
        assertEquals("updated", updated.stringValue());
        assertEquals("hello", testEntity.stringValue()); // Original unchanged
    }
    
    // OPTIONAL LENS TESTS
    
    @Test
    void testOptionalStringLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalString, Optional.of("new optional"))
            .apply();
        
        assertTrue(updated.optionalString().isPresent());
        assertEquals("new optional", updated.optionalString().get());
        assertEquals("optional", testEntity.optionalString().get()); // Original unchanged
    }
    
    // LIST LENS TESTS
    
    @Test
    void testStringListLens() {
        List<String> newList = List.of("x", "y", "z");
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringList, newList)
            .apply();
        
        assertEquals(newList, updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    @Test
    void testStringListElementLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringListGet(0), "updated")
            .apply();
        
        assertEquals(List.of("updated", "b", "c"), updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    // MAP LENS TESTS
    
    @Test
    void testStringMapKeyLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringMapKey("str1"), "updated value")
            .apply();
        
        assertEquals(Map.of("str1", "updated value", "str2", "value2"), updated.stringMap());
        assertEquals(Map.of("str1", "value1", "str2", "value2"), testEntity.stringMap()); // Original unchanged
    }
    
    // MODIFICATION TESTS
    
    @Test
    void testModifyStringValue() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.stringValue, s -> s.toUpperCase())
            .apply();
        
        assertEquals("HELLO", updated.stringValue());
        assertEquals("hello", testEntity.stringValue()); // Original unchanged
    }
    
    
    @Test
    void testModifyStringList() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.stringList, list -> List.of(list.get(0).toUpperCase(), list.get(1).toUpperCase(), list.get(2).toUpperCase()))
            .apply();
        
        assertEquals(List.of("A", "B", "C"), updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    @Test
    void testModifyOptionalString() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.optionalString, opt -> opt.map(String::toUpperCase))
            .apply();
        
        assertTrue(updated.optionalString().isPresent());
        assertEquals("OPTIONAL", updated.optionalString().get());
        assertEquals("optional", testEntity.optionalString().get()); // Original unchanged
    }
    
    // CHAINED OPERATIONS TESTS
    
    @Test
    void testChainedOperations() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringValue, "chained")
            .set(DomainEntityLens.optionalString, Optional.of("chained optional"))
            .set(DomainEntityLens.stringListGet(0), "updated")
            .set(DomainEntityLens.stringMapKey("str1"), "chained value")
            .apply();
        
        assertEquals("chained", updated.stringValue());
        assertEquals("chained optional", updated.optionalString().get());
        assertEquals(List.of("updated", "b", "c"), updated.stringList());
        assertEquals(Map.of("str1", "chained value", "str2", "value2"), updated.stringMap());
        
        // Original should be unchanged
        assertEquals("hello", testEntity.stringValue());
        assertEquals("optional", testEntity.optionalString().get());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList());
        assertEquals(Map.of("str1", "value1", "str2", "value2"), testEntity.stringMap());
    }
    
    @Test
    void testMixedSetAndMod() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringValue, "test")
            .mod(DomainEntityLens.stringValue, s -> s + "_modified")
            .set(DomainEntityLens.optionalString, Optional.of("optional_test"))
            .mod(DomainEntityLens.optionalString, opt -> opt.map(s -> s.toUpperCase()))
            .apply();
        
        assertEquals("test_modified", updated.stringValue()); // Set to "test", then append "_modified"
        assertEquals("OPTIONAL_TEST", updated.optionalString().get()); // Set then uppercase
        
        // Original unchanged
        assertEquals("hello", testEntity.stringValue());
        assertEquals("optional", testEntity.optionalString().get());
    }
    
    // NESTED LENS TESTS
    
    @Test
    void testNestedValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested.andThen(NestedLens.nestedValue), "updatedNested")
            .apply();
        
        assertEquals("updatedNested", updated.nested().nestedValue());
        assertEquals("nestedValue", testEntity.nested().nestedValue()); // Original unchanged
    }
    
    @Test
    void testNestedValueLensWithFriendlySyntax() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested.nestedValue, "updatedNested")
            .apply();
        
        assertEquals("updatedNested", updated.nested().nestedValue());
        assertEquals("nestedValue", testEntity.nested().nestedValue()); // Original unchanged
    }
    
    @Test
    void testMoreNestedValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested.andThen(NestedLens.moreNested).andThen(MoreNestedLens.moreNestedValue), "updatedMoreNested")
            .apply();
        
        assertEquals("updatedMoreNested", updated.nested().moreNested().moreNestedValue());
        assertEquals("moreNestedValue", testEntity.nested().moreNested().moreNestedValue()); // Original unchanged
    }
    
    @Test
    void testMoreNestedValueLensWithFriendlySyntax() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested.moreNested.moreNestedValue, "updatedMoreNested")
            .apply();
        
        assertEquals("updatedMoreNested", updated.nested().moreNested().moreNestedValue());
        assertEquals("moreNestedValue", testEntity.nested().moreNested().moreNestedValue()); // Original unchanged
    }
    
    @Test
    void testReplaceNestedEntity() {
        MoreNested newMoreNested = new MoreNested("newMoreNestedValue");
        Nested newNested = new Nested("newNestedValue", newMoreNested);
        
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested, newNested)
            .apply();
        
        assertEquals("newNestedValue", updated.nested().nestedValue());
        assertEquals("newMoreNestedValue", updated.nested().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("nestedValue", testEntity.nested().nestedValue());
        assertEquals("moreNestedValue", testEntity.nested().moreNested().moreNestedValue());
    }
    
    @Test
    void testChainedNestedOperations() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringValue, "updated")
            .set(DomainEntityLens.nested.andThen(NestedLens.nestedValue), "updatedNested")
            .set(DomainEntityLens.nested.andThen(NestedLens.moreNested).andThen(MoreNestedLens.moreNestedValue), "updatedMoreNested")
            .apply();
        
        assertEquals("updated", updated.stringValue());
        assertEquals("updatedNested", updated.nested().nestedValue());
        assertEquals("updatedMoreNested", updated.nested().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("hello", testEntity.stringValue());
        assertEquals("nestedValue", testEntity.nested().nestedValue());
        assertEquals("moreNestedValue", testEntity.nested().moreNested().moreNestedValue());
    }
    
    // OPTIONAL NESTED LENS TESTS
    
    @Test
    void testOptionalNestedLens() {
        MoreNested newMoreNested = new MoreNested("newOptionalMoreNestedValue");
        Nested newOptionalNested = new Nested("newOptionalNestedValue", newMoreNested);
        
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested, Optional.of(newOptionalNested))
            .apply();
        
        assertTrue(updated.optionalNested().isPresent());
        assertEquals("newOptionalNestedValue", updated.optionalNested().get().nestedValue());
        assertEquals("newOptionalMoreNestedValue", updated.optionalNested().get().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("optionalNestedValue", testEntity.optionalNested().get().nestedValue());
        assertEquals("optionalMoreNestedValue", testEntity.optionalNested().get().moreNested().moreNestedValue());
    }
    
    @Test
    void testOptionalNestedSetEmpty() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested, Optional.empty())
            .apply();
        
        assertTrue(updated.optionalNested().isEmpty());
        assertTrue(testEntity.optionalNested().isPresent()); // Original unchanged
    }
    
    @Test
    void testOptionalNestedDeepAccess() {
        // Create a test entity with empty optional nested first
        DomainEntity entityWithEmpty = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested, Optional.empty())
            .apply();
        
        // Add a new optional nested entity
        MoreNested newMoreNested = new MoreNested("deepOptionalValue");
        Nested newNested = new Nested("deepNestedValue", newMoreNested);
        
        DomainEntity updated = DomainEntityLens.on(entityWithEmpty)
            .set(DomainEntityLens.optionalNested, Optional.of(newNested))
            .apply();
        
        assertTrue(updated.optionalNested().isPresent());
        assertEquals("deepNestedValue", updated.optionalNested().get().nestedValue());
        assertEquals("deepOptionalValue", updated.optionalNested().get().moreNested().moreNestedValue());
        
        // Original entity with empty should be unchanged
        assertTrue(entityWithEmpty.optionalNested().isEmpty());
    }
    
    @Test
    void testOptionalNestedDeepValueAccessWhenPresent() {
        // Test accessing nested values when optional is present - should work
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested.andThen(Lens.of(
                opt -> opt.map(Nested::nestedValue).orElse(null),
                (opt, newValue) -> opt.map(nested -> 
                    new Nested(newValue, nested.moreNested()))
            )), "updatedOptionalNestedValue")
            .apply();
        
        assertTrue(updated.optionalNested().isPresent());
        assertEquals("updatedOptionalNestedValue", updated.optionalNested().get().nestedValue());
        assertEquals("optionalMoreNestedValue", updated.optionalNested().get().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("optionalNestedValue", testEntity.optionalNested().get().nestedValue());
    }
    
    @Test
    void testOptionalNestedDeepValueAccessWhenEmpty() {
        // Create entity with empty optional nested
        DomainEntity entityWithEmpty = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested, Optional.empty())
            .apply();
        
        // Test accessing nested values when optional is empty - should handle gracefully
        DomainEntity updated = DomainEntityLens.on(entityWithEmpty)
            .set(DomainEntityLens.optionalNested.andThen(Lens.of(
                opt -> opt.map(Nested::nestedValue).orElse(null),
                (opt, newValue) -> opt.map(nested -> 
                    new Nested(newValue, nested.moreNested()))
            )), "shouldNotUpdate")
            .apply();
        
        // Should remain empty since the lens handles empty optional gracefully
        assertTrue(updated.optionalNested().isEmpty());
        assertTrue(entityWithEmpty.optionalNested().isEmpty()); // Original unchanged
    }
    
    @Test
    void testOptionalNestedMoreNestedValueAccessWhenPresent() {
        // Test accessing deep nested values when optional is present - should work
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested.andThen(Lens.of(
                opt -> opt.map(nested -> nested.moreNested().moreNestedValue()).orElse(null),
                (opt, newValue) -> opt.map(nested -> new Nested(nested.nestedValue(), new MoreNested(newValue)))
            )), "updatedOptionalMoreNestedValue")
            .apply();
        
        assertTrue(updated.optionalNested().isPresent());
        assertEquals("optionalNestedValue", updated.optionalNested().get().nestedValue()); // Unchanged
        assertEquals("updatedOptionalMoreNestedValue", updated.optionalNested().get().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("optionalMoreNestedValue", testEntity.optionalNested().get().moreNested().moreNestedValue());
    }
    
    @Test
    void testOptionalNestedMoreNestedValueAccessWhenEmpty() {
        // Create entity with empty optional nested
        DomainEntity entityWithEmpty = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested, Optional.empty())
            .apply();
        
        // Test accessing deep nested values when optional is empty - should handle gracefully
        DomainEntity updated = DomainEntityLens.on(entityWithEmpty)
            .set(DomainEntityLens.optionalNested.andThen(Lens.of(
                opt -> opt.map(nested -> nested.moreNested().moreNestedValue()).orElse(null),
                (opt, newValue) -> opt.map(nested -> new Nested(nested.nestedValue(), new MoreNested(newValue)))
            )), "shouldNotUpdate")
            .apply();
        
        // Should remain empty since the lens handles empty optional gracefully
        assertTrue(updated.optionalNested().isEmpty());
        assertTrue(entityWithEmpty.optionalNested().isEmpty()); // Original unchanged
    }
}
