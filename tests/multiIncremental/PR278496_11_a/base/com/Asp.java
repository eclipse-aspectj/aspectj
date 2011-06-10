package com;

aspect Asp {

  before(Dibble d): execution(* *(..)) && args(d) {
  }

}
