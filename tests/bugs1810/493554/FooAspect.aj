package example.aspect;

import example.dep.Dep;

public aspect FooAspect pertarget(setFieldValue(Dep)) {

  // interface ajcMightHaveAspect { }

  pointcut setFieldValue(Dep dep) :
    set(private * Dep.*) && target(dep);

  void around(Dep dep) : setFieldValue(dep) {
System.out.println("advised");
    proceed(dep);
  }

}
