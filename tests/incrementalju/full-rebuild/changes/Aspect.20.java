
public aspect Aspect {
    declare warning : execution(static void main(String[])): "dw";
}