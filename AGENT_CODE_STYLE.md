# Kodstil för agenter

Skriv kod som är lätt för en nybörjare att läsa och följa steg för steg.

## Föredra

- Använd vanliga `for`- och `while`-loopar när du går igenom listor eller upprepar arbete.
- Använd tydliga `if`- och `else`-satser för villkor.
- Dela upp arbetet i enkla steg med beskrivande variabelnamn.
- Håll metoder begripliga och fokuserade på en uppgift.
- Välj den enklaste lösningen som passar projektets befintliga kod.

## Undvik när en enkel lösning fungerar

- Streams och kedjor av funktioner som `map`, `filter` och `reduce`.
- Kompakta villkorsuttryck som ternary (`villkor ? a : b`). Skriv hellre `if` och `else`.
- Nästlade uttryck, smarta genvägar och abstraktioner som gör flödet svårare att följa.
- Att pressa ihop flera steg på en rad.

## Exempel

### Gå igenom en lista

Bra:

```java
int total = 0;

for (int i = 0; i < numbers.size(); i++) {
    int number = numbers.get(i);
    total = total + number;
}
```

Undvik:

```java
int total = numbers.stream().mapToInt(Integer::intValue).sum();
```

### Välja ett värde med ett villkor

Bra:

```java
String message;

if (age >= 18) {
    message = "Vuxen";
} else {
    message = "Minderårig";
}
```

Undvik:

```java
String message = age >= 18 ? "Vuxen" : "Minderårig";
```

### Skriva JUnit-test

Följ JUnit-stilen i HUNDREGISTERFINAL, till exempel i `DR02OwnerTest.java` och
`DR03DogTest.java`. Använd beskrivande `@DisplayName` och separata steg för att
skapa objektet och kontrollera resultatet. När flera indata testar samma sak,
använd ett parametriserat test.

Bra:

```java
@ParameterizedTest(name = "{0} år")
@ValueSource(ints = { 1, 2, 3 })
@DisplayName("Konstruktorn sätter åldern")
public void ctrSetsAge(int age) {
    Dog dog = new Dog("Fido", "Puli", age, 5);

    assertEquals(age, dog.getAge());
}
```

Om projektet redan har egna testhjälpare eller assertions, använd dem i samma
stil som de befintliga testfallen.

Undvik:

```java
@Test
public void test() {
    List.of(1, 2, 3).stream()
            .map(age -> new Dog("Fido", "Puli", age, 5))
            .forEach(dog -> assertTrue(dog.getAge() > 0));
}
```

Det andra testet döljer testvärdena i en stream, använder ett otydligt namn och
kontrollerar bara att åldern är positiv. Skriv ett test som tydligt visar vad
som förväntas för varje indata.

Använd ändå språkets eller projektets befintliga mönster när det behövs för att
koden ska fungera och passa in. Om två lösningar fungerar lika bra, välj den
som är tydligast för en nybörjare.
