package gui;

import java.awt.Container;

import javax.swing.*;

/**
 * Die Klasse erzeugt ein Legend Fenster, in dem die Legenden für den
 * JTree und die 2D Ansicht in einem JTabbedPane untergebracht sind
 * 
 * @author Patrick Helmholz
 */
public class LegendViewer extends JFrame {
	
	private JTabbedPane pane;
	// Größe des Frames
	private final int WIDTH = 325;
	private final int HEIGHT = 450;
	
	/**
	 * Konstruktor von LegendViewer
	 * Erzeugt ein neues Fenster mit einem JTabbedPane
	 * 
	 * @param x_center x Koordinate der centered Position
	 * @param y_center y Koordinate der centered Position
	 */
	public LegendViewer(int x_center, int y_center) {
		this.setTitle("Legend");
		this.setSize(WIDTH, HEIGHT);
		this.setResizable(false);
		super.setIconImage(new ImageIcon("./icons/ViewerMiniIcon.gif").getImage());
		
		// TabbedPane mit den Tabs "Tree View" und "2D View"
		pane = new JTabbedPane();
		pane.add("Tree View", new LegendJTree());
		pane.add("2D View", new Legend2DView());
		// 2D View als ausgewählt setzen
		pane.setSelectedIndex(1);
		
		Container content = this.getContentPane();
		content.add(pane);
		// Position der Legende		
		this.setLocation(x_center - this.getWidth()/2, y_center - this.getHeight()/2);
		this.setVisible(true);		
	}
}
