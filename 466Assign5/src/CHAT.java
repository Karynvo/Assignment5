/**
 * WRITTEN BY: Karyn Vo (32) and Sydney Warner (54)
 * CS466 Assignment5: Part C - Chat program
 * due 11/13 @ 11:59
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class CHAT {
	
	private static StringBuilder desKey = null;
	
	static String host;
	static int port;
	static Socket s;
	static String username;
	
	static StringBuilder privateKeyAlice = new StringBuilder();
	static StringBuilder privateKeyBob = new StringBuilder();
	static StringBuilder publicKeyAlice = new StringBuilder();
	static StringBuilder publicKeyBob = new StringBuilder();
	static StringBuilder aliceModulus = new StringBuilder();
	static StringBuilder bobModulus = new StringBuilder();
	
	public static void main(String[] args) throws IOException {

		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		
//		Process command line arguments
		pcl(args);
						
//		set up server, or join server
		String encryptedKey = null;
		encryptedKey= setupServer(); //desKey is the rsa encrypted DEs key
		DES des = new DES();
		
//		Set up username
		System.out.println("Welcome to encrypted chat program.\nChat starting below:");

//		Make thread to print out incoming messages...
		ChatListenter chatListener = new ChatListenter();
		chatListener.start(); 
		
//		loop through sending and receiving messages
		PrintStream output = null;
		try {
			output = new PrintStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		String input = "";
		while(true){
			if (encryptedKey != null) {
				input = encryptedKey;
				encryptedKey = null;
			}
			else {
				input = keyboard.nextLine();
				input = username + ": " + input;
				
				//encrypt
				input = des.encrypt(desKey, input);
				//System.out.println("message after encryption: " + input);
			}
			output.println(input);
			output.flush();	
		}
	}

	/**
	 * Upon running this function it first tries to make a connection on 
	 * the given ip:port pairing. If it find another client, it will accept
	 * and leave function. 
	 * If there is no client found then it becomes the listener and waits for
	 * a new client to join on that ip:port pairing. 
	*/
	private static String setupServer() {
		String str = null;
		try {
			// This line will catch if there isn't a waiting port
			s = new Socket(host, port);
			
			//first user is listening, second user will generate DES key
			DES des = new DES();
			desKey = new StringBuilder();
			
			String key = des.genDESkey();
		//	System.out.println("key before appending: " + key);
			desKey.append(key);
			
			RSA rsa = new RSA();
			System.out.println("Client Connected, key created and encrypted");
			
			//depending who the user is changes how the key is encrypted with RSA
			if(username.equals("alice"))
				return rsa.RSAencryptCHAT(desKey, bobModulus, publicKeyBob);
			else{
				return rsa.RSAencryptCHAT(desKey, aliceModulus, publicKeyAlice);
			}
			
		} catch (IOException e1) {
			System.out.println("There is no other client on this IP:port pairing, waiting for them to join.");
			
			try {
				ServerSocket listener = new ServerSocket(port);
				s = listener.accept();
				listener.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

		}
		
		System.out.println("Client Connected.");
		//first user will not return key, so they will return a null string which will be checked
		return str;

	}

	/**
	 * This function Processes the Command Line Arguments.
	 * Right now the three accepted Arguments are:
	 * -p for the port number you are using
	 * -i for the IP address/host name of system
	 * -h for calling the usage statement.
	 */
	private static void pcl(String[] args) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/
		LongOpt[] longopts = new LongOpt[2];
		longopts[0] = new LongOpt("alice", LongOpt.NO_ARGUMENT, null, 1);
		longopts[1] = new LongOpt("bob", LongOpt.NO_ARGUMENT, null, 2);
		Getopt g = new Getopt("Chat Program", args, "p:i:a:b:m:n:h", longopts);
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 1:
		     		  username = "alice";
		     		  break;
		     	  case 2:
		     		  username = "bob";
		     		  break;
		          case 'p':
		        	  arg = g.getOptarg();
		        	  port = Integer.parseInt(arg);
		        	  break;
		          case 'i':
		        	  arg = g.getOptarg();
		        	  host = arg;
		        	  break;
		          case 'a':
		        	  arg = g.getOptarg();
		        	  if(username.equals("alice"))
		        		  privateKeyAlice.append(arg);
		        	  else
		        		  publicKeyAlice.append(arg);
		        	  break;
		          case 'm':
		        	  arg = g.getOptarg();
		        	  aliceModulus.append(arg);
		        	  break;
		          case 'b':
		        	  arg = g.getOptarg();
		        	  if(username.equals("alice"))
		        		  publicKeyBob.append(arg);
		        	  else
		        		  privateKeyBob.append(arg);
		        	  break;
		          case 'n':
		        	  arg = g.getOptarg();
		        	  bobModulus.append(arg);
		        	  break;
		          case 'h':
		        	  callUsage(0);
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		              break;
		       }
		   }
	}

	/**
	 * A helper function that prints out the useage help statement
	 * and exits with the given exitStatus
	 * @param exitStatus
	 */
	private static void callUsage(int exitStatus) {
		
		String useage = ""
				+ "-h \t Gives description of all possible options supported by program\n\n"
				+ "--alice \t Role is Alice; server starts up and waits for Bob to connect\n\n"
				+ "--bob \t Role is Bob; connects to server and sends symmetric key\n\n"
				+ "-a \t Takes 1 argument -- if role is Alice (--alice), then Alice's private key encoded in hex (d)\n\t\tif role is Bob (--bob), then Alice's public key encoded in hex (e)\n\n"
				+ "-b \t Takes 1 argument -- if role is Alice (--alice), then Bob's public key encoded in hex (e)\n\t\tif role is Bob (--bob), then Bob's private key encoded in hex (d)\n\n"
				+ "-n \t Takes 1 argument -- Bob's modulus encoded in hex\n\n"
				+ "-m \t Takes 1 argument -- Alice's modulus encoded in hex\n\n"
				+ "-p \t Takes 1 argument -- Port number\n\n"
				+ "-i \t Takes 1 argument -- IP Address\n\n";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}

	/**
	 * A private class which runs as a thread listening to the other 
	 * client. It decodes the messages it gets using the RSAdecode
	 * function and prints out the message on screen.
	 */
	static private class ChatListenter implements Runnable {
		private Thread t;
		ChatListenter(){
		}
		
		public void run() {
			BufferedReader input = null;
			DES des = new DES();
			try {
				input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
				System.err.println("System would not make buffer reader");
				System.exit(1);
			}
			String inputStr = "";
			while(true){
				try {
					//Read lines off the scanner
					if (desKey == null) {
						desKey = new StringBuilder();
						StringBuilder encKey = new StringBuilder();
						inputStr = input.readLine();
						if(inputStr == null){
							System.err.println("The other user has disconnected, closing program...");
							System.exit(1);
						}
						
						//System.out.println("Alice has the encrypted Key: " + inputStr);
						encKey.append(inputStr);
						RSA rsa = new RSA();
						if(username.equals("alice"))
							desKey.append(rsa.RSAdecryptCHAT(encKey, aliceModulus, privateKeyAlice));
						else
							desKey.append(rsa.RSAdecryptCHAT(encKey, bobModulus, privateKeyBob));
						
						//desKey is decrypted
						System.out.println("OK");
					}
		
					//Normal DES message, decrypt with desKey first input will be iv, second will be all blocks of message
					else { 
						//System.out.println("decrypting message with DES");
						String iv = input.readLine();
						if(iv == null){
							System.err.println("The other user has disconnected, closing program...");
							System.exit(1);
						}
						
						//System.out.println("Decrypting: iv is: " + iv);
						String encrypted = input.readLine();
						
						//System.out.println("Decrypting: message is" + encrypted);
						inputStr = iv + "\n" +  encrypted;
						inputStr = des.decrypt(desKey, inputStr);
						System.out.println(inputStr);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		   
		public void start(){
			
			if (t == null){
				t = new Thread(this);
				t.start();
			}
		}
	}
}
