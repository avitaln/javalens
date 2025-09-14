import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import model.DomainEntity;
import model.DomainEntityLens;

public class DomainEntityLensTest {
    
    private DomainEntity testEntity;
    
    @BeforeEach
    void setUp() {
        testEntity = new DomainEntity(
            "hello",
            Optional.of("optional"),
            List.of("a", "b", "c"),
            Map.of("str1", "value1", "str2", "value2")
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
}
