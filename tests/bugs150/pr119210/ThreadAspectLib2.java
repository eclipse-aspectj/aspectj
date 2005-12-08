public aspect ThreadAspectLib2 {
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
        
        int around(): call(* getFive()) {
        	return 3;
        }
}