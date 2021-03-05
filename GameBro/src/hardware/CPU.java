package hardware;

import java.util.ArrayList;

import ui.Debugger;

/* RESOURCES
 * 
 * OPCodes:				http://imrannazar.com/Gameboy-Z80-Opcode-Map
 * Flags and shit: 		http://gameboy.mongenel.com/dmg/opcodes.html
 * Writeup:				https://hacktix.github.io/GBEDG/
 * Gameboy debugger:	In Downloads/gb/gbg
 * 
 */

public class CPU {

	private Memory mem;
	
	public CPU(Memory mem) {
		this.mem = mem;
	}
	
	//****************************************************Memory/Register stuff
	
	//CPU Registers
	public static final int A = 0; //Accumulator Flag
	public static final int B = 1; 
	public static final int C = 2;
	public static final int D = 3;
	public static final int E = 4;
	public static final int H = 5; //HL can be used as memory pointer
	public static final int L = 6;
	//16 Bit Registers (Still use readReg8 to access)
	public int SP = 0; //Stack Pointer
	public int PC = 0; //Program Counter
	
	private int[] registers = new int[7];
	
	public boolean FZ = false;
	public boolean FN = false;
	public boolean FH = false;
	public boolean FC = false;
	
	//TODO Implement interrupts
	public boolean interruptMasterEnable = true;
	
	/*
	FLAGS
	Bit:	7	6	5	4	3	2	1	0
	Flag:	Z	N	H	C	0	0	0	0
	
	7: Z - Zero Flag
	6: N - Subtraction
	5: H - Half-Carry
	4: C - Carry
	*/
	
	//Read and Write of registers
	public int readReg8(int register) { //Use this to access SP and PC as well
		return registers[register];
	}
	public int readReg16(int register) {
		return (registers[register] << 8) + registers[register+1];
	}
	
	public void writeReg8(int register, int data) {
		registers[register] = data & 0xFF;
	}
	
	public void writeReg16(int register, int data) {
		registers[register] = (data >> 8) & 0xFF;
		registers[register+1] = data & 0xFF;
	}	
	
	//***************************************************Clock and main loop
	
	private int queuedCycles = 0;
	private int lastInstruction = 0;
	
	public int getLastInstruction() {
		return lastInstruction;
	}
	
	public int getQueuedCycles() {
		return queuedCycles;
	}
	
	public boolean go() {
		
		if(queuedCycles > 0) { //Decrement Queued Cycles
			queuedCycles--;
			return true;
		}
		
		//Check Interrupts
		
		lastInstruction = nextInstruction(); //Do normal instruction running stuff
		Instr instr = getInstruction(lastInstruction);
		if(instr == null) {
			return false;
		}
		queuedCycles += instr.run() - 1;
		return true;
	}
	

	
	//****************************************************Nice to have
	
	public static String toHex8(int in) {
		return String.format("%02X", in);
	}
	
	public static String toHex16(int in) {
		return String.format("%04X", in);
	}
	
	
	
	//*************************************************CPU loop stuff
	
	public int incPC() { 
		PC++;
		return PC-1;
	}
	
	public int nextInstruction() {
		return mem.read8(incPC());
	}
	//***************************************************Instruction stuff
	
	public abstract class Instr {  //Base class for an instruction
		public int opcode;
		public String instr;
		public ArrayList<String> args = new ArrayList<String>();
		public abstract int run(); //Returns cycles taken
	}
	
	public Instr getInstruction(int opcode) { //Get the instruction object from the opcode
		
		switch(opcode) {
		case 0x00:
			return x00;
		case 0x05:
			return x05;
		case 0x06:
			return x06;
		case 0x0D:
			return x0D;
		case 0x0E:
			return x0E;
		case 0x20:
			return x20;
		case 0x21:
			return x21;
		case 0x31:
			return x31;
		case 0x32:
			return x32;
		case 0x36:
			return x36;
		case 0x3E:
			return x3E;
		case 0xAF:
			return xAF;
		case 0xC3:
			return xC3;
		case 0xE0:
			return xE0;
		case 0xEA:
			return xEA;
		case 0xF0:
			return xF0;
		case 0xF3:
			return xF3;
		case 0xFE:
			return xFE;
			
			
		default:
			return null;
		}
	}
	
	//***********************************************Common instructions

	private void JR(int address) {
		byte dir = (byte)address;
		PC -= (~dir);
	}
	
	private void XOR(int val) {
		writeReg8(A, (byte)(readReg8(A) ^ val));
		if(readReg8(A) == 0) FZ = true;
		else FZ = false;
		FN = false;
		FH = false;
		FC = false;
	}
	
	private void CP(int v1, int v2) {
		int ans = v1 - v2;
		if(ans == 0) FZ = true;
		else FZ = false;
		if(checkHalfCarry(v1, -v2)) FH = true;
		else FH = false;
		FN = true;
		if(ans < 0) FC = true;
		else FC = false;
	}
	
	private void INC16(int register) {
		writeReg16(H, (byte)(readReg16(H)+1));
	}
	private void DEC16(int register) {
		writeReg16(H, (byte)(readReg16(H)-1));
	}
	
	private void INC8(int register) {
		int val = readReg8(register);
		if(checkHalfCarry(val, 1)) FH = true;
		else FH = false;
		if(val + 1 == 0) FZ = true;
		else FZ = false;
		FN = false;
		writeReg8(register, (byte)((val+1)));
	}
	
	private void DEC8(int register) {
		int val = readReg8(register);
		if(checkHalfCarry(val, -1)) FH = true;
		else FH = false;
		if(val - 1 == 0) FZ = true;
		else FZ = false;
		FN = true;
		writeReg8(register, (byte)((val-1)));
	}
	
	
	private boolean checkHalfCarry(int a, int b) {
		if((((a & 0xf) + (b & 0xf)) & 0x10) == 0x10) return true;
		return false;
	}
	
	//*********************************************Individual Instructions
	
	/*     Most basic instruction, ready to be filled in.
	 	class xXX extends Instr {
			public xXX() {
				opcode = 0xXX;
				instr = "";
				args.add();
			}
			@Override
			public void run() {
				
			}
		}
		xnn xnn = new xnn();

	 */
	
	class x00 extends Instr {
		public x00() {
			opcode = 0x00;
			instr = "NOP";
		}
		@Override
		public int run() {
			return 4;
		}
	}
	x00 x00 = new x00();
	
	class x05 extends Instr {
		public x05() {
			opcode = 0x05;
			instr = "DEC";
			args.add("B");
		}
		@Override
		public int run() {
			DEC8(B);
			return 4;
		}
	}
	x05 x05 = new x05();

	class x06 extends Instr {
		public x06() {
			opcode = 0x06;
			instr = "LD";
			args.add("B");
			args.add("n");
		}
		@Override
		public int run() {
			writeReg8(B, mem.read8(PC));
			incPC();
			return 8;
		}
	}
	x06 x06 = new x06();

	class x0D extends Instr {
		public x0D() {
			opcode = 0x0D;
			instr = "DEC";
			args.add("C");
		}
		@Override
		public int run() {
			DEC8(C);
			return 4;
		}
	}
	x0D x0D = new x0D();

	class x0E extends Instr {
		public x0E() {
			opcode = 0x0E;
			instr = "LD";
			args.add("C");
			args.add("n");
		}
		@Override
		public int run() {
			writeReg8(C, mem.read8(PC));
			incPC();
			return 8;
		}
	}
	x0E x0E = new x0E();

	class x20 extends Instr {
		public x20() {
			opcode = 0x20;
			instr = "JR";
			args.add("NZ");
			args.add("n");
		}
		@Override
		public int run() {
			if(!FZ) { JR(mem.read8(PC)); return 12; }
			else { incPC(); return 8; }
		}
	}
	x20 x20 = new x20();

	class x21 extends Instr {
		public x21() {
			opcode = 0x21;
			instr = "LD";
			args.add("HL");
			args.add("nn");
		}
		@Override
		public int run() {
			writeReg16(H, mem.read16(PC));
			incPC();
			incPC();
			return 12;
		}
	}
	x21 x21 = new x21();
	
	class x31 extends Instr {
		public x31() {
			opcode = 0x31;
			instr = "LD";
			args.add("SP");
			args.add("nn");
		}
		@Override
		public int run() {
			SP = mem.read16(incPC());
			incPC();
			return 12;
		}
	}
	x31 x31 = new x31();

	class x32 extends Instr {
		public x32() {
			opcode = 0x32;
			instr = "LDD";
			args.add("(HL)");
			args.add("A");
		}
		@Override
		public int run() {
			mem.write8(readReg16(H), readReg8(A));
			DEC16(H);
			return 8;
		}
	}
	x32 x32 = new x32();
	
	class x36 extends Instr {
		public x36() {
			opcode = 0x36;
			instr = "LD";
			args.add("(HL)");
			args.add("n");
		}
		@Override
		public int run() {
			mem.write8(readReg16(H), mem.read8(incPC()));
			return 12;
		}
	}
	x36 x36 = new x36();
	
	class x3E extends Instr {
		public x3E() {
			opcode = 0x3E;
			instr = "LD";
			args.add("A");
			args.add("n");
		}
		@Override
		public int run() {
			writeReg8(A, mem.read8(PC));
			incPC();
			return 8;
		}
	}
	x3E x3E = new x3E();

	class xAF extends Instr {
		public xAF() {
			opcode = 0xAF;
			instr = "XOR";
			args.add("A");
			args.add("A");
		}
		@Override
		public int run() {
			XOR(readReg8(A));
			return 4;
		}
	}
	xAF xAF = new xAF();

	class xC3 extends Instr {
		public xC3() {
			opcode = 0xC3;
			instr = "JP";
			args.add("nn");
		}
		@Override
		public int run() {
			PC = mem.read16(PC);
			return 16;
		}
	}
	xC3 xC3 = new xC3();
	
	class xE0 extends Instr {
		public xE0() {
			opcode = 0xE0;
			instr = "LDH";
			args.add("0xFF00+");
			args.add("(n)");
			args.add("A");
		}
		@Override
		public int run() {
			mem.write8(0xFF00 + mem.read8(incPC()), readReg8(A));
			return 12;
		}
	}
	xE0 xE0 = new xE0();
	
	class xEA extends Instr {
		public xEA() {
			opcode = 0xEA;
			instr = "LD";
			args.add("(nn)");
			args.add("A");
		}
		@Override
		public int run() {
			mem.write8(mem.read16(incPC()), readReg8(A));
			incPC();
			return 16;
		}
	}
	xEA xEA = new xEA();
	
	class xF0 extends Instr {
		public xF0() {
			opcode = 0xF0;
			instr = "LDH";
			args.add("A");
			args.add("FF00+");
			args.add("(n)");
		}
		@Override
		public int run() {
			writeReg8(A, 0xFF00 + mem.read8(incPC()));
			return 12;
		}
	}
	xF0 xF0 = new xF0();
	
	class xF3 extends Instr {
		public xF3() {
			opcode = 0xF3;
			instr = "DI";
		}
		@Override
		public int run() {
			interruptMasterEnable = false;
			return 4;
		}
	}
	xF3 xF3 = new xF3();
	
	class xFE extends Instr {
		public xFE() {
			opcode = 0xFE;
			instr = "CP";
			args.add("n");
		}
		@Override
		public int run() {
			CP(readReg8(A), mem.read8(incPC()));
			return 8;
		}
	}
	xFE xFE = new xFE();

}
