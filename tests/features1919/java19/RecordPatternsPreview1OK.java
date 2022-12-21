public class RecordPatternsPreview1OK {
  static void printGenericBoxString1(Box<Object> objectBox) {
    if (objectBox instanceof Box<Object>(String s)) {
      System.out.println(s);
    }
  }

  static void printGenericBoxString2(Box<String> stringBox) {
    if (stringBox instanceof Box<String>(var s)) {
      System.out.println(s);
    }
  }
}

record Box<T>(T t) {}
