
package myprofile;

import profile.Profile;

public aspect MyProfile extends Profile {
    protected pointcut withinSystemClasses() : 
        within(java..*) || within(javax..*) || within(com.sun..*);

    /** blunt: target all method executions outside system classes */
    protected pointcut targets() : !withinSystemClasses() 
        && execution(* *(..));
}
