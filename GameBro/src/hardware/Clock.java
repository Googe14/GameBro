package hardware;

import ui.Debugger;

public class Clock implements Runnable {
	
	private CPU cpu;
	private PPU ppu;
	private Debugger deb;
	
	public Clock(CPU cpu, PPU ppu) {
		this.cpu = cpu;
		this.ppu = ppu;
	}
	
	public void setDebugger(Debugger deb) { this.deb = deb; }
	
	
	public static final int CPS = 4194304; //Clock cycles per second (4.194304MHz)
	
	public double speed = 1;
	
	public boolean running = false;
	public void loop() {
		double spc = 1/CPS; //Second per cycle
		double last = System.currentTimeMillis(); 
		int cycles = 0; //What cycle
		
		running = true;
		while(running) { //Loop

			double t = System.currentTimeMillis() - last; //Time this second
			int targetCycles = (int)(t*speed/1000 * (double)CPS); //Cycles we should be at by this second

			if(cycles > targetCycles) continue; //Wait until we have cycles to do
			if(t > 1000) { //Reset value to avoid overflow
				cycles %= CPS;
				last = System.currentTimeMillis();
			}

			//************Do a cycle
			if(!cpu.go()) running = false;
			
			//Tell other hardware to go

			if(deb != null) {deb.update();} //Update the debugger (if there is one)
			cycles++;
		}
		
	}
	
	
	//**************************** Run clock loop on new thread
	@Override
	public void run() {
		loop();
	}
	
	private Thread runner;
	public void start() {
		if(running) return;
		runner = new Thread(this);
		runner.start();
	}
	
	public void stop() {
		running = false;
	}

}
