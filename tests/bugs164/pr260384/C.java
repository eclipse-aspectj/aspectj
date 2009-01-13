public class C {
	SessionFactory sf;
	HT ht;
	
          public void m() {
        	  LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean() {
        		  protected SessionFactory newSessionFactory(Configuration config) throws Exception {
        			  return sf;
        		  }
        	  };
              final TT tt = new TT();
              tt.execute(new TransactionCallback() {
            	   public Object doInTransaction(TransactionStatus status) {
            		   System.out.println("X");
            		   ht.execute(new HibernateCallback() {
            			   public Object doInHibernate(Session session) {
            				   return null;
            			   }
            		   });
            		   tt.execute(new TransactionCallback() {
                    	   public Object doInTransaction(TransactionStatus status) {
                    		   System.out.println("X");
                    		   ht.execute(new HibernateCallback() {
                    			   public Object doInHibernate(Session session) {
                    				   return null;
                    			   }
                    		   });
                    		   return null;
                    	   }
            		   });
            		   return null;
            	   }
              });
          }
}

class TT { public void execute(Object o) {}}
class LocalSessionFactoryBean {}
class HT  { public void execute(Object o) {}}
class Session {}
class TransactionStatus {}
class SessionFactory {}
class Configuration {}
class TransactionCallback {}
class HibernateCallback {}


aspect X {
	before(): within(*) {}
}
