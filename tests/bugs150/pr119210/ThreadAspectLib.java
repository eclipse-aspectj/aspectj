public aspect ThreadAspectLib {
//        pointcut setThreadLocalsField(): set(private int TestLib.myInt);
//
//        Integer around():setThreadLocalsField()
//        {
//                try{
//                        return new Integer(2);
//                }
//                catch(Exception e)
//                {
//                        e.printStackTrace();
//                        return null;
//                }
//        }
        
        Integer around(): call(* getFive()) {
        	return new Integer(3);
        }
}