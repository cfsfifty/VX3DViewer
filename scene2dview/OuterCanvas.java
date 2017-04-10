/*
 * Created on 21.06.2004
 *
 */

package scene2dview;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeNode;

import misc.ScrollPaneNavigator;
import scenejtree.SceneTreeNode;

/**
 * Diese Klasse ist für die Erzeugung, Positionierung und
 * Darstellung der wesentlichen Inhalte
 * (InnerCanvas, Pfadknoten und OuterTriangles)
 * der 2D-ansicht verantwortlich.
 * 
 * @author SEP
 *
 */

public class OuterCanvas extends JPanel{
	public InnerCanvas inner ;
	
	/*
	 * Variable zum Speichern der einzelnen Teilbaeume und deren Position.
	 */
	public JComponent[][] content;
		
	//ausgewählter knoten
	private SceneTreeNode selectedNode;
	
	//vertikaler abstand zwischen den Knoten
	public static final int ABSTAND=20;
	
	public static final int LEVEL_WIDTH=50;
	
	/**
	 * speicher für alle hinzugefügten componenten, 
	 * wird verwendet, um diese wieder zu löschen
	 * und linien zwischen ihnen zu zeichnen
	 */
	private Vector nodes = new Vector();
	
	/**
	 * Diese zwei Konstanten werden in den Methoden zur Ermittlung der 
	 * Kindzahlen und Restknotenanzahlen links bzw. rechts vom zu
	 * überprüfenden Knoten verwendet.
	 */
	private static final int LEFT_SIDE = 1;
	/**
	 * @see LEFT_SIDE
	 */
	private static final int RIGHT_SIDE = 2;
	
	public Linienizer linienizer;

	private JScrollPane scrollContainer;

	/**
	 * nimmt die instanz vom ScrollPaneNavigator auf, der Mausnavigation
	 * durch gedrückthalten der maustaste im scrollContainer ermöglicht
	 */
	private ScrollPaneNavigator scrollPaneNavigator;
	
	/**
	 * Kontruktor mit Initialisierung
	 * @param linienizer Ist der Linienizer der sich über OuterCanvas befindet
	 * @param scrollContainer Ist der ScrollContainer in dem sich OuterCanvas befindet
	 */
	public OuterCanvas(Linienizer linienizer, JScrollPane scrollContainer){		
		inner = new InnerCanvas(this);
		this.linienizer = linienizer;
		this.scrollContainer = scrollContainer;
		//Layout auf null setzen damit man selber anordnen kann
		this.setLayout(null);
		inner.setVisible(false);
		this.add(inner);
		
		inner.setLocation(200-inner.getWidth()/2+ 16, 0);		
		
		// mausscrolling per drag auf scrollpane und inner, später auch auf outer triangles aktivieren
		scrollPaneNavigator = new ScrollPaneNavigator(scrollContainer, linienizer);
		scrollPaneNavigator.setScrollSpeed(ScrollPaneNavigator.NORMAL_SPEED);
		scrollPaneNavigator.activateNavigation(inner);
	}
		
	/**
	 * Diese Methode baut den Inhalt des OuterCanvas
	 * nach Änderung des selectedNode neu auf.
	 * @param selectedNode Der neue Ausgewählte Knoten
	 * @param componentToReceiveMouseEvents Der MouseListener der
	 * das event ausgelöst hat
	 */
	public void setSelectedNode(SceneTreeNode selectedNode, MouseListener componentToReceiveMouseEvents) {
		this.selectedNode = selectedNode;
		Node newChild;
		
		//alte komponenten aus der anzeige entfernen
		removeComponents();
		
		//wenn der KNoten nicht der wurzelknoten ist
		if (!selectedNode.isRoot()) {
			
			//array für den weg vom root zum selecetedNode
			TreeNode[] pathToRoot = selectedNode.getPath();
			
			//bestimmung der anzahl der knoten des grösstmöglichen teilbaums
			//zur Breitenberechnung der OuterTriangles
			int maxDescendants = ((SceneTreeNode)pathToRoot[1]).getSceneNodesDescendantsCount();
			
			
			/*
			 * Array das die Darzustellen Objekte enthält
			 * mit richtiger tiefe initialisieren
			 * (Länge des Pfades vom Root zum selecetedNode)
			*/
			content = new JComponent[pathToRoot.length][3];
			
			/* weg ablaufen, dabei den letzen knoten weglassen (das ist der selectedNode)
			 * Knoten der reihe nach hinzufügen und positionieren
			 */
			for (int i = 0; i < pathToRoot.length; i++) {
				newChild = new Node((SceneTreeNode)pathToRoot[i], componentToReceiveMouseEvents);				
				nodes.add(newChild);
				
				/*
				 * Linkes und Rechts OuterTriangle erstellen und mit knoten
				 * ablegen 
				 */
				content[i][0] = new OuterTriangle(i,OuterCanvas.sumSiblingNodesToLeft((SceneTreeNode)pathToRoot[i]), OuterCanvas.sumSiblingDescendantsToLeft((SceneTreeNode)pathToRoot[i]), OuterCanvas.maxSiblingDepthToLeft((SceneTreeNode)pathToRoot[i]), maxDescendants, componentToReceiveMouseEvents);
				content[i][1] = newChild;
				content[i][2] = new OuterTriangle(i,OuterCanvas.sumSiblingNodesToRight((SceneTreeNode)pathToRoot[i]), OuterCanvas.sumSiblingDescendantsToRight((SceneTreeNode)pathToRoot[i]), OuterCanvas.maxSiblingDepthToRight((SceneTreeNode)pathToRoot[i]), maxDescendants, componentToReceiveMouseEvents);
					
				// drag navigation auch auf den triangles;
				scrollPaneNavigator.activateNavigation(content[i][0]);
				scrollPaneNavigator.activateNavigation(content[i][1]);												
			}
			
			content[content.length-1][1].setVisible(false);
			
			//Für inner zuständige MEthode aufrufen und selected Node
			//und Mouselistener weitergeben
			inner.setSelectedNode(selectedNode, componentToReceiveMouseEvents);
			
			//Variable zum Speichern der max. Breite innerhalb von OuterCanvas
			int width = 0;
			//Variable zum Speichern der max. Höhe innerhalb von OuterCanvas
			int height = 0;
			
			/*
			 * Linke Spalte durchlaufen und somit Linke Teilbäume
			 * anordnen
			 */
			for(int x = 0; x < content.length; x++) {
				OuterTriangle toAdd = (OuterTriangle)content[x][0];
				//innere Knoten im Teilbaum anordnen und anzeigen
				toAdd.addNodes();
				//den Teilbaum der ansicht hinzufügen...
				this.add(toAdd);
				//und positionieren
				toAdd.setLocation(width, (x) * LEVEL_WIDTH);
				//width um breite des teilbaum erweitern
				width += toAdd.getWidth();
				
				//gegebenenfalls höhe von Outer verändern,
				//falls Bäume tiefer gehen
				if(toAdd.getY() + toAdd.getHeight() > height) {
					height = toAdd.getY() + toAdd.getHeight();
				}
			}
			
			//Inneren bereich der ansicht hinzufügen..
			this.add(inner);
			//und positionieren
			inner.setLocation(width, (content.length-1) * LEVEL_WIDTH);
			
			//gegebenenfalls höhe von Outer verändern,
			//wenn Innere BEreich tiefer liegt
			if(inner.getY() + inner.getHeight() > height) {
				height = inner.getY() + inner.getHeight();
			}
			
			//Position für den Pfad zum root, mittig vom inner
			int widthForRootPath = width + inner.getWidth() / 2 - Node.X/2 - Node.BORDERWIDTH;
			
			/*
			 * Mittlere Spalte des Inhalts-arrays von unten
			 * nach oben durchlaufen
			 */
			for(int x = content.length-2; x >= 0; x--) {
				JComponent toAdd = content[x][1];
				//Komponente der ansicht hinzufügen und
				//positionieren
				this.add(toAdd);
				toAdd.setLocation(widthForRootPath, x * LEVEL_WIDTH);
				
				if(toAdd.getY() + toAdd.getHeight() > height) {
					height = toAdd.getY() + toAdd.getHeight();
				}
			}
			
			//breite des inner addieren
			width += inner.getWidth();
			
			//3. spalte von unten nach oben durchlaufen
			for(int x = content.length-1; x >= 0; x--) {
				OuterTriangle toAdd = (OuterTriangle)content[x][2];
				//innere Knoten im Teilbaum anordnen und anzeigen
				toAdd.addNodes();
				//Komponente der Ansicht hinzufügen und
				//positionieren
				this.add(toAdd);
				toAdd.setLocation(width, (x) * LEVEL_WIDTH);
				width += toAdd.getWidth();
				if(toAdd.getY() + toAdd.getHeight() > height) {
					height = toAdd.getY() + toAdd.getHeight();
				}
			}
			
			
			//Nach Anordnen der Inhalte die grösse neu festlegen
			this.setSize(new Dimension(width, height));
			
		}
		
		//wenn selectedNode der wurzelknoten ist
		else {
			//positionieren, SelectedNode ans inenr reichen
			//und grösse setzen 
			inner.setLocation(0,0);
			inner.setSelectedNode(selectedNode,componentToReceiveMouseEvents );	
			this.setSize(new Dimension(inner.getWidth(), inner.getHeight()));
		}
		
		//Falls inenr nicht sichtbar ist
		if (!inner.isVisible())
			inner.setVisible(true);

	}
	
	/**
	 * @return Returns the content.
	 */
	public JComponent[][] getContent() {
		return content;
	}
	
	/**
	 * Überlagerung der paintComponent Methode zum
	 * Zeichnen der Komponente
	 */	
	public void paintComponent(Graphics g) { 					
		super.paintComponent(g);             
		Graphics2D g2 = (Graphics2D)g;
		
		// Set the rendering quality.
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);		
		
		// linien aussehen (kopiert aus inner)
		final float smallDash[] = {2.0f, 1.0f};
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 4.0f, smallDash, 0.0f)); 
		g2.setPaint(Color.DARK_GRAY);
		
		/*
		 * Wenn es einen selectednode gibt und der nicht der
		 * Wurzelknoten ist, dann alle verbidnungs linien
		 * zeichnen
		 */
		if(selectedNode != null && !selectedNode.isRoot()) {
			for(int i = 1; i <= content.length-1; i++) {
				for(int j = 0; j <= content[0].length-1; j++) {
					if(content[i][j].isVisible()) {
						g2.drawLine(content[i][j].getX() + content[i][j].getWidth()/2, content[i][j].getY(), content[i-1][1].getX() + content[i-1][1].getWidth()/2, content[i-1][1].getY() + content[i-1][1].getHeight()/2);
					}
				}
			}
			g2.drawLine(inner.getX() + inner.getWidth()/2 - 1, inner.getY(), content[content.length-2][1].getX() + content[content.length-2][1].getWidth()/2, content[content.length-2][1].getY() + content[content.length-2][1].getHeight());
		}
		
	}
	
	/**
	 * methode zum ausräumen der "alten" jcomponents
	 *
	 */
	private void removeComponents() {
		if(content != null) {
			for(int i = 0; i < content.length; i++) {
				for(int j = 0; j < content[0].length; j++) {
					this.remove(content[i][j]);
				}
			}
		}
	}
		
	/**
	 * Gibt die Anzahl der direkten Nachbarn vom zu untersuchendem Knoten wieder.
	 * Jeder Knoten ist sein eigener Nachbar. Wenn also dieser Knoten die Wurzel ist,
	 * wird 1 zurckgegeben.
	 *  
	 * @param thisNode zu untersuchender Knoten
	 * @return Anzahl der Nachbarn von thisNode
	 */
	private static int getSiblingNodesCount (SceneTreeNode thisNode) {
		if (thisNode.isRoot()) {
			// root sollte keine nachbarn haben, also nur sich selbst ...
			return 1;
		}
		else {
			// anzahl kinder vom vaterknoten
			return ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildCount();
		}
	}
								
	/**
	 * Diese Methode ermittelt die Anzahl der Kinder des Vaters, die links
	 * von diesem Knoten liegen. Also seine direkten Nachbarn zur Linken.
	 * 
	 * @param thisNode zu untersuchender Knoten
	 * @return anzahl der szenenknoten zur linken
	 */
	private static int sumSiblingNodesToLeft (SceneTreeNode thisNode) {
		if (thisNode.isRoot()) {
			// root sollte keine nachbarn haben ...
			return 0;
		}
		else {
			// 0 basierter index dieses knoten unter den vaterknoten
			// = nachbarn zur linken seite dieses knoten
			return ((SceneTreeNode)thisNode.getParent()).getSceneNodeIndex(thisNode);
		}
	}
	/**
	 * Diese Methode ermittelt die Anzahl der Kinder des Vaters, die rechts
	 * von diesem Knoten liegen. Also seine direkten Nachbarn zur Rechten.
	 * 
	 * @param thisNode zu untersuchender Knoten
	 * @return anzahl der szenenknoten zur linken
	 */
	private static int sumSiblingNodesToRight (SceneTreeNode thisNode) {
		if (thisNode.isRoot()) {
			// root sollte keine nachbarn haben ...
			return 0;
			
		}
		else {
			// anzahl der kind knoten des vaters, abzüglich der knoten zur linken seite, minus 1 für diesen knoten; 			
			// = nachbarn zur rechten seite dieses knoten
			return ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildCount()
					- ((SceneTreeNode)thisNode.getParent()).getSceneNodeIndex(thisNode)
					- 1; 
		}
	}
	
	/**
	 * Ermittelt die Summe aller Kinder der Knoten links von thisNode; 
	 * @param thisNode zu untersuchender Knoten
	 * @return summe über linken restbaume
	 */
	private static int sumSiblingDescendantsToLeft(SceneTreeNode thisNode) {
		int sumDescendants = 0;
		int sumNodesToLeft = sumSiblingNodesToLeft(thisNode);
		if (sumNodesToLeft == 0) {
			// Keine Knoten links von thisNode
			return 0;
		}
		else {
			for (int pos = 0; pos < sumNodesToLeft; pos++) {
				sumDescendants += ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildAt(pos).getSceneNodesDescendantsCount(); 
			}
			return sumDescendants;
		}
		
	}
	/**
	 * Ermittelt die Summe aller Kinder der Knoten rechts von thisNode; 
	 * @param thisNode zu untersuchender Knoten
	 * @return summe über rechten restbaume
	 */
	private static int sumSiblingDescendantsToRight(SceneTreeNode thisNode) {
		int sumDescendants = 0;
		int sumNodesToRight = sumSiblingNodesToRight(thisNode);
		if (sumNodesToRight == 0) {
			// Keine Knoten links von thisNode
			return 0;
		}
		else {
			for (int pos = ((SceneTreeNode)thisNode.getParent()).getSceneNodeIndex(thisNode) + 1; pos < ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildCount(); pos++) {
				sumDescendants += ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildAt(pos).getSceneNodesDescendantsCount(); 
			}
			return sumDescendants;
		}
		
	}
	/**
	 * Ermittelt die grösste SceneNode Tiefe der Knoten links von thisNode; 
	 * @param thisNode zu untersuchender Knoten
	 * @return tiefe des linken restbaum
	 */
	private static int maxSiblingDepthToLeft(SceneTreeNode thisNode) {		
		int maxDepth = 0;
		int sumNodesToLeft = sumSiblingNodesToLeft(thisNode);
		if (sumNodesToLeft == 0) {
			// Keine Knoten links von thisNode
			return 0;
		}
		else {
			for (int pos = 0; pos < sumNodesToLeft; pos++) {
				maxDepth = Math.max(maxDepth, ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildAt(pos).getDepth()-1);
			}
			return maxDepth;
		}		
	}
	/**
	 * Ermittelt die grösste SceneNode Tiefe der Knoten rechts von thisNode; 
	 * @param thisNode zu untersuchender Knoten
	 * @return tiefe des rechten restbaum
	 */
	private static int maxSiblingDepthToRight(SceneTreeNode thisNode) {		
		int maxDepth = 0;
		int sumNodesToRight = sumSiblingNodesToRight(thisNode);
		if (sumNodesToRight == 0) {
			// Keine Knoten links von thisNode
			return 0;
		}
		else {
			for (int pos = ((SceneTreeNode)thisNode.getParent()).getSceneNodeIndex(thisNode) + 1; pos < ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildCount(); pos++) {
				maxDepth = Math.max(maxDepth, ((SceneTreeNode)thisNode.getParent()).getSceneNodeChildAt(pos).getDepth()-1);
			}
			return maxDepth;
		}
	}
}

