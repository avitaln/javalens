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

        // Using lenses as requested
        Person updated1 = PersonLens.name.set(person, "someName");
        Person updated2 = PersonLens.address.city.set(updated1, "someCity");
        Address someAddress = new Address("Haifa", "Allenby 2", java.util.List.of("moved"));
        Person updated3 = PersonLens.address.set(updated2, someAddress);

        // Update a child name using nicer node syntax
        Person updatedChild0 = PersonLens.children.get(0).name.set(updated3, "KidA-Renamed");

        // Update tags via lenses
        Person updatedPersonTags = PersonLens.tags.mod(updatedChild0, tags -> {
            java.util.ArrayList<String> t = new java.util.ArrayList<>(tags);
            t.add("vip");
            return java.util.List.copyOf(t);
        });
        Person updatedAddressTags = PersonLens.address.tags.mod(updatedPersonTags, tags -> java.util.List.of("sea", "quiet"));

        // Update a pet inner field via map and pet lenses
        Person updatedPetName = PersonLens.pets.at("rex").name.set(updatedAddressTags, "Rexy");
        Person updatedPetType = PersonLens.pets.at("mimi").type.set(updatedPetName, "kitty");

        // Batch multiple updates using Mutations builder
        Person batch = Mutations.<Person>forType()
                .set(PersonLens.name, "Batchy")
                .set(PersonLens.address.city, "LA")
                .mod(PersonLens.tags, ts -> java.util.List.of("gold"))
                .set(PersonLens.children.get(0).name, "BatchKid")
                .set(PersonLens.pets.at("rex").type, "wolf")
                .apply(updatedPetType);

        System.out.println("original: " + person);
        System.out.println("after name.set: " + updated1);
        System.out.println("after address.city.set: " + updated2);
        System.out.println("after address.set: " + updated3);
        System.out.println("after child[0].name.set: " + updatedChild0);
        System.out.println("after person.tags.mod + address.tags.mod: " + updatedAddressTags);
        System.out.println("after pets updates: " + updatedPetType);
        System.out.println("after batch mutations: " + batch);
    }
}