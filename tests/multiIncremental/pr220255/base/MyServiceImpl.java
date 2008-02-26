
public class MyServiceImpl implements MyInterface
{

    public String doA(int lololo) {
        System.out.println("really did it "+lololo);
        return "really got it: "+lololo;
    }


    public String doB(int lala) {
        return doA(lala);
    }

}
