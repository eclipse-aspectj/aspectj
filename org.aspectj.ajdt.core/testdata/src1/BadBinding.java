aspect A2 {
    after(Object thisObject1): target(thisObject) {  // ERR unbound
    }
    after(Object o1, Object o2): target(o1) || target(o2) { // ERR inconsistent  
    }
    after(Object o1, Object o2): target(o1) && target(o2) { // NO PROB 
    }
    after(Object o1): target(o1) && target(o1) { // ERR multiple 
    }
    after(Object o1): !target(o1) { // ERR can't bind here
    }
    void around(Object o1): target(o1) {
    	proceed(2);  //ERR can't convert from int to Object
    }
    void around(Object o1): target(o1) {
    	proceed(null, 2);  //ERR wrong number of args
    }
    void around(Object o1): target(o1) {
    	proceed();  //ERR wrong number of args
    }

}
