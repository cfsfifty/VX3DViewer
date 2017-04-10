package scene2dview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;

import javax.swing.JComponent;

/**
 * Die Klasse InnerTriangle erzeugt ein aus JComponent abgeleitetes 
 * Dreiecksobjekt, zur groben Visualisierung eines Teilbaums mit 
 * einer von der Anzahl der Nodes und Tiefe des Teilbaums abhängenden
 * Größe
 */

public class InnerTriangle extends JComponent {
	
	// Größe des Triangles 
	private Dimension triangleDimension;
	// Länge der Linie nach oben (zum Knoten)
	private final int LINE_LENGTH = 20;
	// Anzahl der Nodes 
	private int nodeCount;
	// Maximalbreite der Triangles, wenn selected nur ein Kind hat
	private final int MAX_WIDTH_1 = 200;
	// Maximalbreite der Triangles, wenn selected mehrere Kinder hat
	private final int MAX_WIDTH_2 = 60;
	
	/**
	 * Konstruktor der Klasse Triangle
	 * Erstellt eine aus JComponent abgeleitete Triangle mit einer 
	 * festen Größe und ToolTip
	 * 
	 * @param nodeCount die Anzahl der Knoten des Triangles 
	 * @param treeDepth die Tiefe des Triangles
	 */
	public InnerTriangle(int nodeCount, int treeDepth) {
		this.setOpaque(false);
		this.setToolTipText("Nodes: " +nodeCount+ " TreeDepth: " +treeDepth);
		this.nodeCount = nodeCount;
		this.setSize(calculateTriangleSize(nodeCount, treeDepth));
		this.setPreferredSize(calculateTriangleSize(nodeCount, treeDepth));
		if (treeDepth == 0) {
			this.setVisible(false);
		}
	}

	/**
	 * Errechnen der Dreickgrösse. Es gibt zwei Skalierungen.
	 * Einmal eine grössere bei viel Platz im Inner Childpanel,
	 * eine kleinere bei wenig Platz.
	 * 
	 * @param nodeCount Anzahl der Knoten in diesem Restbaum.
	 * @param treeDepth Grösste Tiefe in diesem Restbaum.
	 * @return Grösse des Restbaumes.
	 */
	private Dimension calculateTriangleSize(int nodeCount, int treeDepth) {
			triangleDimension = new Dimension(40 + (nodeCount/10), 30 + LINE_LENGTH + (treeDepth*5));
		// wenn Triangle breiter als maxWidth wäre, wird maxWidth als Breite gesetzt
		if (triangleDimension.getWidth() > MAX_WIDTH_1)
			triangleDimension.setSize(MAX_WIDTH_1, triangleDimension.getHeight());
		// wenn der selected mehrere Kinder hat, ist die maxWidth geringer
		if (InnerCanvas.selectedNode.getChildCount() > 1 && triangleDimension.getWidth() > MAX_WIDTH_2) {
			triangleDimension.setSize(MAX_WIDTH_2, triangleDimension.getHeight());
		}
		return triangleDimension;
	}
	/**
	 * Zeichnen der Triangles mit Anzahl der Nodes
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);             
		Graphics2D g2 = (Graphics2D)g;
		
		/* 
		 * Antialiasing aktivieren, um allen gezeichneten Objekten 
	     * einen geglätteten Rand zu verpassen
		 */
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
				
		// ermitteln der aktuellen, tatsächlichen grösse der Component
		final int height = this.getSize().height;
		final int width = this.getSize().width;
		
		// Rand und Farbe zum Zeichnen setzen
		int strokeWidth = 2;
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setPaint(Color.black);
		
		// Tiangle
		GeneralPath trianglePath = new GeneralPath();
		
		// String mit Anzahl der Nodes
		String nodes = "" + nodeCount;
		int i = nodes.length();
		
		// Anzahl der Nodes schreiben
		g2.drawString(nodes, width/2 - (i+i*strokeWidth), height/2 + LINE_LENGTH);
			
		// Linienaussehen
		final float smallDash[] = {2.0f, 1.0f};
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
	    BasicStroke.JOIN_MITER, 4.0f, smallDash, 0.0f)); 

		g2.setPaint(Color.DARK_GRAY);
		// Linie zeichnen
		g2.drawLine(width/2, 0, width/2, LINE_LENGTH);
			
		// setzen des Triangle-Paths
		trianglePath.moveTo(width/2, LINE_LENGTH + strokeWidth);
		trianglePath.lineTo(width - strokeWidth, height - strokeWidth);
		trianglePath.lineTo(strokeWidth, height - strokeWidth);
		trianglePath.closePath();
		
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setPaint(Color.black);
		// Triangle zeichnen
		g2.draw(trianglePath);
	}
}
