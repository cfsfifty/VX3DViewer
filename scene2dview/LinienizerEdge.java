/*
 * Created on 12.07.2004
 *
 */
package scene2dview;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JViewport;

/**
 * @author fs
 *
 * Diese Klasse speichert die für eine zu zeichnede Kante notwendigen Komponenten.
 * Dies sind zum einen die Zielkomponenten, sowie um in das Koordinatensystem vom
 * Linienizer nötigen "Aufpunkt"-Komponenten (inner, triangles o.ä.).
 * Sollte der Start- oder Endpunkt in einem JViewport liegen, wird dieses ebenfalls
 * gespeichert. Für Routes werden die From- / To-Labels aufgenommen.
 * Die Farbe der Kante kann ebenfalls gesetzt werden.
 * 
 * Um die Pfeilrichtung zu beeinflussen, kann die normalerweise von From nach To
 * gehende Linie mittels der Methode swap() gespiegelt werden.
 */
public class LinienizerEdge {
	private boolean swapped = false;
	/**
	 * Startpunkt Komponente der Kante
	 */
	private JComponent fromComponent = null;
	/**
	 * Endpunkt Komponente der Kante
	 */
	private JComponent toComponent = null;
	/**
	 * für Routes: from Label
	 */
	private String fromLabel = "";
	/**
	 * für Routes: to Label
	 */
	private String toLabel = "";
	/**
	 * nötige Aufpunkte für from
	 */
	private Vector fromParents = new Vector();
	/**
	 * nötige Aufpunkte für to
	 */
	private Vector toParents = new Vector();
	/** 
	 * ggf. Viewport für from
	 */
	private JViewport fromViewport = null;
	/**
	 * ggf. Viewport für to
	 */
	private JViewport toViewport = null;
	/**
	 * standartfarbe für kanten
	 */
	private Paint linePaint = Color.BLACK;
	
	/*
	 * variablen für aufpunkt-berechnungen
	 */
	private int sumFromParentX = 0;
	private int sumFromParentY = 0;
	private int sumToParentX = 0;
	private int sumToParentY = 0;

	/**
	 * Konstruktor legt eine Kante von from nach to (wg. Pfeilrichtung) an.
	 * 
	 * @param fromComponent Startkomponente (Knoten o.ä.)
	 * @param toComponent Endkomponente (z.B. Triangle, oder Knoten)
	 */
	public LinienizerEdge(JComponent fromComponent, JComponent toComponent) {
		this.fromComponent = fromComponent;
		this.toComponent = toComponent;
	}
	/**
	 * abspeichern eines from Aufpunktes
	 * 
	 * @param parent Komponente in der from liegt
	 */
	public void addFromParent(JComponent parent) {
		fromParents.add(parent);	
	}
	/**
	 * abspeichern eines to Aufpunktes
	 * 
	 * @param parent Komponente in der to liegt
	 */
	public void addToParent(JComponent parent) {
		toParents.add(parent);	
	}
	/**
	 * Setzen der Kantenfarbe
	 * @param linePaint Farbe oder Stil für Kante
	 */
	public void setPaint(Paint linePaint) {
		this.linePaint = linePaint;
	}
	/**
	 * Für Routes: from Label
	 * @param fromLabel The fromLabel to set.
	 */
	public void setFromLabel(String fromLabel) {
		this.fromLabel = fromLabel;
	}
	/**
	 * Für Routes; to Label
	 * @param toLabel The toLabel to set.
	 */
	public void setToLabel(String toLabel) {
		this.toLabel = toLabel;
	}
	/**
	 * aktualisiert die Aufpunkte der Kante	 
	 */
	private void refreshMovings() {
		int fromParentX = 0;
		int fromParentY = 0;
		int toParentX = 0;
		int toParentY = 0;
		
		JComponent thisComp;
		
		Enumeration allFromParents = fromParents.elements();
		while (allFromParents.hasMoreElements()) {
			// aufaddieren aller from komponenten koordinaten
			thisComp = (JComponent)allFromParents.nextElement();
			fromParentX += thisComp.getX();
			fromParentY += thisComp.getY();			
		}
		
		Enumeration allToParents = toParents.elements();
		while (allToParents.hasMoreElements()) {
			// aufaddieren aller to komponenten koordinaten
			thisComp = (JComponent)allToParents.nextElement();
			toParentX += thisComp.getX();
			toParentY += thisComp.getY();			
		}

		// ermittelte summen abspeichern
		sumFromParentX = fromParentX;
		sumFromParentY = fromParentY;
		sumToParentX   = toParentX;
		sumToParentY   = toParentY;				
	}
	/**
	 * Zeichnet die Visualisierung dieser Kante auf der Zeichenfläche.
	 * 
	 * @param g2 Zeichenfläche des Linienizers
	 * @param drawFromToLabels true, wenn bei Routes Labels gezeichnet werden sollen
	 */
	public void drawEdge(Graphics2D g2, boolean drawFromToLabels) {
		// aufpunkte ermitteln
		refreshMovings();
		int startX=sumFromParentX;
		int startY=sumFromParentY;
		int endX=sumToParentX;
		int endY=sumToParentY;

		// viewports beachten
		if (fromViewport != null) {
			startX -=fromViewport.getViewRect().x;
		}
		if (toViewport != null) {
			endX -=toViewport.getViewRect().x;
		}

		// position von from und to + aufpunkte vergleichen
		if (fromComponent.getX() + startX < toComponent.getX() + endX) {
			// from liegt weiter links, also rechts an from und links an to zeichnen
			startX += fromComponent.getX() + fromComponent.getWidth();
			endX += toComponent.getX();
			// bei innertriangle zentrieren, sonst immer an den rand
			if (fromComponent instanceof InnerTriangle) {
				startX  -= fromComponent.getWidth() /2;
			}
			if (toComponent instanceof InnerTriangle) {
				endX  += toComponent.getWidth() /2;
			}			
		}
		else {
			// to liegt weiter links, also links an from und rechts an to zeichnen
			startX += fromComponent.getX() ;
			endX += toComponent.getX() + toComponent.getWidth();
			// bei innertriangle zentrieren, sonst immer an den rand
			if (fromComponent instanceof InnerTriangle) {
				startX  += fromComponent.getWidth() /2;
			}
			if (toComponent instanceof InnerTriangle) {
				endX  -= toComponent.getWidth() /2;
			}						
		}	
		// bei viewport überprüfen, ob kante voll sichtbar
		if (fromViewport != null) {				
			if (!fromViewport.getViewRect().contains(startX - sumFromParentX + fromViewport.getViewRect().x,0))
				return;
		}
		if (toViewport != null) {				
			if (!toViewport.getViewRect().contains(endX - sumToParentX + toViewport.getViewRect().x,0))
				return;
		}
		
		// kanten höhe immer mittig
		startY += fromComponent.getY() + fromComponent.getHeight() / 2;
		endY += toComponent.getY() + toComponent.getHeight() / 2;

		// pfeilpunkte ermitteln
		Line2D.Double arrowPoints = getArrowPoints(startX, startY, endX, endY);		
		// pfeilspitze zeichnen
		g2.setPaint(linePaint);
		g2.drawLine(startX, startY, endX, endY);
		g2.drawLine((int)arrowPoints.x1, (int)arrowPoints.y1, endX, endY);
		g2.drawLine((int)arrowPoints.x2, (int)arrowPoints.y2, endX, endY);
		
		// wenn bei routes labels gezeichnet werden sollen:
		if (drawFromToLabels) {
			if((fromLabel != null && fromLabel != "") || (toLabel != null && toLabel != "")) {						
				String from = fromLabel;
				String to = toLabel;
				int x1 = startX;
				int y1 = startY;
				int x2 = endX;
				int y2 = endY;
				double x = x2 - x1;
				double y = y2 - y1;
				// länge der kante
				double vectorLength = Math.sqrt(x*x + y*y);				
				// winkel der kante errechnen
				double angle = getAngle(startX, startY, endX, endY);
								
				// label schrift nicht über kopf zeichnen, also um 180 grad weiterdrehen
				if (angle> Math.toRadians(90.0) && angle< Math.toRadians(270.0)) {

					angle+= Math.toRadians(180.0);
					if (angle>Math.toRadians(360.0))
						angle-= Math.toRadians(360.0);
					
					// aufpunkte tauschen
					x1 = endX;
					y1 = endY;
					x2 = startX;
					y2 = startY;
					
					// labels tauschen, da gespiegelt wurde
					String tempFrom = from;
					from = to;
					to = tempFrom;					
				}

				Graphics2D g2clone = (Graphics2D)g2.create();
				// fontfarbe
				g2clone.setPaint(Color.BLACK);
				// fontgrösse
				g2clone.setFont(g2clone.getFont().deriveFont(11.0f));
				
				// auf winkel der kante drehen für "schräge" schrift			
				g2clone.rotate(angle);
				
				// punkte für start und endpunkt der kannte zurück drehen
				AffineTransform at = new AffineTransform();
				at.setToRotation(-angle);
				Point2D start = new Point2D.Double(x1, y1);
				at.transform(start,start);
				Point2D end = new Point2D.Double(x2, y2);
				at.transform(end,end);

				// fontgrössen ermitteln
				FontMetrics fm = g2clone.getFontMetrics();
				int width = 0;
				int height = fm.getHeight();
				boolean tooSmall = false;
				
				if (fm.stringWidth(from) + fm.stringWidth(to) + 20 > vectorLength)
					tooSmall = true; // zuwenig platz an der kante auf einer seite
				
				if (from != null && from != "") {
					 width = fm.stringWidth(from);
					 if (!tooSmall) {
					 	// zeichen oben "links"
					 	g2clone.drawString(from,(int)start.getX() + 10,(int)start.getY() - 10);
					 }
					 else {
					 	// zeichnen unten "links"
					 	g2clone.drawString(from,(int)start.getX() +10,(int)start.getY() + 10);
					 }
				}
				if (to != null && to != "") {
					width = fm.stringWidth(to);
					if (!tooSmall) {
						// zeichnen oben "rechts"
						g2clone.drawString(to,(int)end.getX() - 10 - width,(int)end.getY() - 10);	
					}
					else {
						// zeichen oben rechts
						g2clone.drawString(to,(int)end.getX() - 10 - width,(int)end.getY() - 3);
					}
					
				}
			}
		}
		
	}
	/**
	 * Berechnet zu einer gegeben Linie die Eckpunkte einer Linie, die mit
	 * dem Linienende verbunden eine Pfeilspitze ergeben.
	 * 
	 * @param x1 startX
	 * @param y1 startY
	 * @param x2 endX
	 * @param y2 endY
	 * @return Linie über die Eckpunkte für Pfeilspitze
	 */
	private Line2D.Double getArrowPoints(int x1, int y1, int x2, int y2)	{
		final int arrowLength = 8;
		final int arrowWidth = 5;
		
		double x = x2 - x1;
		double y = y2 - y1;
		double vectorLength = Math.sqrt(x*x + y*y);
	
		// Der gekürtzte Vektor zum Anfangspunkt der Pfeilspitze (ohne Offset(x1, y1))
		x = x*(vectorLength - arrowLength)/vectorLength;
		y = y*(vectorLength - arrowLength)/vectorLength;
	
		double p1;
		double p2;	
		// vektorberechnung hier:
		if (y != 0)	{
			p1 = 1;
			p2 = (-p1 * x)/y;
		}
		else {
			p2 = 1;
			p1 = (-p2 * y)/x;
		}
		vectorLength = arrowWidth / (float) Math.sqrt(p2*p2 + p1*p1);
		p1 = p1 * vectorLength;
		p2 = p2 * vectorLength;
	
	
		return new Line2D.Double(x1+x+p1, y1+y+p2, x1+x-p1, y1+y-p2);
	}
	/**
	 * Berechnet den Winkel zwischen der Geraden durch die Punnkte
	 * einer Linie und der x-Achse.
	 * 
	 * @param x1 startX
	 * @param y1 startY
	 * @param x2 endX
	 * @param y2 endY
	 * @return Winkel zur x-Achse.
	 */
	private double getAngle(int x1, int y1, int x2, int y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		if (y1 < y2)
			return Math.acos(x / Math.sqrt(x * x + y * y));
		else
			return 2 * Math.PI - Math.acos(x / Math.sqrt(x * x + y * y));
	}	
	
	/**
	 * Tauschen von Anfang und Ende für Routes + Uses wg. pfeilrichtung
	 */
	public void swap() {
		JComponent tempFrom = fromComponent;
		fromComponent = toComponent;
		toComponent = tempFrom;
		
		String tempFromLabel = fromLabel;
		fromLabel = toLabel;
		toLabel = tempFromLabel;
		
		Vector tempFromParents = fromParents;
		fromParents = toParents;
		toParents = tempFromParents;
		
		JViewport tempFromViewport = fromViewport;
		fromViewport = toViewport;
		toViewport = tempFromViewport;
		
		swapped = true;
	}
	/**
	 * Speichert den Viewport, in dem sich der Endpunkt befindet.
	 * @param viewport
	 */
	public void setToViewport(JViewport viewport) {
		this.toViewport = viewport;
		
	}
	/**
	 * Speichert den Viewport, in dem sich der Anfangspunkt befindet.
	 * @param viewport
	 */
	public void addFromViewport(JViewport viewport) {
		this.fromViewport = viewport;
		
	}	
}
