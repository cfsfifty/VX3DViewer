package scenejtree;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Diese Klasse erweitert DefaultMutableTreeNode um einige nützliche Angaben
 * für Szenenelemente. So können z.B. def-Attribute extra gespeichert werden und
 * es können Icons für die Knoten abgelegt werden.
 * 
 * Wenn kein leerer Knoten initialisiert werden soll, wird dieser
 * Konstruktor vewendet:
 * SceneTreeNode(String displayName, String elementName, String defName, String useName,boolean boolSceneNode, ImageIcon elementIcon)
 * 
 * Created on 24.06.2004
 * @author fs
 */
public class SceneTreeNode extends DefaultMutableTreeNode {

	/**
	 * Liste aller SzenenKnoten Kinder, parallel zu super.children,
	 * nimmt aber nur die "echten" Knoten auf (keine Attribute, keine Routeknoten) 
	 */
	protected Vector sceneNodeChildren;
	
	/**
	 * Wird erst im nachhinein gefüllt, wenn dieses ein DEF ist, und dieser geUSEd wird, werden alle Nodes mit dem USE hier reingespeichert
	 */	
	protected Vector correspondingUSEs;
    /**
     * Wird erst im nachhinein gefüllt, wenn dieses ein USE ist, wird der zugehörige DEF Knoten, sobald gefunden, hier abgelegt
     */
	protected SceneTreeNode correspondingDEF; 
	/**
	 * hier kommen durch den TreeTraverser im nachhinein die beleigten route(-definitions)-knoten hinein
	 */
	private Vector routeNodes;
	/**
	 * hier kommen durch den TreeTraverser im nachhinein das user objekt SceneRoute hinein, welches die "interpretierten" routes enthält
	 */
	private Vector sceneRoutes;	
	/**
	 * String für Anzeige (via toString),
	 * bei Knoten (+ROUTE): "Knotentyp" oder wenn vorhanden "Knotentyp (Defname)"
	 * bei Attributen: "Attributname: Attributwert" 
	 */
	private String displayName;
	/**
	 * Hier nur den Knotentyp oder Attributnamen speichern
	 */
	private String elementName;
	//private String attributeValues; // ggf. noch implementieren TODO
	/**
	 * hier wenn vorhanden der Def-Attribut-Wert des Knotens
	 */
	private String defName;
	/**
	 * hier wenn vorhanden der Use-Attribut-Wert des Knotens
	 */
	private String useName;
	/**
	 * wird war beim Root, Scene, allen SceneNodes (ohne Attribute und ROUTEs)
	 */
	private boolean boolSceneNode;
	/**
	 * Icon für dieses Element, Knoten oder Attributicon (vom Konstruktor übergeben)
	 */
	private ImageIcon elementIcon;	
	/**
	 * true, wenn dieser Knotentyp ein vom Typ Route ist
	 */	
	private boolean boolRoute;
	/**
	 * true, wenn dieser Knoten ein Def-Attribut hat
	 */
	private boolean boolDef;
	/**
	 * true, wenn dieser Knoten ein Use-Attribut hat
	 */
	private boolean boolUse;	
	/**
	 * Zählspeicher für direkte Kinder
	 */
	private int intSceneNodeChildCount;
	/**
	 * Zählspeicher für alle Knoten dieses (Teil-)Baumes unterhalb dieses Knotens
	 */
	private int intSceneNodesDescendantsCount;		
	/**
	 * Diese Variable wird im scene2Dview verwendet, InnerCanvas.
	 * Sie speichert im jeweiligen selectedNode die Position des Sliders.
	 * Wird dieser Knoten wieder angezeigt, wird der Slider an dieser Stelle
	 * wiederhergestellt.
	 */
	private int view2DInnerSliderPosition = 0; //startposition 0 = 1. Element
	
	/**
	 * Konstruktor für einen leeren Knoten (wird eigentlich nicht verwendet,
	 * da nicht alle Attribute durch Set-Methoden geändert werden können)
	 */
	public SceneTreeNode() {
		super();		
		sceneNodeChildren = new Vector();
		correspondingUSEs = new Vector();
		correspondingDEF  = null;
		routeNodes        = new Vector();
		sceneRoutes       = new Vector();
	}
	/**
	 * Knoten mit den Einstellungen aus dem Ladevorgang.
	 * 
	 * @param displayName ElementName (für Knoten: 'NODE (Defname)' bzw nur 'NODE', für Attribute: 'bboxSize: 0 0 0'
	 * @param elementName nur den Namen des elementes, ohne ' (DEF)' für nodes und ohne ': Attributwerte' für attribute
	 * @param defName enthält den DEF-Namen dieses Knoten, wenn dieser Knoten mittels DEF Attribut benannt wurde
	 * @param useName enthält den DEF-Namen des Knoten der hier "hergeklont" werden soll, wenn dieser Knoten mittels USE Attribut auf einen anderen Knoten verweist
	 * @param boolSceneNode true, wenn scenenode, false wenn attribut oder ROUTE
	 * @param elementIcon symbol für tree
	 */
	public SceneTreeNode(String displayName, String elementName, String defName, String useName,boolean boolSceneNode, ImageIcon elementIcon) {
		super(displayName);
		
		this.displayName   = displayName;
		this.elementName   = elementName;
		this.defName       = defName;
		this.useName       = useName;
		this.boolSceneNode = boolSceneNode;
		this.elementIcon   = elementIcon;
		
		if (elementName.toUpperCase().equals("ROUTE"))
			this.boolRoute = true;
		
		if (defName == null || defName == "")
			this.boolDef = false;
		else
			this.boolDef = true;
		
		if (useName == null || useName == "")
			this.boolUse = false;
		else
			this.boolUse = true;
		
		sceneNodeChildren = new Vector();
		correspondingUSEs = new Vector();
		correspondingDEF  = null;
		routeNodes        = new Vector();
		sceneRoutes       = new Vector();
	}
	
	/**
	 * neues add, das sicherstellt, das nur SceneTreeNodes hinzugefügt werden,
	 * zählt die scenenode kinder, und aktualisiert bis zum root die descendants anzahl
	 * @param nodeToAdd knoten der angefügt wird als kind
	 */	
	public void addSceneTreeNode(SceneTreeNode nodeToAdd) {
		super.add(nodeToAdd);
		
		if (nodeToAdd.isSceneNode()) {
			// hinzufügen zur liste und hochzählen 
			sceneNodeChildren.add(nodeToAdd);			
			intSceneNodeChildCount++;		

			// wenn dieser knoten nicht die wurzel ist, die anzahl der descendants nach oben durchreichen
			if (!this.isRoot()) {
				TreeNode[] pathToRoot = this.getPath();
				for (int i = 0; i <pathToRoot.length; i++) {
					((SceneTreeNode)pathToRoot[i]).intSceneNodesDescendantsCount += 1 + nodeToAdd.intSceneNodesDescendantsCount;
				}
			}
			else {
				// beim root knoten einfach die anzahl hinzuaddieren von diesem knoten und ggf. seinen descendants
				intSceneNodesDescendantsCount += 1 + nodeToAdd.intSceneNodesDescendantsCount;
			}
		}			
	}
	/**
	 * ausbauen der remove methoden, da noch nich implementiert (werden bisher nicht benötigt,
	 * knoten werden einmal zur laufzeit eingelesen und danch nicht mehr verändert). 
	 */
	public void remove(int pos) {		
	}
	public void remove(MutableTreeNode node) {		
	}
	public void removeAllChildren() {		
	}
	public void removeFromParent() {		
	}	

	/**
	 * @return die anzahl aller SceneNodes unterhalb dieses Knoten
	 */
	public int getSceneNodesDescendantsCount() {		
		return intSceneNodesDescendantsCount;
	}

	/** 
	 * @return die anzahl der SceneNodes, die Kinder dieses Knoten sind
	 */
	public int getSceneNodeChildCount() {
		return intSceneNodeChildCount;
	}
	/**
	 * @return true, wenn Knoten im Szenegraphen (nach web3d.org), also kein attribut und kein Route-Knoten
	 */	
	public boolean isSceneNode() {
		return this.boolSceneNode;
	}

	/**
	 * @return Attribut im Szenegraphen (attribute zum vater knoten)
	 */		
	public boolean isSceneAttribute() {
		return isLeaf();		
	}

	/**
	 * @return Returns the view2DInnerSliderPosition.
	 */
	public int getView2DInnerSliderPosition() {
		return view2DInnerSliderPosition;
	}
	/**
	 * @param view2DInnerSliderPosition The view2DInnerSliderPosition to set.
	 */
	public void setView2DInnerSliderPosition(int view2DInnerSliderPosition) {
		this.view2DInnerSliderPosition = view2DInnerSliderPosition;
	}
	/**
	 * @return Symbol für SceneNodes und Attribute
	 */			
	public ImageIcon getElementIcon() {
		return elementIcon;
	}

	/**
	 * @return Namen des SceneNodes oder Attributes (z.B. GROUP btw. bboxSize) 
	 */				
	public String getSceneElementName() {		// 
		return elementName;
	}

	/**
	 * @return DEF-Name, wenn dies ein knoten ist, und DEF vorhanden, sonst leerer string 
	 */				
	public String getSceneNodeDEF() {
		return defName;
	}

	/**
	 * @return true, wenn dieser knoten ein scenenode ist und ein def hat
	 */
	public boolean isDEF() {
		return boolDef;
	}

	/**
	 * @return true, wenn dieser knoten ein scenenode ist und use verwendet
	 */	
	public boolean isUSE() {
		return boolUse;
	}

	/**
	 *  @return true, wenn dieser knoten ein Route knoten ist
	 */
	public boolean isROUTE() {
		return boolRoute;
	}
	/**
	 * @return true, wenn es zu diesem USE einen DEF Knoten gibt (sollte nach dem Laden immer der Fall sein!)
	 * 
	 */
	// TODO verbinden mit isUSE() ?
	public boolean hasCorrespondingDEF() {
		return correspondingDEF != null;
	}
	/**
	 * @return true, wenn es zu diesem DEF USEs gibt
	 */
	public boolean hasCorrespondingUSEs() {
		return !correspondingUSEs.isEmpty();
	}
	/**
	 * @return anzahl der USEs zu diesem DEF
	 */
	public int getCorrespondingUSEsCount() {
		return correspondingUSEs.size();
	}
	/**
	 * @return Enumeration über alle USEs zu diesem DEF
	 */
	public Enumeration correspondingUSEs() {
		return correspondingUSEs.elements();
	}
	/**
	 * @param correspondingUSE fügt einen USE zu diesem DEF hinzu
	 */
	public void addCorrespondingUSE(SceneTreeNode correspondingUSE) {
		correspondingUSEs.add(correspondingUSE);
	}
	/**
	 * @param correspondingDEF setzt den DEF zu diesem USe
	 */
	public void setCorrespondingDEF(SceneTreeNode correspondingDEF) {
		this.correspondingDEF = correspondingDEF;		
	}
	/**
	 * @return Returns the correspondingDEF.
	 */
	public SceneTreeNode getCorrespondingDEF() {
		return correspondingDEF;
	}
	/**
	 * @param aChild
	 * @return gibt den Index des Knoten aChild unter allen Kindern dieses Knoten zurück, sofern aChild Kind von diesem Knoten ist
	 */
	public int getSceneNodeIndex(SceneTreeNode aChild) {
		return sceneNodeChildren.indexOf(aChild);
		
	}	
	/**
	 * @param index im children array
	 * @return kind oder ArrayIndexOutOfBoundsException 
	 */
	public SceneTreeNode getSceneNodeChildAt(int index) {
		return (SceneTreeNode)sceneNodeChildren.get(index);
	}	
	
	/**
	 * @return Enumeration über alle Kinder
	 */
	public Enumeration sceneNodeChildren() {
		if (intSceneNodeChildCount == 0)
			return EMPTY_ENUMERATION;
		else 
			return sceneNodeChildren.elements();
	}
	
	/**
	 * @return für Anzeige bestimmter displayName 
	 */
	public String toString() {
		return displayName;
	}

	/**
	 * @param routeNode referenz auf den route knoten, an dem dieser knoten beteiligt ist
	 */
	public void addRouteNode(SceneTreeNode routeNode) {
		routeNodes.add(routeNode);		
	}
	/**
	 * überprüft, ob dieser knoten an routes beteiligt ist
	 * @return true, wenn es routes gibt.
	 */
	public boolean hasRoutes() {
		return !routeNodes.isEmpty();
	}
	/**
	 * ermittelt die anzahl an routes, an denen dieser knoten beteiligt ist
	 * @return anzahl der routes
     */
	public int getRouteCount() {
		return routeNodes.size();
	}
	/**
	 * speichert das Route Objekt, dass die Route elemente dieser Route enthält.
	 * @param thisRoute
	 */
	public void addRoute(SceneRoute thisRoute) {
		sceneRoutes.add(thisRoute);		
	}
	
	/** 
	 * aufzählung aller SceneRoute's die an diesem Knoten beteiligt sind.
	 *
	 */ 
	public Enumeration sceneRoutes() {
		if (hasRoutes() == false)
			return EMPTY_ENUMERATION;
		else 
			return sceneRoutes.elements();
	}
}    

