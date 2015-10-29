import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.security.SecureRandom;
import java.math.BigInteger;

import gnu.getopt.Getopt;


public class DES_Skeleton {
	
	public static void main(String[] args) {
		//genDESkey();
		//encrypt("90246fa77f012845", "inputFile", "outputFile");
		
		StringBuilder inputFile = new StringBuilder();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder keyStr = new StringBuilder();
		StringBuilder encrypt = new StringBuilder();
		
		
		pcl(args, inputFile, outputFile, keyStr, encrypt);
		
		if(keyStr.toString() != "" && encrypt.toString().equals("e")){
			encrypt(keyStr, inputFile, outputFile);
		} else if(keyStr.toString() != "" && encrypt.toString().equals("d")){
			decrypt(keyStr, inputFile, outputFile);
		}
		
		
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
	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile, StringBuilder outputFile) {
		ArrayList<BigInteger> subkeys = genSubkeys(keyStr.toString());
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			
			String encryptedText;
			for (String line : Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				encryptedText = DES_encrypt(line, subkeys);
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
	private static String DES_encrypt(String line, ArrayList<BigInteger> subkeys) {
		String cipherText = "";
		SBoxes sbox = new SBoxes();
		int counter = 0;
		/*
		while (line.length() % 8 != 0) {
			line = line + "0";
			counter ++;
		}*/
		int linelength = line.length();
		linelength = linelength % 8;
		
		if (linelength == 0) {
			line = line + "00000000";
		}
		else {
			for (int t = linelength; t < 7; t++) {
			line +="0";
			}
			
			line += 8-linelength;
		}
		
		
		//line = line + counter; //divisible by 8 with number of 0's added at end of string
		String substring=""; //8 chars from original line
		String substringBinary=""; // mixed up substring in binary
//		String substringString=""; // mixedup string of binary
		//substringBI is the big integer mixed up substring
		String encrypted="";
//		char[] substringIP = new char[64];
		
		for (int x = 0; x < line.length(); x+=8) {
			substring = line.substring(x, x+8);
			substringBinary = new BigInteger(substring.getBytes()).toString(2);
			substringBinary = checkBinaryStr(substringBinary, 64);
			
			/**
			 * MIX UP FUNCTION, send substringBinary (binary concatenated string), substringIP[] (size64) and sbox.IP
			 * return big int
			 */
//			for (int i = 0; i < 64; i++) {
//				substringIP[i] = substringBinary.charAt(sbox.IP[i]); //this mixes up the substring in a char[]
//			}
//			substringString = String.valueOf(substringIP); //this takes our mixed up array and turns it into a binary string
			System.out.println("String to go to sbox size: " + substringBinary.length());
			BigInteger substringBI = scramble(substringBinary, 64, sbox.IP); //this makes the above a big int
			
			String checkSubstringBI = checkBinaryStr(substringBI.toString(2), 64);
			String leftBinary = checkSubstringBI.substring(0, 32);	//toString, divide in half L0
			String rightBinary = checkSubstringBI.substring(32, 64); //R0
			
			BigInteger leftBigInt = new BigInteger(leftBinary, 2);			//take the halves and make them big ints
			BigInteger rightBigInt = new BigInteger(rightBinary, 2);
			
			encrypted = encrypted + encryptBlock(cipherText, leftBigInt, rightBigInt, subkeys, 0);		//begin function f
			System.out.println("line: " + line + "\tline length: " + line.length());
		}
					
		
		
		return null;
	}
	
	private static String encryptBlock(String cipherText, BigInteger left, BigInteger right, ArrayList<BigInteger> subkeys, int keyNum) {
			
		if (keyNum == 16) {
			System.out.println("Cipher text: " + cipherText);
			return cipherText;
		}
		
		SBoxes sbox = new SBoxes();
		
		/* begin function f */
		
		// scramble Right using E box send right as binary substring, in (32) -> E -> 48
		String rightStr = checkBinaryStr(right.toString(2), 32);
		BigInteger eBoxRightResult = scramble(rightStr, 48, sbox.E); // E(R0)
		
		// XOR with subkey
		BigInteger xorResult = eBoxRightResult.xor(subkeys.get(keyNum));
		
		// divide into 8 blocks of 6
		String xorString = checkBinaryStr(xorResult.toString(2), 48);
		String cipherBlock=""; //This will be our concatenated string after the addressing (Sbox)
		for(int x = 0; x < 48; x+=6){
			String rowSubStr = xorString.substring(x, x+2);
			String colSubStr = xorString.substring(x+2, x+6);
			
			// make into big int to convert to decimal
			int row = Integer.parseInt(new BigInteger(rowSubStr,2).toString());
			int col = Integer.parseInt(new BigInteger(colSubStr,2).toString());
			
			// get 4 bit from corresponding S box
			// (row - 1) * 15 + col - 1
			System.out.println("keyNum: " + keyNum + "\tx: " + x + "\trow: " + row + "\tcol: " + col);
			int result = sbox.S[x/6][((row) * 15 + col )];
			String fourBit = Integer.toBinaryString(result);
			//checkBinaryStr (size 4)
			for( int count = fourBit.length(); count < 4; count++) {
				cipherBlock = cipherBlock + "0";
			}
			
			cipherBlock = cipherBlock + fourBit;
		}
		
		BigInteger pBoxResult = scramble(cipherBlock, 32, sbox.P);
		cipherText = cipherText + cipherBlock;
		BigInteger newRight = left.xor(pBoxResult); //final right big int
		//left old right
		BigInteger newLeft = right;
		keyNum++;
		encryptBlock(cipherText, newLeft, newRight, subkeys, keyNum);
		return null;
	}
	
	
	/**
	 * generate the subkeys from keyStr, store them in an array, pass to Des_Encrypt which will encrypt every line
	 * change string to stringbuilder
	 */
	private static ArrayList<BigInteger> genSubkeys(String key) {
		//String[] subKeys = new String[16];
		
		SBoxes sbox = new SBoxes();
		String strBinary = new BigInteger(key.getBytes()).toString(2);
		strBinary = checkBinaryStr(strBinary, 64);
		char[] binaryChar = new char[64];
		/*
		for (int i=0; i < 64; i++) 
			binaryChar[i] = strBinary.charAt(i);
		*/
		byte[] binary = key.getBytes(Charset.forName("UTF-8"));
		System.out.println("Binary byte representation: " + Arrays.toString(binary));
//		char[] pc1Key = new char[56];
		System.out.println(strBinary);
//		int count = 0;
		
		/**
		 * MIX UP FUNCTION, send strBinary (binary concatenated string), pc1Key[] (size56) and sbox.PC1
		 * return big int
		 */
//		while (count < 56) {
//			//pc1Key[count] = binaryChar[sbox.PC1[count]];
//			pc1Key[count] = strBinary.charAt(sbox.PC1[count]);
//			count++;
//		}
//		
//		String pc1String = String.valueOf(pc1Key);
		
//		System.out.println("Binary Char: " + Arrays.toString(binaryChar));
//		System.out.println("PC key: " + Arrays.toString(pc1Key) + "length: " + pc1Key.length);
//		System.out.println("pc1String :" + pc1String);
		
		BigInteger fullPC1 = scramble(strBinary, 56, sbox.PC1);
		
		String fullPC1Str = checkBinaryStr(fullPC1.toString(2), 56);

		/* split string into 2 */
		String frontPC1 = fullPC1Str.substring(0, 28);
		String backPC1 = fullPC1Str.substring(28, 56);
		
		BigInteger frontBigInt = new BigInteger(frontPC1, 2); //string to big int
		BigInteger backBigInt = new BigInteger(backPC1, 2);
		System.out.println("Front: " + frontBigInt.toString(2) + "\nBack: " + backBigInt.toString(2));
		
		/* begin rotations */
		ArrayList<BigInteger> subKeyList = new ArrayList<BigInteger>(); //holds 16 subkeys stored as byte arrays
		//char[] subkeyPC2 = new char[48];
//		String subkeyPC2String="";
		for (int x = 0; x < sbox.rotations.length; x++) {
			for (int y = 0; y < sbox.rotations[x]; y++) {
				frontBigInt = rotateLeft(frontBigInt);
				backBigInt = rotateLeft(backBigInt);
				System.out.println("Front binary: " + frontBigInt.toString(2));
				System.out.println("Back binary: " + backBigInt.toString(2));
			}
			String temp = "";
			if (frontBigInt.toString(2).length() < 28 ) {
				for (int t = 0; t < 28-frontBigInt.toString(2).length(); t++)
					temp = temp + "0";
			}
			temp = temp + frontBigInt.toString(2);
			if (backBigInt.toString(2).length() < 28 ) {
				for (int t = 0; t < 28-backBigInt.toString(2).length(); t++)
					temp = temp + "0";
			}
			temp = temp + backBigInt.toString(2);
			System.out.println("concatenated string: " + temp + " size: " + temp.length());
			
			/**
			 * MIX UP FUNCTION, send temp (binary concatenated string), subkeyPC2[] and sbox.PC2
			 * 					return BIGInt
			 */
//			for (int i=0; i < 48; i++) 
//				subkeyPC2[i] = temp.charAt(sbox.PC2[i]); //char[] of mixed up subkey
//			
//			subkeyPC2String = String.valueOf(subkeyPC2);
//			
//			BigInteger bigtemp = new BigInteger(subkeyPC2String, 2);
			BigInteger bigtemp = scramble(temp, 48, sbox.PC2);
			subKeyList.add(bigtemp);
//			System.out.println("Big temp check: " + bigtemp.toString());
		}
		
		return subKeyList;
	}
	
	private static BigInteger rotateLeft(BigInteger bigI) {
	    int value = bigI.intValue();
	    return BigInteger.valueOf(((value << 1) & 0xffffffe) | ((value >>> 27) & 1));
	}

	/**
	 * Scrambles contents of binaryStr specified by sbox
	 * 
	 * @param binaryStr string of binary representation of message
	 * @param sizeOut desired size of output
	 * @param sbox the sbox to use
	 * @return BigInteger representation of scrambled string
	 */
	private static BigInteger scramble(String binaryStr, int sizeOut, int[] sbox){
		char[] binaryChar = new char[sizeOut];
		
		// scramble by putting into char array of size sizeOut
		for(int i = 0; i < sizeOut; i++)
			binaryChar[i] = binaryStr.charAt(sbox[i] - 1);
		
		// convert back to string
		String scrambledStr = String.valueOf(binaryChar);
		
		// make it into a big int
		BigInteger returnBig = new BigInteger(scrambledStr, 2);
		
		return returnBig;
	}
	
	private static String checkBinaryStr(String binaryStr, int size){
		String returnStr = "";
		
		for(int i = binaryStr.length(); i < size; i++){
			returnStr = returnStr + "0";
		}
		
		returnStr += binaryStr;
		return returnStr;
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
