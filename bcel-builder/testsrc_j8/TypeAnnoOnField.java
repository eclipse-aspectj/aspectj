public class TypeAnnoOnField {
  @Anno int f1;

  java.util.List<@Anno String> f2;

  java.util.@Anno List[] f3;

  java.util.List<@Anno String>[] f4;
}
