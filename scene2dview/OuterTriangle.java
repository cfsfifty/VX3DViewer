package scene2dview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeNode;

import scenejtree.SceneTreeNode;

/**
 * Diese Klasse dient zur Generierung und Darstellung der Teilbäume im
 * OuterCanvas.
 * 
 * @author Benjamin Budgenhagen
 * @author Frederik Suhr
 * @author Patrick Helmholz
 * @author Henrik Peters
 */
public class OuterTriangle extends JComponent {
	
	//Breite des Rechtecks
	private static int rectangleWidth = 30;
	
	//minimale und maximale Breite des Dreiecks
	private static int triangleMinWidth = 70;
	private static int triangleMaxWidth = 500;
	
	//Linienbreite
	private static int strokeWidth = 2;
	
	//Breite des Dreiecks
	private int triangleWidth = 0;
	
	//Dimension der Mini-Knoten
	private int nodeWidth = 10;
	private int nodeHeight = 10;
	
	private int treeLevel = 0;
	private int sumSiblingNodes = 0;
	private int sumSiblingDescendants = 0;
	private int treeDepth = 0;
	private int maxDescendants = 0;
	
	/*
	 * erhält alle knoten, die per Route, Def oder Use am selectedNode im inner
	 * beteiligt sind als SceneTreeNode's
	 */
	private Vector nodesToShow = new Vector();
	
	//erhält die anzuzeigenden Knoten, nach Ebenen sortiert
	private ArrayList[] innerTree;
	
	//MouseListener
	private MouseListener componentToReceiveMouseEvents;
	
	/**
	 * Konstruktor von OuterTriangle zum Initialisieren der Instanz.
	 * 
	 * @param treeLevel Level, auf dem sich die Instanz im Baum befindet
	 * @param sumSiblingNodes die anzuzeigende Anzahl an Geschwisterknoten
	 * @param sumSiblingDescendants die anzuzeigende Anzahl an Knoten im Baum
	 * @param treeDepth die im Teilbaum liegende Pfadtiefe
	 * @param maxDescendants die max. Anzahl Knoten in einem Teilbaum
	 * @param componentToReceiveMouseEvents der Mouselistener
	 */
	public OuterTriangle(int treeLevel, int sumSiblingNodes,
			int sumSiblingDescendants, int treeDepth,
			int maxDescendants, MouseListener componentToReceiveMouseEvents) {
		this.setOpaque(false);
		//Errechnen und Setzen der Größe der Komponente
		Dimension dim = new Dimension(calculateTriangleSize(sumSiblingNodes,
				sumSiblingDescendants, treeDepth, maxDescendants));
		this.triangleWidth = dim.getSize().width;
		this.setSize(dim);
		this.setPreferredSize(this.getSize());
		/*
		 * Beträgt die im Baum liegende Pfadtiefe 0 und sind als Anzahl der
		 * Geschwister 0 übergeben worden, wird die Komponente auf unsichtbar
		 * gesetzt.
		 */
		if(treeDepth == 0 & sumSiblingNodes == 0) {
			this.setVisible(false);
		}
		
		//Speichern diverser Größen in die Instanzvariablen
		this.treeLevel = treeLevel;
		this.sumSiblingNodes = sumSiblingNodes;
		this.sumSiblingDescendants = sumSiblingDescendants;
		this.treeDepth = treeDepth;
		this.maxDescendants = maxDescendants;
		this.componentToReceiveMouseEvents = componentToReceiveMouseEvents;
		
		//Setzen eines Tooltips
		this.setToolTipText("Tree Depth: " + treeDepth);
	}
	
	/**
	 * paintComponent dient zum Zeichnen der Komponente. Gezeichnet wird in
	 * dieser Methode das Recht-/Dreieck sowie die Pfade innerhalb des
	 * Teilbaums zum Darstellen der Route bzw. DEF/USE-Knoten.
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
				
		//Ermitteln der aktuellen, tatsächlichen Größe der Komponente
		final int height = this.getSize().height;
		final int width = this.getSize().width;
		
		//Rand und Farbe zum Zeichnen setzen
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setPaint(Color.black);
		
		//Erstellen einer Instanz von GenerelPath zum Darstellen der Linien
		GeneralPath trianglePath = new GeneralPath();
		
		/*
		 * Wird als Pfadtiefe etwas anderes als 0 übergeben, wird ein Dreieck
		 * unter dem Rechteck gemalt, ansonsten wird nur das Rechteck gemalt.
		 */
		if(this.treeDepth != 0) {
			trianglePath.moveTo(width/2 + rectangleWidth/2,
					rectangleWidth + strokeWidth);
			trianglePath.lineTo(width/2 - rectangleWidth/2,
					rectangleWidth + strokeWidth);
			trianglePath.lineTo(width/2 - rectangleWidth/2,
					strokeWidth);
			trianglePath.lineTo(width/2 + rectangleWidth/2,
					strokeWidth);
			trianglePath.lineTo(width/2 + rectangleWidth/2,
					rectangleWidth + strokeWidth);
			trianglePath.lineTo(width/2 + this.triangleWidth/2 - strokeWidth,
					height - strokeWidth);
			trianglePath.lineTo(width/2 - this.triangleWidth/2 + strokeWidth,
					height - strokeWidth);
			trianglePath.lineTo(width/2 - rectangleWidth/2,
					rectangleWidth + strokeWidth);
		}
		else {
			trianglePath.moveTo(width/2 + rectangleWidth/2,
					rectangleWidth + strokeWidth);
			trianglePath.lineTo(width/2 - rectangleWidth/2,
					rectangleWidth + strokeWidth);
			trianglePath.lineTo(width/2 - rectangleWidth/2, strokeWidth);
			trianglePath.lineTo(width/2 + rectangleWidth/2, strokeWidth);
			trianglePath.closePath();
		}
		
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setPaint(Color.black);
		//Triangle zeichnen
		g2.draw(trianglePath);
		
		/*
		 * Zeichnen des inneren Baumes (Mini-Nodes) zur Darstellung der Pfade
		 * zu den Route bzw. DEF/USE-Knoten.
		 * 
		 * Level-Ebenen durchlaufen, bei 1 beginnen, weil von dort nach oben
		 * keine Linien mehr gezeichnet werden
		 */
		for(int i = 1; i < innerTree.length; i++) {
			Iterator iterator = innerTree[i].iterator();
			while(iterator.hasNext()) {
				Node node = (Node)iterator.next();
				SceneTreeNode parentTreeNode =
					(SceneTreeNode)node.getTreeNode().getParent();
				Node parentNode = new Node(parentTreeNode,
						componentToReceiveMouseEvents,
						new Dimension(this.nodeWidth, this.nodeHeight));
				int index = innerTree[i-1].indexOf(parentNode);
				parentNode = (Node)innerTree[i-1].get(index);
				g2.setStroke(new BasicStroke(1));
				g2.drawLine(parentNode.getX() + parentNode.getWidth()/2,
						parentNode.getY() + parentNode.getHeight()/2,
						node.getX() + node.getWidth()/2,
						node.getY() + node.getHeight()/2);
			}
		}
	}
	
	/**
	 * Die Methode dient dazu, die JLabel's für die Anzahl der Knoten im
	 * Rechteck und die Anzahl der Knoten im Dreieck anzuzeigen.
	 */
	public void addLabels() {
		/*
		 * Es wird eine Instanz der Klasse JLabel zur Anzeige der
		 * Geschwister-Knoten erstellt, der Text wird mittig ausgerichtet.
		 */
		JLabel nodes = new JLabel("" + this.sumSiblingNodes,
				SwingConstants.CENTER);
		/*
		 * Als Breite für die Instanz von JLabel kann hier this.getWidth();
		 * verwendet werden, da sich das Rechteck immer mittig befindet und die
		 * Zahl ebenfalls mittig ausgerichtet wird.
		 */
		nodes.setSize(new Dimension(this.getWidth(), 25));
		nodes.setPreferredSize(new Dimension(this.getWidth(), 25));
		nodes.setLocation(0, 0);
		this.add(nodes);
		
		/*
		 * Sofern die aktuelle Instanz von OuterTriangle Knoten im Dreieck
		 * enthält, wird wiederum eine Instanz der Klasse JLabel, dieses Mal
		 * zur Anzeige der Knoten im Dreieck, erstellt.
		 */
		if(this.sumSiblingDescendants != 0) {
			JLabel descendants = new JLabel("" + this.sumSiblingDescendants,
					SwingConstants.CENTER);
			/*
			 * Auch hier wird als Breite die Breite von OuterTriangle gewählt,
			 * da auch hier das Objekt mittig ausgerichtet ist.
			 */
			descendants.setSize(new Dimension(this.getWidth(), 25));
			descendants.setPreferredSize(new Dimension(this.getWidth(), 25));
			/*
			 * Als Höhe wird die Höhe von OuterTriangle abzüglich 25 Pixeln
			 * gewählt.
			 */
			descendants.setLocation(0, this.getHeight()-25);
			this.add(descendants);
		}
	}
	
	/**
	 * Die Methode dient der Berechnung der Größe zum Anzeigen von
	 * OuterTriangle. Die Höhe wird nach der im Teilbaum enthaltenen Pfadtiefe
	 * errechnet. Die Breite wird relativ zur maximalen Teilbaum-Breite
	 * errechnet.<p>
	 * Berechnung der Höhe:<br>
	 * Höhe des Rechtecks + 30 (Platz für Label im Dreieck) + Pfadtiefe *
	 * Breite eines Levels (LEVEL_WIDTH aus OuterCanvas)<br>
	 * <i>Sollte die übergebene Pfadtiefe treeDepth == 0 sein und die
	 * übergebenen Geschwisterknoten sumSiblingNodes > 0 sein, wird die Höhe
	 * auf die Rechtecks-Höhe beschränkt.</i><p>
	 * Berechnung der Breite:<br>
	 * max. Breite eines Teilbaums * Anzahl der Knoten im Teilbaum /
	 * max. mögliche Anzahl von Knoten im Teilbaum<br>
	 * <i>Sollte die errechnete Breite unter die in der Klassen-Variable
	 * triangleMinWidth gesetzten Breite fallen, wird die Breite auf
	 * triangleMinWidth gesetzt. Sollte sumSiblingNodes == 0 übergeben werden,
	 * wird die Breite auf 0 gesetzt.</i><p>
	 * 
	 * @param sumSiblingNodes Anzahl der Geschwisterknoten (Rechteck)
	 * @param sumSiblingDescendants Anzahl der Knoten im Teilbaum (Dreieck)
	 * @param treeDepth die noch im Teilbaum liegende Pfadtiefe
	 * @param maxDescendants die maximal mögliche Anzahl von Knoten im Teilbaum
	 * @return die errechnete Größe als Instanz von Dimension
	 */
	public static Dimension calculateTriangleSize(int sumSiblingNodes,
			int sumSiblingDescendants, int treeDepth, int maxDescendants) {
		
		/*
		 * Berechnung der Breite
		 */
		int width = triangleMaxWidth * sumSiblingDescendants / maxDescendants;
		if(width < triangleMinWidth) {
			width = triangleMinWidth;
		}
		if(sumSiblingNodes == 0) {
			width = 0;
		}
		
		/*
		 * Berechnung der Höhe
		 */
		int height = rectangleWidth + 30 + treeDepth *
				(OuterCanvas.LEVEL_WIDTH);
		if(treeDepth == 0 & sumSiblingNodes > 0) {
			height = 30 + strokeWidth * 2;
		}
		
		return new Dimension(width, height);
	
	}
	
	/**
	 * Diese Methode dient dazu, die "Mini-Knoten" zur vereinfachten
	 * Darstellung der Pfade innerhalb der Teilbäume zu den Route- bzw.
	 * DEF/USE-Knoten einzufügen.
	 */
	public void addNodes() {
		//Abstand zwischen den Mini-Knoten (horizontal)
		int abstand = 1;
		//Breite der Mini-Knoten
		int width = this.nodeWidth;
		//max. Anzahl von Mini-Knoten auf einer Ebene
		int maxCountNodesPerLevel = 0;
		
		try {
			/*
			 * Enumeration über die Elemente von nodesToShow (die per
			 * addNodeToShow eingefügten Knoten, welche Verbindungen zu Routes
			 * oder DEF/USE's darstellen
			 */
			Enumeration enum = nodesToShow.elements();
			//this.innerTree wird mit ArrayList's initialisiert
			this.innerTree = new ArrayList[treeDepth+1];
			for(int i = 0; i < innerTree.length; i++) {
				innerTree[i] = new ArrayList();
			}
			//jeder Knoten in nodesToShow wird einzeln abgearbeitet
			while(enum.hasMoreElements()) {
				Node node = (Node)enum.nextElement();
				TreeNode[] pathToRoot = node.getTreeNode().getPath();
				for(int i = treeLevel; i < pathToRoot.length-1; i++) {
					/*
					 * die Knoten auf dem Pfad zum Root werden ggf. hinzugefügt
					 * und maxCountNodesPerLevel wird ggf. hochgezählt
					 */
					Node toAdd = new Node((SceneTreeNode)pathToRoot[i],
							componentToReceiveMouseEvents,
							new Dimension(nodeWidth, nodeHeight));
					if(!innerTree[i-treeLevel].contains(toAdd)) {
						innerTree[i-treeLevel].add(toAdd);
						if(innerTree[i-treeLevel].size() >
								maxCountNodesPerLevel) {
							maxCountNodesPerLevel =
								innerTree[i-treeLevel].size();
						}
					}
				}
				//der eigentliche Knoten wird ggf. hinzugefügt
				if(!innerTree[pathToRoot.length-1-treeLevel].contains(node)) {
					innerTree[pathToRoot.length-1-treeLevel].add(node);
					if(innerTree[pathToRoot.length-1-treeLevel].size() >
							maxCountNodesPerLevel) {
						maxCountNodesPerLevel =
							innerTree[pathToRoot.length-1-treeLevel].size();
					}
				}
				else {
					int index =
						innerTree[pathToRoot.length-1-treeLevel].indexOf(node);
					innerTree[pathToRoot.length-1-treeLevel].set(index, node);
				}
			}
			
			/*
			 * Überschreitet die Anzahl der Mini-Nodes die Breite der
			 * eigentlichen Komponente, wird die Breite neu errechnet und
			 * gesetzt.
			 */
			if((maxCountNodesPerLevel * (width + abstand)) > this.getWidth()) {
				this.setSize((maxCountNodesPerLevel * (width + abstand)),
						this.getHeight());
			}
			
			/*
			 * Die einzelnen Ebenen werden abgearbeitet (iteriert) und an
			 * gegebener Stelle in die Komponente eingefügt.
			 */
			for(int i = 0; i < innerTree.length; i++) {
				ArrayList list = innerTree[i];
				int size = list.size();
				int index = 0;
				Iterator iterator = list.iterator();
				while(iterator.hasNext()) {
					Node node = (Node)iterator.next();
					node.setLocation(this.getWidth()/2 - (size * (width +
							abstand)/2) + (index * (width + abstand)), 15 +
							(strokeWidth * 2) + i * (OuterCanvas.LEVEL_WIDTH));
					this.add(node);
					index++;
				}
			}
			
			//Hinzufügen der Label's für Anzahl der Geschwister u. Knoten
			this.addLabels();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Die Methode dient dem Hinzufügen eines Knotens zur jeweiligen Instanz
	 * von OuterTriangle. Alle hinzugefügten Knoten werden dann
	 * in der Teilbaum-Darstellung als "Mini-Nodes" dargestellt, damit Route-
	 * oder DEF/USE-Beziehungen mit dem aktuellen "Selected Node" angezeigt
	 * werden können.
	 * 
	 * @param nodeToShow Knoten, der hinzugefügt werden soll
	 */
	public Node addNodeToShow(SceneTreeNode nodeToShow) {
		/*
		 * Aus dem übergebenen SceneTreeNode wird eine Instanz der Klasse Node
		 * erzeugt. Ist die neue Instanz nocht nicht in der Klassenvariable
		 * Vector nodesToShow enthalten, wird sie hinzugefügt und
		 * zurückgegeben. Andernfalls wird die im Vector vorhandene Instanz
		 * zurückgegeben.
		 */
		Node newNode = new Node(nodeToShow, componentToReceiveMouseEvents,
				new Dimension(nodeWidth, nodeHeight));
		if(!nodesToShow.contains(newNode)) {
			nodesToShow.add(newNode);
		}
		else {
			int index = nodesToShow.indexOf(newNode);
			newNode = (Node)nodesToShow.elementAt(index);
		}
		return newNode;
	}
}
