aspect A  {
    boolean around() : (target(java.util.HashSet) && call(boolean add(..) ) )
       {
         return false;
       }
   }