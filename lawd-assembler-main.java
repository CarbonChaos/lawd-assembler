import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/*
 * Assembler for the LAWD processor.
 * Processor designed by Perry Du, Yvonne Lumetta, John Wang, and Vibha Alangar.
 * Assembler by Yvonne Lumetta
 * 
 * 
 * 
 * Assembler supports three pseudoinstructions: init, setmem, and LOADI.  See the README for how to use these.
 * 
 * Assembler handles errors pertaining to the paths passed, nonexistant registers, labels, and variables, and prints a list of labels
 * to the console.
 * 
 * Assembler handles variables for the setmem operation.
 * 
 * Assembler supports pc-relative addressing.
 */

public class AssemblerMain {

	static PrintWriter writer = null;
	static int pc; //program counter
	static String[] input;
	static final String DONT_CARE = "0"; //set here so it can easily be changed to 'X' or something for debugging
	static HashMap<String, Integer> labels;
	static HashMap<String, String> opcodes;
	static HashMap<String, Integer> variables;
	static int userMemory = 384;
	
	public static void main(String[] args) throws NaLException{
		File file = null;
		Scanner scanner = null;
		Scanner labelScanner = null;
		Scanner systemInput = new Scanner(System.in);
		opcodes = new HashMap<String, String>();
		variables = new HashMap<String, Integer>();
		
		setOpcodes();
		
		//try to get a file from command line arguments
		 if(args.length > 0) {
			 	String filePath = "";
			 	for(String s : args){
			 		filePath = filePath + s + " ";
			 	}
	            file = new File(filePath.trim());
	            System.out.println("Reading lines from "+filePath);
		 }else{
			 System.out.println("Please pass the path of the file as an argument.");
			 System.exit(0);
		}
		 
		 
		//if there is a file, create a scanner with it
		try {
			scanner = new Scanner(file);
			labelScanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("Exception: File not found.");
			System.exit(0); //if nothing can be done, exit immediately
		}
		
		
		//if a scanner is created, make an output file
		System.out.println("Write to where?");
		try {
			writer = new PrintWriter(systemInput.nextLine());
		} catch (FileNotFoundException e) {
			System.out.println("Exception:  Cannot create a file with this path.");
			System.exit(0);
		}
		
		 //if the program gets here, the file is ready to be translated.
		pc = 0;
		labels = doFirstPass(labelScanner);
		pc = 0;
		doSecondPass(scanner);
		
		 
		systemInput.close();
		writer.close();
		System.out.println("Program assembled.");
	}  //end main
	
	
	//  Useful functions below!
	
	private static HashMap<String,Integer> doFirstPass(Scanner scanner){
		HashMap<String,Integer> labelMap = new HashMap<String,Integer>();
		String nextLine = null;
		String toAdd = null;
		while(scanner.hasNextLine()){
			nextLine = scanner.nextLine();
			if(!nextLine.isEmpty()){
				toAdd = parseNextForLabels(nextLine.trim());
				if(toAdd != null){
					labelMap.put(toAdd,pc);
				}
				pc++; //increment the program counter if and only if the next line is a command
			}
		}
		System.out.println("Labels: "+labelMap);
		return labelMap;
	}
	
	private static void doSecondPass(Scanner scanner) throws NaLException{
		String nextLine = null;
		 while(scanner.hasNextLine()){
				nextLine = scanner.nextLine();
				if(!nextLine.isEmpty())
					parseNextLine(nextLine.trim());	
					pc++;
			 }
	}
	
	
	//as the name states, parses the next line, looking for labels.
	private static String parseNextForLabels(String nextLine){
		String label = null;
		String[] input = nextLine.split("\\s+");
		if(input[0].endsWith(":"))
			label = input[0].substring(0,input[0].length()-1);
		return label;
	}
	
	
	
	
	//prepares the next line of work for the second pass - ignores labels
	private static void parseNextLine(String nextLine) throws NaLException{
		input =  nextLine.split("\\s+");
		//absurd idea that works absurdly well
		if(input[0].endsWith(":")){
			ArrayList<String> temp = new ArrayList<String>();
			Collections.addAll(temp, input);
			temp.remove(0);
			input = temp.toArray(new String[temp.size()]);
		}
		doNextLine(input[0]);
	}


	
	private static void doNextLine(String instruction) throws NaLException{
		//I could use a dictionary... but I could also use a gigantic case statement.
		switch(instruction){
		case "push":
			doOType(instruction);
			break;
		case "pop":
			doOType(instruction);
			break;
		case "ret":
			doOType(instruction);
			break;
			
		case "addi":
			doIType(instruction);
			break;
		case "subi":
			doIType(instruction);
			break;
		case "ori":
			doIType(instruction);
			break;
		case "andi":
			doIType(instruction);
			break;
		case "lui":
			doIType(instruction);
			break;
		case "sar":
			doIType(instruction);
			break;
		case "sll":
			doIType(instruction);
			break;
		case "loadi":
			doIType(instruction);
			break;
			
		case "add":
			doRType(instruction);
			break;
		case "sub":
			doRType(instruction);
			break;
		case "and":
			doRType(instruction);
			break;
		case "load":
			doRType(instruction);
			break;
		case "store":
			doRType(instruction);
			break;
		case "setreg":
			doRType(instruction);
			break;
		case "or":
			doRType(instruction);
			break;
		case "xor":
			doRType(instruction);
			break;
		case "ilt":
			doRType(instruction);
			break;
			
		case "leapal":
			doLeap(instruction);
			break;
		case "lz":
			doLeap(instruction);
			break;
		case "lnz":
			doLeap(instruction);
			break;
		case "leap":
			doLeap(instruction);
			break;
			
		case "init":
			doInit();
			break;
		case "LOADI":
			doLoadBig();
			break;
		case "setmem":
			doSetMem();
			break;
				
		default:
			System.out.println("Instruction '"+instruction+"' not supported.");
			break;
		}
	}
	private static void doRType(String op){
		writer.println(opcodes.get(op) + getRegister(input[1]) + dontCares(8));
	}
	private static void doOType(String op){
		writer.println(opcodes.get(op) + dontCares(11));
	}
	private static void doIType(String op){
		writer.println(opcodes.get(op) + dontCares(3) + binaryInput(input[1]));
	}
	private static void doLeap(String op) throws NaLException {
		writer.println(opcodes.get(op) + dontCares(3) + getLabelPCDifference(input[1]));
	}
	
	
	//Wild pseudoinstruction appeared! (cue battle music)
	private static void doInit(){
		variables.put(input[1], userMemory + variables.size());
	}
	
	private static void doLoadBig(){
		String binaryInput = largeBinaryInput(input[1]);
		writer.println(opcodes.get("lui") +  dontCares(3) + binaryInput.substring(0,8));
		writer.println(opcodes.get("ori") + dontCares(3) + binaryInput.substring(8));
	}
	
	// setmem <memory address> <register> <value>
	private static void doSetMem(){
		String memory;
		if(input[1].matches("\\d+"))
			memory = largeBinaryInput(input[1]);
		else
			memory = largeBinaryInput(variables.get(input[1]).toString());
		String value = largeBinaryInput(input[3]);
		writer.println(opcodes.get("lui") +  dontCares(3) + memory.substring(0,8));
		writer.println(opcodes.get("ori") + dontCares(3) + memory.substring(8));
		writer.println(opcodes.get("setreg") + getRegister(input[2]) + dontCares(8));
		writer.println(opcodes.get("lui") +  dontCares(3) + value.substring(0,8));
		writer.println(opcodes.get("ori") + dontCares(3) + value.substring(8));
		writer.println(opcodes.get("store") + getRegister(input[2]) + dontCares(8));		
	}
	
	
	
	private static String getRegister(String c){
		switch(c){
		case "$a":
			return "000";
		case "$b":
			return "001";
		case "$c":
			return "010";
		case "$d":
			return "011";
		case "$e":
			return "100";
		case "$f":
			return "101";
		case "$g":
			return "110";
		case "$h":
			return "111";
		default:
			System.out.println("NaR");
			return "NaR";		
		}
		
		
	}
	
	
	
	private static void setOpcodes(){
		opcodes.put("setreg", "00000");
		opcodes.put("ilt", "00001");
		opcodes.put("add", "00010");
		opcodes.put("sub", "00011");
		opcodes.put("subi", "00100");
		opcodes.put("addi", "00101");
		opcodes.put("and", "00110");
		opcodes.put("or", "00111");
		opcodes.put("andi", "01000");
		opcodes.put("ori", "01001");
		opcodes.put("xor", "01010");
		opcodes.put("sar", "01110");
		opcodes.put("sll", "01111");
		opcodes.put("lz", "10000");
		opcodes.put("lnz", "10001");
		opcodes.put("leapal", "10010");
		opcodes.put("leap", "10011");
		opcodes.put("push", "10100");
		opcodes.put("pop", "10101");
		opcodes.put("lui", "11010");
		opcodes.put("loadi", "11100");
		opcodes.put("load", "11101");
		opcodes.put("ret", "11011");
		opcodes.put("store", "11111");
	}
	
	
	//spawns 'don't care' bits the given number of times
	private static String dontCares(int n){
		String dontCares = "";
		for(int i=0;i<n;i++)
			dontCares = dontCares + DONT_CARE;
		return dontCares;
	}
	
	
	
	//positive numbers get left-padded with zeroes
	private static String binaryInput(String s){
		String nonPaddedInt = Integer.toBinaryString(Integer.parseInt(s));
		return String.format("%08d", Integer.parseInt(nonPaddedInt));
	}
	
	private static String largeBinaryInput(String s){
		String nonPaddedInt = Integer.toBinaryString(Integer.parseInt(s));
		return String.format("%016d", Integer.parseInt(nonPaddedInt));
	}
	
	//negative numbers have support with the Integer class, but it returns a 2s complement number in 32 bits
	private static String negativeBinaryInput(String s){
		String nonPaddedInt = Integer.toBinaryString(Integer.parseInt(s));
		return nonPaddedInt.substring(24, 32);  //chop the 32 bit number to 8 bits
	}
	
	
	//gets the address of the label - program counter (pc-relative addressing)
	private static String getLabelPCDifference(String s) throws NaLException{
		if(labels.containsKey(s)){
			int address = (labels.get(s) - pc)-1;
			if(address >= 0)
				return binaryInput(Integer.toString(address));
			else
				System.out.println("Address: " +address);
				System.out.println("PC:  " + pc);
				System.out.println(address + 1);
				return negativeBinaryInput(Integer.toString(address+2));  //this accounts for the subtraction, and the program counter 
																		  //incrementing before the leap.
			
			
		}else{
			throw new NaLException(s + "is not a valid label.");
		}
	}
}

/*
 * Custom exception class
 */
@SuppressWarnings("serial")
class NaLException extends Exception
{
  public NaLException(String message)
  {
    super(message);
  }
}
