
package recursivepackage;

public aspect RecursiveCatcher {

       pointcut recur() : 
           call(public void 
                           *.recursiveCall(int));


       before(): recur() {
           // empty
       }


        public void recursiveCall(int i) {  // marker is here
                                recursiveCall(i);  // marker should be here
        }

}

