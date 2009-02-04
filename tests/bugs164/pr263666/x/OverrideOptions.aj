package x;
public aspect OverrideOptions
{















   /**
    * Comment A
    */
   boolean around() : execution( public boolean A.a() ) && this( A )
   {
       return false;
   }
   /**
    * Comment B
    */
   int around() : execution(private int B.b(..)) && this(B){
       return 0;
   }
}  