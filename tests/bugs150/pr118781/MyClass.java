package blah;

import java.util.Random;

public class MyClass {
       public Integer[] getRandomInt(String[][] param)
       {
               for (int i = 0; i < param.length; i++)
               {
                       System.out.print("[" + i + "] = [");
                       for (int j = 0; j < param[i].length; j++)
                       {
                               System.out.print(param[i][j]);
                               if (j != param[i].length-1)
                                       System.out.print(',');
                       }
                       System.out.println(']');
               }
               return new Integer[] { new Integer(new Random().nextInt())};
       }
}
