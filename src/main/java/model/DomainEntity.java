package model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DomainEntity(
    // String primitive
    String stringValue,
    
    // Optional String
    Optional<String> optionalString,
    
    // List of Strings
    List<String> stringList,
    
    // Map from String to String
    Map<String, String> stringMap
) {
}
