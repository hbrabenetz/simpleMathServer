package simpleMathServer;

//How to kill a process on the port from the command line
//netstat -ano | findstr :8888 
//from the answer we read the pid for example 10392 and enter it into below command
//taskkill /PID 10392 /F

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.CopyOnWriteArrayList;

public class MathServer {

    private static ArrayList<Socket> as; // for further use eventually
    private static ArrayList<Thread> at; // for further use eventually

    private static int clientNumber = 0;
    //  choose a thread safe collection for credit counting:
    //	List<...> list = Collections.synchronizedList(new ArrayList<...>());
    //  CopyOnWriteArrayList<...> threadSafeList = new CopyOnWriteArrayList<...>();
  	public static ConcurrentMap<Integer, Integer> credits; // <clientNumber, credits>
    
  	
    public static boolean deductCredits(int clientNumber) {
    	// we deduct one credit per usage
    	int newCredit = credits.get(clientNumber)-1;
    	credits.replace(clientNumber, newCredit < 0 ? 0 : newCredit);
    	
    	if(newCredit <= 0)
    		return true; // give notice
    	else 
    		return false; // all ok
    }
    
    
	public static void main(String[] args) {

		int portNumber = 8888; // for incoming connections from clients
	    boolean listening = true; 
	    as = new ArrayList<Socket>(); // for further use eventually
	    at = new ArrayList<Thread>(); // for further use eventually
	    credits = new ConcurrentHashMap<Integer, Integer>(); // <clientNumber, credits>
	    
	    
		try (ServerSocket ss = new ServerSocket(portNumber)){ // ServerSocket will be created and bound to portNumber
			
			while(listening) {
				
		       	Socket s = ss.accept(); // ServerSocket waits here until a client connects and returns a communications Socket if it happens. This new Socket has already negotiated a new Port for communication with the client and meets the client at the new port
		       	as.add(s); // for further use eventually
		       	
		       	Thread t = new Thread(new ServerThreads(s, ++clientNumber));
		       	at.add(t); // for further use eventually
	        	t.start();
	        	
	        	credits.put(clientNumber, 10); // every client starts with 10 credits 
	        	
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			
			// no need for finally here since we use a "try with" block
			// closes the ServerSocket automatically when created inside try(...){  
			// since ServerSocket implements java.lang.AutoCloseable
			
		}
		
	}

}
