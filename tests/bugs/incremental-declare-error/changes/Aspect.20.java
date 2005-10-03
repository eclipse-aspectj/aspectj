
package pack;

public aspect Aspect {
    declare error : execution(private static void Main.main(String[])) : "main";
}