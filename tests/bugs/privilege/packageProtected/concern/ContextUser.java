package packageProtected.concern;

import packageProtected.core.Base;

/** @testcase PR#XXX omnibus privileged access */
public privileged aspect ContextUser {
   pointcut call2trigger(Base argument):
      execution(* Base.trigger(..))
      && args(argument);
      
   before(Base base): call2trigger(base) {
      int i = base.private_int 
        + base.default_int
       + base.protected_int
       + base.public_int;
      base.private_method();
      base.default_method();
      base.protected_method(); 
      base.public_method();

      base.private_method(null);
      base.default_method(null);
      base.protected_method(null); 
      base.public_method(null);

      base.private_method(null, null);
      base.default_method(null, null);
      base.protected_method(null, null); 
      base.public_method(null, null);


      i = Base.private_staticInt 
        + Base.default_staticInt
       + Base.protected_staticInt
       + Base.public_staticInt;
      Base.private_staticMethod();
      Base.default_staticMethod();
      Base.protected_staticMethod(); 
      Base.public_staticMethod();

      Base.private_staticMethod(null);
      Base.default_staticMethod(null);
      Base.protected_staticMethod(null); 
      Base.public_staticMethod(null);

      Base.private_staticMethod(null, null);
      Base.default_staticMethod(null, null);
      Base.protected_staticMethod(null, null); 
      Base.public_staticMethod(null, null);
   }  
   pointcut call2trigger_samePackage(BaseTarget arg):
      execution(* BaseTarget.trigger(..))
      && args(arg);
   
   before(BaseTarget base): call2trigger_samePackage(base) {
      
      int i = base.private_int 
        + base.default_int
       + base.protected_int
       + base.public_int;
      base.private_method();
      base.default_method();
      base.protected_method(); 
      base.public_method();

      base.private_method(null);
      base.default_method(null);
      base.protected_method(null); 
      base.public_method(null);

      base.private_method(null, null);
      base.default_method(null, null);
      base.protected_method(null, null); 
      base.public_method(null, null);

      i = BaseTarget.private_staticInt 
        + BaseTarget.default_staticInt
       + BaseTarget.protected_staticInt
       + BaseTarget.public_staticInt;
      BaseTarget.private_staticMethod();
      BaseTarget.default_staticMethod();
      BaseTarget.protected_staticMethod(); 
      BaseTarget.public_staticMethod();

      BaseTarget.private_staticMethod(null);
      BaseTarget.default_staticMethod(null);
      BaseTarget.protected_staticMethod(null); 
      BaseTarget.public_staticMethod(null);

      BaseTarget.private_staticMethod(null, null);
      BaseTarget.default_staticMethod(null, null);
      BaseTarget.protected_staticMethod(null, null); 
      BaseTarget.public_staticMethod(null, null);
   }  
}