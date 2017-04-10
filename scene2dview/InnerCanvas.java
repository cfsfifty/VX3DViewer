package scene2dview;

/*
 * Created on 21.06.2004
 *
 */

/** 
 *
 * In dieser Klasse wird die innere Detailansicht des 2D Szenegraphen implementiert.
 * 
 * Sie zeichnet den ausgewählten Knoten als "Node", sobald dieser übergeben wird
 * mittels der Methode "setSelectedNode()". Dieser bekommt ggf. eine Linie nach
 * oben, falls er nich selber der root Knoten ist.
 * Dann werden die Kinder dieses Knoten ermittelt, und einem "JViewport" hinzugefügt.
 * Es werden dynamische Linien zwischen dem Vater und seinen Kindern gezeichnet.
 * Es werden mittels "ScrollButton" Scrollknöpfe für nach links / nach rechts
 * definiert. Als nächsten werden im nun scrollbaren JViewport ggf. unter die
 * Kindknoten "InnerTriangle"s plaziert, die Anzahl der hier noch befindlichen
 * Knoten + max. Tiefe  visualisieren.
 * Schließlich wird der ausgewählte Knoten auf vorhandene Routes und korrespondierende
 * DEF/USEs untersucht. Je nach vorhandensein und Lage der Referenzierungen werden
 * "LinienizerEdge"s beim "Linienizer" mit den richtigen Aufpunkten und Koordinaten 
 * angemeldet.  
 *
 * @author fs, ph, hp, bb 
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import scenejtree.SceneRoute;
import scenejtree.SceneTreeNode;

public class InnerCanvas extends JPanel implements MouseWheelListener, ChangeListener{
	/**
	 * ausgewählter Knoten, statisch für Referenzierungen
	 */
	public  static SceneTreeNode selectedNode;
	/*
	 * Gui-Elemente
	 */
	private JLabel selectedDefName;
	private Node selectedNodeComponent;
	private Node[] childNodeComponents;
	private JViewport childrenViewport;
	private ScrollButton scrollLeft;
	private ScrollButton scrollRight;	
	private JSlider scrollSlider;
	public JPanel childrenPanel;
	private InnerTriangle[] childTriangleComponents;
	/*
	 * Gui-Elemente Ende
	 */
	
	/**
	 * Anzahl Kinder links nicht sichtbar
	 */
	private int childrenLeftNotVisible;
	/**
	 * Anzahl Kinder rechts nicht sichtbar
	 */
	private int childrenRightNotVisible;
	/**
	 * Anzahl der in eine Ansicht passenden Kinder, Startwert 1
	 */
	private int maxChildrenVisible = 1;
	
    /**
     *  Breite der ScrollLabels
     */
	public static final int SCROLL_X = 34;
	/**
	 *  Höhe der ScrollLabels
	 */
	public static final int SCROLL_Y = 34;
	/**
	 * Einstellung, ab wieviel Kindern der JSlider eingeblendet werden soll
	 */
	private static final int NUMBER_CHILDREN_TO_SHOW_SLIDER = 10;
	/**
	 *  wird gesetzt, sobald der Slider vom Code verändert wird, z.B. in Minimum und Maximum,
	 *  damit während des Setups die Events nicht verarbeitet werden.
	 */
	private boolean lockSlider;
	/**
	 *  Speicher für eine evtl. vorhandene reflexive ROUTE, die dann gezeichnet werden soll
	 */
	private SceneRoute reflexiveRoute = null;
	/**
	 * Referenz auf outer, wird vom Konstruktor erfasst.
	 */
	private OuterCanvas outer;
		
						 								
	/**
	 * Der Konstruktor erfasst zunächst eine Referenz auf OuterCanvas.
	 * Dann wird das Layout definiert, die immer Vorhandenen Componenten hinzugefügt
	 * und Maus-Ereignis Listener registriert.
	 * 
	 * @param outer äußere Ansicht, die diese innere Ansicht enthält
	 */
	public InnerCanvas(OuterCanvas outer){
		// layout vom Panel setzen
		super(true);
		this.outer = outer;
		this.setBackground(Color.white);		
		this.setSize(new Dimension(350,300));
		this.setLayout(null);
		
		// container für kindknoten aufbauen
		childrenPanel = new JPanel();			
		childrenViewport = new JViewport();
		childrenViewport.setView(childrenPanel);		
		childrenViewport.setSize(271, 2* Node.Y + 2 * Node.BORDERWIDTH);
		childrenViewport.setLocation((this.getWidth() - childrenViewport.getWidth())/2,100);
		childrenViewport.setBackground(Color.WHITE);
		childrenPanel.setLayout(null);
		childrenPanel.setBackground(Color.WHITE);
		this.add(childrenViewport);	
		
		// beschriftung vom selected node ermöglichen, zunächst aber unsichtbar
		selectedDefName = new JLabel();
		selectedDefName.setVisible(false);
		this.add(selectedDefName);
		
        // scrollknöpfe neben den viewport platzieren
		scrollLeft = new ScrollButton(true);         
        scrollRight = new ScrollButton(false);                 
        scrollLeft.setBounds(childrenViewport.getX() - 34,childrenViewport.getY(), SCROLL_X, SCROLL_Y);
        scrollRight.setBounds(childrenViewport.getX() + childrenViewport.getWidth(),childrenViewport.getY(), SCROLL_X, SCROLL_Y);	        
        this.add(scrollLeft);
		this.add(scrollRight);
		
        // maus ereignisse für rechts / links scrolling definieren, synchroniziert über jslider
        scrollLeft.addMouseListener(new MouseAdapter() {
        	public void mousePressed (MouseEvent e) {
        		if (childrenLeftNotVisible >0) {	        			        		            			
        			scrollSlider.setValue(scrollSlider.getValue() -1);
        		}
        	}
        });        
        scrollRight.addMouseListener(new MouseAdapter() {
        	public void mousePressed (MouseEvent e) {
        		if (childrenRightNotVisible >0) {
        			scrollSlider.setValue(scrollSlider.getValue() +1);
        		}
        	}
        });
        
        // scrolling über mausrad ermöglichen
        this.addMouseWheelListener(this);
        
        // slider definieren, dieser wird immer zum scrolling verwendet, ob sichtbar
        // oder nicht.
        scrollSlider = new JSlider();
        scrollSlider.setBounds(childrenViewport.getX(),childrenViewport.getY()+childrenViewport.getHeight(), childrenViewport.getWidth(), 29);
        scrollSlider.setMinimum(0);
        scrollSlider.setMaximum(0);
        scrollSlider.setValue(0);
        scrollSlider.addChangeListener(this);
        scrollSlider.addMouseWheelListener(this);
        scrollSlider.setBorder(null);
        scrollSlider.setFocusable(false);        
        scrollSlider.setEnabled(false);
        scrollSlider.setVisible(false);
        scrollSlider.setBackground(Color.white);
        
        this.add(scrollSlider);
	}
	
	/**
	 * Überschrieben aus JComponent, wird von der GUI / SWING aufgerufen,
	 * sobald sich diese Komponente zeichnen soll. Hier finden Rahmen und Linien
	 * ihr zu Hause. 
	 * Des weiteren wird geguckt, wieviele Elemente links / rechts nicht sichbar sind
	 * im JViewport. Sollte vom/zum selectedNode eine reflexive Route existieren, wird
	 * diese gezeichnet.
	 */
	public void paintComponent(Graphics g) { 						
		super.paintComponent(g);             
		Graphics2D g2 = (Graphics2D)g;
		
		/* Antialiasing aktivieren, um allen gezeichneten Objekten 
	     * einen geglätteten Rand zu verpassen
		 */
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		// selectedNode "umranden"
		g2.setPaint(new Color(250,200,0));
		final int selectedBorderWidth = 3;
		g2.fillOval(selectedNodeComponent.getX() -selectedBorderWidth, selectedNodeComponent.getY() -selectedBorderWidth, selectedNodeComponent.getWidth() +2*selectedBorderWidth, selectedNodeComponent.getHeight() +2*selectedBorderWidth);													                    
       
		// rechteck zur visualisierung der ränder der komponente
		Rectangle2D r = new Rectangle2D.Double(2, 2, this.getWidth() - 4, this.getHeight() - 4);
		float dash[] = {10.0f, 5.0f}; 
	    g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT, 
	                BasicStroke.JOIN_MITER, 4.0f, dash, 0.0f)); 
		g2.setPaint(Color.red);
		g2.draw(r);
		
		
		// linien aussehen definieren
		final float smallDash[] = {2.0f, 1.0f};
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 4.0f, smallDash, 0.0f)); 

		g2.setPaint(Color.DARK_GRAY);

		// ggf. zeichnen der linie vom selectednode zum top anchor		
		if (selectedNodeComponent != null && !selectedNodeComponent.getTreeNode().isRoot()) {
			g2.drawLine(selectedNodeComponent.getX() + selectedNodeComponent.getWidth() / 2,selectedNodeComponent.getY(), selectedNodeComponent.getX() + selectedNodeComponent.getWidth() / 2 ,0);
		}
					
		// zeichnen der linien zwischen selectedNode und children
		childrenLeftNotVisible = 0;
		childrenRightNotVisible = 0;
		
		if (childNodeComponents != null && childNodeComponents.length != 0) {	        
			int childrenCenterX;
	        
	        Rectangle currentChildrenView = childrenViewport.getViewRect();
			
	        //evtl. Effizieter durch sortierte Collection gestalten, ausserhalb von paint?
	        for (int pos=0; pos < childNodeComponents.length ; pos++) {			
	        	childrenCenterX  = childNodeComponents[pos].getX() + childNodeComponents[pos].getWidth() / 2; 
							 
				if (currentChildrenView.contains(childrenCenterX,0)) {																
					g2.drawLine(selectedNodeComponent.getX() + selectedNodeComponent.getWidth() / 2,selectedNodeComponent.getY()+selectedNodeComponent.getHeight(), childrenViewport.getX() + childrenCenterX - currentChildrenView.x,childrenViewport.getY());										
				}
				else if(childrenCenterX>currentChildrenView.x+currentChildrenView.width) {										
					childrenRightNotVisible = childNodeComponents.length - pos;
					break;
	        	}
				else if(childrenCenterX<currentChildrenView.x) {
					childrenLeftNotVisible = pos + 1;
	        	}							
			}	        
		}
		else {			
			// keine Kindern, also werte zurücksetzen			
			childrenLeftNotVisible = 0;
			childrenRightNotVisible = 0;
		}
		
        // anzeigen der Elementanzahl links und rechts nicht sichtbar
		scrollLeft.setDisplayValue(childrenLeftNotVisible);
        scrollRight.setDisplayValue(childrenRightNotVisible);
		
		if (childrenLeftNotVisible >0) 
        	scrollLeft.setEnabled(true);
        else 
        	scrollLeft.setEnabled(false);

        if (childrenRightNotVisible >0)
        	scrollRight.setEnabled(true);
        else
        	scrollRight.setEnabled(false);
        
        // zeichen einer evtl. vorhandenen reflexiven route
        if (reflexiveRoute != null) {
        	
        	int selectedLeftX = selectedNodeComponent.getX();
        	int selectedRightX = selectedNodeComponent.getX() + selectedNodeComponent.getWidth();
        	int selectedCenterY = selectedNodeComponent.getY() + selectedNodeComponent.getHeight() / 2;        		
        	
        	final int arcWidth = 70; //selectedLeftX / 2  - 5;
        	final int arcHeight = 35; //selectedNodeComponent.getHeight();
        	
        	g2.setPaint(Node.ROUTE_COLOR_HIGHLIGHTED);
        	g2.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_MITER, 4.0f));        	
        	g2.drawArc(selectedLeftX - arcWidth, selectedCenterY- arcHeight/2,arcWidth,arcHeight, 10, 320);        	        	
        	
        	// pfeilspitze
        	final int startX = selectedLeftX - 5;
        	final int startY = selectedCenterY +9;        	
        	g2.drawLine(startX, startY, startX-5, startY -3);
        	g2.drawLine(startX, startY, startX-1, startY +6);
        	
        	// from und to field
        	Font oldFont = g2.getFont();
        	g2.setFont(oldFont.deriveFont(8));
        	g2.setPaint(Color.BLACK);
        	g2.drawString(reflexiveRoute.getFromFieldName(),selectedLeftX-2*arcWidth - 10,selectedCenterY-13);
        	g2.drawString(reflexiveRoute.getToFieldName(),selectedLeftX-2*arcWidth - 10,selectedCenterY+15);
        	g2.setFont(oldFont);        	        	
        }
	}
	/**
	 * Die Wunschgrösse entspricht der festen Start-Grösse.
	 */
	public Dimension getPreferredSize() {
		return this.getSize();	
	}
	/**
	 * Die Minimalgrösse entspricht der festen Start-Grösse.
	 */
	public Dimension getMinimumSize() {		
		return this.getSize();
	}

	/**
	 * Aktualisieren / initialisieren des ausgewählten, anzuzeigendem Knoten.
	 * Um diesen schart sich die ganze 2D Ansicht. Es werden ebenfalls dessen
	 * Kinder im childrenPanel zur Anzeige gebracht.
	 * Sollten vom / zum selected Node Routes und/oder Def-Uses zeigen, werden
	 * LinienizerEdges instanziiert und beim Linienizer zum Zeichnen angemeldet.
	 * 
	 * @param selectedNode wird in der inneren Ansicht visualisiert
	 * @param componentToReceiveMouseEvents wird von oben durchgereicht, erhält beim klicken auf Nodes die Möglichkeit, diesen anzuzeigen
	 */
	public void setSelectedNode(SceneTreeNode selectedNode, MouseListener componentToReceiveMouseEvents) {
		// speichern ausgewählter Datenknoten
		InnerCanvas.selectedNode = selectedNode;
		// alte kanten löschen
		outer.linienizer.resetLines();
		// wenn bereits ein knoten gezeigt wurde, diesen löschen
		if (selectedNodeComponent != null)
			this.remove(selectedNodeComponent);
		
		// wenn der zuletzt gezeigte knoten kinder hatte, diese ebenfalls entfernen
		if (childNodeComponents != null) {
			for (int deletePosition = 0; deletePosition < childNodeComponents.length; deletePosition++) {
				childrenPanel.remove(childNodeComponents[deletePosition]);
				childNodeComponents[deletePosition] = null;
				childrenPanel.remove(childTriangleComponents[deletePosition]);
				childTriangleComponents[deletePosition] = null;
			}
			childrenPanel.removeAll(); // etwas doppelt, momentan für labels
		}
		
		// erzeugen der visualisierung des ausgewählten knotens
		selectedNodeComponent = new Node(selectedNode, componentToReceiveMouseEvents);									
		this.add(selectedNodeComponent);
		selectedNodeComponent.setLocation((this.getWidth() - selectedNodeComponent.getWidth()) / 2, 10);
		
		// label für den ausgewählten knoten anpassen
		selectedDefName.setLocation(selectedNodeComponent.getX() + selectedNodeComponent.getWidth() + 10, selectedNodeComponent.getY() + 10);
		selectedDefName.setSize(160, 15);
		selectedDefName.setFont(new Font("Arial",Font.BOLD,12));
		selectedDefName.setText(selectedNode.getSceneNodeDEF());
		selectedDefName.setVisible(true);
		
		// kinder des knotens ermitteln
		DefaultMutableTreeNode[] children = SelectedNodeFactory.getChildrenArray(selectedNode);
		
		this.childNodeComponents     = new Node[children.length];
		this.childTriangleComponents = new InnerTriangle[children.length];
		
		Node newChild;
		InnerTriangle newTriangle;
		int maxColumnWidth = 50; // startwert für (grösste)s paltenbreite
		int maxTriangleHeight = 5; //startwert für dreieck höhe
		int maxNodeHeight = Node.Y; //startwert für knoten höhe
		
		// abstand zwischen den spalten
		final int X_PAD = 5;
		
		// für alle kinder durchlaufen
		for (int i = 0; i < children.length; i++) {
			// kindknoten visualisieren
			newChild    = new Node((SceneTreeNode)children[i], componentToReceiveMouseEvents);
			// kinddreieck erzeugen
			newTriangle = new InnerTriangle(((SceneTreeNode)children[i]).getSceneNodesDescendantsCount(),children[i].getDepth()-1);
			// spaltenbreite, dreieckhöhe und knotenhöhe ggf. aktualisieren:
			maxColumnWidth = Math.max(maxColumnWidth, newTriangle.getWidth());
			maxTriangleHeight = Math.max(maxTriangleHeight, newTriangle.getHeight());
			maxNodeHeight = Math.max(maxNodeHeight, newChild.getHeight());			
			// ablegen von knoten und dreieck komponenten
			childNodeComponents[i] = newChild;
			childTriangleComponents[i] = newTriangle;
		}
		// aus spaltenbreite errechnen, wieviele kinder auf einmal sichtbar sein können
		int maxVisible = childrenViewport.getWidth() / (maxColumnWidth); // evtl. noch nich ganz richtig bei wenig oder auf der kippe liegenden spalten
		if (maxVisible == 0)
			maxVisible = 1;
		maxChildrenVisible = maxVisible;
				
		if ((maxColumnWidth + X_PAD) * children.length > childrenViewport.getWidth()) {
			// mehr als in eine anzeige passen, layouting erfolgt für alle elemente:			
			
			for (int i = 0; i < children.length; i++) {
				newChild = childNodeComponents[i];
				newTriangle = childTriangleComponents[i];
				if (((SceneTreeNode)children[i]).isDEF()){
					// kind def namen anzeigen
					InnerChildLabel defLabel = new InnerChildLabel(((SceneTreeNode)children[i]).getSceneNodeDEF());
					defLabel.setBounds((i * (maxColumnWidth + X_PAD)), maxNodeHeight+1,maxColumnWidth,15);
					this.childrenPanel.add(defLabel);
				}
				
				newChild.setLocation((i * (maxColumnWidth + X_PAD))+ (maxColumnWidth - newChild.getWidth()) / 2,0);
				newTriangle.setLocation((i * (maxColumnWidth + X_PAD))+ (maxColumnWidth - newTriangle.getWidth()) / 2, maxNodeHeight);
												
				this.childrenPanel.add(newChild);	
				this.childrenPanel.add(newTriangle);
				
				childrenRightNotVisible = 1; // annahme als startwert
			}			
		}
		else {
			// weniger als in eine anzeige passen, abstände vergrössern			 			
			
			childrenLeftNotVisible = 0;  // es sollten alle 
			childrenRightNotVisible = 0; // sichtbar sein

			// abstand zum auffüllen errechnen
			int pad = 0;
			if (children.length>0) 
				pad = (childrenViewport.getWidth() - ((maxColumnWidth + X_PAD) * children.length)) / children.length;			
						
			// alle kinder / dreiecke durchlaufen und layouten
			for (int i = 0; i < children.length; i++) {												
				newChild = childNodeComponents[i];
				newTriangle = childTriangleComponents[i];
				if (((SceneTreeNode)children[i]).isDEF()){
					//def label für kind anzeigen
					InnerChildLabel defLabel = new InnerChildLabel(((SceneTreeNode)children[i]).getSceneNodeDEF());
					defLabel.setBounds((i * (maxColumnWidth + X_PAD + pad)), maxNodeHeight+1,maxColumnWidth,15);
					this.childrenPanel.add(defLabel);
				}				
				newChild.setLocation((i * (maxColumnWidth + X_PAD + pad ))+ (maxColumnWidth - newChild.getWidth()) / 2,0);
				newTriangle.setLocation((i * (maxColumnWidth + X_PAD + pad ))+ (maxColumnWidth - newTriangle.getWidth()) / 2, maxNodeHeight);
												
				this.childrenPanel.add(newChild);	
				this.childrenPanel.add(newTriangle);				
			}
			
			if (children.length != 0) {
				if (children.length % 2 == 0) {
					// gerade anzahl, also zwischen zweien zentrieren
					Node centerOnNode1 = childNodeComponents[(childNodeComponents.length) / 2 -1];
					Node centerOnNode2 = childNodeComponents[(childNodeComponents.length) / 2];
					int centerX = ((centerOnNode1.getX() + centerOnNode1.getWidth()/2) + (centerOnNode2.getX() + centerOnNode2.getWidth()/2)) / 2;				
					Point newViewPoint = new Point(centerX - childrenViewport.getWidth()/2,0);
					childrenViewport.setViewPosition(newViewPoint);				
				}
				else {
					// ungerade, also auf mitte zentrieren								
					Node centerOnNode = childNodeComponents[(childNodeComponents.length -1) / 2];								
					int centerX = (centerOnNode.getX() + centerOnNode.getWidth()/2);				
					Point newViewPoint = new Point(centerX - childrenViewport.getWidth()/2,0);
					childrenViewport.setViewPosition(newViewPoint);
				}
			}			
		}
		
		childrenPanel.validate(); // ansicht aktualisieren		

		// grösse children panel anpassen
		if (children.length > 0) {
			childrenPanel.setSize(childNodeComponents[children.length - 1].getX() + childNodeComponents[children.length - 1].getWidth() + 2* childrenViewport.getWidth(), maxNodeHeight + maxTriangleHeight);
			childrenPanel.setPreferredSize(childrenPanel.getSize());
			childrenPanel.setMinimumSize(childrenPanel.getSize());
		}			
		else
			childrenPanel.setSize(0,0);

		
		// grösse an children panel anpassen
		childrenViewport.setSize(childrenViewport.getWidth(), childrenPanel.getHeight()); //childrenPanel.getPreferredSize().height
		childrenViewport.validate();		
		scrollSlider.setBounds(childrenViewport.getX(),childrenViewport.getY()+childrenViewport.getHeight(), childrenViewport.getWidth(), 29);		      
        
        // slider aktualisieren
		lockSlider = true;
        scrollSlider.setMinimum(0);                         
        scrollSlider.setMaximum(children.length - maxVisible); // in einer ansicht befindliche anzahl abziehen
        lockSlider = false;
                                               
        if (children.length > NUMBER_CHILDREN_TO_SHOW_SLIDER) {
        	// slider sichtbar machen
        	scrollSlider.setEnabled(true);
        	scrollSlider.setVisible(true);        	
        	this.setSize(this.getWidth(), scrollSlider.getY() +scrollSlider.getHeight() + 5);
        }
        else {
        	// slider verstecken, wird zum scrollen dennoch verwendet
        	scrollSlider.setEnabled(false);
        	scrollSlider.setVisible(false);        	
        	this.setSize(this.getWidth(), childrenViewport.getY() +childrenViewport.getHeight() + 5);
        }
        
        // keine kinder, grösse von inner nochmalig anpassen
        if (children.length == 0)
        	this.setSize(this.getWidth(), selectedNodeComponent.getY() +  selectedNodeComponent.getHeight() + 10);
        
        // ggf. gespeicherte position des sliders wiederherstellen:
        scrollSlider.setValue(selectedNode.getView2DInnerSliderPosition());
        
        // anlegen von Linien für Routes / DEF-USEs
        LinienizerEdge edge = null;
        if (selectedNode.hasCorrespondingDEF()) {        	
        	// kante von diesem use zum def erzeugen
        	edge = buildEdge(selectedNode.getCorrespondingDEF());
        	edge.setPaint(Node.USES_COLOR_HIGHLIGHTED);
        	outer.linienizer.addEdge(edge);
        }
        if (selectedNode.hasCorrespondingUSEs()) {
        	// kanten von diesem def zu seinen uses erzeugen
        	Enumeration uses = selectedNode.correspondingUSEs();
        	while (uses.hasMoreElements()) {
        		edge = buildEdge((SceneTreeNode)uses.nextElement());
        		edge.swap(); // drehen wg. pfeilrichtung
        		edge.setPaint(Node.DEF_COLOR_HIGHLIGHTED);
        		outer.linienizer.addEdge(edge);
        	}
        }
        reflexiveRoute = null; // zurücksetzen einer evtl vorher vorhandenen reflexiven route
        if (selectedNode.hasRoutes()) {
        	// visualisieren der routes
        	Enumeration sceneRoutes = selectedNode.sceneRoutes();
        	while (sceneRoutes.hasMoreElements()) {
        		SceneRoute thisRoute = (SceneRoute)sceneRoutes.nextElement();
        		if (thisRoute.isRouteReflexive()) {
        			//zeichnen einer reflexiven route, evtl. noch überprüfung für mehrere reflexive (Wenns sowas auch noch gibt)
        			this.reflexiveRoute = thisRoute;
        		} else {
        			// normale kante anlegen
        			edge = buildEdge(thisRoute.getOtherNode(selectedNode));
        			edge.setFromLabel(thisRoute.getThisField(selectedNode));
        			edge.setToLabel(thisRoute.getOtherField(selectedNode));
        			if (thisRoute.getDirection(selectedNode) == SceneRoute.DIRECTION_IN)
        				edge.swap(); // drehen wg. pfeilrichtung
        			edge.setPaint(Node.ROUTE_COLOR_HIGHLIGHTED);
        			outer.linienizer.addEdge(edge);
        		}
        	}
        }                
	}

	/**
	 * Erzeugt eine Kante von selectedNode zu dem übergebenen otherNode.
	 * Sollte die Visualisierung von otherNode gewünscht sein, wird dies hier
	 * veranlasst.
	 * Die Pfeilrichtung wird hier zunächst immer von selectedNode zu 
	 * otherNode angenommen. Diese kann später mittels LinienizerEdge.swap()
	 * getauscht werden.
	 * 
	 * @param otherNode Knoten, zu dem eine Kante von selectedNode aus aufgebaut werden soll
	 * @return Kante zum registrieren beim Linienizer für Visualisierung.
	 */
	private LinienizerEdge buildEdge(SceneTreeNode otherNode) {
		if (!selectedNode.isNodeDescendant(otherNode)) { 
			// Knoten liegt nicht im Teilbaum unterhalb vom selectedNode. Also ein in einem 
			// OuterTriangle oder direkt als Node.
			
			JComponent targetComponent = getTriangleOrNodeFor(otherNode);
			if (targetComponent != null && targetComponent instanceof OuterTriangle) {
				// Kante zum OuterTriangle, zusätzlich Knotenvisualierung veranlassen.
				LinienizerEdge edge = new LinienizerEdge(selectedNodeComponent, ((OuterTriangle)targetComponent).addNodeToShow(otherNode));
				edge.addFromParent(this);
				edge.addToParent(targetComponent);
				return edge;												
				
			}
			else if (targetComponent != null && targetComponent instanceof Node) {
				// Kante zum Knoten im Pfad bis zum Root vom selectedNode zeichnen
				LinienizerEdge edge = new LinienizerEdge(selectedNodeComponent, targetComponent);
				edge.addFromParent(this);				
				return edge;												
			}
			else {
				System.err.println ("Zum Knoten " + otherNode + " konnte kein Teilbaum zum anzeigen gefunden werden!");
				return null;
			}
		}
		else {
			// unterhalb von selected node, also Kindknoten oder Knoten für InnerTriangle			
			if (selectedNode.isNodeChild(otherNode)) {
				// direkt zum Kindknoten
				LinienizerEdge edge = new LinienizerEdge(selectedNodeComponent, childNodeComponents[selectedNode.getSceneNodeIndex(otherNode)] );
				edge.addFromParent(this);
				edge.addToParent(this);
				edge.addToParent(this.childrenViewport);
				// Beachten des Viewports sowie dessen Scrollings.
				edge.setToViewport(childrenViewport);
				int listener = getPositionInArray(outer.linienizer, childrenViewport.getChangeListeners());
				if (listener <0)						
					childrenViewport.addChangeListener(outer.linienizer);
				return edge;
			} 
			else {
				// zum InnerTriangle
				// (evtl. noch mininode im innertriangle anzeigen, ohne path einfach so)
				
				// linie zum triangle, dazu ermitteln welches das richtige dafür ist
				
				TreeNode[] selectedPath = selectedNode.getPath();
				TreeNode[] otherPath = otherNode.getPath();				
				// durchlaufen von otherPath via index i, angefangen beim höchsten index
				// vergleiche für jeden index i, ob dieser knoten im selectedPath[] enthalten ist
				// wenn ja, schnittpunkt ermittelt
				int pos = -1; // gefundene Stelle in selectedPath, als startwert nicht gefunden.
				SceneTreeNode intersectingNode = null; // speicher für schnittmenge
				
				for (int i = otherPath.length -1; i >=0; i--) {
					pos = getPositionInArray (otherPath[i], selectedPath);
					if (pos >= 0) {
						//gefunden, erzeuge kannte zu diesem triangle
						LinienizerEdge edge = new LinienizerEdge(selectedNodeComponent, childTriangleComponents[selectedNode.getSceneNodeIndex((SceneTreeNode)otherPath[i+1])] );
						edge.addFromParent(this);
						edge.addToParent(this);
						// viewport und scrolling berücksichtigen
						edge.addToParent(this.childrenViewport);
						edge.setToViewport(childrenViewport);
						int listener = getPositionInArray(outer.linienizer, childrenViewport.getChangeListeners());
						if (listener <0)						
							childrenViewport.addChangeListener(outer.linienizer);
						return edge;
					}
				}
				// wenn man bis hierher kommt, ist ein fehler aufgetreten
				return null;																	
			}
		}		
	}

	/**
	 * Sucht das OuterTriangle in welchem otherNode liegen soll, sofern
	 * es disen Knoten nicht bereits als Node im OuterCanvas (Pfad zum Root) gibt.
	 * 
	 * @param otherNode Knoten, zu dem der Teilbaum oder dessen Visualisierung gesucht werden soll
	 * @return gefundenes OuterTriangle, oder Node, sonst null bei Fehlschlag
	 */
	private JComponent getTriangleOrNodeFor(SceneTreeNode otherNode) {
		// OuterTriangle's und Node's im outer ermitteln
		JComponent outerContent[][] = outer.getContent();
		// durch schnittmengen suche gewünschte position der Visualisierung von
		// otherNode ermitteln
		TreeNode[] selectedPath = selectedNode.getPath();
		TreeNode[] otherPath = otherNode.getPath();				
		// durchlaufen von otherPath via index i, angefangen beim höchsten index
		// vergleiche für jeden index i, ob dieser knoten im selectedPath[] enthalten ist
		// wenn ja, schnittpunkt ermittelt
		int pos = -1; // gefundene Stelle in selectedPath, als startwert nicht gefunden.
		SceneTreeNode intersectingNode = null; // speicher für schnittmenge
		
		for (int i = otherPath.length -1; i >=0; i--) {
			pos = getPositionInArray (otherPath[i], selectedPath);
			if (pos >= 0) {
				//schnittmenge ermittelt!
				intersectingNode = (SceneTreeNode)selectedPath[pos];		
				if (i == otherPath.length -1) {
					// hier liegt eine route direkt zu einem vater, großvater und so weiter vor
					return (Node)outer.getContent()[otherNode.getLevel()][1];
				}
				else {
					// normaler route/def/use
					SceneTreeNode selectedIntersectingParent = intersectingNode;
					SceneTreeNode selectedIntersectingChild = (SceneTreeNode)selectedPath[pos+1];
					SceneTreeNode otherIntersectingChild = (SceneTreeNode)otherPath[i+1];
					// ermitteln, ob triangle links oder rechts liegt
					int relativePosition = selectedIntersectingParent.getSceneNodeIndex(otherIntersectingChild) - selectedIntersectingParent.getSceneNodeIndex(selectedIntersectingChild);
					
					if (relativePosition >0)      // rechtes triangle						
						return (OuterTriangle)outer.getContent()[intersectingNode.getLevel()+1][2];
					else if (relativePosition <0) // linkes triangle
						return (OuterTriangle)outer.getContent()[intersectingNode.getLevel()+1][0];
					else 
						System.err.println("kein triangle gefunden...");
				}				
				break; 
			}
		}		
		// fehler aufgetreten ...
		return null;
	}

	/**
	 * Einfache Suchmethode, findet den Index eines Elementes vom Typ Object
	 * in einem Array von Objects (hab ich in der API nicht gefunden).
	 * 
	 * @param elementToSearchFor gesuchtes Object
	 * @param arrayToSearchIn Array, was Object enthalten soll
	 * @return index oder -1 bei nicht gefunden.
	 */
	private int getPositionInArray(Object elementToSearchFor, Object[] arrayToSearchIn) {		
		for (int i = 0; i<arrayToSearchIn.length; i++) {
			if (arrayToSearchIn[i].equals(elementToSearchFor))
				return i;
		}
		return -1;
	}

	/**
	 * Wird auf dem JViewport und den Scrollknöpfen registriert. 
	 * Beim Scrollen nach oben, ggf. nach links scrollen,
	 * beim Scrollen nach unten, ggf. nach rechts scrollen.
	 * 
	 * @param mouseScrolled Mausrad Event
	 */
	
	public void mouseWheelMoved(MouseWheelEvent mouseScrolled) {
		final int scrollClicks = mouseScrolled.getWheelRotation();
		
		if (scrollClicks < 0) {
			// scroll left um scrollClicks, oder bis zum Minimum    		
			if (childrenLeftNotVisible >0) {				
				scrollSlider.setValue(Math.max(scrollSlider.getValue() + scrollClicks , scrollSlider.getMinimum()));
			}			    								
		}
		else {
			// scroll right um scrollClicks, oder bis zum Maximum
    		if (childrenRightNotVisible >0) {
    			scrollSlider.setValue(Math.min(scrollSlider.getValue() + scrollClicks , scrollSlider.getMaximum()));
    		}
		}		
	}

	/**
	 * Wird vom JSlider bei Änderung aufgerufen. Zentrieren auf mittiges (bei ungerader Anzahl
	 * Elemente) bzw. die beiden mittigen Knoten. Ggf. die geänderte Position im selectedNode
	 * speichern
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {
		// wenn der slider in den grenzen ist, scrollen
		
		if (childNodeComponents.length>0 &&  scrollSlider.getValue() >= 0 && scrollSlider.getValue() <= scrollSlider.getMaximum()) { 			
			if (childrenLeftNotVisible >0 || childrenRightNotVisible >0) {
				if (maxChildrenVisible % 2 == 0) {
					//gerade anzahl, auf zwei elemente zentrieren					
					Node centerOnNode1 = childNodeComponents[scrollSlider.getValue()+maxChildrenVisible/2-1];
					Node centerOnNode2 = childNodeComponents[scrollSlider.getValue()+maxChildrenVisible/2];
					int centerX = ((centerOnNode1.getX() + centerOnNode1.getWidth()/2) + (centerOnNode2.getX() + centerOnNode2.getWidth()/2)) / 2;				
					Point newViewPoint = new Point(centerX - childrenViewport.getWidth()/2,0);
					childrenViewport.setViewPosition(newViewPoint);										
				}
				else {
					//ungerade anzahl, mittig zentrieren
					Node centerOnNode = childNodeComponents[scrollSlider.getValue()+(maxChildrenVisible-1)/2];								
					int centerX = (centerOnNode.getX() + centerOnNode.getWidth()/2);				
					Point newViewPoint = new Point(centerX - childrenViewport.getWidth()/2,0);
					childrenViewport.setViewPosition(newViewPoint);
				}
								
				if (!lockSlider) {
					selectedNode.setView2DInnerSliderPosition(scrollSlider.getValue());
				}
			}
			 // nich schön, bezieht sich eigentlich nur auf die linien, besser machen?
			this.revalidate();						
		}		
	}
}