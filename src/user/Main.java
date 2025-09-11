package user;

import model.Address;
import model.Person;
import model.PersonLens;
import model.Pet;

public class Main {
    public static void main(String[] args) {
        java.util.List<Person> kids = java.util.List.of(
                new Person("KidA", new Address("TA", "A st", java.util.List.of("school")), java.util.List.of(), java.util.List.of("tagA"), java.util.Map.of()),
                new Person("KidB", new Address("TA", "B st", java.util.List.of("park")), java.util.List.of(), java.util.List.of("tagB"), java.util.Map.of())
        );
        Person person = new Person("John", new Address("Tel Aviv", "Herzl 1", java.util.List.of("home")), kids, java.util.List.of("parent"), java.util.Map.of(
                "rex", new Pet("Rex", "dog"),
                "mimi", new Pet("Mimi", "cat")
        ));

        // Batch using bound builder syntax
        Person updatedPerson = PersonLens.on(person)
                .set(PersonLens.name, "Batchy")
                .set(PersonLens.address.city, "LA")
                .mod(PersonLens.tags, ts -> java.util.List.of("gold"))
                .set(PersonLens.children.get(0).name, "BatchKid")
                .set(PersonLens.pets.at("rex").name, "Rexy")
                .set(PersonLens.pets.at("mimi").type, "kitty")
                .set(PersonLens.address.tags, java.util.List.of("sea", "quiet"))
                .apply();

        System.out.println("original: " + person);
        System.out.println("after batch mutations: " + updatedPerson);
    }
}