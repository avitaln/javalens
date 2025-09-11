public final class PersonLens {

    public static final Lens<Person, String> name = Lens.of(
            Person::name,
            (person, newName) -> new Person(newName, person.address(), person.children(), person.tags(), person.pets())
    );

    public static final Lens<Person, java.util.List<String>> tags = Lens.of(
            Person::tags,
            (person, newTags) -> new Person(person.name(), person.address(), person.children(), newTags, person.pets())
    );

    public static final AddressNode address = new AddressNode();

    public static final class AddressNode {
        private final Lens<Person, Address> lens = Lens.of(
                Person::address,
                (person, newAddress) -> new Person(person.name(), newAddress, person.children(), person.tags(), person.pets())
        );

        public Address get(Person person) {
            return lens.get(person);
        }

        public Person set(Person person, Address newAddress) {
            return lens.set(person, newAddress);
        }

        public final Lens<Person, String> city = lens.andThen(
                Lens.of(
                        Address::city,
                        (address, newCity) -> new Address(newCity, address.street(), address.tags())
                )
        );

        public final Lens<Person, String> street = lens.andThen(
                Lens.of(
                        Address::street,
                        (address, newStreet) -> new Address(address.city(), newStreet, address.tags())
                )
        );

        public final Lens<Person, java.util.List<String>> tags = lens.andThen(
                Lens.of(
                        Address::tags,
                        (address, newTags) -> new Address(address.city(), address.street(), newTags)
                )
        );
    }

    public static final ChildrenNode children = new ChildrenNode();

    public static ChildNode childAt(int index) { return children.at(index); }

    public static final class ChildrenNode {
        private final Lens<Person, java.util.List<Person>> lens = Lens.of(
                Person::children,
                (person, newChildren) -> new Person(person.name(), person.address(), newChildren, person.tags(), person.pets())
        );

        public java.util.List<Person> get(Person person) { return lens.get(person); }
        public Person set(Person person, java.util.List<Person> newChildren) { return lens.set(person, newChildren); }

        public ChildNode at(int index) { return new ChildNode(index); }
        public ChildNode get(int index) { return at(index); }

        public Lens<Person, Person> lensAt(int index) {
            return lens.andThen(ListLens.index(index));
        }
    }

    public static final PetsNode pets = new PetsNode();

    public static final class PetsNode {
        private final Lens<Person, java.util.Map<String, Pet>> lens = Lens.of(
                Person::pets,
                (person, newPets) -> new Person(person.name(), person.address(), person.children(), person.tags(), newPets)
        );

        public java.util.Map<String, Pet> get(Person person) { return lens.get(person); }
        public Person set(Person person, java.util.Map<String, Pet> newPets) { return lens.set(person, newPets); }

        public PetNode at(String key) { return new PetNode(key); }

        public Lens<Person, Pet> lensAt(String key) { return lens.andThen(MapLens.key(key)); }
    }

    public static final class PetNode {
        private final Lens<Person, Pet> lens;
        public final Lens<Person, String> name;
        public final Lens<Person, String> type;

        private PetNode(String key) {
            this.lens = pets.lensAt(key);
            this.name = this.lens.andThen(Lens.of(Pet::name, (pet, newName) -> new Pet(newName, pet.type())));
            this.type = this.lens.andThen(Lens.of(Pet::type, (pet, newType) -> new Pet(pet.name(), newType)));
        }

        public Pet get(Person person) { return lens.get(person); }
        public Person set(Person person, Pet newPet) { return lens.set(person, newPet); }
    }

    public static final class ChildNode {
        private final Lens<Person, Person> lens;
        public final Lens<Person, String> name;

        private ChildNode(int index) {
            this.lens = children.lensAt(index);
            this.name = this.lens.andThen(PersonLens.name);
        }

        public Person get(Person person) {
            return lens.get(person);
        }

        public Person set(Person person, Person newChild) {
            return lens.set(person, newChild);
        }

    }
}


