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
    //
    // NOTE: As explained in https://bugs.eclipse.org/bugs/show_bug.cgi?id=442425#c2, @DeclareParents will add the
    // raw form of the parent to the target class, not the generic one. Therefore, the additional cast is necessary.
    // Otherwise, AJC would throw:
    //   Type mismatch: cannot convert from MyAnnotatedController<String> to IEntityController<String>
    IEntityController<String> entityTextController = (IEntityController<String>) annotatedTextController;
    entityTextController.setEntity("foo");
    // Would not work here because generic interface type is type-safe:
    // entityNumberController.setEntity(123);
    System.out.println("Entity value = " + entityTextController.getEntity());

    // Create another object and directly assign it to interface type.
    //
    // NOTE: As explained in https://bugs.eclipse.org/bugs/show_bug.cgi?id=442425#c2, @DeclareParents will add the
    // raw form of the parent to the target class, not the generic one. Therefore, the additional cast is necessary.
    // Otherwise, AJC would throw:
    //   Cannot infer type arguments for MyAnnotatedController<>
    IEntityController<Integer> entityNumberController = (IEntityController<Integer>) new MyAnnotatedController<>();
    entityNumberController.setEntity(123);
    // Would not work here because generic interface type is type-safe:
    // entityNumberController.setEntity("foo");
    System.out.println("Entity value = " + entityNumberController.getEntity());
  }
}
