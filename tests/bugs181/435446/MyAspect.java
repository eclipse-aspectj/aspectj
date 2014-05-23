package de.scrum_master.aspect;

import de.scrum_master.app.Application;

public aspect MyAspect {
  before() : execution(* Application.lambda$0(..)) {
//     System.out.println(thisJoinPointStaticPart);
  }
}
