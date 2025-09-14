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
        
        model.RecursiveNested recursiveNested = new model.RecursiveNested("recursive-root", Optional.empty());
        
        Nested nestedItem1 = new Nested("listItem1", new MoreNested("listMoreNested1"));
        Nested nestedItem2 = new Nested("listItem2", new MoreNested("listMoreNested2"));
        List<Nested> nestedList = List.of(nestedItem1, nestedItem2);
        
        Nested mapItem1 = new Nested("mapItem1", new MoreNested("mapMoreNested1"));
        Nested mapItem2 = new Nested("mapItem2", new MoreNested("mapMoreNested2"));
        Map<String, Nested> nestedMap = Map.of("key1", mapItem1, "key2", mapItem2);
        
        testEntity = new DomainEntity(
            "hello",
            Optional.of("optional"),
            List.of("a", "b", "c"),
            Map.of("str1", "value1", "str2", "value2"),
            nested,
            Optional.of(optionalNestedValue),
            nestedList,
            nestedMap,
            recursiveNested
        );
    }
    
    // PRIMITIVE LENS TESTS
    
    @Test
    void testStringValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringValue(), "updated")
            .apply();
        
        assertEquals("updated", updated.stringValue());
        assertEquals("hello", testEntity.stringValue()); // Original unchanged
    }
    
    // OPTIONAL LENS TESTS
    
    @Test
    void testOptionalStringLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalString(), Optional.of("new optional"))
            .apply();
        
        assertTrue(updated.optionalString().isPresent());
        assertEquals("new optional", updated.optionalString().get());
        assertEquals("optional", testEntity.optionalString().get()); // Original unchanged
    }
    
    @Test
    void testStringListLens() {
        List<String> newList = List.of("x", "y", "z");
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringList(), newList)
            .apply();
        
        assertEquals(newList, updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    @Test
    void testStringListElementLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringList().get(0), "updated")
            .apply();
        
        assertEquals(List.of("updated", "b", "c"), updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    @Test
    void testStringMapKeyLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringMap().key("str1"), "updated value")
            .apply();
        
        assertEquals(Map.of("str1", "updated value", "str2", "value2"), updated.stringMap());
        assertEquals(Map.of("str1", "value1", "str2", "value2"), testEntity.stringMap()); // Original unchanged
    }
    
    
    @Test
    void testModifyStringValue() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.stringValue(), s -> s.toUpperCase())
            .apply();
        
        assertEquals("HELLO", updated.stringValue());
        assertEquals("hello", testEntity.stringValue()); // Original unchanged
    }
    
    
    @Test
    void testModifyStringList() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.stringList(), list -> List.of(list.get(0).toUpperCase(), list.get(1).toUpperCase(), list.get(2).toUpperCase()))
            .apply();
        
        assertEquals(List.of("A", "B", "C"), updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    @Test
    void testModifyOptionalString() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.optionalString(), opt -> opt.map(String::toUpperCase))
            .apply();
        
        assertTrue(updated.optionalString().isPresent());
        assertEquals("OPTIONAL", updated.optionalString().get());
        assertEquals("optional", testEntity.optionalString().get()); // Original unchanged
    }
    
    // CHAINED OPERATIONS TESTS
    
    @Test
    void testChainedOperations() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringValue(), "chained")
            .set(DomainEntityLens.optionalString(), Optional.of("chained optional"))
            .set(DomainEntityLens.stringList().get(0), "updated")
            .set(DomainEntityLens.stringMap().key("str1"), "chained value")
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
            .set(DomainEntityLens.stringValue(), "test")
            .mod(DomainEntityLens.stringValue(), s -> s + "_modified")
            .set(DomainEntityLens.optionalString(), Optional.of("optional_test"))
            .mod(DomainEntityLens.optionalString(), opt -> opt.map(s -> s.toUpperCase()))
            .apply();
        
        assertEquals("test_modified", updated.stringValue()); // Set to "test", then append "_modified"
        assertEquals("OPTIONAL_TEST", updated.optionalString().get()); // Set then uppercase
        
        // Original unchanged
        assertEquals("hello", testEntity.stringValue());
        assertEquals("optional", testEntity.optionalString().get());
    }
    
    @Test
    void testNestedValueLensWithFriendlySyntax() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested().nestedValue(), "updatedNested")
            .apply();
        
        assertEquals("updatedNested", updated.nested().nestedValue());
        assertEquals("nestedValue", testEntity.nested().nestedValue()); // Original unchanged
    }
    
    @Test
    void testMoreNestedValueLensWithFriendlySyntax() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested().moreNested().moreNestedValue(), "updatedMoreNested")
            .apply();
        
        assertEquals("updatedMoreNested", updated.nested().moreNested().moreNestedValue());
        assertEquals("moreNestedValue", testEntity.nested().moreNested().moreNestedValue()); // Original unchanged
    }
    
    @Test
    void testReplaceNestedEntity() {
        MoreNested newMoreNested = new MoreNested("newMoreNestedValue");
        Nested newNested = new Nested("newNestedValue", newMoreNested);
        
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nested(), newNested)
            .apply();
        
        assertEquals("newNestedValue", updated.nested().nestedValue());
        assertEquals("newMoreNestedValue", updated.nested().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("nestedValue", testEntity.nested().nestedValue());
        assertEquals("moreNestedValue", testEntity.nested().moreNested().moreNestedValue());
    }
    
    @Test
    void testOptionalNestedLens() {
        MoreNested newMoreNested = new MoreNested("newOptionalMoreNestedValue");
        Nested newOptionalNested = new Nested("newOptionalNestedValue", newMoreNested);
        
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested(), Optional.of(newOptionalNested))
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
            .set(DomainEntityLens.optionalNested(), Optional.empty())
            .apply();
        
        assertTrue(updated.optionalNested().isEmpty());
        assertTrue(testEntity.optionalNested().isPresent()); // Original unchanged
    }
    
    @Test
    void testOptionalNestedDeepAccess() {
        // Create a test entity with empty optional nested first
        DomainEntity entityWithEmpty = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested(), Optional.empty())
            .apply();
        
        // Add a new optional nested entity
        MoreNested newMoreNested = new MoreNested("deepOptionalValue");
        Nested newNested = new Nested("deepNestedValue", newMoreNested);
        
        DomainEntity updated = DomainEntityLens.on(entityWithEmpty)
            .set(DomainEntityLens.optionalNested(), Optional.of(newNested))
            .apply();
        
        assertTrue(updated.optionalNested().isPresent());
        assertEquals("deepNestedValue", updated.optionalNested().get().nestedValue());
        assertEquals("deepOptionalValue", updated.optionalNested().get().moreNested().moreNestedValue());
        
        // Original entity with empty should be unchanged
        assertTrue(entityWithEmpty.optionalNested().isEmpty());
    }
    
    @Test
    void testOptionalNestedMoreNestedValueAccess() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalNested().moreNested().moreNestedValue(), "updatedOptionalMoreNestedValue")
            .apply();
        
        assertTrue(updated.optionalNested().isPresent());
        assertEquals("optionalNestedValue", updated.optionalNested().get().nestedValue()); // Unchanged
        assertEquals("updatedOptionalMoreNestedValue", updated.optionalNested().get().moreNested().moreNestedValue());
        
        // Original unchanged
        assertEquals("optionalMoreNestedValue", testEntity.optionalNested().get().moreNested().moreNestedValue());
    }
    
    // NESTED LIST TESTS
    
    @Test
    void testNestedListLens() {
        List<Nested> newNestedList = List.of(
            new Nested("newItem1", new MoreNested("newMoreNested1")),
            new Nested("newItem2", new MoreNested("newMoreNested2"))
        );
        
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedList(), newNestedList)
            .apply();
        
        assertEquals(newNestedList, updated.nestedList());
        assertEquals("listItem1", testEntity.nestedList().get(0).nestedValue()); // Original unchanged
    }
    
    @Test
    void testNestedListElementAccess() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedList().nested(0).nestedValue(), "updatedListItem")
            .apply();
        
        assertEquals("updatedListItem", updated.nestedList().get(0).nestedValue());
        assertEquals("listMoreNested1", updated.nestedList().get(0).moreNested().moreNestedValue()); // Unchanged
        assertEquals("listItem2", updated.nestedList().get(1).nestedValue()); // Other elements unchanged
        
        // Original unchanged
        assertEquals("listItem1", testEntity.nestedList().get(0).nestedValue());
    }
    
    @Test
    void testNestedListMoreNestedAccess() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedList().nested(1).moreNested().moreNestedValue(), "updatedListMoreNested")
            .apply();
        
        assertEquals("listItem2", updated.nestedList().get(1).nestedValue()); // Unchanged
        assertEquals("updatedListMoreNested", updated.nestedList().get(1).moreNested().moreNestedValue());
        assertEquals("listItem1", updated.nestedList().get(0).nestedValue()); // Other elements unchanged
        
        // Original unchanged
        assertEquals("listMoreNested2", testEntity.nestedList().get(1).moreNested().moreNestedValue());
    }
    
    // NESTED MAP TESTS
    
    @Test
    void testNestedMapLens() {
        Map<String, Nested> newNestedMap = Map.of(
            "newKey1", new Nested("newMapItem1", new MoreNested("newMapMoreNested1")),
            "newKey2", new Nested("newMapItem2", new MoreNested("newMapMoreNested2"))
        );
        
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedMap(), newNestedMap)
            .apply();
        
        assertEquals(newNestedMap, updated.nestedMap());
        assertEquals("mapItem1", testEntity.nestedMap().get("key1").nestedValue()); // Original unchanged
    }
    
    @Test
    void testNestedMapKeyAccess() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedMap().nested("key1").nestedValue(), "updatedMapItem")
            .apply();
        
        assertEquals("updatedMapItem", updated.nestedMap().get("key1").nestedValue());
        assertEquals("mapMoreNested1", updated.nestedMap().get("key1").moreNested().moreNestedValue()); // Unchanged
        assertEquals("mapItem2", updated.nestedMap().get("key2").nestedValue()); // Other entries unchanged
        
        // Original unchanged
        assertEquals("mapItem1", testEntity.nestedMap().get("key1").nestedValue());
    }
    
    @Test
    void testNestedMapMoreNestedAccess() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedMap().nested("key2").moreNested().moreNestedValue(), "updatedMapMoreNested")
            .apply();
        
        assertEquals("mapItem2", updated.nestedMap().get("key2").nestedValue()); // Unchanged
        assertEquals("updatedMapMoreNested", updated.nestedMap().get("key2").moreNested().moreNestedValue());
        assertEquals("mapItem1", updated.nestedMap().get("key1").nestedValue()); // Other entries unchanged
        
        // Original unchanged
        assertEquals("mapMoreNested2", testEntity.nestedMap().get("key2").moreNested().moreNestedValue());
    }
    
    // CHAINED OPERATIONS WITH NESTED COLLECTIONS
    
    @Test
    void testChainedOperationsWithNestedCollections() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.nestedList().nested(0).nestedValue(), "chainedListUpdate")
            .set(DomainEntityLens.nestedMap().nested("key1").moreNested().moreNestedValue(), "chainedMapUpdate")
            .set(DomainEntityLens.nested().nestedValue(), "chainedNestedUpdate")
            .apply();
        
        assertEquals("chainedListUpdate", updated.nestedList().get(0).nestedValue());
        assertEquals("chainedMapUpdate", updated.nestedMap().get("key1").moreNested().moreNestedValue());
        assertEquals("chainedNestedUpdate", updated.nested().nestedValue());
        
        // Original unchanged
        assertEquals("listItem1", testEntity.nestedList().get(0).nestedValue());
        assertEquals("mapMoreNested1", testEntity.nestedMap().get("key1").moreNested().moreNestedValue());
        assertEquals("nestedValue", testEntity.nested().nestedValue());
    }
}
