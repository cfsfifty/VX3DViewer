/*
 * Created on 24.06.2004
 *
 */
package printer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JTree;
import org.jibble.epsgraphics.*;
import scenejtree.SceneTreeNode;
import java.util.LinkedList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Line2D;

/**
 * Die Klasse Pinterator bereitet einen übergebenen jTree so auf, 
 * daß er als Baumgraph angezeigt und in eine eps-Datei geschrieben werden kann. 
 * Die Positionierung der Knoten wird dafür von der Methode proTree() vorgenommen
 * 
 * @author Timo und Philipp
 * @author Sep-Vrml97 Group
 */
public class Printerator extends JComponent{
	
	public SceneTreeNode node, root;
	public JTree jTree;
	public ScrollPane scroll;
	public EpsGraphics2D eps;
	public Graphics2D g2;
	public static final float NODEHEIGHT = 25, NODEWIDTH = 80, NODEARC = 5;
	public int maxLevel, maxNumber,maxWidth,maxHeight;
	public int[] numberLevel;
	public LinkedList[] nodeTreePosLink;
	public LinkedList[] childrenQueue;
	public LinkedList edgeList;
	public double zoomx = 1.0, zoomy = 1.0;

	/**
	 * Der Konstruktor ruft unter anderem die Methode proTree auf. Danach sind die Positionen der KNoten gesetzt
	 * @param jTree der JTree, der als gesammter Graph angezeigt und als eps abgespeichert werden soll
	 */
	
	public Printerator(JTree jTree){
		super();
		this.jTree = jTree;
		root = (SceneTreeNode)jTree.getModel().getRoot();		
		maxLevel = root.getDepth()-1;
		arrayInit();
		edgeList = new LinkedList();
		// die Methode proTree übernimmt alle Berechnung zur  Positionierung der Knoten eines Baumes
		maxNumber=proTree((SceneTreeNode)jTree.getModel().getRoot());
		//die Maximale Breite des Baumes wird gebraucht, um dem EPS outputstream eine Breite zuteilen zu können
		setHeightWidthZoom();
		//setzt die Größe dieser Komponente
		setSize(maxWidth, maxHeight);
		setPreferredSize(new Dimension (maxWidth, maxHeight));
		setVisible(true);		
	}
	
	/**
	 * Diese Methode setzt den Zoom um 1.5 hoeher
	 * 
	 */
	public void setZoomPlus(){
		zoomx=zoomx*1.5;
		zoomy=zoomy*1.5;
	}
	
	/**
	 * Diese Methode setzt den Zoom um 0.5 niedriger
	 * 
	 */
	public void setZoomMinus(){
		zoomx=zoomx*0.5;
		zoomy=zoomy*0.5;
		
	}

	/**
	 * Diese Methode setzt die Ausgangshoehe und Breite des Baumes
	 * 
	 */
	public void setHeightWidth(){
		maxWidth = 200 + maxNumber*(int)(NODEWIDTH+15);
		maxHeight = 200 + (maxLevel+1)*((int)(NODEHEIGHT)+40);
	}
	
	/**
	 * Diese Methode setzt die neue Hoehe und Briete des Baumes mit Zoom
	 * 
	 */
	public void setHeightWidthZoom(){
		
		maxWidth = 200 + maxNumber*(int)(NODEWIDTH+15);
		double newWidth = (double)maxWidth*zoomx;
		maxWidth = (int)newWidth;
		maxHeight = 200 + (maxLevel+1)*((int)(NODEHEIGHT)+40);
		double newHeight = (double)maxHeight*zoomy;
		maxHeight = (int)newHeight;
		setSize(maxWidth, maxHeight);
		setPreferredSize(new Dimension(maxWidth, maxHeight));
		
	}
	
	public int getWidth(){
		return maxWidth;
		
	}
	public int getHeight(){
		return maxHeight;
		
	}
	/**
	 * Diese Methode initialisiert die benötigten Arrays, damit die fuer die Berechnung des
	 *  Baumes zur Verfügung stehen
	 *
	 */
	
	public void arrayInit(){
		//Dieser Array speichert für jede Ebene die Anzahl der schon belegten Knotenplätze
		numberLevel = new int[maxLevel+1];
		for (int i = 0; i<maxLevel+1; i++){
			numberLevel[i] = 0;
		}
		//jede LinkedList dieses Arrays speichert die horizontale Position der Knoten auf dieser Ebene 
		nodeTreePosLink = new LinkedList[maxLevel+1];
		for (int j = 0; j<maxLevel+1; j++){
			nodeTreePosLink[j] = new LinkedList();
		}
		//dieser Array speichert die Positionen der Kinder eines Knotens, die dann an die Klasse EdgeStore gegeben werden
		childrenQueue = new LinkedList[maxLevel+2];
		for (int j = 0; j<maxLevel+2; j++){
			childrenQueue[j] = new LinkedList();
		}
	}
	
	/**
	 * paintComponent
	 * hier wird die Methode PaintComponent so überschrieben, daß gleich der Graph gezeichnet wird
	 * @param g ist das Graphics Object dieser Komponente, welches wir verändern wollen
	 */


	public void paintComponent(Graphics g){
			g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			//Die SceneTreeNodes werden in der richtigen Reihenfolge in eine Enumeration gespeichert 
			Enumeration breadth = ((SceneTreeNode)jTree.getModel().getRoot()).breadthFirstEnumeration();
			
			SceneTreeNode curNode;
			
			String output;
			int level = 0;
			//dieser Iterator enthällt zuerst die Node Positionen für die erste Ebene
			Iterator levelIt = nodeTreePosLink[0].listIterator(0);
			
			//dieser Iterator anthällt alle Kanten des Graphen
			Iterator edgeIt = edgeList.listIterator(0);
			g2.setColor(Color.BLACK);
			
			g2.scale(zoomx,zoomy);

//			Hier werden die Kannten durchlaufen
			while (edgeIt.hasNext()){
				EdgeStore theEdge = (EdgeStore)edgeIt.next();
				//Hier werden (hoffentlich) die Kannten gezeichnet
				Line2D.Float line = new Line2D.Float(theEdge.parentCoor[0], theEdge.parentCoor[1], theEdge.childCoor[0], theEdge.childCoor[1]);
				g2.draw(line);
			}
			
			//in dieser While Schleife werden die Knoten durchlaufen und an der richtigen Stelle gezeichnet
			while (breadth.hasMoreElements()) {
				curNode = (SceneTreeNode)breadth.nextElement();
				//Attribute werden übersprungen
				if (curNode.isLeaf() || curNode.toString().matches("ROUTE"))
					continue;
				else {
					//Hier werden die verschieden Iterator entsprechend der Ebenen erstellt 
					if (level != curNode.getLevel()){
						level = curNode.getLevel();
						levelIt = nodeTreePosLink[level].listIterator(0);
					}
					
					// Die Position der Nodes wird gesetzt...
					float x = ((Float)levelIt.next()).floatValue(); 
					float xPos = 20 + x*(NODEWIDTH+15);
					float yPos = 20 + level*(NODEHEIGHT+40);
					
					//Unterscheidung: hat die Node einen Def-Namen oder nicht
					if (curNode.isDEF()){
						//das Rechteck ist hier ein wenig höher, als bei den anderen Nodes
						RoundRectangle2D rect = new RoundRectangle2D.Double(xPos,yPos,NODEWIDTH,NODEHEIGHT+15,NODEARC+4,NODEARC+4);
						
						g2.setColor(Color.red);
						g2.draw(rect);
						g2.fill(rect);
						
						//die beiden Strings für den output
						output = getStringUpToFirstBlank(curNode.toString());
						String defName= curNode.getSceneNodeDEF();
						
						//Nun werden beide Linien geschrieben, dabei wird die Fontgröße immer angepasst und hinterher wieder zurückgesetzt
						FontMetrics fm = g2.getFontMetrics();
						
						if (fm.stringWidth(output) > (NODEWIDTH-8)) {
							int outputwidth = fm.stringWidth(output);
							g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D()* (NODEWIDTH-8) / outputwidth));
							fm = getFontMetrics(g2.getFont());	
							g2.setColor(Color.white);
							g2.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos -2 + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
							g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D()* outputwidth/(NODEWIDTH-8)));
							fm = getFontMetrics(g2.getFont());	
						}else {
							g2.setColor(Color.white);
							g2.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos -2 + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
						}
						
						if (fm.stringWidth(defName) > (NODEWIDTH-8)) {
							int outputwidth = fm.stringWidth(defName);
							g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D()* (NODEWIDTH-8) / outputwidth));
							fm = getFontMetrics(g2.getFont());	
							g2.setColor(Color.white);
							g2.drawString(defName,xPos + (NODEWIDTH/2 - fm.stringWidth(defName) /2) ,yPos +14+ (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
							g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D()* outputwidth/(NODEWIDTH-8)));
							fm = getFontMetrics(g2.getFont());	
						}else {
							g2.setColor(Color.white);
							g2.drawString(defName,xPos + (NODEWIDTH/2 - fm.stringWidth(defName) /2) ,yPos +14+ (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
						}
			
						
					}
					
					//Wenn die Node keinen Def-Namen hat
					else{
						output = getStringUpToFirstBlank(curNode.toString());						
						
						RoundRectangle2D rect = new RoundRectangle2D.Double(xPos,yPos,NODEWIDTH,NODEHEIGHT,NODEARC,NODEARC);
						
						g2.setColor(Color.BLUE);
						g2.draw(rect);
						g2.fill(rect);
											
						//graphis2dmetrics
						//auch hier wird die Schriftgröße angepasst, damit der String ins Rechteck passt
						FontMetrics fm = g2.getFontMetrics();	
						if (fm.stringWidth(output) > (NODEWIDTH-8)) {
							int outputwidth = fm.stringWidth(output);
							g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D()* (NODEWIDTH-8) / outputwidth));
							fm = getFontMetrics(g2.getFont());	
							g2.setColor(Color.white);
							g2.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
							g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D()* outputwidth/(NODEWIDTH-8)));
							fm = getFontMetrics(g2.getFont());	
						}else {
							g2.setColor(Color.white);
							g2.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
						}
					}
				} 					
			}
			
			

			
			
	}
	
	
	/**
	 * paintEps ist eine fast direkte Kopie von paintComponent, bis auf die Benutung des EpsGraphics2D Objektes, 
	 * daß dann in den outputstream geschrieben wird. Das Malen des Graphen funktioniert exakt wie in obiger Methode
	 * 
	 * @param file der Pfad der Datei, in die das EPS-Objekt geschrieben wird. 
	 * @param name nur der Name der Datei
	 */
	public void paintEps(String file, String name){
		
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			eps = new EpsGraphics2D(name, outputStream, 0, 0, maxWidth, maxHeight);
			
			Enumeration breadth = root.breadthFirstEnumeration();
			SceneTreeNode curNode;
			
			String output;
			int level = 0, number = 0;
			Iterator levelIt = nodeTreePosLink[0].listIterator(0);
			
			//der Iterator anthällt die Kanten
			Iterator edgeIt = edgeList.listIterator(0);
			eps.setColor(Color.BLACK);
			
			eps.scale(zoomx,zoomy);
						
//			Hier werden (hoffentlich) die Kannten gezeichnet
			while (edgeIt.hasNext()){
				EdgeStore theEdge = (EdgeStore)edgeIt.next();
				Line2D.Float line = new Line2D.Float(theEdge.parentCoor[0], theEdge.parentCoor[1], theEdge.childCoor[0], theEdge.childCoor[1]);
				eps.draw(line);
			}
			
			//in dieser While Schleife werden die Knoten durchlaufen und an der richtigen Stelle gezeichnet
			while (breadth.hasMoreElements()) {
				
				curNode = (SceneTreeNode)breadth.nextElement();

				if (curNode.isLeaf() || curNode.toString().matches("ROUTE"))
				
					continue;
				else {
				
					if (level == curNode.getLevel()){
						number++;
		
					}else {
						level = curNode.getLevel();
						levelIt = nodeTreePosLink[level].listIterator(0);
						number = 0;						
					}
					
					float x = ((Float)levelIt.next()).floatValue(); 
					float xPos = 20 + x*(NODEWIDTH+15);
					float yPos = 20 + level*(NODEHEIGHT+40);
					//Output wird erstellt
					
					if (curNode.isDEF()){
						
						RoundRectangle2D rect = new RoundRectangle2D.Double(xPos,yPos,NODEWIDTH,NODEHEIGHT+15,NODEARC+4,NODEARC+4);
						
						eps.setColor(Color.red);
						eps.draw(rect);
						eps.fill(rect);
						
						output = getStringUpToFirstBlank(curNode.toString());
						String defName= curNode.getSceneNodeDEF();
						
						FontMetrics fm = eps.getFontMetrics();
						
						if (fm.stringWidth(output) > (NODEWIDTH-8)) {
							int outputwidth = fm.stringWidth(output);
							eps.setFont(eps.getFont().deriveFont(eps.getFont().getSize2D()* (NODEWIDTH-8) / outputwidth));
							fm = getFontMetrics(eps.getFont());	
							eps.setColor(Color.white);
							eps.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos -2 + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
							eps.setFont(eps.getFont().deriveFont(eps.getFont().getSize2D()* outputwidth/(NODEWIDTH-8)));
							fm = getFontMetrics(eps.getFont());	
						}else {
							eps.setColor(Color.white);
							eps.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos -2 + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
						}
						
						if (fm.stringWidth(defName) > (NODEWIDTH-8)) {
							int outputwidth = fm.stringWidth(defName);
							eps.setFont(eps.getFont().deriveFont(eps.getFont().getSize2D()* (NODEWIDTH-8) / outputwidth));
							fm = getFontMetrics(eps.getFont());	
							eps.setColor(Color.white);
							eps.drawString(defName,xPos + (NODEWIDTH/2 - fm.stringWidth(defName) /2) ,yPos +14+ (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
							eps.setFont(eps.getFont().deriveFont(eps.getFont().getSize2D()* outputwidth/(NODEWIDTH-8)));
							fm = getFontMetrics(eps.getFont());	
						}else {
							eps.setColor(Color.white);
							eps.drawString(defName,xPos + (NODEWIDTH/2 - fm.stringWidth(defName) /2) ,yPos +14+ (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
						}
			
						
					}
					else{
						output = getStringUpToFirstBlank(curNode.toString());
						
						
						
						RoundRectangle2D rect = new RoundRectangle2D.Double(xPos,yPos,NODEWIDTH,NODEHEIGHT,NODEARC,NODEARC);
						
						eps.setColor(Color.BLUE);
						eps.draw(rect);
						eps.fill(rect);
											
						//graphis2dmetrics
						
						FontMetrics fm = eps.getFontMetrics();	
						if (fm.stringWidth(output) > (NODEWIDTH-8)) {
							int outputwidth = fm.stringWidth(output);
							eps.setFont(eps.getFont().deriveFont(eps.getFont().getSize2D()* (NODEWIDTH-8) / outputwidth));
							fm = getFontMetrics(eps.getFont());	
							eps.setColor(Color.white);
							eps.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
							eps.setFont(eps.getFont().deriveFont(eps.getFont().getSize2D()* outputwidth/(NODEWIDTH-8)));
							fm = getFontMetrics(eps.getFont());	
						}else {
							eps.setColor(Color.white);
							eps.drawString(output,xPos + (NODEWIDTH/2 - fm.stringWidth(output) /2) ,yPos + (NODEHEIGHT/2 +fm.getHeight() * 1/2)  ); //
							
						}
					}
				} 					
			}
			
			eps.toString();
		}
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * Diese Methode gibt nur bis zum ersten Leerzeichen den gleichen String zurück, 
	 * damit dieser als Knoten Namen im Baum auftauchen kann
	 * 
	 * @param string  der gesammte Name der Node aus dem JTree
	 * @return schneidet String direkt vor dem ersten Leerzeichen ab.
	 */
	private String getStringUpToFirstBlank(String string) {
		int pos = string.indexOf(" "); 
		if (pos > 0)
			return string.substring(0,pos);
		else
			return string;		
	}
	
	/**
	 * proTree
	 * Diese Methode durchläuft rekursiv den Baum und "returned" dabei die Breiten der Teilbäume, 
	 * um die für den übergebenen Knoten richtige Position zu berechnen.
	 * Sie dient also ausschliesslich der Balance des Baumes
	 * 
	 * @param node der Knoten, für den die Position berechnet wird
	 * @return die Menge an Knoten, für die der Teilbaum dieses Knotens horizontalen Platz beansprucht  
	 */
	
	
	public int proTree(SceneTreeNode node){
		//in width wird die Teilbaumbreite dieses Knotens gespeichert
		int width = 0;
		int nodeLevel = node.getLevel(); 
		// dieser Float wird in den unteren If Schleifen die horizontale Position des Knotens speichern
		float thisPosition;
		//hier werden alle Kinder durchlaufen, width wird berechnet
		Enumeration children = node.sceneNodeChildren();
		while (children.hasMoreElements()){
			//Attribute sowie Routes müssen auch hier ausgelassen werden
			if (node.isLeaf() || node.toString().matches("ROUTE"))
				continue;
			else{
				//hier findet die Rekursion statt: die Breite der Kinder wird,durch mehrmaligen Aufruf von proTree, bis in tiefere Ebenen berechnet
				SceneTreeNode currentNode = (SceneTreeNode)children.nextElement();			
				int childProTree = proTree(currentNode);
				//wenn das Kind keine Kinder hat...
				if (childProTree == 0)
					width++;
				//...oder es hat Kinder der Breite childProTree
				else{
					width += childProTree;
				}
			}
			
			
		}
//		die Position dieses Knotens wird gespeichert(Aufruf von setPos), der "verbrauchte Platz" (numberLevel)auch
		//außerdem wird der Knoten in die KinderListe dieses Levels geschrieben
		if (width==0){
			
			for (int j = nodeLevel; j<numberLevel.length ;j++)
				numberLevel[j]++;
			thisPosition = numberLevel[nodeLevel];
			setPos(nodeLevel,thisPosition);	
			childrenQueue[nodeLevel].add(new Float(thisPosition));
		}
		if(width ==1){
			numberLevel[nodeLevel]++;
			thisPosition = numberLevel[nodeLevel];
			setPos(nodeLevel,thisPosition);	
			childrenQueue[nodeLevel].add(new Float(thisPosition));
		}
		
		if (width>1){
			thisPosition = numberLevel[nodeLevel] + (0.5f*(width)) + 0.5f;
			setPos(nodeLevel,thisPosition);
			childrenQueue[nodeLevel].add(new Float(thisPosition));
			numberLevel[nodeLevel]= numberLevel[nodeLevel] + width;
		}
		
		return width;
	}
	
	/**
	 * setPos
	 * diese Methode speichert die Position des Knotens in die der Ebenen entsprechenden LinkedLists nodeTreePosLink,
	 * außerdem werden die Kannten zwischen diesem Knoten und seinen Kindern
	 *  durch erzeugen von EdgeStore Objekten gesetzt und in edgeList gespeichert
	 * 
	 * @param nodeLevel das Level des zu positionierenden Knoten
	 * @param pos die horizontale Platzierung
	 */
	
	public void setPos(int nodeLevel,float pos){
		nodeTreePosLink[nodeLevel].add(new Float(pos));
		
		//Hier werdden die Kanten berechnet 
		while (!childrenQueue[nodeLevel + 1].isEmpty()){
			//die horizontale Position des Kindes wird zurückgegeben, und aus der LinkedListe gelöscht (poll())
			Float childOne = (Float)childrenQueue[nodeLevel + 1].getFirst();
			childrenQueue[nodeLevel + 1].removeFirst();
			//eine neue Kannte wird erzeugt
			EdgeStore edge = new EdgeStore(nodeLevel,pos, childOne.floatValue());
			//und in einer LinkedList gespeichert
			edgeList.add(edge);
		}
		
		
	}


}
