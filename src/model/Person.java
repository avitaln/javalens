package model;

import java.util.List;
import java.util.Map;

public record Person(String name, Address address, List<Person> children, List<String> tags, Map<String, Pet> pets) {
}


