package avbeeri;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionTest {
	private static final int PORT_NUMBER = 6667;
	private static final String SERVER_ADDRESS = "irc.dal.net";
	
	
	static BufferedReader input;
	static Socket IRCClient;
	static PrintWriter output;
	private static Thread recieverThread;
	private static Thread inputThread;
	private static Timer pingTimer;
	private static String currentRoom;

	public static void main(String[] argv) {
		
		
		
		
	    try {
	    	
			IRCClient = new Socket(SERVER_ADDRESS, PORT_NUMBER);
			//input = new DataInputStream(IRCClient.getInputStream());

	        input = new BufferedReader(new InputStreamReader(IRCClient.getInputStream()));
	        output = new PrintWriter(IRCClient.getOutputStream());
	        
	        
	        
	        
			recieverThread = new Thread() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String line;
					try {
						while (!this.isInterrupted() && (line = input.readLine()) != null) {
						    Date d = new Date();
							System.out.println("S> " + line);
						    if (line.startsWith("PING :")) {
						    	String pongResponse = line.substring(line.indexOf(":") + 1);
						    	output.print("PONG :" + pongResponse + "\r\n");
						    	System.out.println("C> " + "PONG :" + pongResponse + "\r\n");
						    }
						}
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					
					} finally {
						closeDown();
					}

					
					System.out.println("recieverThread ended");
				}
				
			};
			recieverThread.start();
			
			
			System.in.read();
			//Login Procedure
			output.print("PASS *\r\n");
			System.out.println("PASS *");
			//output.flush();
			
			//System.in.read();
			output.print("NICK test\r\n");
			System.out.println("NICK test");
			//output.flush();
			//System.in.read();
			output.print("USER Test 8 * :Real Name\r\n");
			System.out.println("USER Test 8 * :Real Name");
			output.flush();
			
			inputThread = new Thread() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String line;
					Scanner s = new Scanner(System.in);
					while (!this.isInterrupted() && s.hasNext()) {
						line = s.nextLine();
						if (line.startsWith("/me ")) {
							String action = line.substring(line.indexOf(" ") + 1);
							output.print("PRIVMSG " + currentRoom + " :");
							output.write(0x01);
							output.print("ACTION " + action);
							output.write(0x01);
							output.print("\r\n");
						} else if (line.startsWith("/say ")) {
								String action = line.substring(line.indexOf(" ") + 1);
								output.print("PRIVMSG " + currentRoom + " :");
								output.print(action);
								output.print("\r\n");
						} else if (line.startsWith("/room ")) {
							currentRoom = "#" + line.substring(line.indexOf(" ") + 1);
							
						} else {
							output.print(line.trim() + "\r\n");
						}
						output.flush();
					}
					closeDown();
					

					
					System.out.println("inputThread ended");
				}
				
			};
			
			output.print("JOIN #test\r\n");
			output.flush();
			inputThread.start();
			System.out.println("Input started!");
			pingTimer = new Timer();
			
			pingTimer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					java.util.Random r = new java.util.Random();
					Date d = new Date();
					System.out.println("PINGING...");
					output.write("PING " + r.nextInt(1000) + "\r\n");
					output.flush();
				}
	        	
	        }, 10000, 60000);
			
			
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	}
	
	private static void closeDown() {
		try {
			//output.print("QUIT");
			recieverThread.interrupt();
			inputThread.interrupt();
			pingTimer.cancel();
			input.close();
			output.close();
			IRCClient.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
