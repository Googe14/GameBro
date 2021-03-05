package hardware;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class PPU extends JPanel {

	private Memory mem;
	
	public PPU(Memory mem) {
		this.mem = mem;
		
		screen = new BufferedImage(160, 144, BufferedImage.TYPE_INT_RGB);
		this.setMinimumSize(new Dimension(160, 144));
		this.setPreferredSize(new Dimension(160, 144));
	}
	
	@Override
	public void paintComponent(Graphics g) { //Draw current image to JPanel
		g.drawImage(screen, 0, 0, this.getWidth(), this.getHeight(), null);
	}
	
	private BufferedImage screen;
	
}
