
import java.math.BigInteger;
import java.security.SecureRandom;

import gnu.getopt.Getopt;


public class RSA {
	public static void main(String[] args){
		
		StringBuilder bitSizeStr = new StringBuilder();
		StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		StringBuilder eStr = new StringBuilder();
		StringBuilder m = new StringBuilder();

		pcl(args, bitSizeStr, nStr, dStr, eStr,m);
		
		if(!bitSizeStr.toString().equalsIgnoreCase("")){
			//This means you want to create a new key
			genRSAkey(bitSizeStr);
		}
		
		if(!eStr.toString().equalsIgnoreCase("")){
			RSAencrypt(m, nStr, eStr);
		}
		
		if(!dStr.toString().equalsIgnoreCase("")){
			RSAdecrypt(m, nStr, dStr);
		}
		
	}


	/**
	 * given n and e and a string message, this encrypts so that; used as stand-alone RSA
	 * C = M^e mod(n); where C is the cipherText
	 * @param m
	 * @param nStr
	 * @param eStr
	 */
	static String RSAencrypt(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {
		String cipher = "";
		BigInteger c = null;
		BigInteger e = new BigInteger(eStr.toString(), 16);
		BigInteger n = new BigInteger(nStr.toString(), 16);
		BigInteger message = new BigInteger(m.toString(), 16);

		//System.out.println("Message is: " + message.toString(16));
		
		c = message.modPow(e, n);
		cipher += c.toString(16);
		System.out.println("cipher text is: " + cipher);
		return cipher;
	}
	
	/**
	 * given n and e and a string message, this encrypts so that; used within CHAT
	 * C = M^e mod(n); where C is the cipherText
	 * @param m
	 * @param nStr
	 * @param eStr
	 */
	static String RSAencryptCHAT(StringBuilder m, StringBuilder nStr, StringBuilder eStr) {
		String cipher = "";
		BigInteger c = null;
		BigInteger e = new BigInteger(eStr.toString(), 16);
		BigInteger n = new BigInteger(nStr.toString(), 16);
		BigInteger message = new BigInteger(m.toString(), 16);

		//System.out.println("Message is: " + message.toString(16));
		
		c = message.modPow(e, n);
		cipher += c.toString(16);
		//System.out.println("cipher text is: " + cipher);
		return cipher;
	}

	/**
	 * Decrypts hex-encoded cipher and prints decrypted message; used in stand-alone RSA
	 * @param cStr
	 * @param nStr
	 * @param dStr
	 * @return
	 */
	static String RSAdecrypt(StringBuilder cStr, StringBuilder nStr, StringBuilder dStr){
		
		String message = "";
		BigInteger m = null;
		BigInteger cipher = new BigInteger(cStr.toString(), 16);
		BigInteger n = new BigInteger(nStr.toString(), 16);
		BigInteger d = new BigInteger(dStr.toString(), 16);
		
		m = cipher.modPow(d, n);
		message = m.toString(16);
		System.out.println("decrypted message is: " + message);
		return message;
	}
	
	/**
	 * Decrypts hex-encoded cipher; used in CHAT
	 * @param cStr
	 * @param nStr
	 * @param dStr
	 * @return
	 */
	static String RSAdecryptCHAT(StringBuilder cStr, StringBuilder nStr, StringBuilder dStr){
		
		String message = "";
		BigInteger m = null;
		BigInteger cipher = new BigInteger(cStr.toString(), 16);
		BigInteger n = new BigInteger(nStr.toString(), 16);
		BigInteger d = new BigInteger(dStr.toString(), 16);
		
		m = cipher.modPow(d, n);
		message = m.toString(16);
		//System.out.println("decrypted message is: " + message);
		return message;
	}
	
	/**
	 * Generates public and private keys (e, d, n)
	 * @param bitSizeStr
	 */
	private static void genRSAkey(StringBuilder bitSizeStr) {
		long longOne = 1;
		BigInteger one = null;
		one = BigInteger.valueOf(longOne);
		BigInteger p, q;
		
		SecureRandom rand = new SecureRandom();
		
		// default if bitSizeStr is empty
		if(bitSizeStr.toString().equals("")){
			p = BigInteger.probablePrime(512, rand);
			q = BigInteger.probablePrime(512, rand);
			
		}else{
			int bitSize = Integer.parseInt(bitSizeStr.toString());
			p = BigInteger.probablePrime(bitSize/2, rand);
			q = BigInteger.probablePrime(bitSize - bitSize/2, rand); //in case odd k
		}
		
		
		//phi = (p-1)*(q-1)
		
		BigInteger product = p.multiply(q);
		BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));
		
		//Create e which is relatively prime to phi
		BigInteger e = null;
		
		for (int x = 3; true; x+=2) {
			e = BigInteger.valueOf(x);
			if (e.gcd(phi).equals(one)) {
				break;
			}
		}
		//System.out.println("e is: " + e.toString());
		
		BigInteger d = (e.modInverse(phi));
		//System.out.println("d is: " + d.toString());
		
		System.out.println("Public Key: (" + e.toString(16) + ", " + product.toString(16) + ")");
		System.out.println("Private Key: (" + d.toString(16) + ", " + product.toString(16) + ")");
	}
	
	/**
	 * This function Processes the Command Line Arguments.
	 */
	private static void pcl(String[] args, StringBuilder bitSizeStr,
							StringBuilder nStr, StringBuilder dStr, StringBuilder eStr,
							StringBuilder m) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:b:n:i:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  m.append(arg);
		        	  break;
		          case 'e':
		        	  arg = g.getOptarg();
		        	  eStr.append(arg);
		        	  break;
		     	  case 'n':
		        	  arg = g.getOptarg();
		        	  nStr.append(arg);
		        	  break;
		     	  case 'd':
		        	  arg = g.getOptarg();
		        	  dStr.append(arg);
		        	  break;
		          case 'k':
		        	  bitSizeStr.setLength(0);
		        	  bitSizeStr.append(1024);
		        	  break;
		     	  case 'b':
		     		  bitSizeStr.setLength(0);
		        	  arg = g.getOptarg();
		        	  bitSizeStr.append(arg);
		        	  break;
		          case 'h':
		        	  callUsage(0);
		          case '?':
		            break; // getopt() already printed an error
		          default:
		              break;
		       }
		   }
	}
	
	private static void callUsage(int exitStatus) {

		String useage = ""
				+ "-h \t Gives description of all possible options supported by program\n\n"
				+ "-k \t Generates 1024 character public and private RSA keys encoded in hex to be used for encryption and decryption\n\n"
				+ "-b \t Takes 1 argument -- a specified bit size for the keys; Generates public and private RSA keys encoded in hex to be used for encryption and decryption for the specified bit size\n\n"
				+ "-e \t Takes 1 argument -- a hex-encoded public key (e)\n\n"
				+ "-d \t Takes 1 argument -- a hex-encoded public key (d)\n\n"
				+ "-n \t Takes 1 argument -- a hex-encoded modulus (n)\n\n"
				+ "-i \t Takes 1 argument -- a hex-encoded integer/plaintext value if encrypting or a hex-encoded ciphertext value if decrypting\n\n";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
}
