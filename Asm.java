import java.util.*;
import java.io.*;

public class Asm {
	//opcodes
	private enum opcodes {
		HALT, PUSH, RVALUE, LVALUE, POP, STO, COPY, ADD, SUB,
		MPY, DIV, MOD, NEG, NOT, OR, AND, EQ, NE, GT, GE, LT,
		LE, LABEL, GOTO, GOFALSE, GOTRUE, PRINT, READ, GOSUB, RET;
	}
	
	//Symbol table
	private static class Record {
		String 	lexeme;
		String 	type;
		int	offset;
		int 	val;

		public Record (String lex, String type, int off) {
			this.lexeme = lex;
			this.type = type;
			this.offset = off;
			setVal();
		}

		public String getLex () {
			return lexeme;
		}

		public int getOff () {
			return offset;
		}

		public int getVal () {
			return val;
		}

		public void setVal () {
			for (opcodes op : opcodes.values()) {
				if (lexeme.equalsIgnoreCase (op.name())) {
					val = op.ordinal();
				}
			}
		}
				
	}
			
	private static ArrayList<Record> table = new ArrayList<Record>();		

	public static void main (String args[]) throws FileNotFoundException, IOException {
		String filename;
		Scanner input = new Scanner(System.in);

		//Get input file
		if (args.length == 0) {
			System.out.println("Please input file name: ");
			filename = input.next();
		} else {
			filename = args[0];
		}
			
		//Open file
		input = new Scanner (new File (filename));

		//Pass1: symbol table
		pass1 (input);
		input.close();

		//TEST
		for (Record rec : table) {
			System.out.println (rec.getLex());
		}

		//Pass2: generate code
		input = new Scanner (new File (filename));		
		pass2 (input);
	}

	public static void pass1 (Scanner infile) {
		// initialize location counter, etc.
		int locationCounter = 0;
		String line;
		Scanner input;
		String lexeme;

		// find start of data section
		do {
			line = infile.nextLine();
			System.out.println(line);
			input = new Scanner(line);
		} 
		while (!input.next().equalsIgnoreCase("Section"));

		if (!input.next().equalsIgnoreCase(".data"))
		{
			System.err.println("Error:  Missing 'Section .data' directive");
			System.exit(1);
		}
		else
		{
			
		}

		// build symbol table from variable declarations
		line = infile.nextLine();
		input = new Scanner(line);

		// data section ends where code section begins
		while(!(lexeme = input.next()).equalsIgnoreCase("Section"))
		{
			// look for labels (they end with a colon)
			int pos = lexeme.indexOf(':');
			if (pos > 0)
			{
				lexeme = lexeme.substring(0,pos);
			}
			else
			{
				System.err.println("error parsing " + line);
			}
	
			if (!isInTable (lexeme)) {
				table.add (new Record (lexeme,"Int",locationCounter));
				locationCounter++;
				line = infile.nextLine();
				input = new Scanner(line);
			}
		}

		// Now, parse the code section, looking for the label directive
		locationCounter=0;
		while (infile.hasNext ())
		{
			line = infile.nextLine ();
			input = new Scanner (line);
			lexeme = input.next ();
			// when a label is found, place it and it's code offset in the symbol table
			if (lexeme.equalsIgnoreCase ("label"))
			{
				lexeme = input.next ();
				table.add (new Record (lexeme,"Code",locationCounter));
			}

			locationCounter++;
		}

		System.out.println ("Pass 1 complete.");
	}

	public static void pass2 (Scanner infile) throws FileNotFoundException, IOException {
		// initialize location counter, etc.
		int locationCounter = 0;
		int ptr;
		int opcode;
		int operand;
		int outputVal;
		String line;
		Scanner input;
		String lexeme;
		FileOutputStream outFile;
		DataOutputStream output;
		final int NULL = -1;

		do
		{
			line = infile.nextLine();
			input = new Scanner(line);

		} while (!input.next().equalsIgnoreCase("Section"));

		if (!input.next().equalsIgnoreCase(".data"))
		{
			System.err.println("Error:  Missing 'Section .data' directive");
			System.exit(1);
		}
		else
		{
			
		}
		line = infile.nextLine();
		input = new Scanner(line);

		while(!(lexeme = input.next()).equalsIgnoreCase("Section"))
		{
			// data section has been processed in previous pass, so skip this
			line = infile.nextLine();
			input = new Scanner(line);
		}

		// Now, let's generate some code
		locationCounter=0;
		outFile = new FileOutputStream ("hexdump.bin");
		output = new DataOutputStream (outFile);

		// while not end of file keep parsing
		while(infile.hasNext())
		{
			line = infile.nextLine();
			input = new Scanner(line);
			lexeme = input.next();
			operand = 0;

			// lookup opcode and generate appropriate instructions
			if (lexeme.equalsIgnoreCase (":=")) {
				opcode = 5;
			} else {
				opcode = lookUpOpcode(lexeme);
			}

			outputVal = opcode;

			switch(opcode)
			{
				//HALT
				case 0:
					//Print to bin file
					output.writeInt (outputVal);
					break;
				//PUSH
				case 1:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//RVALUE
				case 2:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();

					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//LVALUE
				case 3:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//POP
				case 4:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//STO
				case 5:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//COPY
				case 6:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//ADD
				case 7:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//SUB
				case 8:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//MPY
				case 9:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//DIV
				case 10:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//MOD
				case 11:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//NEG
				case 12:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//NOT
				case 13:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//OR
				case 14:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//AND
				case 15:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//EQ
				case 16:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//NE
				case 17:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//GT
				case 18:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//GE
				case 19:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//LT
				case 20:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//LE
				case 21:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//LABEL
				case 22:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//GOTO
				case 23:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//GOFALASE
				case 24:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//GOTRUE
				case 25:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//PRINT
				case 26:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//READ
				case 27:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				//GOSUB
				case 28:
					//put opcode in place
					outputVal = outputVal << 16;

					//Add operand
					lexeme = input.next();
					if (!isANum (lexeme)) {
						operand = table.get (getTableIndex (lexeme)).getOff ();

					if (operand == -1) {
						System.out.println ("Operand undefined.");
						continue;
					}

					} else {
						operand = Integer.parseInt (lexeme);
					}

					outputVal = outputVal ^ operand;

					//Print to bin file
					output.writeInt (outputVal);				
					break;
				//RET
				case 29:
					//put opcode in place
					outputVal = outputVal << 16;

					//Print to bin file
					output.writeInt (outputVal);
					break;
				default:
					System.err.println("Unimplemented opcode:  " + opcode);
			}

			//TEST	
			System.out.println (opcode + "\t" + operand);

			locationCounter++;
		}

		output.close();
		outFile.close();

		System.out.println ("Pass 2 complete.");
		
	}

	public static int lookUpOpcode (String lex) {
		return opcodes.valueOf (lex).ordinal ();

	}

	public static int getTableIndex (String lex) {
		int index = 0;

		for (Record rec : table) {
			if (lex.equalsIgnoreCase (rec.getLex())) {
				return index;
			} else {
				index++;
			}
		}
			
		return -1;
	}

	public static boolean isANum (String lex) {
		try { 
        		Integer.parseInt (lex); 
    		} catch (NumberFormatException e) { 
        		return false; 
		}

		return true;
	} 

	public static boolean isInTable (String lex) {
		// Check if multiply-defined; if not, insert into symbol table
		for (Record rec : table) {
			if (lex.equalsIgnoreCase (rec.getLex())) {
				System.out.println (lex + " already in symbol table.");
				return true;
			}
		}
	
		return false;
	}
}

