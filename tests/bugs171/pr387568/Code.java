import java.lang.annotation.*;


class AAA {
  public void m() {
    Color[] cs = Color.values();
  }
}

aspect Foo {
  after(): @annotation(Anno) {}
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

enum Color {R,G,B;}
