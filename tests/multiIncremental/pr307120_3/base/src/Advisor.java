aspect Advisor {
//  Object around(Object o): get(@Anno * *) && this(o) {
//    return proceed(o);
//  }

  Object around(Object o,Object newval): set(@Anno * *) && this(o) && args(newval) {
    return proceed(o,newval);
  }
}
