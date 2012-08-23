import java.lang.annotation.*;

class AAA {
  public void m() {
    Color[] cs = Color.values();
    Color c = Color.valueOf("R");
  }
}

@Anno
aspect Foo {
  after(): @annotation(Anno) {}
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

