package scenejtree;

/** 
 * Diese Klasse bietet eine Möglichkeit eine Route 
 * zwischen zwei SceneTreeNodes zu speichern.
 * 
 * Es werden die beteiligten Knoten, der fromNode bzw. EventOut
 * Ausgangsknoten, dessen fromField, und der toNode bzw. EventIn
 * Eingangsnkoten, sowie dessen toField gespeichert.
 * 
 * Zusätzlich gibt es natürlich Methoden, um diese Werte wieder auszulesen.
 * Des weiteren kann die Richtung der Route ermittelt werden. Oder man kann 
 * die den jeweils anderen Knoten zu einem Knoten der Route ermitteln.
 * Auch gibt es eine Möglichkeit, zu ermitteln, ob die Route Reflexiv ist.
 * 
 * Created on 08.07.2004
 * 
 * @author fs
 */
public class SceneRoute {

	/**
	 * Feld um für getDirection(SceneTreeNode thisNode), zeigt an,
	 * dass die Route auf diesen Knoten zeigt 
	 */
	public static final boolean DIRECTION_IN = true;
	/**
	 * Feld um für getDirection(SceneTreeNode thisNode), zeigt an,
	 * dass die Route von diesem Knoten weg zeigt 
	 */
	public static final boolean DIRECTION_OUT = false;
	
	/**
	 * Startknoten der Route.
	 */	
	private SceneTreeNode fromNode;
	/**
	 * Startfeldname des Startknotens der Route.
	 */
	private String fromFieldName;
	/**
	 * Endknoten der Route.
	 */	
	private SceneTreeNode toNode;
	/**
	 * Endfeldname des Startknotens der Route.
	 */
	private String toFieldName;

	/**
	 * Konstruktor für die Aufnahme der an einer Route beteiligten
	 * Knoten und Felder.
	 * 
	 * @param fromNode      Ursprungsknoten
	 * @param fromFieldName + Feld
	 * @param toNode        ZielKnoten
	 * @param toFieldName   +Feld
	 */
	public SceneRoute(SceneTreeNode fromNode, String fromFieldName, SceneTreeNode toNode, String toFieldName) {
		this.fromNode = fromNode;
		this.fromFieldName = fromFieldName;
		this.toNode = toNode;
		this.toFieldName = toFieldName;		
	}

	/**
	 * @return Returns the fromFieldName.
	 */
	public String getFromFieldName() {
		return fromFieldName;
	}
	/**
	 * @return Returns the fromNode.
	 */
	public SceneTreeNode getFromNode() {
		return fromNode;
	}
	/**
	 * @return Returns the toFieldName.
	 */
	public String getToFieldName() {
		return toFieldName;
	}
	/**
	 * @return Returns the toNode.
	 */
	public SceneTreeNode getToNode() {
		return toNode;
	}
	
	/**
	 * Ermittelt unter Angabe des "aktuellen" Knoten den anderen an der Route beteiligten Knoten.
	 * 
	 * @param thisNode aktueller Knoten, zu dem der verroutete Knoten gesucht wird
	 * @return Knoten am anderen Ende der Route (von thisNode aus gesehen)
	 */	
	public SceneTreeNode getOtherNode (SceneTreeNode thisNode) {
		if (fromNode.equals(thisNode))
			return toNode;
		else if (toNode.equals(thisNode))
			return fromNode;
		else
			return null; // fehler: thisNode ist an dieser Route nich beteiligt
	}
	/**
	 * Ermittelt unter Angabe des "aktuellen" Knoten das Feld des anderen an der Route beteiligten Knoten.
	 * 
	 * @param thisNode aktueller Knoten, zu dem das Feld des verrouteten Knoten gesucht wird
	 * @return Feldname des Knoten am anderen Ende der Route (von thisNode aus gesehen)
	 */	
	public String getOtherField (SceneTreeNode thisNode) {
		if (fromNode.equals(thisNode))
			return toFieldName;
		else if (toNode.equals(thisNode))
			return fromFieldName;
		else
			return null; // fehler: thisNode ist an dieser Route nich beteiligt
	}	

	/**
	 * Ermittelt unter Angabe des "aktuellen" Knoten das Feld dieses Knotens, dass an der Route beteiligt ist.
	 * 
	 * @param thisNode aktueller Knoten, zu dem dessen verroutetes Feld gesucht wird
	 * @return Feldname dieses Knotens (von thisNode aus gesehen)
	 */	
	public String getThisField (SceneTreeNode thisNode) {
		if (fromNode.equals(thisNode))
			return fromFieldName;
		else if (toNode.equals(thisNode))
			return toFieldName;
		else
			return null; // fehler: thisNode ist an dieser Route nich beteiligt
	}	
	
	/**
	 * Ermittelt die Richtung der Route, von thisNode aus betrachtet.
	 * 
	 * @param thisNode Knoten, von dem ausgehend die Richtung der Route ermittelt werden soll
	 * @return SceneRoute.DIRECTION_OUT, wenn die Richtung (Pfeil) von thisNode wegzeigt, oder SceneRoute.DIRECTION_IN bei umgekehrter Richtung
	 */
	public boolean getDirection (SceneTreeNode thisNode) {
		if (fromNode.equals(thisNode))
			return SceneRoute.DIRECTION_OUT;
		else if (toNode.equals(thisNode))
			return SceneRoute.DIRECTION_IN;
		else {
			System.err.println("Knoten " + thisNode + " nicht an dieser Route + " + this.toString() + " beteiligt. Richtung abgehend angenommen");
			return SceneRoute.DIRECTION_OUT;
		}
		
	}
	
	/**
	 * Schaut, ob die Route mit Anfang und Ende am gleichen Knoten anliegt.
	 * 
	 * @return true, wenn Start- und Endknoten gleich sind.
	 */
	public boolean isRouteReflexive() {
		return fromNode.equals(toNode);
	}
	
	/**
	 * Überschriebenes toString, gibt die Felder aus, die eine Route definieren.
	 */
	public String toString() {
		return "ROUTE (fromNode=" + fromNode + ", fromField=" + fromFieldName + ", toNode=" + toNode + ", toField=" + toFieldName + ")";
	}	
}
