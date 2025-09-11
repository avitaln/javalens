package model;

import java.util.List;

public record Address(String city, String street, List<String> tags) {
}


