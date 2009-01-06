package com;

public class StartClass {
    public static void main(String[] args) {
        TargetClass bean = new TargetClass();
        System.out.println("IS-A NameAware: " + (bean instanceof NameAware));
        System.out.println("IS-A NameManager: " + (bean instanceof
NameManager));
        System.out.println("");
        System.out.println("Calling get() ...");
        ((NameManager)bean).getName();
        System.out.println("Done.");
        System.out.println("");
        System.out.println("Calling set() ... ");
        ((NameAware)bean).setName("asd");
        System.out.println("Done.");
    }
}

