/*
 * Created on 22.06.2004
 *
 */
package scene2dview;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Dieses JPanel liegt über dem gesammten Zeichenbereich in der 2D-Ansicht.
 * Es wird verwendet, um Verbindung zwischen Knoten mit ROUTEs oder DEF-USEs
 * zu visualisieren. Dazu werden von der entscheidenden Komponente, hier
 * InnerCanvas zu zeichnende LinienizerEdge-Objekte mittels addEdge() registriert.
 * 
 * @author hp, bb, fs
 *
 */
public class Linienizer extends JPanel implements ChangeListener {
	/**
	 * nimmt alle LinienizerEdge's auf, die gezeichnet werden sollen
	 */
	private Vector lines = new Vector();
	
	/**
	 * legt fest, ob bei den linien ggf. vorhandene labels gezeichnet werden sollen.
	 * Kann somit zur übersicht deaktiviert werden. 
	 */
	private boolean drawFromToLabels = true; 
		
	/**
	 * Konstruktor, legt nur das Panel an. Ist dann bereit für Aufnahme von Kanten.
	 */
	public Linienizer() {
		super();		
	}
	
	/**
	 * überschriebenes Paint, veranlasst die Zeichnung aller Linien.
	 */		
	
	public void paintComponent(Graphics g) { 						
		super.paintComponent(g);             
		Graphics2D g2 = (Graphics2D)g;
		
		// Set the rendering quality.
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setStroke(new BasicStroke(2));
		
		if (!lines.isEmpty()) {
			Enumeration allLines = lines.elements();
			LinienizerEdge thisEdge = null;
			while (allLines.hasMoreElements()) {
				thisEdge = (LinienizerEdge) allLines.nextElement();
				// zeichnen der Kante auf diesem Panel,
				// bei Routes mit Labels, wenn drawFromToLabels true ist.			
				thisEdge.drawEdge(g2, drawFromToLabels);
			}
		}		
	}

	/**
	 * Diese methode löscht alle Kanten (wird aufgerufen 
	 * wenn die Ansicht gewechselt wurde) und zeichnet das Panel neu.
	 */
	public void resetLines() {
		lines.removeAllElements();
		this.repaint();
	}
	/**
	 * Mit dieser methode werden Linien registriert zur späteren Visualisierung.
	 * 
	 * @param line zu zeichnende Linie
	 */
	public void addEdge(LinienizerEdge line) {
		this.lines.add(line);
	}

	/**
	 * Wird verwendet, wenn Linien in oder aus einer scrollbaren Komponente
	 * (z.B. JViewport) zeigen. Beim Scrollen wird dann ein Neuzeichnen veranlasst.
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {		
		this.repaint();		
	}

	/**
	 * Stellt ein, ob bei Routes die From- und To-Fields gezeichnet werden sollen.
	 * Bei Änderung wird die Ansicht aktualisiert.
	 * 
	 * @param drawFromToLabels The drawFromToLabels to set.
	 */
	public void setDrawFromToLabels(boolean drawFromToLabels) {
		if (drawFromToLabels != this.drawFromToLabels) {
			this.drawFromToLabels = drawFromToLabels;
			this.repaint();
		}
	}
}

