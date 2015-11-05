/**
 * WRITTEN BY: Karyn Vo and Sydney Warner
 * CS466 Assignment5: Part A - DES in CBC mode
 * due 10/30 @ 11:59
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.security.SecureRandom;
import java.math.BigInteger;

import gnu.getopt.Getopt;


public class DES_Skeleton {
	
	public static void main(String[] args) {
		
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
		ArrayList<BigInteger> subkeys = genSubkeys(keyStr.toString());
		Collections.reverse(subkeys); //INVERSE SUBKEYS
		try {
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			List<String> lines = Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset());
			String IVStr = lines.get(0);
			lines.remove(0);
			String encryptedText;
			String trimmed = "";
			String readIn="";
			for (String line : lines) {
				readIn += line;
			}
				encryptedText = DES_decrypt(IVStr, readIn, subkeys);
				//remove last line which is padding
				trimmed = encryptedText.substring(0, encryptedText.length() - (8-Integer.parseInt(encryptedText.substring(encryptedText.length()-1))));
				writer.print(trimmed);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_decrypt(String iVStr, String line, ArrayList <BigInteger> subkeys) {
		
		SBoxes sbox = new SBoxes();
		
		//System.out.println("Subkeys");
		//for(BigInteger big : subkeys)
			//System.out.println(big.toString(16));
		String substring=""; //8 chars from original line
		String substringBinary=""; // mixed up substring in binary
		String decrypted="";
		BigInteger substringBI = null;
		
		for (int x = 0; x < line.length(); x+=16) {
			//System.out.println("Line: " + line);
			substring = line.substring(x, x+16);
			substringBinary = new BigInteger(substring, 16).toString(2);
			substringBinary = checkBinaryStr(substringBinary, 64);
			
			// IP box first
			BigInteger substringFP = scramble(substringBinary, 64, sbox.IP);
			substringBinary = checkBinaryStr(substringFP.toString(2), 64);
			
			//split into left and right without running through IP box
			String leftBinary = substringBinary.substring(0, 32);
			String rightBinary = substringBinary.substring(32, 64);
			
			BigInteger leftBigInt = new BigInteger(leftBinary, 2);			//take the halves and make them big ints
			BigInteger rightBigInt = new BigInteger(rightBinary, 2);
			
			String plaintextBlock = encryptBlock(leftBigInt, rightBigInt, subkeys, 0);
			BigInteger plainBI = new BigInteger(plaintextBlock, 2);
			
			//System.out.println("Decrypt after encryptBlock: " + plainBI.toString(16));
			
			
			// after decrypting block
			
			// xor with iv or previous
			if(x == 0){
				BigInteger iV = new BigInteger(iVStr, 16);
				substringBI = iV.xor(plainBI);
			}else{
				//System.out.println(line.substring(x-16, x));
				BigInteger previousCipher = new BigInteger(line.substring(x-16, x), 16);
				substringBI = previousCipher.xor(plainBI);
			}
			substringBI = scramble(checkBinaryStr(substringBI.toString(2), 64), 64, sbox.FP);
			substringBinary = checkBinaryStr(substringBI.toString(2), 64);
			
			byte[] byteSubstring = substringBI.toByteArray();
			String decryptedSubStr = new String(byteSubstring);
			
			decrypted = decrypted + decryptedSubStr;
		}
		return decrypted;
	}
	
	

//change string to string builder
	private static void encrypt(StringBuilder keyStr, StringBuilder inputFile, StringBuilder outputFile) {
		ArrayList<BigInteger> subkeys = genSubkeys(keyStr.toString());
	
		//System.out.println("Subkeys from encrypt");
		/*for(BigInteger big : subkeys) {
			System.out.println(big.toString(16));
		}*/
	//	System.out.println("After subkeys");
		try {
		//	System.out.println("In try block");
			PrintWriter writer = new PrintWriter(outputFile.toString(), "UTF-8");
			String textToPass="";
			String encryptedText="";
			for (String line : Files.readAllLines(Paths.get(inputFile.toString()), Charset.defaultCharset())) {
				textToPass += line + "\n";
			}
				int linelength = textToPass.length();
				int extraChars = linelength % 8;
				
				if (extraChars == 0) {
					textToPass = textToPass + "00000000";
				}
				else {
					for (int t = extraChars; t < 7; t++) {
					textToPass +="0";
					}
				
					textToPass += extraChars;
				}
				//System.out.println("Lines read in" + textToPass);
			//	System.out.println("\ngoing to call DES_Encrypt\n");
				encryptedText = DES_encrypt(textToPass, subkeys);
			//	System.out.println(encryptedText);
				writer.print(encryptedText);
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	/**
	 * TODO: You need to write the DES encryption here.
	 * @param line
	 */
	private static String DES_encrypt(String line, ArrayList<BigInteger> subkeys) {
		//System.out.println("\nSTARTED DES_ENCRYPT\n");
		SBoxes sbox = new SBoxes();
		/*
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
		*/
		/*System.out.println("Subkeys");
		for(BigInteger big : subkeys)
			System.out.println(big.toString(16));
		*/
		String substring=""; //8 chars from original line
		String substringBinary=""; // mixed up substring in binary
		String encrypted="";
		BigInteger previousCipher = null;
		
		for (int x = 0; x < line.length(); x+=8) {
			substring = line.substring(x, x+8);
			substringBinary = new BigInteger(substring.getBytes()).toString(2);
			substringBinary = checkBinaryStr(substringBinary, 64);
			
			//System.out.println("Encrypt Before IP: " + new BigInteger(substring.getBytes()).toString(16));
			
			BigInteger substringBI = scramble(substringBinary, 64, sbox.IP); //this makes the above a big int
		//	System.out.println("Checking substringBI after scramble" + checkBinaryStr(substringBI.toString(2), 64));
			
			if(x == 0){
				//CREATE IV
				String hexiV = genDESkey();
				BigInteger iV = new BigInteger(hexiV, 16);
				//xor BIG INT and IV, add IV to String that will contain ciphertext;
				substringBI = iV.xor(substringBI);
				encrypted = hexiV + "\n";
//				System.out.println("iV is: " + encrypted);
			}
			else {
				substringBI = previousCipher.xor(substringBI);
			}
			
			String checkSubstringBI = checkBinaryStr(substringBI.toString(2), 64);
			
			String leftBinary = checkSubstringBI.substring(0, 32);	//toString, divide in half L0
			String rightBinary = checkSubstringBI.substring(32, 64); //R0
			
			BigInteger leftBigInt = new BigInteger(leftBinary, 2);			//take the halves and make them big ints
			BigInteger rightBigInt = new BigInteger(rightBinary, 2);
			//System.out.println("Encrypt before encryptblock: " + new BigInteger(checkSubstringBI, 2).toString(16));
			String cipherblock =  encryptBlock(leftBigInt, rightBigInt, subkeys, 0);
			
			//final permutation box
			cipherblock = checkBinaryStr(scramble(cipherblock, 64, sbox.FP).toString(16), 16); /* ALERT CHECKED CIPHERBLOCK*/
			previousCipher = new BigInteger(cipherblock, 16);
			
			encrypted = encrypted + cipherblock + "\n";		//begin function f
			//System.out.println("line: " + line + "\tline length: " + line.length());
		//	System.out.println("cipherblock is: " + cipherblock + "\tlength: " + cipherblock.length());
		}
		return encrypted;
	}
	
	private static String encryptBlock(BigInteger left, BigInteger right, ArrayList<BigInteger> subkeys, int keyNum) {
		
		String cipherBlock=""; //This will be our concatenated string after the addressing (Sbox)
		
		SBoxes sbox = new SBoxes();
		
		if (keyNum == 16) {
			//right + left = 64 bit ciphertext
			String rightLeft = checkBinaryStr(right.toString(2), 32) + checkBinaryStr(left.toString(2), 32);
			return rightLeft;
		}
		
		/* begin function f */
		
		// scramble Right using E box send right as binary substring, in (32) -> E -> 48
		String rightStr = checkBinaryStr(right.toString(2), 32);
		BigInteger eBoxRightResult = scramble(rightStr, 48, sbox.E); // E(R0)
		
		// XOR with subkey
		BigInteger xorResult = eBoxRightResult.xor(subkeys.get(keyNum));
		
		// divide into 8 blocks of 6
		String xorString = checkBinaryStr(xorResult.toString(2), 48);
		for(int x = 0; x < 48; x+=6){
			String rowSubStr = xorString.substring(x, x+2);
			String colSubStr = xorString.substring(x+2, x+6);
			
			// make into big int to convert to decimal
			int row = Integer.parseInt(new BigInteger(rowSubStr,2).toString());
			int col = Integer.parseInt(new BigInteger(colSubStr,2).toString());
			
			// get 4 bit from corresponding S box
			// (row - 1) * 15 + col - 1
			//System.out.println("keyNum: " + keyNum + "\tx: " + x + "\trow: " + row + "\tcol: " + col);
			int result = sbox.S[x/6][((row) * 15 + col )];
			/* FOUR BIT */
			String fourBit = Integer.toBinaryString(result);
			//checkBinaryStr (size 4)
			
			for( int count = fourBit.length(); count < 4; count++) {
				cipherBlock = cipherBlock + "0";
			}
			
			cipherBlock = cipherBlock + fourBit;
		}
		
		BigInteger pBoxResult = scramble(cipherBlock, 32, sbox.P);
		BigInteger newRight = left.xor(pBoxResult); //final right big int
		//left old right
		BigInteger newLeft = right;
		keyNum++;
		return encryptBlock(newLeft, newRight, subkeys, keyNum);
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
		
		BigInteger fullPC1 = scramble(strBinary, 56, sbox.PC1);
		String fullPC1Str = checkBinaryStr(fullPC1.toString(2), 56);

		/* split string into 2 */
		String frontPC1 = fullPC1Str.substring(0, 28);
		String backPC1 = fullPC1Str.substring(28, 56);
		
		BigInteger frontBigInt = new BigInteger(frontPC1, 2); //string to big int
		BigInteger backBigInt = new BigInteger(backPC1, 2);
		
		/* begin rotations */
		ArrayList<BigInteger> subKeyList = new ArrayList<BigInteger>(); //holds 16 subkeys stored as byte arrays
		for (int x = 0; x < sbox.rotations.length; x++) {
			for (int y = 0; y < sbox.rotations[x]; y++) {
				frontBigInt = rotateLeft(frontBigInt);
				backBigInt = rotateLeft(backBigInt);
//				System.out.println("Front binary: " + frontBigInt.toString(2));
//				System.out.println("Back binary: " + backBigInt.toString(2));
			}
			
			String frontStr = checkBinaryStr(frontBigInt.toString(2), 28);
			String backStr = checkBinaryStr(backBigInt.toString(2), 28);
			String temp = frontStr + backStr;
			BigInteger bigtemp = scramble(checkBinaryStr(temp, 56), 48, sbox.PC2);
			subKeyList.add(bigtemp);
		}
		
		return subKeyList;
	}
	
	// Rotates left
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
	
	// Pads string of binary with additional zeros
	private static String checkBinaryStr(String binaryStr, int size){
		String returnStr = "";
		
		for(int i = binaryStr.length(); i < size; i++){
			returnStr = returnStr + "0";
		}
		
		returnStr += binaryStr;
		return returnStr;
	}

	// Generates a random DES key
	static String genDESkey(){
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
			return genDESkey();
		}
		System.out.println("Key: " + sb.toString());
		return sb.toString();
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
		
		String useage = ""
				+ "-k \t Generates a 16 character DES key encoded in hex to be used for encryption and decryption\n"
				+ "-e \t Takes 1 argument; a 16 hexadecimal key from the user and encrypts a message from a specified inputfile (-i inputFileName) in PlaintText to a specified outputfile (-o outputFileName) in CipherText\n"
				+ "-d \t Takes 1 argument; a 16 hexadecimal key from the user and decrypts a message from a specified inputfile (-i inputFileName) in CipherText to a specified outputfile (-o outputFileName) in PlainText\n"
				+ "-i \t Takes 1 argument that is the name of the input file from which the encryption program will read\n"
				+ "-o \t Takes 1 argument that is the name of the output file from which the encryption program will write\n";
		
		System.out.println("-k\tGenerates a DES key encoded in hex\n");
		System.out.println("-e\tTakes 1 argument that takes a 64 bit key in hex and encrypts file specified by -i\n");
		System.out.println("-d\tTakes 1 argument that takes a 64 bit key in hex and decrypts file specified by -i\n");
		System.out.println("-i\tTakes 1 argument that is the name of the input file\n");
		System.out.println("-o\tTakes 1 argument that is the name of the output file\n");
		
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
	//	System.out.println("check key\n");
		if (desKey == null) {
			return false;
		}
		if (desKey.length() != 16) {
			//System.out.println("key length" + desKey.length() + "\n");
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
