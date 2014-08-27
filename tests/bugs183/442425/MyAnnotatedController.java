package de.scrum_master.app;

import java.lang.reflect.Method;

@EntityController
public class MyAnnotatedController<T> {
    public void doSomething() {
        System.out.println("Doing something");
    }

    public static void main(String[] args) {
        // Use class type directly so as to call its method
        MyAnnotatedController<String> annotatedTextController = new MyAnnotatedController<>();
        annotatedTextController.doSomething();

        // Print all declared methods (should also show interface methods introduced via ITD)
        for (Method method : annotatedTextController.getClass().getDeclaredMethods()) {
            if (!method.getName().startsWith("ajc$"))
                System.out.println(method);
        }

        // Prove that class type is compatible with interface type
        //IEntityController<String> entityTextController = annotatedTextController;
        //entityTextController.setEntity("foo");
        // Would not work here because generic interface type is type-safe:
        // entityNumberController.setEntity(123);
        //System.out.println("Entity value = " + entityTextController.getEntity());

        // Create another object and directly assign it to interface type
        //IEntityController<Integer> entityNumberController = new MyAnnotatedController<>();
        //entityNumberController.setEntity(123);
        // Would not work here because generic interface type is type-safe:
        // entityNumberController.setEntity("foo");
        //System.out.println("Entity value = " + entityNumberController.getEntity());
    }
}

