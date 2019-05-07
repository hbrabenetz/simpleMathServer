package simpleMathServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerThreads implements Runnable{

	Socket s; // is a communication socket for both directions with one client. They meet at a different port than portNumber
	int clientNumber;
	String input;	
	PrintWriter out; 
	BufferedReader in;
    private final static Logger LOGGER = Logger.getLogger(ServerThreads.class.getName());
    private static FileHandler fh = null;
    
	ServerThreads(Socket s, int clientNumber) throws IOException{

		this.s = s;
		this.clientNumber = clientNumber;
		
		// Attention: in, out can be obtained only once from a socket otherwise socket closes immediately

		OutputStream os = s.getOutputStream(); // bytes
		out = new PrintWriter(os, true);      // creates the OutputStreamWriter automatically
		
		InputStream is = s.getInputStream(); // bytes
		InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
		in = new BufferedReader(isr);
	
		fh=new FileHandler("logger.txt", false);
		LOGGER.addHandler(fh);
		fh.setFormatter(new SimpleFormatter());
		LOGGER.setLevel(Level.INFO);
	}
	
	
	public void run() {
		
		try {
			while ((input = in.readLine()) != null) { // Socket waits for a clients message
														
				synchronized(LOGGER) {
				LOGGER.log(Level.INFO, "Client action: {0}", input);
				}
						
				// we eliminate blanks, eliminate eventually used < > symbols, then we split and ignore more than one blank in between
				String[] tokens = input.trim().replaceAll("[<>]","").split("\\s+");  

				if (tokens.length == 3 && (tokens[0].equals("!add") || tokens[0].equals("!subtract"))) {
					
						try {
							
							double v1 = Double.parseDouble(tokens[1]);
							double v2 = Double.parseDouble(tokens[2]);
							
							// only if its a valid try I check the credits before I would calculate
							if(MathServer.deductCredits(clientNumber)) {
								out.println("Sorry your credits are all used");
								continue;
							}

							if (tokens[0].equals("!add")) {
								
								out.println(v1+v2);
								
							}
							else if (tokens[0].equals("!subtract")) {
								
								out.println(v1-v2);
								
							}
														
						} catch (NumberFormatException e) {
							
							out.println("Wrong Input. Please input two numbers");
							
						}						
								
					}
					else if (tokens[0].equals("!view")) {
						
						out.println(MathServer.credits.toString());

					}
					else if(tokens[0].equals("!exit")) {
						
						s.close();   // we close the socket properly
						MathServer.credits.remove(clientNumber); // we eliminate this clientNumber from the credits collection
						return; 	// and we terminate this thread
						
					} 
					else {
						
						out.println("Wrong input. Please use: !add <z1> <z2>, !subtract <z1> <z2>, !view, !exit");
						
					}
				
				}
							
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
