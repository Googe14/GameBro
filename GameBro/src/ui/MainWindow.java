package ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import main.GameBoy;

public class MainWindow extends JFrame {

	private GameBoy gb;
	
	public MainWindow(GameBoy gb) {
		this.gb = gb;
		
		this.setTitle("GameBro!");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menubar = new JMenuBar(); //Setup window stuff
		setJMenuBar(menubar);
		
		
		JMenu fileMenu = new JMenu("File");
		menubar.add(fileMenu);
		
		
		JMenu gameMenu = new JMenu("Game"); //Game menu
		menubar.add(gameMenu);
		
		JMenuItem runItem = new JMenuItem("Resume");
		runItem.setAction(new AbstractAction("Resume") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gb.clock.start();
			}
		});
		gameMenu.add(runItem);
		
		JMenuItem pauseItem = new JMenuItem("Pause");
		pauseItem.setAction(new AbstractAction("Pause") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gb.clock.stop();
			}
		});
		gameMenu.add(pauseItem);
		
		
		
		JMenu debugMenu = new JMenu("Debug"); //Debug menu
		menubar.add(debugMenu);
		JMenuItem debugItem = new JMenuItem("Open Debugger");
		debugItem.setAction(new AbstractAction("Open Debugger") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gb.deb.setVisible(true);
			}
		});
		debugMenu.add(debugItem);
		
		
		this.add(gb.ppu);
		this.pack();
		this.setVisible(true);
	}
	
	
}


