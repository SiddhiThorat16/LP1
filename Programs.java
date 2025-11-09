/*
Chit No 1 – Assembler Pass I
Problem Statement: Design suitable data structures and implement Pass I of a two-pass
assembler. Generate the Symbol Table and Intermediate Code.
Sample Input:
START 200
MOVER AREG, ='5'
ADD BREG, ONE
MOVEM AREG, TEMP
ONE DC 1
TEMP DS 1
END
*/


// AssemblerPassOne.java
import java.io.*;
import java.util.*;

class Tuple {
    String mnemonic, mclass, opcode;
    int length;

    Tuple(String m, String c, String o, String l) {
        mnemonic = m;
        mclass = c;
        opcode = o;
        length = Integer.parseInt(l);
    }
}

class SymTuple {
    String symbol, address;
    SymTuple(String s, String a) {
        symbol = s;
        address = a;
    }
}

class LitTuple {
    String literal, address;
    LitTuple(String l, String a) {
        literal = l;
        address = a;
    }
}

public class AssemblerPassOne {
    static int lc = 0;
    static Map<String, Tuple> MOT = new HashMap<>();
    static Map<String, SymTuple> SYMTAB = new LinkedHashMap<>();
    static ArrayList<LitTuple> LITTAB = new ArrayList<>();
    static Map<String, String> REGTAB = new HashMap<>();

    public static void main(String[] args) throws Exception {
        initializeTables();
        pass1("input.txt");
    }

    static void initializeTables() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("mot.txt"));
        String s;
        while ((s = br.readLine()) != null) {
            String[] arr = s.trim().split(" ");
            if (arr.length < 4) continue;
            MOT.put(arr[0], new Tuple(arr[0], arr[1], arr[2], arr[3]));
        }
        br.close();

        REGTAB.put("AREG", "1");
        REGTAB.put("BREG", "2");
        REGTAB.put("CREG", "3");
        REGTAB.put("DREG", "4");
    }

    static void pass1(String filename) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        // Output files
        PrintWriter outIC = new PrintWriter(new FileWriter("output_pass1.txt"));
        PrintWriter outSym = new PrintWriter(new FileWriter("symtab.txt"));
        PrintWriter outLit = new PrintWriter(new FileWriter("littab.txt"));

        String line;
        System.out.println("=== INTERMEDIATE CODE ===");

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] tokens = line.trim().split("[ ,]+");
            if (tokens.length == 0) continue;

            String output = "";
            int index = 0;

            // Check if first token is a label
            if (!MOT.containsKey(tokens[0])) {
                String label = tokens[0];
                SYMTAB.putIfAbsent(label, new SymTuple(label, String.valueOf(lc)));
                index = 1;
            }

            if (index >= tokens.length) continue;

            String op = tokens[index];
            Tuple t = MOT.get(op);
            if (t == null) continue;

            // AD - Assembler Directive
            if (t.mclass.equals("AD")) {
                if (op.equals("START")) {
                    lc = Integer.parseInt(tokens[index + 1]);
                    output = "(AD,01) (C," + lc + ")";
                } else if (op.equals("END")) {
                    output = "(AD,02)";
                    assignLiteralAddresses();
                }
            }

            // IS - Imperative Statement
            else if (t.mclass.equals("IS")) {
                output = lc + " (" + t.mclass + "," + t.opcode + ") ";
                output += getOperand(Arrays.copyOfRange(tokens, index + 1, tokens.length));
                lc += t.length;
            }

            // DL - Declarative Statement
            else if (t.mclass.equals("DL")) {
                String symbol = tokens[0];
                String constant = tokens[index + 1];
                output = lc + " (" + t.mclass + "," + t.opcode + ") (C," + constant + ")";
                SYMTAB.put(symbol, new SymTuple(symbol, String.valueOf(lc)));

                if (t.mnemonic.equals("DS"))
                    lc += Integer.parseInt(constant);
                else
                    lc += t.length;
            }

            System.out.println(output);
            outIC.println(output);
        }

        // Print symbol table
        System.out.println("\n=== SYMBOL TABLE ===");
        for (SymTuple s : SYMTAB.values()) {
            System.out.println(s.symbol + "\t" + s.address);
            outSym.println(s.symbol + "\t" + s.address);
        }

        // Print literal table
        System.out.println("\n=== LITERAL TABLE ===");
        for (LitTuple l : LITTAB) {
            System.out.println(l.literal + "\t" + l.address);
            outLit.println(l.literal + "\t" + l.address);
        }

        br.close();
        outIC.close();
        outSym.close();
        outLit.close();
    }

    static String getOperand(String[] operands) {
        StringBuilder res = new StringBuilder();
        for (String tok : operands) {
            if (REGTAB.containsKey(tok))
                res.append(REGTAB.get(tok)).append(" ");
            else if (tok.startsWith("=")) {
                LITTAB.add(new LitTuple(tok, "-"));
                res.append("(L,").append(LITTAB.size()).append(") ");
            } else {
                SYMTAB.putIfAbsent(tok, new SymTuple(tok, "-"));
                res.append("(S,").append(SYMTAB.size()).append(") ");
            }
        }
        return res.toString();
    }

    static void assignLiteralAddresses() {
        for (LitTuple l : LITTAB) {
            if (l.address.equals("-")) {
                l.address = String.valueOf(lc++);
            }
        }
    }
}
/*
create input.txt file 
			START 200
			MOVER AREG,='5'
			ADD BREG, ONE
			MOVEM AREG, TEMP
			ONE DC 1
			TEMP DS 1
			END


create mot.txt file
			START AD 01 0
			END AD 02 0
			LTORG AD 03 0
			DS DL 01 1
			DC DL 02 1
			ADD IS 01 1
			SUB IS 02 1
			MULT IS 03 1
			MOVER IS 04 1
			MOVEM IS 05 1
*/

/*
create output_pass1.txt file
create symtab.txt file
create littab.txt file
*/






/*
🧠 AIM

To design and implement the Pass I of a Two-Pass Assembler using Java, which generates:

Symbol Table (SYMTAB)

Literal Table (LITTAB)

Intermediate Code (IC)

🧰 APPARATUS / SOFTWARE REQUIRED

System with JDK installed

Text editor / IDE (VS Code, Notepad++, etc.)

Java compiler (javac) and interpreter (java)

🧾 THEORY
🔹 What is an Assembler?

An assembler is a program that converts assembly language instructions into machine code (binary).
It’s used to translate low-level programs written using mnemonics like MOVER, ADD, etc.

🔹 Two-Pass Assembler

A Two-Pass Assembler scans the source code twice:

Pass I:

Assign addresses to all labels and symbols.

Build the Symbol Table and Literal Table.

Generate Intermediate Code (IC).

Pass II:

Use the IC, Symbol Table, and Literal Table to generate the final machine code.

🔹 Data Structures Used
Table	Purpose	Example
MOT (Mnemonic Opcode Table)	Stores details of each instruction — mnemonic, class, opcode, length	MOVER IS 04 1
SYMTAB (Symbol Table)	Stores symbols and their addresses	ONE 203
LITTAB (Literal Table)	Stores literals and their assigned addresses	='5' 205
REGTAB (Register Table)	Maps registers to codes	AREG=1, BREG=2
🔹 Flow of Pass I

Read first instruction (START) → Initialize Location Counter (LC)

For each line:

If line has a label, enter it in SYMTAB

If line has literal, enter it in LITTAB

If instruction is imperative, increment LC

If instruction is declarative (DC/DS), allocate memory

At END, assign addresses to all literals

Output the Intermediate Code, SYMTAB, and LITTAB

🧮 ALGORITHM

Start.

Initialize data structures (MOT, SYMTAB, LITTAB, REGTAB).

Open the source input file.

For each line:

If it contains a label, store it in SYMTAB with LC.

Identify the instruction from MOT.

If it’s:

AD (Assembler Directive) → Handle START/END

IS (Imperative Statement) → Generate intermediate code, increment LC

DL (Declarative Statement) → Update SYMTAB, increment LC by operand value

If operand contains a literal, store it in LITTAB.

When END is found, assign addresses to all literals.

Print all tables and intermediate code.

Stop.

⚙️ PROGRAM OUTPUT (Example)

Input:

START 200
MOVER AREG,='5'
ADD BREG, ONE
MOVEM AREG, TEMP
ONE DC 1
TEMP DS 1
END


Output:

=== INTERMEDIATE CODE ===
(AD,01) (C,200)
200 (IS,04) 1 (L,1)
201 (IS,01) 2 (S,1)
202 (IS,05) 1 (S,2)
203 (DL,02) (C,1)
204 (DL,01) (C,1)
(AD,02)

=== SYMBOL TABLE ===
ONE     203
TEMP    204

=== LITERAL TABLE ===
='5'    205

💬 VIVA QUESTIONS & ANSWERS (THEORY)
🔹 1. What is the role of an assembler?

An assembler translates assembly language programs into machine language that the computer can execute.

🔹 2. What is a two-pass assembler?

It is an assembler that scans the source code twice —

Pass I: Builds symbol/literal tables, assigns addresses.

Pass II: Generates actual machine code using these tables.

🔹 3. What happens in Pass I of the assembler?

Assigns addresses to labels

Creates symbol and literal tables

Generates intermediate code

🔹 4. What are assembler directives?

Instructions for the assembler, not for execution.
Examples: START, END, LTORG, DS, DC.

🔹 5. What is a literal?

A literal is a constant value written directly in the instruction.
Example: ='5' is a literal representing constant 5.

🔹 6. What is the function of the Location Counter (LC)?

It keeps track of the memory address of the next instruction or data.

🔹 7. What is a symbol?

A symbol is a name representing a memory location.
Example: TEMP DS 1 → TEMP is a symbol.

🔹 8. What is Intermediate Code?

A partially processed output of Pass I that includes symbolic references and opcode details, used in Pass II to generate final machine code.

🔹 9. What data structures are used in Pass I?

MOT (HashMap)

SYMTAB (LinkedHashMap)

LITTAB (ArrayList)

REGTAB (HashMap)

🔹 10. Why is two-pass assembly required?

Because during the first pass, some symbols might be undefined (forward references).
Pass II resolves them using the symbol table built in Pass I.

💻 VIVA QUESTIONS & ANSWERS (PRACTICAL)
🔹 1. What output files are generated by your program?

output_pass1.txt → Intermediate Code

symtab.txt → Symbol Table

littab.txt → Literal Table

🔹 2. What happens if a label appears before it is defined?

It is added to the Symbol Table with an address “-” initially, and resolved later when defined.

🔹 3. What does (IS,04) represent?

It means Imperative Statement (IS) with opcode 04, which corresponds to the mnemonic MOVER.

🔹 4. How is a literal address assigned?

All literals are assigned addresses after the END directive, sequentially starting from the current LC.

🔹 5. What is the difference between DC and DS?
Directive	Meaning	Example	Action
DC	Define Constant	ONE DC 1	Allocates memory and stores 1
DS	Define Storage	TEMP DS 1	Reserves memory block
🔹 6. What is the LC value after the START 200 directive?

LC = 200 (initialized from operand)

🔹 7. What is stored in the Symbol Table?

Symbols with their memory addresses.
Example:

ONE 203
TEMP 204

🔹 8. What are the components of Intermediate Code?

LC (Location Counter)

Mnemonic class and opcode

Operand references (Symbol or Literal)

Example:

200 (IS,04) 1 (L,1)

🔹 9. Which data structure did you use for MOT and why?

A HashMap — because it allows O(1) lookup time for mnemonics.

🔹 10. How can you extend this program for Pass II?

In Pass II, read the Intermediate Code, replace symbols and literals with actual addresses from SYMTAB and LITTAB, and generate final machine code.

🧾 Conclusion

Thus, Pass I of the assembler was successfully implemented in Java.
The program correctly generated the Symbol Table, Literal Table, and Intermediate Code using suitable data structures.
*/










/*
Chit No 2 – Assembler Pass II
Problem Statement: Implement Pass II of a two-pass assembler using the given Intermediate Code
and Symbol Table to generate Object Code.
Sample Input:
Intermediate Code:
(AD,01)(C,100)
(IS,04)(1)(S,1)
(IS,01)(2)(S,2)
(IS,02)(1)(S,3)
(AD,02)
Symbol Table:
Symbol Address
A 100
B 101
C 102
*/

package A2;

import java.io.*;
import java.util.*;

// Symbol Table Tuple
class SymTuple {
    String symbol, address;

    SymTuple(String s, String a) {
        symbol = s;
        address = a;
    }
}

public class Assembler_PassTwo {

    static ArrayList<SymTuple> symtable;
    static PrintWriter out_pass2;

    // Read Symbol Table
    static void readSymbolTable() throws Exception {
        symtable = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("A2/symtable.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split("\t");
            symtable.add(new SymTuple(tokens[0], tokens[1]));
        }
        br.close();
    }

    // Get symbol address from S,index
    static String getSymbolAddress(String sIndex) {
        String[] parts = sIndex.split(",");
        int index = Integer.parseInt(parts[1]);
        return symtable.get(index - 1).address;
    }

    // Pass II: Generate object code
    static void pass2() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("A2/output_pass1.txt"));
        out_pass2 = new PrintWriter(new FileWriter("A2/output_pass2.txt"), true);

        String line;
        while ((line = br.readLine()) != null) {
            String objCode = "";

            // Split each part between parentheses
            List<String> tokens = new ArrayList<>();
            int i = 0;
            while (i < line.length()) {
                if (line.charAt(i) == '(') {
                    int j = line.indexOf(')', i);
                    if (j > i) {
                        tokens.add(line.substring(i + 1, j));
                        i = j;
                    }
                }
                i++;
            }

            if (tokens.size() == 0) continue;

            String first = tokens.get(0); // e.g., AD,01 or IS,04

            if (first.startsWith("AD")) {
                objCode = "----";
            } else if (first.startsWith("IS")) {
                String[] isParts = first.split(",");
                int opcode = Integer.parseInt(isParts[1]);
                String opCodeStr = String.format("%02d", opcode);

                if (tokens.size() > 1) {
                    int reg = Integer.parseInt(tokens.get(1)); // Register number
                    String memAddr = "0";

                    if (tokens.size() > 2 && tokens.get(2).startsWith("S,")) {
                        memAddr = getSymbolAddress(tokens.get(2));
                    }

                    objCode = opCodeStr + " " + reg + " " + memAddr;
                } else {
                    objCode = opCodeStr + " 0 0";
                }
            } else {
                objCode = "----";
            }

            System.out.println(line + "      ->      " + objCode);
            out_pass2.println(line + "      ->      " + objCode);
        }

        br.close();
        out_pass2.close();
    }

    public static void main(String[] args) throws Exception {
        readSymbolTable();
        pass2();
    }
}


/*
create output_pass1.txt file
		(AD,01)(C,100)
		(IS,04)(1)(S,1)
		(IS,01)(2)(S,2)
		(IS,02)(1)(S,3)
		(AD,02)


create symtable.txt file
		A	100
		B	101
		C	102
*/


/*
create output_pass2.txt file (optional)
*/


/*
1️⃣ Explanation of Code: Pass II Assembler
Objective

Pass II of a two-pass assembler reads the intermediate code (generated by Pass I) and symbol/literal tables, and produces object code.

The object code is what a CPU can directly execute.

Files Used

output_pass1.txt – contains intermediate code, e.g. (IS,04)(1)(S,1)

symtable.txt – symbol table mapping symbols to memory addresses, e.g. A 100

output_pass2.txt – file where the final object code is written

Classes Used

SymTuple

Represents a symbol table entry:

class SymTuple {
    String symbol, address;
}


LitTuple (optional for literals)

Represents a literal table entry.

Steps in Assembler_PassTwo

Read Symbol Table

readSymbolTable()


Reads symtable.txt and stores each symbol with its address in an ArrayList<SymTuple>.

Parse Intermediate Code

pass2()


Reads each line from output_pass1.txt.

Extracts tokens between parentheses, e.g., (IS,04)(1)(S,1) → ["IS,04","1","S,1"].

Distinguishes between:

AD → Assembler Directives (e.g., START, END) → no object code.

IS → Imperative Statements → generate object code using:

Object Code = <opcode> <register> <memory address>


S,index → Symbol → fetch address from symbol table.

Generate Object Code

Example:

(IS,04)(1)(S,1) → opcode=04, register=1, memory address=100 → 04 1 100


Write Output

Prints object code to console and writes to output_pass2.txt.

Key Code Snippets

Token extraction

List<String> tokens = new ArrayList<>();
int i = 0;
while (i < line.length()) {
    if (line.charAt(i) == '(') {
        int j = line.indexOf(')', i);
        if (j > i) tokens.add(line.substring(i + 1, j));
        i = j;
    }
    i++;
}


Opcode and memory address handling

if (first.startsWith("IS")) {
    String[] isParts = first.split(",");
    int opcode = Integer.parseInt(isParts[1]);
    int reg = Integer.parseInt(tokens.get(1));
    String memAddr = getSymbolAddress(tokens.get(2));
    objCode = String.format("%02d %d %s", opcode, reg, memAddr);
}

2️⃣ Theoretical Questions & Answers
Q1: What is Pass II of a two-pass assembler?

Answer:

Pass II reads the intermediate code generated by Pass I and uses the symbol table and literal table to generate object code.

It resolves addresses of symbols and literals.

Q2: Why do we need a two-pass assembler?

Answer:

Because in one-pass, forward references (symbols used before defined) cannot be resolved.

Pass I: builds symbol table and literal table.

Pass II: generates final object code using these tables.

Q3: What are the components of an intermediate code line?

Answer:

(IS,xx) → Imperative Statement (machine instruction)

(DL,xx) → Declarative Statement (like DC, DS)

(AD,xx) → Assembler Directive (START, END, ORIGIN)

(S,xx) → Symbol Table reference

(L,xx) → Literal Table reference

Q4: How is a symbol address accessed in Pass II?

Answer:

The intermediate code has (S,index).

Pass II uses the index to look up the symbol table and fetch the memory address.

Q5: What is the format of object code?

Answer:

<Opcode> <Register> <Memory Address>

Example: 04 1 100 → Opcode 04, Register 1, Address 100.

3️⃣ Practical Questions & Answers (Viva)
Q1: How do you run this Pass II assembler?

Answer:

Compile:

javac A2/Assembler_PassTwo.java


Run:

java A2.Assembler_PassTwo


Make sure symtable.txt and output_pass1.txt are in the same folder A2.

Q2: What is the output of Pass II for the sample input?

Answer:

(AD,01)(C,100) -> ----
(IS,04)(1)(S,1) -> 04 1 100
(IS,01)(2)(S,2) -> 01 2 101
(IS,02)(1)(S,3) -> 02 1 102
(AD,02) -> ----

Q3: How does the program handle AD (Assembler Directive)?

Answer:

It does not generate any object code.

Printed as ----.

Q4: How is a forward reference handled?

Answer:

Symbols in (S,index) are already stored in the symbol table by Pass I.

Pass II fetches the memory address using the index, so forward references are resolved.

Q5: What changes if a literal table is used?

Answer:

(L,index) → Pass II fetches literal address from the literal table instead of symbol table.

Object code is generated the same way as symbols.

1️⃣ Difference between Pass I and Pass II
Feature	Pass I	Pass II
Objective	Analyze source code, build tables	Generate object code
Handles forward refs	Cannot resolve forward references	Resolves addresses using tables
Outputs	Symbol table, literal table, intermediate code	Final object code
Processing	Reads source statements line by line	Reads intermediate code line by line
Directives	Handles START, ORIGIN, DS, DC etc.	Ignores AD (assembler directives) in object code

Answer for viva:

“Pass I scans the source program to create the symbol table, literal table, and intermediate code. Pass II reads this intermediate code and tables to generate actual object code. Forward references are resolved in Pass II.”

2️⃣ Symbol/Literal Tables and why two passes are needed

Symbol Table

Contains all identifiers (labels, variables) and their memory addresses.

Example:

Symbol Address
A      100
B      101
C      102


Literal Table

Stores constants or literals used in the program with assigned memory addresses.

Example:

Literal Address
=5      110
=10     111


Why Two Passes?

A single pass cannot resolve forward references, i.e., symbols/literals used before declaration.

Pass I: collects all symbols and literals with placeholders.

Pass II: replaces placeholders with actual memory addresses to produce object code.

Answer for viva:

“Two passes are needed because a symbol or literal may be used before it is defined. Pass I creates tables and intermediate code, Pass II uses these tables to generate object code.”

3️⃣ Sample Input and Object Code Generation

Intermediate Code:

(AD,01)(C,100)
(IS,04)(1)(S,1)
(IS,01)(2)(S,2)
(IS,02)(1)(S,3)
(AD,02)


Symbol Table:

Symbol Address
A      100
B      101
C      102


Generated Object Code (Pass II Output):

(AD,01)(C,100) -> ----
(IS,04)(1)(S,1) -> 04 1 100
(IS,01)(2)(S,2) -> 01 2 101
(IS,02)(1)(S,3) -> 02 1 102
(AD,02)        -> ----


Explanation:

(AD,01) → Assembler Directive → No object code → ----

(IS,04)(1)(S,1) → Opcode 04, Register 1, Symbol S1 → Address 100 → 04 1 100

(IS,01)(2)(S,2) → Opcode 01, Register 2, Symbol S2 → Address 101 → 01 2 101

4️⃣ Object Code Format

Format:

<Opcode> <Register> <Memory Address>


Example:

04 1 100


04 → Machine Opcode

1 → Register number (AREG=1, BREG=2, etc.)

100 → Memory address of symbol

5️⃣ Drawing Memory Allocation, Symbol Table, Intermediate Code

Memory Allocation Example:

Address	Content
100	Value of A
101	Value of B
102	Value of C

Symbol Table:

Symbol Address
A      100
B      101
C      102


Intermediate Code:

(IS,04)(1)(S,1)
(IS,01)(2)(S,2)
(IS,02)(1)(S,3)


Object Code:

04 1 100
01 2 101
02 1 102


Answer for viva:

“In memory, each symbol is stored at a specific address as per the symbol table. The intermediate code references symbols by index. Pass II resolves these indices to addresses and produces the final object code in the format Opcode Register Address.”
*/






/*
Chit No 3 – Macro Processor Pass I
Problem Statement: Design suitable data structures and implement Pass I of a macro processor to
generate MNT, MDT, and Intermediate Code.
Sample Input:
MACRO
INCR &A
ADD &A, =1
MEND
START
INCR DATA
END
*/

package A3;

import java.io.*;
import java.util.*;

public class MacroProcessor_PassOne {

    static List<String> MDT = new ArrayList<>();              // Macro Definition Table
    static Map<String, String> MNT = new LinkedHashMap<>();  // Macro Name Table
    static Map<String, String> ALA = new HashMap<>();        // Argument List Array
    static int mntPtr = 0, mdtPtr = 0;

    public static void main(String[] args) {
        try {
            pass1();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void pass1() throws Exception {
        BufferedReader input = new BufferedReader(new FileReader("A3/input.txt"));
        PrintWriter out_pass1 = new PrintWriter(new FileWriter("A3/output_pass1.txt"), true);
        PrintWriter out_mnt = new PrintWriter(new FileWriter("A3/MNT.txt"), true);
        PrintWriter out_mdt = new PrintWriter(new FileWriter("A3/MDT.txt"), true);

        String s;
        boolean processingMacroDefinition = false;
        boolean processMacroName = false;

        System.out.println("======================== Pass 1 Output ============================");

        while ((s = input.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0) continue;

            String[] s_arr = tokenizeString(s, " ");
            String curToken = s_arr[0];

            // Start of macro
            if (curToken.equalsIgnoreCase("MACRO")) {
                processingMacroDefinition = true;
                processMacroName = true;
                continue;
            }

            // Inside Macro Definition
            if (processingMacroDefinition) {

                // End of macro
                if (curToken.equalsIgnoreCase("MEND")) {
                    MDT.add(mdtPtr++, s);  // Add MEND line
                    processingMacroDefinition = false;
                    continue;
                }

                // Macro name line
                if (processMacroName) {
                    String macroName = s_arr[0];
                    String argList = s.substring(macroName.length()).trim();

                    // Add to Macro Name Table
                    MNT.put(macroName, String.valueOf(mdtPtr));
                    mntPtr++;

                    // Process arguments
                    processArgumentList(argList);

                    // Add macro definition first line to MDT
                    MDT.add(mdtPtr++, macroName + " " + argList);

                    processMacroName = false;
                    continue;
                }

                // Macro body
                String indexedLine = processArguments(s);
                MDT.add(mdtPtr++, indexedLine);

            } else {
                // Non-macro line, just output as intermediate code
                out_pass1.println(s);
                System.out.println(s);
            }
        }

        input.close();

        // Print and save MNT
        System.out.println("\n======================== MNT ============================");
        for (Map.Entry<String, String> entry : MNT.entrySet()) {
            String row = entry.getKey() + " " + entry.getValue();
            System.out.println(row);
            out_mnt.println(row);
        }

        // Print and save MDT
        System.out.println("\n======================== MDT ============================");
        for (int i = 0; i < MDT.size(); i++) {
            String row = i + " " + MDT.get(i);
            System.out.println(row);
            out_mdt.println(row);
        }

        out_pass1.close();
        out_mnt.close();
        out_mdt.close();
    }

    // Process macro argument list and store in ALA
    static void processArgumentList(String argList) {
        StringTokenizer st = new StringTokenizer(argList, ",", false);
        ALA.clear();
        int index = 1;
        while (st.hasMoreTokens()) {
            String curArg = st.nextToken().trim();
            if (curArg.contains("=")) {
                curArg = curArg.substring(0, curArg.indexOf("="));
            }
            ALA.put(curArg, "#" + index);  // Replace &ARG with #1, #2, etc.
            index++;
        }
    }

    // Replace macro arguments in macro body with positional parameters
    static String processArguments(String line) {
        for (Map.Entry<String, String> entry : ALA.entrySet()) {
            line = line.replace(entry.getKey(), entry.getValue());
        }
        return line;
    }

    // Tokenize string by separator
    static String[] tokenizeString(String str, String separator) {
        StringTokenizer st = new StringTokenizer(str, separator, false);
        String[] s_arr = new String[st.countTokens()];
        for (int i = 0; i < s_arr.length; i++) {
            s_arr[i] = st.nextToken();
        }
        return s_arr;
    }
}


/*
create input.txt file
		MACRO
		INCR &ARG1,&ARG2
		ADD &ARG1,&ARG2
		MEND
		MACRO
		DECR &ARG1,&ARG2,&REG=AREG
		SUB &ARG1,&ARG2,&REG
		MEND
		START 100
		READ N1
		READ N2
		INCR N1,N2
		DECR N1,N2,REG=CREG
		STOP
		N1 DS 1
		N2 DS 1
		END
*/


/*
create MDT.txt file (optional)
create MNT.txt file (optional)
create output_pass1.txt file (optional)
*/


/*
1️⃣ Code Explanation – Macro Processor Pass I

Objective:
Pass I of a macro processor scans the source program, identifies macro definitions, and generates:

MNT (Macro Name Table) → stores macro names and pointer to MDT

MDT (Macro Definition Table) → stores macro body lines with positional parameters

Intermediate Code → source program without macro definitions

How it works:

Read each line of the source file.

Detect MACRO keyword:

Begin processing a macro definition.

Next line is the macro name + argument list → add to MNT and first line of MDT.

Process macro body:

Replace formal arguments (&ARG) with positional parameters (#1, #2…).

Add each line to MDT.

Detect MEND:

Marks the end of the macro → add to MDT.

Non-macro lines:

Write directly to intermediate code (output_pass1.txt).

Example:

Input Macro:

MACRO
INCR &ARG1,&ARG2
ADD &ARG1,&ARG2
MEND


Pass I Output:

MNT: INCR 0 → macro starts at MDT line 0

MDT:

0 INCR &ARG1,&ARG2
1 ADD #1,#2
2 MEND


Intermediate Code: only non-macro lines remain, macro calls are left as-is.

2️⃣ Theoretical Questions & Answers

Q1. What is a Macro Processor?

“A Macro Processor is a program that processes macros—reusable code blocks—in a source program. It replaces macro calls with the corresponding code before actual assembly or compilation.”

Q2. What are Pass I and Pass II in macro processing?

Pass I: Build tables (MNT, MDT), remove macro definitions, produce intermediate code.

Pass II: Expand macro calls in the intermediate code using the tables.

Q3. What is MNT (Macro Name Table)?

“MNT stores macro names along with a pointer to the first line of its definition in MDT.”

Q4. What is MDT (Macro Definition Table)?

“MDT stores macro body lines with positional parameters (#1, #2…) replacing formal arguments (&ARG1, &ARG2).”

Q5. What is the purpose of ALA (Argument List Array)?

“ALA maps formal arguments of a macro to positional parameters for easier substitution during macro expansion.”

Q6. Why do we need positional parameters (#1, #2…)?

“They help in replacing macro arguments consistently during macro expansion in Pass II.”

Q7. What happens to non-macro lines in Pass I?

“Non-macro lines are written to intermediate code as-is for further processing in Pass II.”

Q8. Give an example of macro expansion (conceptually).

Macro call: INCR N1,N2

MDT:

ADD #1,#2


Expansion: ADD N1,N2

3️⃣ Practical Questions & Answers

Q1. What is the input file for your program?

input.txt contains source program with MACRO definitions and calls.

Q2. What are the output files of Pass I?

output_pass1.txt → intermediate code without macro definitions

MNT.txt → Macro Name Table

MDT.txt → Macro Definition Table

Q3. How does your code handle macro arguments?

Formal arguments in macro body (&ARG1) are replaced with positional parameters (#1, #2) using ALA.

Q4. How does your code detect the end of a macro?

When the line MEND is encountered, it is added to MDT and macro processing ends.

Q5. How do you identify a macro call in intermediate code?

Macro calls remain in the intermediate code; Pass II will expand them using MNT and MDT.

Q6. Explain the line MDT.add(mdtPtr++, indexedLine);

Adds the current macro line (with positional parameters) to MDT and increments the pointer.

Q7. What will happen if the source program has multiple macros?

Each macro is processed separately. MNT stores multiple macro names, each pointing to its start in MDT.

Q8. How does your code handle default argument values like &REG=AREG?

The default is stripped during argument indexing in Pass I; actual substitution happens in Pass II during macro expansion.

1️⃣ Show Intermediate Files

a) MNT.txt (Macro Name Table)
Shows all macros with the pointer to their first line in MDT. Example:

INCR 0
DECR 3


b) MDT.txt (Macro Definition Table)
Contains macro body with positional parameters:

0 INCR &ARG1,&ARG2
1 ADD #1,#2
2 MEND
3 DECR &ARG1,&ARG2,&REG=AREG
4 SUB #1,#2,#3
5 MEND


c) output_pass1.txt (Intermediate Code)
Source code without macro definitions:

START 100
READ N1
READ N2
INCR N1,N2
DECR N1,N2,REG=CREG
STOP
N1 DS 1
N2 DS 1
END


Tip for viva: Open these files and point to the macro definitions removed, and macro calls remaining in intermediate code.

2️⃣ Explain Positional Parameters Clearly

Macro arguments like &ARG1, &ARG2 are replaced by #1, #2 in MDT.

Purpose: Makes it easier to substitute actual arguments during Pass II expansion.

Example:

Macro body:

ADD &ARG1, &ARG2


After Pass I (in MDT):

ADD #1, #2


During expansion (Pass II) for macro call INCR N1, N2:

ADD #1, #2 → ADD N1, N2


Viva answer: “Positional parameters allow the macro processor to replace formal arguments with actual values consistently during expansion.”

3️⃣ Pass I vs Pass II Difference
Feature	Pass I	Pass II
Objective	Create tables (MNT, MDT)	Expand macros using tables
Handles forward refs	Not needed	Resolves macro calls to actual code
Output	Intermediate code without macro definitions	Fully expanded source code
Processing	Reads source program	Reads intermediate code
Tables Used	MNT, MDT, ALA	MNT, MDT, ALA

Viva answer: “Pass I collects macro definitions and generates tables. Pass II replaces macro calls in intermediate code with actual instructions using these tables.”

4️⃣ Draw Tables and Explain Macro Expansion Step by Step

Example Macro Call:

INCR N1,N2


Step 1 – Find Macro in MNT:

INCR → starting at MDT line 0

Step 2 – Replace Positional Parameters using ALA:

MDT line 1: ADD #1,#2

#1 → N1, #2 → N2

Expanded line: ADD N1,N2

Step 3 – Replace in Intermediate Code:

Original: INCR N1,N2

Expanded: ADD N1,N2

Step 4 – Repeat for other macro calls
*/







/*
Chit No 4 – Macro Processor Pass II
Problem Statement: Implement Pass II of a macro processor to perform macro expansion using MNT,
MDT and Intermediate Code.
MN T:
Index Macro Name
1 INCR
MD T:
Index Definition
1 ADD &A, =1
2 MEND
Intermediate Code:
START
INCR DATA
END

*/

package A4;

import java.io.*;
import java.util.*;

public class MacroProcessor_PassTwo {

    static List<String> MDT;
    static Map<String, String> MNT;
    static List<String> formalParams, actualParams;

    public static void main(String[] args) {
        try {
            initializeTables();
            pass2();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Pass II – Macro Expansion
    static void pass2() throws Exception {
        BufferedReader input = new BufferedReader(new InputStreamReader(
                new FileInputStream("A4/output_input_pass1.txt")));
        PrintWriter out_pass2 = new PrintWriter(new FileWriter("A4/output_pass2.txt"), true);

        System.out.println("=========== Pass 2 Output ============");

        String s;
        while ((s = input.readLine()) != null) {
            String[] s_arr = tokenizeString(s, " ");
            if (s_arr.length == 0) continue;

            if (MNT.containsKey(s_arr[0])) {
                // Macro call detected
                String[] actParams = tokenizeString(s_arr[1], ",");
                actualParams.clear();
                for (String param : actParams) {
                    if (param.contains("=")) {
                        param = param.substring(param.indexOf("=") + 1);
                    }
                    actualParams.add(param);
                }

                int mdtPtr = Integer.parseInt(MNT.get(s_arr[0]));
                boolean firstLine = true;

                while (true) {
                    String macroLine = MDT.get(mdtPtr);
                    String[] tokens = tokenizeString(macroLine, " ");

                    if (firstLine) {
                        createFormalParamList(macroLine);
                        firstLine = false;
                    } else {
                        if (tokens[0].equalsIgnoreCase("MEND")) {
                            break;
                        }
                        // Replace positional parameters (#1, #2) with actual parameters
                        String paramStr = replaceFormalParams(tokens[1]);
                        String printStr = "\t" + tokens[0] + " " + paramStr;
                        System.out.println(printStr);
                        out_pass2.println(printStr);
                    }
                    mdtPtr++;
                }

            } else {
                // Normal assembly line
                System.out.println(s);
                out_pass2.println(s);
            }
        }

        input.close();
        out_pass2.close();
    }

    static String replaceFormalParams(String formalParamList) {
        String returnStr = "";
        formalParamList = formalParamList.replace("#", "");
        String[] paramArray = tokenizeString(formalParamList, ",");
        for (int i = 0; i < paramArray.length; i++) {
            String actualParam;
            try {
                int index = Integer.parseInt(paramArray[i]);
                actualParam = actualParams.get(index - 1);
            } catch (NumberFormatException e) {
                actualParam = paramArray[i];
            }
            returnStr += actualParam + ",";
        }
        if (returnStr.endsWith(",")) {
            returnStr = returnStr.substring(0, returnStr.length() - 1);
        }
        return returnStr;
    }

    static void createFormalParamList(String macroDef) {
        formalParams.clear();
        String[] tokens = tokenizeString(macroDef, " ");
        String argList = tokens[1];
        String[] argArray = tokenizeString(argList, ",");
        for (String param : argArray) {
            if (param.contains("=")) {
                param = param.substring(param.indexOf("=") + 1);
            }
            formalParams.add(param);
        }
    }

    static void initializeTables() throws Exception {
        MDT = new ArrayList<>();
        MNT = new LinkedHashMap<>();
        formalParams = new ArrayList<>();
        actualParams = new ArrayList<>();

        // Read MNT
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("A4/MNT.txt")));
        String s;
        while ((s = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s, " ");
            MNT.put(st.nextToken(), st.nextToken());
        }
        br.close();

        // Read MDT
        br = new BufferedReader(new InputStreamReader(
                new FileInputStream("A4/MDT.txt")));
        while ((s = br.readLine()) != null) {
            String[] tokens = tokenizeString(s, " ");
            if (tokens.length == 2) {
                MDT.add(tokens[1]);
            } else if (tokens.length == 3) {
                MDT.add(tokens[1] + " " + tokens[2]);
            }
        }
        br.close();
    }

    static String[] tokenizeString(String str, String separator) {
        StringTokenizer st = new StringTokenizer(str, separator);
        String[] tokens = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            tokens[i++] = st.nextToken();
        }
        return tokens;
    }
}



/*
create output_input_pass1.txt file
		START 100
		READ N1
		READ N2
		MAC1 N1,N2
		MAC2 N1,N2
		STOP
		N1 DS 1
		N2 DS 1
		END

create MNT.txt file 
		MAC1 0
		MAC2 4

create MDT.txt file 
		0 MAC1 #1,#2
		1 +MOVER AREG,#1
		2 +ADD AREG,#1
		3 MEND
		4 MAC2 #1,#2
		5 +MOVER CREG,#1
		6 +SUB CREG,#2
		7 MEND
*/

/*
create output_pass2.txt file (optional)
*/

/*
1️⃣ Code Explanation – Macro Processor Pass II

Objective: Expand macro calls in intermediate code using MNT and MDT.

Key Concepts:

MNT (Macro Name Table): Stores macro name → starting index in MDT.

MDT (Macro Definition Table): Stores macro instructions with positional parameters (#1, #2).

Intermediate Code: Code after Pass I; may contain macro calls.

Workflow of Pass II:

Initialize Tables:

Read MNT.txt into MNT map.

Read MDT.txt into MDT list.

Read Intermediate Code:

Open output_input_pass1.txt.

Process each line:

Check if first token exists in MNT:

If yes → Macro call:

Extract actual parameters.

Get macro starting index from MDT.

Read MDT lines until MEND.

Replace formal parameters (#1, #2) with actual parameters.

Write expanded instructions to output.

If no → Normal assembly instruction:

Write directly to output file.

Parameter Replacement:

#1, #2 in MDT are replaced by actual parameters from macro call.

Output File:

output_pass2.txt contains fully expanded assembly code.

Example:

Input Intermediate Code:

START 100
READ N1
READ N2
MAC1 N1,N2
MAC2 N1,N2
STOP


MDT:

0 MAC1 #1,#2
1 +MOVER AREG,#1
2 +ADD AREG,#1
3 MEND
4 MAC2 #1,#2
5 +MOVER CREG,#1
6 +SUB CREG,#2
7 MEND


Output after Pass II:

START 100
READ N1
READ N2
    +MOVER AREG,N1
    +ADD AREG,N1
    +MOVER CREG,N1
    +SUB CREG,N2
STOP

2️⃣ Theoretical Questions & Answers

Q1: What is a macro in assembly language?
A1: A macro is a sequence of instructions that can be called by a name. It allows reusing code without writing it multiple times.

Q2: What is the purpose of Macro Processor Pass II?
A2: Pass II expands macro calls in the intermediate code using MNT and MDT, replacing formal parameters with actual parameters.

Q3: What are MNT and MDT?
A3:

MNT (Macro Name Table): Stores macro name and starting index in MDT.

MDT (Macro Definition Table): Stores macro instructions with formal parameters (#1, #2).

Q4: Difference between Pass I and Pass II of macro processor?

Feature	Pass I	Pass II
Purpose	Identify macros, create MNT and MDT	Expand macro calls in intermediate code
Input	Source code with macro definitions	Intermediate code from Pass I
Output	MNT, MDT, ALA (Argument List Array)	Fully expanded code
Parameter Handling	Converts formal parameters to positional (#1, #2)	Replaces positional parameters with actual parameters

Q5: What is the significance of #1, #2 in MDT?
A5: They are positional placeholders representing actual parameters passed during macro call.

3️⃣ Practical Questions & Answers (Viva)

Q1: How do you detect a macro call in Pass II?
A1: By checking if the first token of a line exists in the MNT.

Q2: How are actual parameters mapped to formal parameters?
A2: Formal parameters in MDT (#1, #2) are replaced by actual parameters from the macro call.

Q3: How do you handle default parameter values?
A3: If a macro call has a parameter with = (e.g., REG=AREG), the program extracts the value after = and uses it during expansion.

Q4: What is the input and output of Pass II?
A4:

Input: output_input_pass1.txt, MNT.txt, MDT.txt

Output: output_pass2.txt (fully expanded assembly code)

Q5: Explain the output with an example.
A5:
Macro call: MAC1 N1,N2
MDT lines:

+MOVER AREG,#1
+ADD AREG,#1


Output after expansion:

+MOVER AREG,N1
+ADD AREG,N1
*/









/*
Chit No 5 – CPU Scheduling (FCFS and Priority)
Problem Statement: Write a program to simulate CPU Scheduling algorithms – FCFS and Priority (NonPreemptive). Calculate the Waiting Time, Turnaround Time, Average Waiting Time, and Average
Turnaround Time for all processes.
Sample Input:
Process Arrival Time Burst Time Priority
P1 				0 					5 					2
P2 				1 				    3 				    1
P3 				2  				8  				4
P4 				3 				    6 				    3
*/
import java.util.*;

class Process {
    int pid; // Process ID
    int arrivalTime, burstTime, priority;
    int completionTime, turnaroundTime, waitingTime;

    Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "P" + pid + "\t\t" + arrivalTime + "\t\t" + burstTime + "\t\t" + priority + "\t\t" +
                completionTime + "\t\t" + turnaroundTime + "\t\t" + waitingTime;
    }
}

abstract class SchedulingAlgorithm {
    protected Process[] processes;
    protected int n;

    public SchedulingAlgorithm(Process[] processes) {
        this.processes = processes;
        this.n = processes.length;
    }

    public abstract void schedule();

    protected void printResults() {
        System.out.println("\nProcess\tAT\tBT\tPR\tCT\tTAT\tWT");
        System.out.println("-------------------------------------------------------------");
        for (Process p : processes) {
            System.out.println(p);
        }
    }

    protected void calculateAverages() {
        double avgTAT = 0, avgWT = 0;
        for (Process p : processes) {
            avgTAT += p.turnaroundTime;
            avgWT += p.waitingTime;
        }
        System.out.println("\nAverage Turnaround Time: " + (avgTAT / n));
        System.out.println("Average Waiting Time: " + (avgWT / n));
    }
}

// ---------- FCFS Scheduling ----------
class FCFS extends SchedulingAlgorithm {
    public FCFS(Process[] processes) {
        super(processes);
    }

    @Override
    public void schedule() {
        Arrays.sort(processes, Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        for (Process p : processes) {
            if (currentTime < p.arrivalTime)
                currentTime = p.arrivalTime;

            p.completionTime = currentTime + p.burstTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;

            currentTime = p.completionTime;
        }

        printResults();
        calculateAverages();
    }
}

// ---------- Priority Scheduling (Non-Preemptive) ----------
class PriorityScheduling extends SchedulingAlgorithm {
    public PriorityScheduling(Process[] processes) {
        super(processes);
    }

    @Override
    public void schedule() {
        Arrays.sort(processes, Comparator.comparingInt(p -> p.arrivalTime));

        boolean[] completed = new boolean[n];
        int completedCount = 0;
        int currentTime = 0;

        while (completedCount < n) {
            int idx = -1;
            int highestPriority = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (!completed[i] && processes[i].arrivalTime <= currentTime) {
                    if (processes[i].priority < highestPriority) {
                        highestPriority = processes[i].priority;
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                currentTime++;
            } else {
                Process p = processes[idx];
                p.completionTime = currentTime + p.burstTime;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;

                currentTime = p.completionTime;
                completed[idx] = true;
                completedCount++;
            }
        }

        printResults();
        calculateAverages();
    }
}

// ---------- Main Class ----------
public class CPUScheduling {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        Process[] processes = new Process[n];
        for (int i = 0; i < n; i++) {
            System.out.println("\nEnter details for Process P" + (i + 1) + ":");
            System.out.print("Arrival Time: ");
            int at = sc.nextInt();
            System.out.print("Burst Time: ");
            int bt = sc.nextInt();
            System.out.print("Priority (lower = higher priority): ");
            int pr = sc.nextInt();

            processes[i] = new Process(i + 1, at, bt, pr);
        }

        System.out.println("\nSelect Scheduling Algorithm:");
        System.out.println("1. First Come First Serve (FCFS)");
        System.out.println("2. Priority Scheduling (Non-Preemptive)");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();

        SchedulingAlgorithm scheduler;
        if (choice == 1)
            scheduler = new FCFS(processes);
        else if (choice == 2)
            scheduler = new PriorityScheduling(processes);
        else {
            System.out.println("Invalid choice!");
            return;
        }

        scheduler.schedule();
        sc.close();
    }
}


/*
1. Code Explanation
Classes and Structure

Process Class

Represents a single process.

Attributes:

pid: Process ID

arrivalTime: Time at which process arrives

burstTime: CPU execution time

priority: Process priority (lower number = higher priority)

completionTime, turnaroundTime, waitingTime: Calculated after scheduling

toString(): Formats process details for display.

SchedulingAlgorithm (Abstract Class)

Base class for scheduling algorithms.

Holds processes[] and n (number of processes)

Abstract method schedule(): Must be implemented by subclasses.

Helper methods:

printResults(): Prints process table with all times.

calculateAverages(): Computes average TAT & WT.

FCFS (First Come First Serve)

Extends SchedulingAlgorithm

Logic:

Sort processes by arrivalTime.

For each process:

If CPU is idle, advance currentTime to process arrival.

completionTime = currentTime + burstTime

turnaroundTime = completionTime - arrivalTime

waitingTime = turnaroundTime - burstTime

Update currentTime = completionTime

Non-preemptive scheduling (process runs fully once started).

Priority Scheduling (Non-Preemptive)

Extends SchedulingAlgorithm

Logic:

Sort processes by arrivalTime.

Maintain completed[] array to track finished processes.

Loop until all processes are completed:

Pick process with highest priority (lowest number) among arrived ones.

If no process has arrived, increment currentTime.

Calculate CT, TAT, WT like in FCFS.

Non-preemptive: a running process cannot be interrupted.

Main Class

Takes input for n processes, arrival, burst, and priority.

User selects FCFS or Priority Scheduling.

Calls schedule() method of chosen algorithm.

Sample Output Table
Process	AT	BT	PR	CT	TAT	WT
P1	0	5	2	5	5	0
P2	1	3	1	8	7	4
P3	2	8	4	16	14	6
P4	3	6	3	22	19	13

Average TAT = 11.25, Average WT = 5.75

2. Theoretical Questions & Answers

Q1. What is CPU Scheduling?
A: CPU Scheduling decides the order in which processes in the ready queue are executed by the CPU to maximize efficiency and minimize waiting time.

Q2. What are types of CPU Scheduling?
A:

FCFS (First Come First Serve): Executes processes in order of arrival. Non-preemptive.

SJF (Shortest Job First): Picks process with shortest burst time. Can be preemptive or non-preemptive.

Priority Scheduling: Executes process with highest priority (lower number = higher priority). Non-preemptive or preemptive.

Round Robin (RR): Each process gets fixed CPU time (time quantum). Preemptive.

Q3. Define Turnaround Time (TAT) and Waiting Time (WT).

TAT = Completion Time - Arrival Time

WT = TAT - Burst Time

Q4. Difference between FCFS and Priority Scheduling?

Feature	FCFS	Priority Scheduling
Order of execution	Arrival time	Priority
Preemption	No	Optional (here Non-preemptive)
Starvation	Rare	Possible for low-priority processes
Simplicity	Easy	Slightly complex

Q5. Why sort processes in FCFS by arrival time?
A: To ensure processes are executed in the order they arrive at the CPU, which is the core logic of FCFS.

Q6. How do you handle idle CPU in Priority Scheduling?
A: Increment currentTime until the next process arrives.

3. Practical Questions & Answers (Viva)

Q1. What is the purpose of Arrays.sort() in the code?
A: It sorts processes based on arrival time so that scheduling can start in correct order.

Q2. How is waiting time calculated?
A: WT = TAT - BT. It represents the time a process spends in the ready queue.

Q3. Can Priority Scheduling cause starvation?
A: Yes, if lower-priority processes keep waiting while higher-priority processes keep arriving.

Q4. What happens if two processes have same priority in Priority Scheduling?
A: The process which arrived earlier will be executed first (FIFO tie-breaking).

Q5. Why do we need completed[] array in Priority Scheduling?
A: To track which processes are already executed, so the scheduler picks only pending processes.

Q6. How can you modify this code for Preemptive Priority Scheduling?
A: Continuously check the ready queue at each time unit; if a new process with higher priority arrives, preempt the running process.

Q7. How are averages calculated?
A: Sum all TATs and WTs and divide by number of processes:
Average TAT = ΣTAT / n, Average WT = ΣWT / n
*/










/*
Chit No 6 – CPU Scheduling (SJF Preemptive and Round Robin)
Problem Statement: Write a program to simulate CPU Scheduling algorithms – Shortest Job First (SJF
Preemptive) and Round Robin (Preemptive).
Calculate the Waiting Time, Turnaround Time, Average Waiting Time, and Average Turnaround Time for
all processes.
Sample Input:
Process Arrival Time Burst Time
P1 0 7
P2 2 4
P3 4 1
P4 5 4
*/

import java.util.*;

class Process {
    int pid;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int completionTime;
    int turnaroundTime;
    int waitingTime;

    Process(int pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}

public class CPUSchedulingAdvanced {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        Process[] processes = new Process[n];
        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess P" + (i + 1) + " details:");
            System.out.print("Arrival Time: ");
            int at = sc.nextInt();
            System.out.print("Burst Time: ");
            int bt = sc.nextInt();
            processes[i] = new Process(i + 1, at, bt);
        }

        System.out.println("\nSelect Scheduling Algorithm:");
        System.out.println("1. Shortest Job First (Preemptive)");
        System.out.println("2. Round Robin (Preemptive)");
        int choice = sc.nextInt();

        if (choice == 1) {
            sjfPreemptive(processes);
        } else if (choice == 2) {
            System.out.print("Enter Time Quantum: ");
            int tq = sc.nextInt();
            roundRobin(processes, tq);
        } else {
            System.out.println("Invalid choice!");
        }
    }

    // ------------------ SJF Preemptive ------------------
    static void sjfPreemptive(Process[] processes) {
        int n = processes.length;
        int completed = 0, currentTime = 0;
        int minRemaining;
        boolean anyProcess;
        
        while (completed != n) {
            minRemaining = Integer.MAX_VALUE;
            anyProcess = false;
            Process currentProcess = null;

            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0 && p.remainingTime < minRemaining) {
                    minRemaining = p.remainingTime;
                    currentProcess = p;
                    anyProcess = true;
                }
            }

            if (!anyProcess) {
                currentTime++;
                continue;
            }

            currentProcess.remainingTime--;
            currentTime++;

            if (currentProcess.remainingTime == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                completed++;
            }
        }

        printResults(processes, "SJF Preemptive");
    }

    // ------------------ Round Robin ------------------
    static void roundRobin(Process[] processes, int tq) {
        int n = processes.length;
        int completed = 0, currentTime = 0;
        Queue<Process> queue = new LinkedList<>();
        boolean[] inQueue = new boolean[n];

        while (completed != n) {
            // Add processes that have arrived
            for (int i = 0; i < n; i++) {
                if (!inQueue[i] && processes[i].arrivalTime <= currentTime && processes[i].remainingTime > 0) {
                    queue.add(processes[i]);
                    inQueue[i] = true;
                }
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process p = queue.poll();
            int execTime = Math.min(tq, p.remainingTime);
            p.remainingTime -= execTime;
            currentTime += execTime;

            // Check for newly arrived processes during execution
            for (int i = 0; i < n; i++) {
                if (!inQueue[i] && processes[i].arrivalTime <= currentTime && processes[i].remainingTime > 0) {
                    queue.add(processes[i]);
                    inQueue[i] = true;
                }
            }

            if (p.remainingTime > 0) {
                queue.add(p); // re-add to queue if not finished
            } else {
                p.completionTime = currentTime;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
                completed++;
            }
        }

        printResults(processes, "Round Robin (TQ=" + tq + ")");
    }

    // ------------------ Print Results ------------------
    static void printResults(Process[] processes, String algo) {
        System.out.println("\n---------- " + algo + " Scheduling ----------");
        System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");
        double totalTAT = 0, totalWT = 0;
        for (Process p : processes) {
            System.out.println("P" + p.pid + "\t" + p.arrivalTime + "\t" + p.burstTime + "\t" +
                    p.completionTime + "\t" + p.turnaroundTime + "\t" + p.waitingTime);
            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
        }
        System.out.printf("Average Turnaround Time: %.2f\n", totalTAT / processes.length);
        System.out.printf("Average Waiting Time: %.2f\n", totalWT / processes.length);
    }
}


/*
1. Code Explanation

Your Java program has these main components:

a) Process Class
class Process {
    int pid;
    int arrivalTime, burstTime, remainingTime;
    int completionTime, turnaroundTime, waitingTime;

    Process(int pid, int at, int bt) {
        this.pid = pid;
        this.arrivalTime = at;
        this.burstTime = bt;
        this.remainingTime = bt; // for preemptive algorithms
    }
}


pid → Process ID

arrivalTime → When process enters ready queue

burstTime → CPU time needed

remainingTime → For preemptive scheduling

completionTime (CT) → When process finishes

turnaroundTime (TAT) = CT – AT

waitingTime (WT) = TAT – BT

b) Main Program Flow

Input phase

Number of processes

Arrival time and burst time for each process

Choice of scheduling algorithm (SJF Preemptive or Round Robin)

Time quantum (for Round Robin)

SJF Preemptive Scheduler

At each time unit, select the process with shortest remaining time among arrived processes.

Update CT, TAT, WT when process finishes.

Repeat until all processes finish.

Round Robin Scheduler

Maintain a ready queue.

Execute each process for time quantum or until it finishes.

If not finished, add back to queue.

Repeat until queue is empty.

Compute CT, TAT, WT for each process.

Output Phase

Display table with PID, AT, BT, CT, TAT, WT

Compute Average TAT and Average WT.

c) Key Calculations

Turnaround Time (TAT) = Completion Time – Arrival Time

Waiting Time (WT) = Turnaround Time – Burst Time

Average TAT / WT = Sum of TAT / WT ÷ Number of Processes

2. Theoretical Questions & Answers
Q	A
What is CPU Scheduling?	It's the process of selecting which process in the ready queue should execute on the CPU.
Difference between Preemptive & Non-Preemptive?	Preemptive: a process can be interrupted before completion. Non-Preemptive: runs until completion.
What is SJF Preemptive?	Shortest Job First with preemption: always executes process with shortest remaining burst time; may interrupt running process.
What is Round Robin Scheduling?	Preemptive scheduling with a fixed time quantum, cycling through ready queue.
Advantages of SJF?	Minimizes average waiting time.
Advantages of Round Robin?	Fair CPU allocation; good for time-sharing systems.
How to calculate CT, TAT, WT?	CT: when process finishes; TAT = CT – AT; WT = TAT – BT.
When to prefer SJF over RR?	SJF for minimum waiting time; RR for fairness and time-sharing systems.
3. Practical Viva Questions & Answers
Q	A
Explain your program.	The program takes process info, simulates SJF Preemptive or Round Robin, calculates CT, TAT, WT, and averages.
How did you implement preemption in SJF?	At each time unit, we select the process with shortest remaining time; if a new shorter process arrives, current process is preempted.
How is Round Robin implemented?	Maintain a queue; execute each process for time quantum; unfinished processes go back to queue until completion.
Why do you use remainingTime in SJF?	To track how much burst time is left for preemptive scheduling.
How do you compute averages?	Sum of TAT / WT for all processes ÷ number of processes.
What happens if two processes have same remaining time?	Typically, choose the one that arrived first (FCFS tie-breaker).
4. Example Viva Demonstration

Input:

P1: AT=0, BT=7
P2: AT=2, BT=4
P3: AT=4, BT=1
P4: AT=5, BT=4
Time Quantum for RR = 2


SJF Output:

PID	AT	BT	CT	TAT	WT
P1	0	7	16	16	9
P2	2	4	7	5	1
P3	4	1	5	1	0
P4	5	4	11	6	2

Round Robin Output (TQ=2):

PID	AT	BT	CT	TAT	WT
P1	0	7	16	16	9
P2	2	4	9	7	3
P3	4	1	7	3	2
P4	5	4	15	10	6
*/








/*
Chit No 7 – Memory Placement Strategies (Best Fit, First Fit, Next Fit)
Problem Statement: Simulate Best Fit, First Fit, Next Fit allocation strategies. Output table should
show which process is allocated to which block and unused space.
Sample Input:
Memory blocks = [100, 500, 200, 300, 600]
Process sizes = [212, 417, 112, 426]
Expected output format: Show Separate table for Best Fit, First Fit, Next Fit
Process Process Size(KB) Block No Block Size(KB) Unused Space(KB)
P1
P2
P3
P4
*/

package A6;

import java.util.Arrays;

public class MemoryPlacement {

    static int[] blocks = {100, 500, 200, 300, 600};
    static int[] processSizes = {212, 417, 112, 426};
    static int nBlocks = blocks.length;
    static int nProcesses = processSizes.length;

    public static void main(String[] args) {
        System.out.println("========== MEMORY ALLOCATION STRATEGIES ==========\n");

        bestFit();
        firstFit();
        nextFit();
    }

    // ---------- Best Fit ----------
    static void bestFit() {
        System.out.println("---- BEST FIT ----");
        int[] blockSizes = Arrays.copyOf(blocks, nBlocks);
        int[] allocation = new int[nProcesses];
        Arrays.fill(allocation, -1);

        for (int i = 0; i < nProcesses; i++) {
            int bestIdx = -1;
            for (int j = 0; j < nBlocks; j++) {
                if (blockSizes[j] >= processSizes[i]) {
                    if (bestIdx == -1 || blockSizes[j] < blockSizes[bestIdx]) {
                        bestIdx = j;
                    }
                }
            }
            if (bestIdx != -1) {
                allocation[i] = bestIdx;
                blockSizes[bestIdx] -= processSizes[i];
            }
        }
        printAllocation(allocation, blocks);
    }

    // ---------- First Fit ----------
    static void firstFit() {
        System.out.println("\n---- FIRST FIT ----");
        int[] blockSizes = Arrays.copyOf(blocks, nBlocks);
        int[] allocation = new int[nProcesses];
        Arrays.fill(allocation, -1);

        for (int i = 0; i < nProcesses; i++) {
            for (int j = 0; j < nBlocks; j++) {
                if (blockSizes[j] >= processSizes[i]) {
                    allocation[i] = j;
                    blockSizes[j] -= processSizes[i];
                    break;
                }
            }
        }
        printAllocation(allocation, blocks);
    }

    // ---------- Next Fit ----------
    static void nextFit() {
        System.out.println("\n---- NEXT FIT ----");
        int[] blockSizes = Arrays.copyOf(blocks, nBlocks);
        int[] allocation = new int[nProcesses];
        Arrays.fill(allocation, -1);

        int lastAlloc = 0; // start searching from last allocated block

        for (int i = 0; i < nProcesses; i++) {
            int j = lastAlloc;
            boolean allocated = false;
            int count = 0;
            while (count < nBlocks) {
                if (blockSizes[j] >= processSizes[i]) {
                    allocation[i] = j;
                    blockSizes[j] -= processSizes[i];
                    lastAlloc = j; // next search will start from this block
                    allocated = true;
                    break;
                }
                j = (j + 1) % nBlocks;
                count++;
            }
        }
        printAllocation(allocation, blocks);
    }

    // ---------- Helper Method to Print Allocation Table ----------
    static void printAllocation(int[] allocation, int[] originalBlocks) {
        System.out.printf("%-8s %-12s %-10s %-15s %-15s\n",
                "Process", "Process Size", "Block No", "Block Size(KB)", "Unused Space(KB)");

        for (int i = 0; i < nProcesses; i++) {
            if (allocation[i] != -1) {
                int blockNo = allocation[i] + 1; // for 1-based index
                int unused = originalBlocks[allocation[i]] - (originalBlocks[allocation[i]] - allocation[i] >= 0 ? processSizes[i] : 0);
                System.out.printf("%-8s %-12d %-10d %-15d %-15d\n",
                        "P" + (i + 1), processSizes[i], blockNo, originalBlocks[allocation[i]], originalBlocks[allocation[i]] - (originalBlocks[allocation[i]] - processSizes[i]));
            } else {
                System.out.printf("%-8s %-12d %-10s %-15s %-15s\n",
                        "P" + (i + 1), processSizes[i], "Not Allocated", "-", "-");
            }
        }
    }
}


/*
Code Explanation

Purpose:
Simulate memory allocation strategies (Best Fit, First Fit, Next Fit) and display which memory block is assigned to which process along with unused space.

1. Inputs

Memory blocks: [100, 500, 200, 300, 600]

Process sizes: [212, 417, 112, 426]

2. Allocation Strategies Implemented

Best Fit

Choose the smallest block that can accommodate the process.

Reduces memory wastage but may take longer to search.

First Fit

Allocate the first block that is large enough.

Faster, but may leave more fragmentation.

Next Fit

Similar to First Fit, but search starts from the last allocated block.

Distributes memory allocation more evenly.

3. Algorithm Flow

Loop through each process.

For each process:

Check all memory blocks according to the strategy.

Allocate a block if it fits.

Update the block’s unused space.

Print a table showing:

Process ID

Process Size

Allocated Block Number

Block Size

Unused Space

4. Output Example (from your run)

---- BEST FIT ----
Process  Process Size Block No   Block Size(KB)  Unused Space(KB)
P1       212          4          300             212
P2       417          2          500             417
P3       112          3          200             112
P4       426          5          600             426


Shows which block each process got and remaining space.

Theoretical Questions & Answers (Viva)

Q1. What is memory allocation?
A: It is the process of assigning memory blocks to processes in an operating system to ensure efficient utilization of memory.

Q2. What are memory allocation strategies?
A: Methods to allocate processes to memory blocks: Best Fit, First Fit, Next Fit, Worst Fit, etc.

Q3. Difference between Best Fit, First Fit, and Next Fit?

Strategy	Method	Pros	Cons
Best Fit	Smallest block that fits	Minimizes wastage	Slow search
First Fit	First block that fits	Fast search	More fragmentation
Next Fit	Like First Fit, but starts from last allocated block	Faster than Best Fit	Can skip better blocks

Q4. What is external fragmentation?
A: Free memory is divided into small non-contiguous blocks, making it difficult to allocate large processes even if total free memory is enough.

Q5. How can you reduce external fragmentation?
A: Use compaction or efficient allocation strategies like Best Fit.

Practical Questions & Answers (Viva)

Q1. Write the memory block and process allocation for Best Fit for given inputs.
A:

P1 → Block 4

P2 → Block 2

P3 → Block 3

P4 → Block 5

Q2. What happens if a process cannot be allocated any block?
A: The process is marked as "Not Allocated".

Q3. Explain the difference in outputs between First Fit and Next Fit.
A:

First Fit always searches from the start of the memory block array.

Next Fit searches from the last allocated block onward.

Q4. If a new process of size 150 arrives, which allocation will place it in a block?

Check each strategy based on current block usage to determine allocation.

Q5. How do you calculate unused memory in a block?
A: Unused = Block Size - Process Size.
*/







/*
Chit No 8 – Memory Placement Strategies (First Fit, Next Fit, Worst Fit)
Problem Statement: Simulate First Fit, Next Fit, Worst Fit allocation strategies. Output table should
show which process is allocated to which block and unused space.
Sample Input:
Memory blocks = [100, 500, 200, 300, 600]
Process sizes = [212, 417, 112, 426]
Expected output format: Show Separate table for First Fit, Next Fit, Worst Fit
Process Process Size(KB) Block No Block Size(KB) Unused Space(KB)
P1
P2
P3
P4
*/

package A7;

import java.util.Arrays;

public class MemoryPlacementStrategies {

    public static void main(String[] args) {
        int[] memoryBlocks = {100, 500, 200, 300, 600};
        int[] processSizes = {212, 417, 112, 426};

        System.out.println("========== MEMORY ALLOCATION STRATEGIES ==========\n");

        // First Fit
        firstFit(memoryBlocks, processSizes);

        // Next Fit
        nextFit(memoryBlocks, processSizes);

        // Worst Fit
        worstFit(memoryBlocks, processSizes);
    }

    static void firstFit(int[] blocks, int[] processes) {
        System.out.println("---- FIRST FIT ----");
        System.out.printf("%-8s %-12s %-8s %-15s %-15s\n",
                "Process", "Process Size", "Block No", "Block Size(KB)", "Unused Space(KB)");

        int[] blockSizes = Arrays.copyOf(blocks, blocks.length);
        for (int i = 0; i < processes.length; i++) {
            boolean allocated = false;
            for (int j = 0; j < blockSizes.length; j++) {
                if (blockSizes[j] >= processes[i]) {
                    System.out.printf("%-8s %-12d %-8d %-15d %-15d\n",
                            "P" + (i + 1), processes[i], j + 1, blocks[j], blockSizes[j] - processes[i]);
                    blockSizes[j] -= processes[i];
                    allocated = true;
                    break;
                }
            }
            if (!allocated) {
                System.out.printf("%-8s %-12d %-8s %-15s %-15s\n",
                        "P" + (i + 1), processes[i], "Not Allocated", "-", "-");
            }
        }
        System.out.println();
    }

    static void nextFit(int[] blocks, int[] processes) {
        System.out.println("---- NEXT FIT ----");
        System.out.printf("%-8s %-12s %-8s %-15s %-15s\n",
                "Process", "Process Size", "Block No", "Block Size(KB)", "Unused Space(KB)");

        int[] blockSizes = Arrays.copyOf(blocks, blocks.length);
        int lastIndex = 0; // start from first block
        for (int i = 0; i < processes.length; i++) {
            boolean allocated = false;
            int count = 0;
            int j = lastIndex;
            while (count < blockSizes.length) {
                if (blockSizes[j] >= processes[i]) {
                    System.out.printf("%-8s %-12d %-8d %-15d %-15d\n",
                            "P" + (i + 1), processes[i], j + 1, blocks[j], blockSizes[j] - processes[i]);
                    blockSizes[j] -= processes[i];
                    allocated = true;
                    lastIndex = j; // update last allocation index
                    break;
                }
                j = (j + 1) % blockSizes.length;
                count++;
            }
            if (!allocated) {
                System.out.printf("%-8s %-12d %-8s %-15s %-15s\n",
                        "P" + (i + 1), processes[i], "Not Allocated", "-", "-");
            }
        }
        System.out.println();
    }

    static void worstFit(int[] blocks, int[] processes) {
        System.out.println("---- WORST FIT ----");
        System.out.printf("%-8s %-12s %-8s %-15s %-15s\n",
                "Process", "Process Size", "Block No", "Block Size(KB)", "Unused Space(KB)");

        int[] blockSizes = Arrays.copyOf(blocks, blocks.length);
        for (int i = 0; i < processes.length; i++) {
            int worstIndex = -1;
            for (int j = 0; j < blockSizes.length; j++) {
                if (blockSizes[j] >= processes[i]) {
                    if (worstIndex == -1 || blockSizes[j] > blockSizes[worstIndex]) {
                        worstIndex = j;
                    }
                }
            }
            if (worstIndex != -1) {
                System.out.printf("%-8s %-12d %-8d %-15d %-15d\n",
                        "P" + (i + 1), processes[i], worstIndex + 1, blocks[worstIndex], blockSizes[worstIndex] - processes[i]);
                blockSizes[worstIndex] -= processes[i];
            } else {
                System.out.printf("%-8s %-12d %-8s %-15s %-15s\n",
                        "P" + (i + 1), processes[i], "Not Allocated", "-", "-");
            }
        }
        System.out.println();
    }
}




/*
Code Explanation

First Fit

Allocates each process to the first block that is large enough.

Example: P1 (212 KB) goes to block 2 (500 KB), leaving 288 KB unused.

Next Fit

Similar to First Fit, but starts searching from the last allocated block.

Keeps a pointer (lastIndex) to the last block allocated.

Example: P3 (112 KB) is allocated to block 5 instead of block 2 because the search continues from the last allocation.

Worst Fit

Allocates each process to the largest available block.

Example: P1 (212 KB) is allocated to block 5 (600 KB) since it’s the largest block.

General Notes

If no block is large enough for a process, it is marked Not Allocated.

Unused Space = Block Size – Process Size.

Each strategy manages fragmentation differently:

First Fit & Next Fit → faster allocation.

Worst Fit → tries to reduce small leftover fragments.

Theoretical Q&A

Q: What is memory allocation in OS?
A: Memory allocation is the process of assigning blocks of main memory to processes in a way that maximizes utilization and minimizes fragmentation.

Q: Define First Fit, Next Fit, and Worst Fit.
A:

First Fit: Assign the first available block that can hold the process.

Next Fit: Like First Fit, but continues searching from the last allocated block.

Worst Fit: Assign the largest available block to the process to reduce leftover fragments.

Q: What is fragmentation?
A: Fragmentation occurs when memory is wasted due to small unusable gaps:

External Fragmentation: Free blocks exist but are too small for processes.

Internal Fragmentation: Allocated block is larger than process needs, leaving unused space.

Q: Advantages & disadvantages of these strategies?
A:

Strategy	Advantage	Disadvantage
First Fit	Fast allocation	Can cause external fragmentation
Next Fit	Slightly fairer distribution	May skip free blocks leading to fragmentation
Worst Fit	Reduces small leftover gaps	Can leave very large gaps unused

Q: How does Worst Fit reduce external fragmentation?
A: By allocating the largest block, it avoids creating many tiny unusable holes.

Practical Q&A for Viva

Q: How did you implement First Fit?
A: I iterated over the memory blocks for each process and allocated it to the first block with enough space, then reduced the block’s available size.

Q: How did you implement Next Fit?
A: Maintained a lastIndex pointer for the last allocation. For each process, I started searching from this index in a circular manner.

Q: How did you implement Worst Fit?
A: For each process, I searched all blocks to find the largest available block and allocated the process there.

Q: How do you handle a process that cannot be allocated?
A: I print Not Allocated and leave block sizes unchanged.

Q: Why do we need a copy of block sizes for each strategy?
A: Each strategy modifies block sizes differently. Using a copy ensures one strategy doesn’t affect the others.

Q: How is Unused Space calculated?
A: Unused Space = Original Block Size – Allocated Process Size.
*/








/*
Chit No 9 – Page Replacement Algorithms (FIFO & LRU)
Problem Statement: Simulate page-replacement algorithms FIFO, LRU using 3 frames. Show step-bystep frame allocation, mark page faults and hits for each reference, and print the total page-fault count.
1. FIFO
2. LRU
Sample Input: Reference string = 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2
*/

package A9;

import java.util.*;

public class PageReplacement {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Input reference string
        System.out.println("Enter number of pages in reference string:");
        int n = sc.nextInt();
        int[] ref = new int[n];
        System.out.println("Enter the reference string:");
        for (int i = 0; i < n; i++) {
            ref[i] = sc.nextInt();
        }

        int frames = 3; // Number of frames
        System.out.println("\n--- FIFO Page Replacement ---");
        fifo(ref, frames);

        System.out.println("\n--- LRU Page Replacement ---");
        lru(ref, frames);

        sc.close();
    }

    // FIFO Page Replacement
    static void fifo(int[] ref, int frames) {
        int[] memory = new int[frames];
        Arrays.fill(memory, -1);
        int pointer = 0;
        int pageFaults = 0;

        System.out.println("Step\tPage\tFrames\tStatus");
        for (int i = 0; i < ref.length; i++) {
            boolean hit = false;
            for (int j = 0; j < frames; j++) {
                if (memory[j] == ref[i]) {
                    hit = true;
                    break;
                }
            }
            String status;
            if (!hit) {
                memory[pointer] = ref[i];
                pointer = (pointer + 1) % frames;
                pageFaults++;
                status = "Page Fault";
            } else {
                status = "Hit";
            }

            System.out.print((i + 1) + "\t" + ref[i] + "\t");
            for (int j = 0; j < frames; j++) {
                if (memory[j] != -1) System.out.print(memory[j] + " ");
                else System.out.print("- ");
            }
            System.out.println("\t" + status);
        }

        System.out.println("Total Page Faults: " + pageFaults);
    }

    // LRU Page Replacement
    static void lru(int[] ref, int frames) {
        int[] memory = new int[frames];
        Arrays.fill(memory, -1);
        int pageFaults = 0;
        List<Integer> recent = new ArrayList<>();

        System.out.println("Step\tPage\tFrames\tStatus");
        for (int i = 0; i < ref.length; i++) {
            boolean hit = false;
            for (int j = 0; j < frames; j++) {
                if (memory[j] == ref[i]) {
                    hit = true;
                    break;
                }
            }
            String status;
            if (!hit) {
                // If empty slot exists
                boolean inserted = false;
                for (int j = 0; j < frames; j++) {
                    if (memory[j] == -1) {
                        memory[j] = ref[i];
                        inserted = true;
                        break;
                    }
                }
                // Replace least recently used if full
                if (!inserted) {
                    int lruIndex = recent.get(0);
                    memory[lruIndex] = ref[i];
                    recent.remove(0);
                }
                pageFaults++;
                status = "Page Fault";
            } else {
                status = "Hit";
            }

            // Update recent list
            int memIndex = -1;
            for (int j = 0; j < frames; j++) {
                if (memory[j] == ref[i]) memIndex = j;
            }
            recent.remove((Integer) memIndex);
            recent.add(memIndex);

            System.out.print((i + 1) + "\t" + ref[i] + "\t");
            for (int j = 0; j < frames; j++) {
                if (memory[j] != -1) System.out.print(memory[j] + " ");
                else System.out.print("- ");
            }
            System.out.println("\t" + status);
        }

        System.out.println("Total Page Faults: " + pageFaults);
    }
}



/*
Code Explanation

Input

Number of pages in the reference string.

The reference string itself (sequence of page numbers requested by the CPU).

Number of frames is fixed at 3 for simulation.

FIFO (First In First Out)

Uses an array memory to store pages currently in frames.

A pointer cycles through the array to replace the oldest page when a page fault occurs.

Steps:

Check if the current page exists in memory.

If yes → Hit.

If no → Page Fault, replace the page at the current pointer position, increment pointer.

Print frame contents and status after each reference.

LRU (Least Recently Used)

Uses an array memory and a List named recent to track recently used frames.

Steps:

Check if the current page exists → Hit.

If no → Page Fault:

If empty slot exists, place page there.

Else, replace the page that was least recently used (the first index in recent list).

Update recent list to mark most recent usage.

Print frame contents and status after each reference.

Output

Step-wise frame allocation showing:

Page requested.

Frames’ contents.

Hit or Page Fault.

Total number of page faults at the end.

Sample Output Explanation
FIFO Example
Reference string: 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2
Frames = 3

Step 1: Page 7 → Not in memory → Page Fault → Memory: [7 - -]
Step 2: Page 0 → Not in memory → Page Fault → Memory: [7 0 -]
Step 3: Page 1 → Not in memory → Page Fault → Memory: [7 0 1]
Step 4: Page 2 → Not in memory → Page Fault → Replace oldest (7) → Memory: [2 0 1]
Step 5: Page 0 → Already in memory → Hit
...
Total Page Faults = 10

LRU Example
Step 1: Page 7 → Page Fault → Memory: [7 - -]
Step 2: Page 0 → Page Fault → Memory: [7 0 -]
Step 3: Page 1 → Page Fault → Memory: [7 0 1]
Step 4: Page 2 → Page Fault → Replace LRU (7) → Memory: [2 0 1]
Step 5: Page 0 → Already in memory → Hit
...
Total Page Faults = 9


Observation: LRU has fewer page faults because it replaces least recently used pages instead of just oldest.

Theoretical Questions & Answers

Q: What is a page replacement algorithm?
A: It decides which page to remove from memory when a new page needs to be loaded and memory is full.

Q: What is a page fault?
A: A page fault occurs when the requested page is not present in memory, forcing a page to be loaded from secondary storage.

Q: Difference between FIFO and LRU?

FIFO: Replaces the oldest page. Simple but may cause Belady’s anomaly.

LRU: Replaces the least recently used page. Better performance as recently used pages stay in memory.

Q: What is Belady’s anomaly?
A: In FIFO, increasing the number of frames can sometimes increase page faults, which is counterintuitive.

Q: Advantages of LRU over FIFO?
A: LRU usually produces fewer page faults because it keeps frequently used pages in memory.

Practical Questions & Answers for Viva

Q: How is FIFO implemented in your code?
A: Using an array for memory frames and a pointer that cycles to replace the oldest page.

Q: How is LRU implemented in your code?
A: Using an array for memory and a list recent to track usage. The first element in recent is the least recently used frame, replaced when a page fault occurs.

Q: How do you count page faults?
A: A counter increments every time the requested page is not found in memory.

Q: How do you display frame contents step by step?
A: After each page reference, the array representing frames is printed with the page fault or hit status.

Q: How does LRU reduce page faults compared to FIFO?
A: By replacing the page that has not been used for the longest time, avoiding removal of pages that are still frequently accessed.
*/








/*
Chit No 10 – Page Replacement Algorithms (FIFO & Optimal Algorithm)
Problem Statement: Simulate page-replacement algorithms FIFO, Optimal algorithm using 3 frames.
Show step-by-step frame allocation, mark page faults and hits for each reference, and print the total
page-fault count.
1. FIFO
2. Optimal Algorithm
Sample Input: Reference string = 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2
*/

package A10;

import java.util.*;

public class PageReplacementFIFOOptimal {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of pages in reference string: ");
        int n = sc.nextInt();

        int[] refString = new int[n];
        System.out.println("Enter the reference string:");
        for (int i = 0; i < n; i++) {
            refString[i] = sc.nextInt();
        }

        int frames = 3;

        System.out.println("\n--- FIFO Page Replacement ---");
        fifo(refString, frames);

        System.out.println("\n--- Optimal Page Replacement ---");
        optimal(refString, frames);

        sc.close();
    }

    // FIFO Page Replacement
    static void fifo(int[] refString, int frames) {
        int[] memory = new int[frames];
        Arrays.fill(memory, -1);
        int pointer = 0, pageFaults = 0;

        System.out.println("Step\tPage\tFrames\tStatus");

        for (int i = 0; i < refString.length; i++) {
            boolean hit = false;
            for (int j = 0; j < frames; j++) {
                if (memory[j] == refString[i]) {
                    hit = true;
                    break;
                }
            }

            String status;
            if (hit) {
                status = "Hit";
            } else {
                memory[pointer] = refString[i];
                pointer = (pointer + 1) % frames;
                pageFaults++;
                status = "Page Fault";
            }

            System.out.print((i + 1) + "\t" + refString[i] + "\t");
            for (int j = 0; j < frames; j++) {
                System.out.print((memory[j] == -1 ? "-" : memory[j]) + " ");
            }
            System.out.println("\t" + status);
        }

        System.out.println("Total Page Faults: " + pageFaults);
    }

    // Optimal Page Replacement
    static void optimal(int[] refString, int frames) {
        int[] memory = new int[frames];
        Arrays.fill(memory, -1);
        int pageFaults = 0;

        System.out.println("Step\tPage\tFrames\tStatus");

        for (int i = 0; i < refString.length; i++) {
            boolean hit = false;
            for (int j = 0; j < frames; j++) {
                if (memory[j] == refString[i]) {
                    hit = true;
                    break;
                }
            }

            String status;
            if (hit) {
                status = "Hit";
            } else {
                pageFaults++;
                // Check if empty slot exists
                boolean placed = false;
                for (int j = 0; j < frames; j++) {
                    if (memory[j] == -1) {
                        memory[j] = refString[i];
                        placed = true;
                        break;
                    }
                }
                // If no empty slot, replace page that will not be used for the longest time
                if (!placed) {
                    int replaceIndex = -1;
                    int farthest = i + 1;
                    for (int j = 0; j < frames; j++) {
                        int k;
                        for (k = i + 1; k < refString.length; k++) {
                            if (memory[j] == refString[k]) {
                                if (k > farthest) {
                                    farthest = k;
                                    replaceIndex = j;
                                }
                                break;
                            }
                        }
                        if (k == refString.length) {
                            replaceIndex = j;
                            break;
                        }
                    }
                    if (replaceIndex == -1) replaceIndex = 0;
                    memory[replaceIndex] = refString[i];
                }
                status = "Page Fault";
            }

            System.out.print((i + 1) + "\t" + refString[i] + "\t");
            for (int j = 0; j < frames; j++) {
                System.out.print((memory[j] == -1 ? "-" : memory[j]) + " ");
            }
            System.out.println("\t" + status);
        }

        System.out.println("Total Page Faults: " + pageFaults);
    }
}




/*
Code Explanation

Inputs:

User enters the number of pages and the reference string.

Number of frames is set to 3.

FIFO Algorithm:

Uses a circular queue.

The oldest page in memory is replaced first.

Checks for hit (page already in memory) and page fault (page not in memory).

Maintains a pointer to the next frame to replace.

Optimal Algorithm:

Replaces the page which will not be used for the longest time in the future.

Steps:

If page is in memory → Hit

If memory has empty slot → add page

If memory full → replace the page whose next use is farthest in the reference string

Typically results in fewer page faults than FIFO.

Output:

Step-wise table showing:

Current page reference

Frame contents

Status (Hit or Page Fault)

Total page faults for each algorithm.

Sample Output Analysis
FIFO
Step	Page	Frames	Status
1	7	7 - -	Page Fault
2	0	7 0 -	Page Fault
3	1	7 0 1	Page Fault
4	2	2 0 1	Page Fault
5	0	2 0 1	Hit
…	…	…	…
13	2	0 2 3	Hit

Total Page Faults: 10

Optimal
Step	Page	Frames	Status
1	7	7 - -	Page Fault
2	0	7 0 -	Page Fault
3	1	7 0 1	Page Fault
4	2	2 0 1	Page Fault
5	0	2 0 1	Hit
…	…	…	…
13	2	2 0 3	Hit

Total Page Faults: 7

Observation: Optimal algorithm has fewer page faults than FIFO because it chooses the best page to replace.

Theoretical Questions & Answers

Q: What is a page fault?
A: A page fault occurs when the requested page is not in main memory, so the OS must fetch it from secondary memory.

Q: Difference between FIFO and Optimal Page Replacement?
A:

FIFO replaces the oldest loaded page.

Optimal replaces the page least likely to be used in near future.

Q: Why does Optimal have fewer page faults than FIFO?
A: Because it uses future knowledge to minimize unnecessary replacements.

Q: What are frames?
A: Fixed-size slots in memory where pages are loaded.

Q: What is the impact of frame size on page faults?
A: More frames generally reduce page faults because more pages can be kept in memory.

Practical Viva Questions & Answers

Q: What is the purpose of memory[] array in code?
A: Stores the current pages in frames.

Q: How do you detect a hit?
A: If the current page exists in memory[], it’s a hit.

Q: How is FIFO pointer updated?
A: Using (pointer + 1) % frames to cycle through memory positions.

Q: How is the optimal page chosen for replacement?
A: Check future references. Replace the page whose next use is farthest or not used at all.

Q: How would the program change for 4 frames instead of 3?
A: Change frames = 4; the logic remains same.

Q: What is the total page fault count for the given reference string?
A: FIFO → 10, Optimal → 7.
*/