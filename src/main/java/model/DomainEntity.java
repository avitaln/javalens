package model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DomainEntity(
    String stringValue,
    Optional<String> optionalString,
    List<String> stringList,
    Map<String, String> stringMap
) {
}
