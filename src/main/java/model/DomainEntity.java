package model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DomainEntity(
    // Primitives
    int intValue,
    long longValue,
    double doubleValue,
    boolean booleanValue,
    String stringValue,
    
    // Optionals of primitives
    Optional<Integer> optionalInt,
    Optional<Long> optionalLong,
    Optional<Double> optionalDouble,
    Optional<Boolean> optionalBoolean,
    Optional<String> optionalString,
    
    // Arrays/Lists of primitives
    List<Integer> intList,
    List<Long> longList,
    List<Double> doubleList,
    List<Boolean> booleanList,
    List<String> stringList,
    
    // Maps from String to primitives
    Map<String, Integer> intMap,
    Map<String, Long> longMap,
    Map<String, Double> doubleMap,
    Map<String, Boolean> booleanMap,
    Map<String, String> stringMap
) {
}
