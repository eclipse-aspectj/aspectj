// PR55341
aspect F { 
  private int HW.intField = 999;
}

aspect M {
  public String HW.getMessage () {
    return message;
  }
}
