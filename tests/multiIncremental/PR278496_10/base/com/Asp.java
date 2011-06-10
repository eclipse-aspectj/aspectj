package com;

aspect Asp {
  before(String[] ss): execution(* *(..)) && args(ss) {
  }
  before(int[] ss): execution(* *(..)) && args(ss) {
  }
  before(float[][] ss): execution(* *(..)) && args(ss) {
  }
}
