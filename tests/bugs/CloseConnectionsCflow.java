// Bug # 28702

import java.util.Stack;

 

interface Connection {

            Connection open();

            void close();

}

 

aspect CloseConnectionsCflow percflow(layerEntryMethods()) {

   Stack openConnections;

   pointcut layerMethods() : 

       execution(public * com.example.businessFacade.*.*(..));

   pointcut layerEntryMethods() : 

       layerMethods() && !cflowbelow(layerMethods());

   pointcut openedConnection() : 

       call(* Connection.open(..));

   pointcut layerBoundary() : cflow(layerEntryMethods());

 

   after() returning (Connection conn) : 

           openedConnection() && layerBoundary() {

       openConnections.push(conn);

   }

   after() : layerBoundary() {

       while (!openConnections.empty()) {

           Connection conn = (Connection)openConnections.pop();

           conn.close();

        }

   }

}
