
package org.smart.persist;

aspect Persist {

    after(String string) returning : set(public String *)
        && !target(Persist) && args(string) {
        String name = thisJoinPoint.getSignature().getName();
        System.out.println("set " + name + " to " + string);            
    }
}