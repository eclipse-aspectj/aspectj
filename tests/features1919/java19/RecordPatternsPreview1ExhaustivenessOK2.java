public class RecordPatternsPreview1ExhaustivenessOK2 {
  public static void main(String[] args) {
    Person person = new Person("Bob", 12);
    switch (person) {
      case Person(var name, var age) -> System.out.println(name + " " + age);
    }
  }
}

record Person(String name, int age) { }
