interface IMarker<CLOCK,STATE> { }

public aspect MyAspect
{

       public void IMarker<CLOCK,STATE>.map()
       {
               CLOCK[] var = find();
       };



       public CLOCK[] IMarker<CLOCK,STATE>.find()
       {
               return null;
       }

}
