import java.util.Arrays;

public aspect NegatedTypeAspect {
  before(): execution(!void get*()) {
    System.out.println("[GETTER] " + thisJoinPoint);
  }

  before(): execution(!String get*()) {
    System.out.println("[NON-STRING GETTER] " + thisJoinPoint);
  }

  before(): execution(String[] get*()) {
    System.out.println("[STRING-ARRAY GETTER] " + thisJoinPoint);
  }

  before(): execution(!String[] get*()) {
    System.out.println("[NON-STRING-ARRAY GETTER] " + thisJoinPoint);
  }

  before(): execution(!String[][] get*()) {
    System.out.println("[NON-STRING-ARRAY-ARRAY GETTER] " + thisJoinPoint);
  }

  before(): execution(void set*(*)) {
    System.out.println("[SETTER] " + thisJoinPoint);
  }

  public static void main(String[] args) {
    Person person = new Person();
    person.setId(11);
    person.setFirstName("Marie");
    person.setLastName("Curie");
    System.out.println(person);
    person.getId();
    person.getFirstName();
    person.getLastName();
    System.out.println(person.getFullName(false));
    person.setFullName("Albert Einstein");
    person.setId(22);
    System.out.println(person);
    System.out.println(person.getFullName(true));
    person.getVoid();
    System.out.println(Arrays.deepToString(person.getStringArray()));
    System.out.println(Arrays.deepToString(person.getStringArrayArray()));
    System.out.println(person.setSomething("something"));
  }
}

class Person {
  private int id;
  private String lastName;
  private String firstName;

  // Bean getters/setters, matched by aspect

  // Non-string getter, matched by corresponding pointcut
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  // Non-string getter (String[] != String)
  public String[] getStringArray() {
    return new String[] {"Hello", "world"};
  }

  // Non-string, non-string-array getter (String[] != String, String[] != String[][])
  public String[][] getStringArrayArray() {
    return new String[][] {{"Hello", "world"}, {"Hallo", "Welt"}};
  }

  // Non-bean getters/setters, not matched by aspect

  public String getFullName(boolean lastNameFirst) {
    return lastNameFirst
      ? lastName + ", " + firstName
      : firstName + " " + lastName;
  }

  public void setFullName(String fullName) {
    boolean lastNameFirst = fullName.contains(",");
    String[] nameParts = fullName.split("[, ]+");
    if (lastNameFirst) {
      firstName = nameParts[1];
      lastName = nameParts[0];
    } else {
      firstName = nameParts[0];
      lastName = nameParts[1];
    }
  }

  public String setSomething(String something) {
    return "AspectJ rules!";
  }

  // Non-string getter, matched by corresponding pointcut
  public void getVoid() {}

  // Other methods, not matched by aspect

  @Override
  public String toString() {
    return "Person(" + "id=" + id + ", lastName='" + lastName + '\'' + ", firstName='" + firstName + '\'' + ')';
  }
}
