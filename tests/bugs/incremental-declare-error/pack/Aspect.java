
package pack;

public aspect Aspect {
    declare error : execution(public static void Main.main(String[])) : "main";
}