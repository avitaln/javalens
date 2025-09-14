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
            // Primitives
            42, 123L, 3.14, true, "hello",
            // Optionals
            Optional.of(100), Optional.of(200L), Optional.of(2.71), Optional.of(false), Optional.of("optional"),
            // Lists
            List.of(1, 2, 3), List.of(10L, 20L, 30L), List.of(1.1, 2.2, 3.3), List.of(true, false, true), List.of("a", "b", "c"),
            // Maps
            Map.of("key1", 1, "key2", 2), Map.of("long1", 10L, "long2", 20L), 
            Map.of("double1", 1.1, "double2", 2.2), Map.of("bool1", true, "bool2", false),
            Map.of("str1", "value1", "str2", "value2")
        );
    }
    
    // PRIMITIVE LENS TESTS
    
    @Test
    void testIntValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.intValue, 999)
            .apply();
        
        assertEquals(999, updated.intValue());
        assertEquals(42, testEntity.intValue()); // Original unchanged
        
        // All other fields should remain the same
        assertEquals(testEntity.longValue(), updated.longValue());
        assertEquals(testEntity.stringValue(), updated.stringValue());
    }
    
    @Test
    void testLongValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.longValue, 888L)
            .apply();
        
        assertEquals(888L, updated.longValue());
        assertEquals(123L, testEntity.longValue()); // Original unchanged
    }
    
    @Test
    void testDoubleValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.doubleValue, 9.99)
            .apply();
        
        assertEquals(9.99, updated.doubleValue(), 0.001);
        assertEquals(3.14, testEntity.doubleValue(), 0.001); // Original unchanged
    }
    
    @Test
    void testBooleanValueLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.booleanValue, false)
            .apply();
        
        assertFalse(updated.booleanValue());
        assertTrue(testEntity.booleanValue()); // Original unchanged
    }
    
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
    void testOptionalIntLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalInt, Optional.of(777))
            .apply();
        
        assertTrue(updated.optionalInt().isPresent());
        assertEquals(777, updated.optionalInt().get());
        assertEquals(100, testEntity.optionalInt().get()); // Original unchanged
    }
    
    @Test
    void testOptionalIntLensSetEmpty() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalInt, Optional.empty())
            .apply();
        
        assertTrue(updated.optionalInt().isEmpty());
        assertTrue(testEntity.optionalInt().isPresent()); // Original unchanged
    }
    
    @Test
    void testOptionalLongLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalLong, Optional.of(666L))
            .apply();
        
        assertTrue(updated.optionalLong().isPresent());
        assertEquals(666L, updated.optionalLong().get());
        assertEquals(200L, testEntity.optionalLong().get()); // Original unchanged
    }
    
    @Test
    void testOptionalDoubleLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalDouble, Optional.of(7.77))
            .apply();
        
        assertTrue(updated.optionalDouble().isPresent());
        assertEquals(7.77, updated.optionalDouble().get(), 0.001);
        assertEquals(2.71, testEntity.optionalDouble().get(), 0.001); // Original unchanged
    }
    
    @Test
    void testOptionalBooleanLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.optionalBoolean, Optional.of(true))
            .apply();
        
        assertTrue(updated.optionalBoolean().isPresent());
        assertTrue(updated.optionalBoolean().get());
        assertFalse(testEntity.optionalBoolean().get()); // Original unchanged
    }
    
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
    void testIntListLens() {
        List<Integer> newList = List.of(7, 8, 9);
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.intList.lens, newList)
            .apply();
        
        assertEquals(newList, updated.intList());
        assertEquals(List.of(1, 2, 3), testEntity.intList()); // Original unchanged
    }
    
    @Test
    void testIntListElementLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.intList.get(1), 999)
            .apply();
        
        assertEquals(List.of(1, 999, 3), updated.intList());
        assertEquals(List.of(1, 2, 3), testEntity.intList()); // Original unchanged
    }
    
    @Test
    void testLongListLens() {
        List<Long> newList = List.of(70L, 80L, 90L);
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.longList.lens, newList)
            .apply();
        
        assertEquals(newList, updated.longList());
        assertEquals(List.of(10L, 20L, 30L), testEntity.longList()); // Original unchanged
    }
    
    @Test
    void testDoubleListLens() {
        List<Double> newList = List.of(7.7, 8.8, 9.9);
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.doubleList.lens, newList)
            .apply();
        
        assertEquals(newList, updated.doubleList());
        assertEquals(List.of(1.1, 2.2, 3.3), testEntity.doubleList()); // Original unchanged
    }
    
    @Test
    void testBooleanListLens() {
        List<Boolean> newList = List.of(false, false, false);
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.booleanList.lens, newList)
            .apply();
        
        assertEquals(newList, updated.booleanList());
        assertEquals(List.of(true, false, true), testEntity.booleanList()); // Original unchanged
    }
    
    @Test
    void testStringListLens() {
        List<String> newList = List.of("x", "y", "z");
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringList.lens, newList)
            .apply();
        
        assertEquals(newList, updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    @Test
    void testStringListElementLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringList.get(0), "updated")
            .apply();
        
        assertEquals(List.of("updated", "b", "c"), updated.stringList());
        assertEquals(List.of("a", "b", "c"), testEntity.stringList()); // Original unchanged
    }
    
    // MAP LENS TESTS
    
    @Test
    void testIntMapLens() {
        Map<String, Integer> newMap = Map.of("new1", 11, "new2", 22);
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.intMap.lens, newMap)
            .apply();
        
        assertEquals(newMap, updated.intMap());
        assertEquals(Map.of("key1", 1, "key2", 2), testEntity.intMap()); // Original unchanged
    }
    
    @Test
    void testIntMapKeyLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.intMap.key("key1"), 999)
            .apply();
        
        assertEquals(Map.of("key1", 999, "key2", 2), updated.intMap());
        assertEquals(Map.of("key1", 1, "key2", 2), testEntity.intMap()); // Original unchanged
    }
    
    @Test
    void testLongMapLens() {
        Map<String, Long> newMap = Map.of("newLong1", 111L, "newLong2", 222L);
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.longMap.lens, newMap)
            .apply();
        
        assertEquals(newMap, updated.longMap());
        assertEquals(Map.of("long1", 10L, "long2", 20L), testEntity.longMap()); // Original unchanged
    }
    
    @Test
    void testDoubleMapKeyLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.doubleMap.key("double1"), 9.99)
            .apply();
        
        assertEquals(Map.of("double1", 9.99, "double2", 2.2), updated.doubleMap());
        assertEquals(Map.of("double1", 1.1, "double2", 2.2), testEntity.doubleMap()); // Original unchanged
    }
    
    @Test
    void testBooleanMapKeyLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.booleanMap.key("bool1"), false)
            .apply();
        
        assertEquals(Map.of("bool1", false, "bool2", false), updated.booleanMap());
        assertEquals(Map.of("bool1", true, "bool2", false), testEntity.booleanMap()); // Original unchanged
    }
    
    @Test
    void testStringMapKeyLens() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.stringMap.key("str1"), "updated value")
            .apply();
        
        assertEquals(Map.of("str1", "updated value", "str2", "value2"), updated.stringMap());
        assertEquals(Map.of("str1", "value1", "str2", "value2"), testEntity.stringMap()); // Original unchanged
    }
    
    // MODIFICATION TESTS
    
    @Test
    void testModifyIntValue() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.intValue, x -> x * 2)
            .apply();
        
        assertEquals(84, updated.intValue()); // 42 * 2
        assertEquals(42, testEntity.intValue()); // Original unchanged
    }
    
    @Test
    void testModifyStringValue() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.stringValue, s -> s.toUpperCase())
            .apply();
        
        assertEquals("HELLO", updated.stringValue());
        assertEquals("hello", testEntity.stringValue()); // Original unchanged
    }
    
    @Test
    void testModifyIntList() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .mod(DomainEntityLens.intList.lens, list -> List.of(list.get(0) + 10, list.get(1) + 10, list.get(2) + 10))
            .apply();
        
        assertEquals(List.of(11, 12, 13), updated.intList());
        assertEquals(List.of(1, 2, 3), testEntity.intList()); // Original unchanged
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
            .set(DomainEntityLens.intValue, 999)
            .set(DomainEntityLens.stringValue, "chained")
            .set(DomainEntityLens.optionalInt, Optional.of(777))
            .set(DomainEntityLens.intList.get(0), 100)
            .set(DomainEntityLens.intMap.key("key1"), 500)
            .apply();
        
        assertEquals(999, updated.intValue());
        assertEquals("chained", updated.stringValue());
        assertEquals(777, updated.optionalInt().get());
        assertEquals(List.of(100, 2, 3), updated.intList());
        assertEquals(Map.of("key1", 500, "key2", 2), updated.intMap());
        
        // Original should be unchanged
        assertEquals(42, testEntity.intValue());
        assertEquals("hello", testEntity.stringValue());
        assertEquals(100, testEntity.optionalInt().get());
        assertEquals(List.of(1, 2, 3), testEntity.intList());
        assertEquals(Map.of("key1", 1, "key2", 2), testEntity.intMap());
    }
    
    @Test
    void testMixedSetAndMod() {
        DomainEntity updated = DomainEntityLens.on(testEntity)
            .set(DomainEntityLens.intValue, 100)
            .mod(DomainEntityLens.intValue, x -> x + 50)
            .set(DomainEntityLens.stringValue, "test")
            .mod(DomainEntityLens.stringValue, s -> s + "_modified")
            .apply();
        
        assertEquals(150, updated.intValue()); // Set to 100, then add 50
        assertEquals("test_modified", updated.stringValue()); // Set to "test", then append "_modified"
        
        // Original unchanged
        assertEquals(42, testEntity.intValue());
        assertEquals("hello", testEntity.stringValue());
    }
}
