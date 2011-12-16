aspect PerCflow percflow(execution(* Bar.*(..))) {
}

class Bar {
  void m() { 
    n();
  }
  void n() { 
  }
}
