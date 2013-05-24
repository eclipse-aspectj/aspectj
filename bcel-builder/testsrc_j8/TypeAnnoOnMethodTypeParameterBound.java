class TypeAnnoOnMethodTypeParameterBound {
  <T extends @Anno String & java.util.@Anno(2) Map<Integer, @Anno(3) String>> void m() {}
}
