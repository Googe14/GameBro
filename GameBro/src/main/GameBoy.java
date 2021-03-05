package main;

import files.RomReader;
import hardware.CPU;
import hardware.Clock;
import hardware.Memory;
import hardware.PPU;
import ui.Debugger;
import ui.MainWindow;

public class GameBoy {

	public CPU cpu;
	public Memory mem;
	public Debugger deb;
	public PPU ppu;
	public Clock clock;
	public MainWindow mw;
	
	public GameBoy() {
		mem = new Memory();
		cpu = new CPU(mem);
		ppu = new PPU(mem);
		clock = new Clock(cpu, ppu);
		
		deb = new Debugger(mem, cpu, ppu, clock);
		mw = new MainWindow(this);
		clock.setDebugger(deb);
	}
	
	public void loadROM(String file) {
		int[] rom = RomReader.readRom(file);
		mem.writeBytes(rom, 0);
	}
	
}
