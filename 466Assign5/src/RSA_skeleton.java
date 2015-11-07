
import java.math.BigInteger;
import java.security.SecureRandom;

import gnu.getopt.Getopt;


public class RSA_skeleton {
	/*
	private static String message = "";
	private static String myN = "";
	private static String myE = "";
	private static String myD = "";
	private static String encrypted = "";
	*/
	public static void main(String[] args){
		
/*
		DES_Skeleton des = new DES_Skeleton();
		message = des.genDESkey();
		System.out.println("Key from DES is: " + message);
	*/
		StringBuilder bitSizeStr = new StringBuilder();
		StringBuilder nStr = new StringBuilder();
		StringBuilder dStr = new StringBuilder();
		StringBuilder eStr = new StringBuilder();
		StringBuilder cStr = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder m = new StringBuilder();
/*
		m.append(message);
		
		bitSizeStr.append("1024");
		
		genRSAkey(bitSizeStr);
		nStr.append(myN);
		eStr.append(myE);
		dStr.append(myD);
		
		RSAencrypt(m, nStr, eStr);
		
		cStr.append(encrypted);
		RSAdecrypt(cStr, nStr, dStr);
		
		
*/
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
	 * given n and e and a string message, this encrypts so that
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

		
	//	System.out.println("Message is: " + message.toString(16));
		
		c = message.modPow(e, n);
		cipher += c.toString(16);
	//	System.out.println("cipher text is: " + cipher);
		return cipher;
	//	encrypted = cipher;
		
	}

	static String RSAdecrypt(StringBuilder cStr, StringBuilder nStr, StringBuilder dStr){
		
		String message = "";
		BigInteger m = null;
		BigInteger cipher = new BigInteger(cStr.toString(), 16);
		BigInteger n = new BigInteger(nStr.toString(), 16);
		BigInteger d = new BigInteger(dStr.toString(), 16);
		
		m = cipher.modPow(d, n);
		message = m.toString(16);
	//	System.out.println("decrypted message is: " + message);
		// TODO Auto-generated method stub
		return message;
	}
	
	private static void genRSAkey(StringBuilder bitSizeStr) {
		// TODO Auto-generated method stub
		long longOne = 1;
		BigInteger one = null;
		one = BigInteger.valueOf(longOne);
		//BigInteger two = one.add(one);
		//System.out.print("Is it really a one no is two?" + two.toString());
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
		
		/*
		 *  phi = (p-1)*(q-1)
		*/
		//System.out.println("p is: " + p.toString() + "\nq is: " + q.toString());
		BigInteger product = p.multiply(q);
	//	System.out.println("Product bit length is: " + product.bitLength() + "\nn is: " + product.toString());
		BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));
//		System.out.println("phi is: " + phi.toString());
		
		/*
		 * Create e which is relatively prime to phi
		*/
		BigInteger e = null;
		
		for (int x = 3; true; x+=2) {
			e = BigInteger.valueOf(x);
			if (e.gcd(phi).equals(one)) {
				break;
			}
		}
		
	//	System.out.println("e is: " + e.toString());
		
		
		BigInteger d = (e.modInverse(phi));
	//	System.out.println("d is: " + d.toString());
		
		System.out.println("Public Key: (" + e.toString(16) + ", " + product.toString(16) + ")");
		System.out.println("Private Key: (" + d.toString(16) + ", " + product.toString(16) + ")");
	/*
		myE = e.toString(16);
		myD = d.toString(16);
		myN = product.toString(16);
	*/
		
	}
/*
	/**
	 * Generates a secure random odd number no more than 32 bits big
	 * @return
	 
	private static int oddRand() {
		SecureRandom rand = new SecureRandom();
		int num; 
		while (true) {
			num = rand.nextInt(32);
			if (num % 2 != 0 ) {
				break;
			}
		}
		
		return num;
		
	}
*/
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
		        	  break;
		     	  case 'b':
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

		String useage = "";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}


}
