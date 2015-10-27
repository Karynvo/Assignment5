import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.security.SecureRandom;
import java.math.BigInteger;

import gnu.getopt.Getopt;


public class DES_Skeleton {
	
	public static void main(String[] args) {
		genDESkey();
		encrypt("90246fa77f012845", "inputFile", "outputFile");
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		/*
		pcl(args, inputFile, outputFile, keyStr, encrypt);
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			decrypt(keyStr, inputFile, outputFile);
		}
		
		*/
	}
	

	private static void decrypt(StringBuilder keyStr, StringBuilder inputFile,
			StringBuilder outputFile) {
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			List<String> lines = Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset());
			String IVStr = lines.get(0);
			lines.remove(0);
			String encryptedText;
			
			for (String line : lines) {
				encryptedText = DES_decrypt(IVStr, line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line) {
		
		return null;
	}

//change string to string builder
	private static void encrypt(String keyStr, String inputFile,
			String outputFile) {
		genSubkeys(keyStr);
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			
			String encryptedText;
			for (String line : Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				encryptedText = DES_encrypt(line);
				writer.print(encryptedText);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_encrypt(String line) {
		
		return null;
	}
	
	/**
	 * generate the subkeys from keyStr, store them in an array, pass to Des_Encrypt which will encrypt every line
	 * change string to stringbuilder
	 */
	private static byte[] genSubkeys(String key) {
		String[] subKeys = new String[16];
		
		SBoxes sbox = new SBoxes();
		String strBinary = new BigInteger(key.getBytes()).toString(2);
		BitSet bs = new BitSet(64);
		for(int i = 0; i < strBinary.length(); i++){
			if(strBinary.charAt(i) == '1')
				bs.set(i);
			
		}
		/*
		System.out.println("bitset:\n" + bs.get(0, 64));
		
		for(int j = 0; j < strBinary.length(); j++)
			System.out.print(bs.get(j)? 1 : 0);
		System.out.println();
		*/
		char[] binaryChar = new char[64];
		for (int i=0; i < 64; i++) 
			binaryChar[i] = strBinary.charAt(i);
		//byte[] binary = key.getBytes(Charset.forName("UTF-8"));
		char[] pc1Key = new char[56];
		System.out.println(strBinary);
		int count = 0;
		while (count < 56) {
			pc1Key[count] = binaryChar[sbox.PC1[count]];
			count++;
		}
		System.out.println("Binary Char: " + Arrays.toString(binaryChar));
		System.out.println("PC key: " + Arrays.toString(pc1Key) + "length: " + pc1Key.length);
		return null;
	}


	static void genDESkey(){
		//System.out.println("New key goes here");
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 16; i++){
			int randInt = random.nextInt(16);
			String oneRand = Integer.toHexString(randInt);
			//System.out.println(oneRand);
			sb.append(oneRand);
		}
		if (!checkKey(sb.toString())) {
			genDESkey();
		}
		System.out.println("Key: " + sb.toString());
		return;
	}


	/**
	 * This function Processes the Command Line Arguments.
	 * -p for the port number you are using
	 * -h for the host name of system
	 */
	private static void pcl(String[] args, StringBuilder inputFile,
							StringBuilder outputFile, StringBuilder keyString,
							StringBuilder encrypt) {
		/*
		 * http://www.urbanophile.com/arenn/hacking/getopt/gnu.getopt.Getopt.html
		*/	
		Getopt g = new Getopt("Chat Program", args, "hke:d:i:o:");
		int c;
		String arg;
		while ((c = g.getopt()) != -1){
		     switch(c){
		     	  case 'o':
		        	  arg = g.getOptarg();
		        	  outputFile.append(arg);
		        	  break;
		     	  case 'i':
		        	  arg = g.getOptarg();
		        	  inputFile.append(arg);
		        	  break;
	     	  	  case 'e':
		        	  arg = g.getOptarg();
		        	  keyString.append(arg);
		        	  encrypt.append("e");
		        	  break;
	     	  	  case 'd':
		        	  arg = g.getOptarg();
		        	  keyString.append(arg);
		        	  encrypt.append("d");
		        	  break;
		          case 'k':
		        	  genDESkey();
		        	  break;
		          case 'h':
		        	  callUseage(0);
		          case '?':
		            break; // getopt() already printed an error
		            //
		          default:
		              break;
		       }
		   }
		
	}
	
	private static void callUseage(int exitStatus) {
		
		String useage = "";
		
		System.err.println(useage);
		System.exit(exitStatus);
		
	}
	
	/**
	 * Takes a string key and checks hexadecimal for weak keys
	 * described at http://www.umich.edu/~x509/ssleay/des-weak.html
	 * @param desKey
	 * @return false if weak key, true if acceptable
	 */
	private static boolean checkKey(String desKey) {
		System.out.println("check key\n");
		if (desKey == null) {
			return false;
		}
		if (desKey.length() != 16) {
			System.out.println("key length" + desKey.length() + "\n");
			return false;
		}
		if (desKey.equals("0101010101010101")) {
			return false;
		}
		if (desKey.equals("fefefefefefefefe")) {
			return false;
		}
		if (desKey.equals("1f1f1f1f1f1f1f1f")) {
			return false;
		}
		if (desKey.equals("e0e0e0e0e0e0e0e0")) {
			return false;
		}
		
		return true;
		
	}
	
}
