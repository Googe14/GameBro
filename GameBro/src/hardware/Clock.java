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
	
	public boolean running = false; //Exit condition for loop
	public void loop() {
		double last = System.currentTimeMillis(); 
		int cycles = 0; //Cycle the clock is currently up to
		
		running = true;
		while(running) { //Loop

			double t = System.currentTimeMillis() - last; //Time this second
			int targetCycles = (int)(t*speed/1000 * (double)CPS); //Cycles we should be at by this time

			if(cycles > targetCycles) continue; //Wait until we have cycles to do
			if(t > 1000) { //Reset values to avoid overflow
				if(cycles > CPS) System.out.println("More cycles than intended: " + cycles);
				cycles %= CPS;
				last = System.currentTimeMillis();
			}

			//************Do a cycle
			if(!cpu.go()) running = false; //Step the CPU, if it encounters a problem then stop the clock
			
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
