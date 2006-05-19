package a;

public aspect Aspect {

  // will generate a closure class...
  String around(String in) : execution(String A.*(..)) && args(in) {
    String ret = proceed(in.toLowerCase());
    ret = proceed(in.toUpperCase());
    return ret+" dada!";
  }


}