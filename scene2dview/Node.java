package scene2dview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.*;

import javax.swing.JComponent;

import scenejtree.SceneTreeNode;

/**
 * Diese Klasse dient zur grafischen Darstellung der Nodes in der 2D Ansicht
 * und leitet JComponent ab. Dies bietet eine einfache Nutzung von Events 
 * und anderen nützlichen Dingen.
 * 
 * @author Frederik Suhr, Patrick Helmholz 
 */

public class Node extends JComponent implements MouseListener {
	
	/**
	 * zugrunde liegender Datenknoten.
	 */
	private SceneTreeNode treeNode;
	/**
	 * Breite des Rahmens vom Node
	 */
	public static final int BORDERWIDTH = 2;
	/**
	 * Breite des Nodes
	 */
	public static final int X = 32;
	/**
	 * Höhe des Nodes
	 */
	public static final int Y = 32;
		
	/**
	 *  Grösse für Preferred und Minimum
	 */
	private static final Dimension NODE_PREFERRED_SIZE = new Dimension(X + BORDERWIDTH + 1,Y + BORDERWIDTH + 1);
	
	/**
	 * true, wenn Mauscursor über der Komponente ist (wird vom MouseMotionListener ermittelt) 
	 */
	private boolean isMouseOver = false;
	
	// Farben speziell für Ringe der Nodes (und Linien)
	public static final Color ROUTE_COLOR = new Color(200,0,0);   
	public static final Color USES_COLOR = new Color(0,0,255);     
	public static final Color DEF_COLOR = new Color(100,100,255); 
	public static final Color NORMAL_COLOR = Color.black;
	
	// Farben, bei mouseover (aufgehellt)
	public static final Color ROUTE_COLOR_HIGHLIGHTED = new Color(255,180,180);
	public static final Color USES_COLOR_HIGHLIGHTED = new Color(150,150,255);
	public static final Color DEF_COLOR_HIGHLIGHTED = new Color(200,200,255);
	public static final Color COLOR_HIGHLIGHTED = new Color(180,180,180);

	/**
	 *  normale Strichart
	 */
	private final BasicStroke BORDERSTROKE = new BasicStroke(BORDERWIDTH); 	

	/**
	 *  Breite für gestrichelte Linien, "Strich und nicht Strich"
	 */
	private static final float DASH[] = {6, 6};

	/**
	 *  gestrichelte Linien
	 */
	public static final BasicStroke DASHED_STROKE = new BasicStroke( BORDERWIDTH,
	  BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
	  1, DASH, 0 );
	
	/**
	 *  gestrichelte Linien, um Strichlänge verschoben
	 */
	public static final BasicStroke DASHED_STROKE_2 = new BasicStroke( BORDERWIDTH,
			  BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			  1,DASH, 6 );	
	
	/**
	 * Konstruktor der Klasse Node
	 * Erstellt eine aus JComponent abgeleitete Node mit einer 
	 * festen Größe und ToolTip und wenn gewünscht einem MouseListener bei Klick
	 * auf den Knoten.
	 * 
	 * @param treeNode die jeweilige SceneTreeNode
	 * @param componentToReceiveMouseEvents der MouseListener
	 */
	public Node(SceneTreeNode treeNode, MouseListener componentToReceiveMouseEvents) {		
		super();
				
		this.treeNode = treeNode;
		// aussehen und tooltip setzen
		this.setOpaque(false);		
		this.setToolTipText("<html>&nbsp;"+treeNode.toString()+"&nbsp;<br>&nbsp;Level: " +treeNode.getLevel()+ "&nbsp;</html>");
		this.setSize(NODE_PREFERRED_SIZE);
		
		// ggf. MouseListener registrieren
		if (componentToReceiveMouseEvents != null)
			this.addMouseListener(componentToReceiveMouseEvents);
		this.addMouseListener(this);
	}
	
	/**
	 * Konstruktor der Klasse Node für kleinere Nodes 
	 * 
	 * @param treeNode die jeweilige SceneTreeNode
	 * @param componentToReceiveMouseEvents der MouseListener
	 * @param size die Größe des Nodes
	 */
	public Node(SceneTreeNode treeNode, MouseListener componentToReceiveMouseEvents, Dimension size) {
		super();
		
		this.treeNode = treeNode;
		// aussehen
		this.setOpaque(false);
		
		// Typ und evtl Name des Nodes als Tooltip
		this.setToolTipText("<html>&nbsp;"+treeNode.toString()+"&nbsp;<br>&nbsp;Level: " +treeNode.getLevel()+ "&nbsp;</html>");
		this.setSize(size);
		
		if (componentToReceiveMouseEvents != null)
			this.addMouseListener(componentToReceiveMouseEvents);
		
		this.addMouseListener(this);
	}
	
	/**
	 * Diese Methode gibt für die jeweilige Node
	 * die SceneTreeNode zurück (Datengrundlage für Visualisierung).
	 * 
	 * @return treeNode: Returns the treeNode.
	 */
	public SceneTreeNode getTreeNode() {
		return treeNode;
	}
	
	/**
	 * Diese Methode zeichnet in die jeweilige NodeComponent
	 * einen weißen Kreis mit (bei "normalen" Nodes schwarzen) Rand 
	 * sowie das jeweilige NodeIcon
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
				
		// ermitteln der aktuellen, tatsächlichen Grösse der Component
		final int height = this.getSize().height;
		final int width = this.getSize().width;
		
		Ellipse2D circle;
		
		if (this.getSize().equals(NODE_PREFERRED_SIZE)) {
			// Kreis für die Darstellung der Nodes				
			circle = new Ellipse2D.Double((width - X)/2, (height - Y)/2 , X, Y);
		}
		else {
			// Kreis für die Darstellung der Nodes	
			circle = new Ellipse2D.Double(BORDERWIDTH, BORDERWIDTH, this.getWidth() - 2*BORDERWIDTH, this.getHeight() - 2*BORDERWIDTH);
		}
		g2.setStroke(BORDERSTROKE);
		g2.setPaint(Color.white);
		g2.fill(circle);
										
		// Farben nach mouseover oder nicht
		final Color route;
		final Color uses;
		final Color def;
		final Color normal;		
		// bei mouseover
		if (isMouseOver) {
			route = ROUTE_COLOR_HIGHLIGHTED;
			uses = USES_COLOR_HIGHLIGHTED;
			def = DEF_COLOR_HIGHLIGHTED;
			normal = COLOR_HIGHLIGHTED;		
		}
		// ... sonst
		else {
			route = ROUTE_COLOR;
			uses = USES_COLOR;
			def = DEF_COLOR;
			normal = NORMAL_COLOR;
		}
		
		// zeichnen je nachdem, ob dieser Node besondere Eigenschaften hat
		
		// hat routes, und ggf. noch uses oder def
		if (treeNode.hasRoutes()) {
			if (treeNode.hasCorrespondingUSEs()) {
				g2.setStroke(DASHED_STROKE);
				g2.setPaint(route);
				g2.draw(circle);
				
				g2.setStroke(DASHED_STROKE_2);
				g2.setPaint(uses);
			}
			else if (treeNode.hasCorrespondingDEF()) {
				g2.setStroke(DASHED_STROKE);
				g2.setPaint(route);
				g2.draw(circle);
				
				g2.setStroke(DASHED_STROKE_2);
				g2.setPaint(def);
			}
			else
				g2.setPaint(route);
			
		}		
		// Zeichnen des Randes bei DEFS mit Use in dunkelblau ...
		else if (treeNode.hasCorrespondingUSEs()) {
			g2.setPaint(uses);			
		}
		// ...  bei USEs in hellblau ...
		else if (treeNode.hasCorrespondingDEF()) {
			g2.setPaint(def);
		}
		
		// ... sonst schwarz
		else {
			g2.setPaint(normal);

		}
		
		// den Kreis zeichnen
		g2.draw(circle);
					
		// das Icon des jeweiligen Nodetyps wird in den Kreis gezeichnet, wenn es ein 
		// normaler Node ist
		if(height >= 32 && width >= 32) {
			final int iconPosX = (width - treeNode.getElementIcon().getIconWidth()) / 2;
			final int iconPosY = (height - treeNode.getElementIcon().getIconHeight()) / 2;
			g2.drawImage(treeNode.getElementIcon().getImage(),iconPosX,iconPosY, null);
		}
	}
    
	/**
	 * Diese Methode gibt die bevorzugte Größe zurück
	 * 
	 * @return preferredSize: bevorzugte Größe
	 */
	public Dimension getPreferredSize() {
		return NODE_PREFERRED_SIZE;
	}
	
	/**
	 * Diese Methode gibt die minimale Größe der Node zurück
	 * 
	 * @return minimumSize: minimale Größe (entpricht der bevorzugten Größe)
	 */
	public Dimension getMinimumSize() {
		return NODE_PREFERRED_SIZE;
	}

	/**
	 * Wurde für MouseListener leer implementiert.
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {		
	}

	/**
	 * Wenn Maus über einem Node ist, wird neu gezeichnet.
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		isMouseOver = true; 
		this.repaint();
	}

	/**
	 * Wenn Maus einen Node verlässt, wird neu gezeichnet.
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		isMouseOver = false;
		this.repaint();				
	}

	/**
	 * Wurde für MouseListener leer implementiert.
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {		
	}

	/**
	 * Wurde für MouseListener leer implementiert.
	 *  
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
	
	}
	/**
	 * Überschriebenes equals, vergleicht die zugrunde liegenden Datenknoten
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Node) 
			return this.treeNode.equals(((Node)obj).treeNode);
		else
			return false;
	}	
}
