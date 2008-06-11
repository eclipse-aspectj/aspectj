public aspect X {
  void around(): call(* m()) { System.out.println("advice running"); proceed();}
}
