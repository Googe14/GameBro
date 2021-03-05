package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import hardware.CPU;
import hardware.CPU.Instr;
import hardware.Clock;
import hardware.Memory;
import hardware.PPU;

public class Debugger extends JFrame {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	JCheckBoxMenuItem chckbxmntmAnimate = new JCheckBoxMenuItem("Animate");
	
	public Debugger(Memory mem, CPU cpu, PPU ppu, Clock clock) {
		
		this.mem = mem;
		this.cpu = cpu;
		this.ppu = ppu;
		this.clock = clock;
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu runMenu = new JMenu("Run");
		menuBar.add(runMenu);
		
		JMenuItem mntmStep = new JMenuItem("Step");
		mntmStep.setAction(action_2);
		runMenu.add(mntmStep);
		
		JMenuItem mntmRun = new JMenuItem("Run");
		mntmRun.setAction(action_4);
		runMenu.add(mntmRun);
		
		JMenuItem mntmPause = new JMenuItem("Pause");
		mntmPause.setAction(action_3);
		runMenu.add(mntmPause);
		
		chckbxmntmAnimate.setAction(action_1);
		runMenu.add(chckbxmntmAnimate);
		chckbxmntmAnimate.setSelected(animate);
		
		JMenuItem mntmRefreshMemory = new JMenuItem("Refresh Memory");
		mntmRefreshMemory.setAction(action);
		runMenu.add(mntmRefreshMemory);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Instructions", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel_2.add(panel_5);
		panel_5.setLayout(new GridLayout(0, 1, 0, 0));
		
		ta_Instructions.setColumns(100);
		ta_Instructions.setEditable(false);
		ta_Instructions.setFont(new Font("monospaced", Font.PLAIN, 12));
		JScrollPane scrollInstr = new JScrollPane (ta_Instructions, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel_5.add(scrollInstr);
		
		ta_Instructions.addKeyListener(key);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Registers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4);
		panel_4.setLayout(new GridLayout(0, 1, 0, 0));
		flag_Z.setAction(action_5);
		
		panel_4.add(flag_Z);
		flag_N.setAction(action_6);
		
		panel_4.add(flag_N);
		flag_H.setAction(action_7);
		
		panel_4.add(flag_H);
		flag_C.setAction(action_8);
		
		panel_4.add(flag_C);
		
		JPanel panel_7 = new JPanel();
		panel_1.add(panel_7);
		panel_7.setLayout(new GridLayout(1, 0, 0, 0));
		
		ta_Registers.setColumns(10);
		ta_Registers.setEditable(false);
		ta_Registers.setFont(new Font("monospaced", Font.PLAIN, 12));
		panel_7.add(ta_Registers);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Memory", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.add(panel_6, BorderLayout.SOUTH);
		panel_6.setLayout(new GridLayout(0, 1, 0, 0));
		
		ta_Memory.setRows(15);
		ta_Memory.setEditable(false);
		ta_Memory.setFont(new Font("monospaced", Font.PLAIN, 12));
		JScrollPane scrollMem = new JScrollPane (ta_Memory, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel_6.add(scrollMem);
		
		update();
	}
	
	private HashMap<Integer, Instr> record = new HashMap<>();
	
	private Memory mem;
	private CPU cpu;
	private PPU ppu;
	private Clock clock;
	
	JTextArea ta_Instructions = new JTextArea();
	JTextArea ta_Memory = new JTextArea();

	JTextArea ta_Registers = new JTextArea();

	JCheckBox flag_Z = new JCheckBox("Z");
	JCheckBox flag_N = new JCheckBox("N");
	JCheckBox flag_H = new JCheckBox("H");
	JCheckBox flag_C = new JCheckBox("C");
	private final Action action = new SwingAction();
	private final Action action_1 = new SwingAction_1();
	
	public void update() { //Update information displayed on the debugger
		int last = cpu.getLastInstruction();
		appendInstruction(last);
		updateRegisters();
		updateFlags();
	}
	
	public boolean step() { //Run 1 instruction on the cpu and update the debugger
		System.out.println(cpu.getQueuedCycles());
		if(cpu.getQueuedCycles() > 0) {
			while(cpu.getQueuedCycles() > 0) cpu.go();
		}
		cpu.go();
		update();
		return true;
	}
	
	public void updateFlags() {
		if(cpu.FZ) flag_Z.setSelected(true); //Set flag checkboxes
		else flag_Z.setSelected(false);
		if(cpu.FN) flag_N.setSelected(true);
		else flag_N.setSelected(false);
		if(cpu.FH) flag_H.setSelected(true);
		else flag_H.setSelected(false);
		if(cpu.FC) flag_C.setSelected(true);
		else flag_C.setSelected(false);
	}
	
	public void updateRegisters() { //Update values shown for registers on the right
		StringBuilder sb = new StringBuilder();
		sb.append("A:\t");
		sb.append(CPU.toHex8(cpu.readReg8(CPU.A)));
		sb.append("\nBC:\t");
		sb.append(CPU.toHex16(cpu.readReg16(CPU.B)));
		sb.append("\nDE:\t");
		sb.append(CPU.toHex16(cpu.readReg16(CPU.D)));
		sb.append("\nHL:\t");
		sb.append(CPU.toHex16(cpu.readReg16(CPU.H)));
		sb.append("\nPC:\t");
		sb.append(CPU.toHex16(cpu.PC));
		sb.append("\n(PC):\t");
		sb.append(CPU.toHex8(mem.read8(cpu.PC)));
		sb.append("\nSP:\t");
		sb.append(CPU.toHex16(cpu.PC));
		ta_Registers.setText(sb.toString());
	}
	
	ArrayList<String> queuedInstr = new ArrayList<String>();
	
	public void updateInstructions() {
		while(queuedInstr.size() > 0) {
			ta_Instructions.append(queuedInstr.get(0));
			queuedInstr.remove(0);
		}
	}
	
	boolean animate = true;
	private final Action action_2 = new SwingAction_2();
	public Instr appendInstruction(int opcode) {   //Append instructions to the instruction list
		Instr i = cpu.getInstruction(opcode);
		
		int addr = cpu.PC-1;
		Instr instr = record.get(addr);
		if(instr != null) { //If a previous record exists
			if(instr == i) { //If it's equal to our existing record
				//Select line it's at
				return i;
			}
		}
		record.put(addr, i);
		
		if(i == null) {
			updateInstructions();
			
			StringBuilder sb = new StringBuilder();
			sb.append(CPU.toHex16(cpu.PC - 1));
			sb.append("\t");
			sb.append(CPU.toHex8(opcode & 0xFF));
			int len = sb.toString().length();
			int targetLen = 30;
			for(int j = 0; j < targetLen - len; j++) {
				sb.append(" ");
			}
			sb.append("Unknown opcode!\n");
			ta_Instructions.append(sb.toString());
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(CPU.toHex16(cpu.PC - 1));
		sb.append("\t");
		sb.append(CPU.toHex8(i.opcode & 0xFF));
		for(String s : i.args) {
			sb.append(" ");
			sb.append(s);
		}
		
		int len = sb.toString().length();
		int targetLen = 30;
		for(int j = 0; j < targetLen - len; j++) {
			sb.append(" ");
		}
		
		/*
		sb.append("\t");
		if(sb.toString().length() < 19) sb.append("\t");
		if(sb.toString().length() < 15) sb.append("\t");
		if(sb.toString().length() < 11) sb.append("\t");
		*/

		sb.append(i.instr);
		sb.append(" ");
		for(String s : i.args) {
			if(s.equals("(HL)")) {
				sb.append(CPU.toHex16(mem.read16(cpu.readReg8(CPU.H))));
			} else if(s.equals("n")) {
				sb.append(CPU.toHex8(mem.read8(cpu.PC)));
			} else if(s.equals("nn")) {
				sb.append(CPU.toHex16(mem.read16(cpu.PC)));
			} else if (s.equals("(n)")) {
				sb.append(CPU.toHex8(mem.read8(cpu.PC)));
			} else if (s.equals("(nn)")) {
				sb.append(CPU.toHex16(mem.read16(cpu.PC)));
			} else{
				sb.append(s);
			}
			sb.append(" ");
		}
		sb.append("\n");
		
		if(!animate) queuedInstr.add(sb.toString());
		else ta_Instructions.append(sb.toString());
		
		return i;
	}
	
	public String memorySector(int address) {
		String sec = "";
		
		if(address >= 0x0000) sec = "ROM0";
		if(address >= 0x4000) sec = "ROM1";
		if(address >= 0x8000) sec = "VRA0";
		if(address >= 0xA000) sec = "SRA0";
		if(address >= 0xC000) sec = "WRA0";
		if(address >= 0xD000) sec = "WRA1";
		if(address >= 0xE000) sec = "ECH0";
		if(address >= 0xF000) sec = "ECH1";
		if(address >= 0xFE00) sec = "OAM ";
		if(address >= 0xFEA0) sec = "----";
		if(address >= 0xFF00) sec = "I/O ";
		if(address >= 0xFF80) sec = "HRAM";
		
		return sec;
	}
	
	public void updateMemory() { //Update the TextArea showing Memory
		ta_Memory.setText("");

		for(int i = 0; i < 4096; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(memorySector(i*16));
			sb.append(": ");
			sb.append(CPU.toHex16(i*16));
			sb.append("\t");
			for(int j = 0; j < 16; j++) {
				sb.append(CPU.toHex8(mem.read8(i*16+j) & 0xFF));
				sb.append(" ");
				if(j == 7) sb.append("| ");
			}
			sb.append("\n");
			ta_Memory.append(sb.toString());
		}
	}
	
	//*******************Actions for buttons and stuffs
	
	private final Action action_3 = new SwingAction_3();
	private final Action action_4 = new SwingAction_4();
	private final Action action_5 = new SwingAction_5();
	private final Action action_6 = new SwingAction_6();
	private final Action action_7 = new SwingAction_7();
	private final Action action_8 = new SwingAction_8();
	
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "Refresh Memory");
			putValue(SHORT_DESCRIPTION, "Dump Current Gameboy Memory to the Memory Area");
		}
		public void actionPerformed(ActionEvent e) {
			updateMemory();
		}
	}
	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "Animate Steps");
			putValue(SHORT_DESCRIPTION, "Update Instruction list as instructions are executed");
		}
		public void actionPerformed(ActionEvent e) {
			animate = chckbxmntmAnimate.isSelected();
		}
	}
	private class SwingAction_2 extends AbstractAction {
		public SwingAction_2() {
			putValue(NAME, "Step");
			putValue(SHORT_DESCRIPTION, "Executes the single next instruction");
		}
		public void actionPerformed(ActionEvent e) {
			boolean cache = animate;
			animate = true;
			step();
			animate = cache;
		}
	}
	private class SwingAction_3 extends AbstractAction {
		public SwingAction_3() {
			putValue(NAME, "Pause");
			putValue(SHORT_DESCRIPTION, "Stop executing instructions");
		}
		public void actionPerformed(ActionEvent e) {
			clock.stop();
			updateInstructions();
		}
	}
	private class SwingAction_4 extends AbstractAction {
		public SwingAction_4() {
			putValue(NAME, "Run");
			putValue(SHORT_DESCRIPTION, "Start executing instructions until further notice");
		}
		public void actionPerformed(ActionEvent e) {
			clock.start();
		}
	}
	
	private class SwingAction_5 extends AbstractAction {
		public SwingAction_5() {
			putValue(NAME, "Z");
			putValue(SHORT_DESCRIPTION, "Set Zero Flag");
		}
		public void actionPerformed(ActionEvent e) {
			cpu.FZ = flag_Z.isSelected();
		}
	}
	private class SwingAction_6 extends AbstractAction {
		public SwingAction_6() {
			putValue(NAME, "N");
			putValue(SHORT_DESCRIPTION, "Set Subtraction flag");
		}
		public void actionPerformed(ActionEvent e) {
			cpu.FN = flag_N.isSelected();
		}
	}
	private class SwingAction_7 extends AbstractAction {
		public SwingAction_7() {
			putValue(NAME, "H");
			putValue(SHORT_DESCRIPTION, "Set Half-Carry Flag");
		}
		public void actionPerformed(ActionEvent e) {
			cpu.FH = flag_H.isSelected();
		}
	}
	private class SwingAction_8 extends AbstractAction {
		public SwingAction_8() {
			putValue(NAME, "C");
			putValue(SHORT_DESCRIPTION, "Set Carry Flag");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	
	private KeyListener key = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent arg0) {
			
			if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				boolean cache = animate;
				animate = true;
				step();
				animate = cache;
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			
		}
		
	};
	
}
