package scenejtree;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

//import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Diese Klasse enthält Methoden, die zu einem JTree bzw. Teilbaum
 * von SceneTreeNodes statistische Daten liefert. Des weiteren füllt
 * sie einige Felder der SceneTreeNodes auf.
 * 
 * Created on 30.05.2004
 * 
 * @author Patrick Helmholz, Frederik Suhr
 */
public class TreeTraverser {
    /**
     * key=name (ohne def), object=treepath zum knoten
     */
	private TreeMap sortedListOfDEFs;      
	/**
	 * key=name (ohne use), object=list mit allen treepathes der uses (evtl treenode[])
	 */
	private TreeMap sortedListOfUSEs; 
	/**
	 * alle SceneTreeNodes vom Typ "Route"
	 */
	private Vector routeNodes;		   
	/**
	 * momentan nur zu Testzwecken, speicher für alle verrouteten knoten (From + To)
	 */
	private Vector allRoutedNodes; 
    	
	/**
	 * Anzahl Knoten der Szene
	 */
	private int intNodes       = 0; 
	/**
	 * Anzahl Attribute der Szene
	 */
	private int intAttributes  = 0; 
	/**
	 * Anzahl der DEFs
	 */
	private int intDefs        = 0; 
	/**
	 * Anzahl der USEs
	 */
	private int intUses        = 0; 
    /**
     * Anzahl der ROUTEs
     */
	private int intRoutes      = 0;     
    /**
     * Grösste Anzahl an Kindknoten im JTree
     */
	private int maxChildrenPerNode   = 0;

    /**
     * Tiefe des Baumes
     */
	private int intTreeDepth   = 0;
    /**
     * Speicher für Anzahl an Attributen und Knoten pro Level.
     * 
     * [level][0] = nodes, [level][1] = attributes
     */
	private int[][] arrNodesAndAttributesPerLevel;
    
    /** 
     * Speicher für Start- bzw. Rootknoten
     */
	private SceneTreeNode startNode;
    
	/**
	 * Konstruktor beginnt das durchlaufen ab startNode (im Normalfall
	 * dem Root des ganzen Baumes
	 */
	public TreeTraverser (SceneTreeNode startNode)  {
		this.startNode = startNode;
		fetchData();
		
		// routes ermitteln
		// eine referenz auf den route knoten in die beteiligten knoten legen
		allRoutedNodes = new Vector();	
						
		Enumeration allRouteNodes = routeNodes.elements();
		while (allRouteNodes.hasMoreElements()) {			
			SceneTreeNode routeNode = (SceneTreeNode)allRouteNodes.nextElement();

			SceneTreeNode fromNode = null;
			SceneTreeNode toNode   = null;
			String fromFieldName   = "";
			String toFieldName     = "";
			
			Enumeration routeAttributes = routeNode.children();
			while (routeAttributes.hasMoreElements()) {
				SceneTreeNode routeAttribute = (SceneTreeNode)routeAttributes.nextElement();
				
				final String attribute = routeAttribute.getSceneElementName().toUpperCase();				
				
				if (attribute.equals("FROMNODE")) {
					LexicographicalString defToSearch = new LexicographicalString(routeAttribute.toString().substring(10));
					if (sortedListOfDEFs.containsKey(defToSearch)) {
						fromNode = (SceneTreeNode)((SceneTreeNode)(((TreePath)(sortedListOfDEFs.get(defToSearch))).getLastPathComponent())).getParent();
					}
					else {
						System.err.print("Route fromNode " + defToSearch + " was not found in this scene! ");
					}
				}
				else if (attribute.equals("FROMFIELD")) {				
					fromFieldName = routeAttribute.toString().substring(11);						
				}
				else if (attribute.equals("TONODE")) {
					LexicographicalString defToSearch = new LexicographicalString(routeAttribute.toString().substring(8));
					if (sortedListOfDEFs.containsKey(defToSearch)) {
						toNode = (SceneTreeNode)((SceneTreeNode)(((TreePath)(sortedListOfDEFs.get(defToSearch))).getLastPathComponent())).getParent();
					}
					else {
						System.err.print("Route toNode " + defToSearch + " was not found in this scene! ");
					}
				}
				else if (attribute.equals("TOFIELD")) {
					toFieldName = routeAttribute.toString().substring(9);
				}															
			}
			if (fromNode !=null && toNode != null && fromFieldName != "" && toFieldName != "") {
				// alle knoten und feldnamen gefunden
				// speichern der route referenz in fromNode und toNode
				// feldnamen werden nicht verwendet
				SceneRoute thisRoute = new SceneRoute(fromNode, fromFieldName, toNode, toFieldName);				
				fromNode.addRouteNode(routeNode);
				fromNode.addRoute(thisRoute);
				allRoutedNodes.add(fromNode);
				if (!thisRoute.isRouteReflexive()) {//nur einfügen, wenn nich bereits vorhanden
						toNode.addRouteNode(routeNode);
						toNode.addRoute(thisRoute);
						allRoutedNodes.add(toNode); //bisher nur temporär zum test
				}												
			}
			else {
				System.err.println ("Defective ROUTE detected in file. " + routeNode + ": fromNode " + fromNode + " fromField " + fromFieldName + " toNode " + toNode + " toField " + toFieldName + ".");
			}						
		}				
		
		// hier die neuen SceneTreeNodes um
		// corresponding USEs und DEFs auffüllen
		
		// dazu: sortedListOfUSEs durchlaufen
		// zu jedem key im DEF: key die elemente der liste aus object einfügen
		// zu jedem element der liste aus object den DEF eintragen 
		Set allDEFsWithUSEs = sortedListOfUSEs.keySet();
		Iterator getAll = allDEFsWithUSEs.iterator();
		
		String tmpDEF;
		while (getAll.hasNext()) {
			tmpDEF = (String)getAll.next();
			SceneTreeNode defNode = (SceneTreeNode)((SceneTreeNode)(((TreePath)(sortedListOfDEFs.get(new LexicographicalString(tmpDEF)))).getLastPathComponent())).getParent();
			LinkedList uses = (LinkedList)sortedListOfUSEs.get(tmpDEF); 
			Iterator thisUses = uses.iterator();
			while (thisUses.hasNext()) {
				SceneTreeNode useNode = (SceneTreeNode)((SceneTreeNode)(((TreePath)(thisUses.next())).getLastPathComponent())).getParent();
				defNode.addCorrespondingUSE(useNode);				
				useNode.setCorrespondingDEF(defNode);				
			}			
		}
	}
		

	/**
	 * Ermittelte Anzahl an Attributen.
	 * 
	 * @return Returns the intAttributes.
	 */
	public int getIntAttributes() {
		return intAttributes;
	}
	/**
	 * Ermittelte Anzahl an geDEFten Knoten.
	 * 
	 * @return Returns the intDefs.
	 */
	public int getIntDefs() {
		return intDefs;
	}
	/**
	 * Liste der Pfade zu den DEF-Attributen.
	 * 
	 * @return Returns the sortedListOfDEFs.
	 */
	public TreeMap getListOfDEFs() {
		return sortedListOfDEFs;
	}
	/**
	 * Liste über DEF - Namen und dazu jeweils eine Liste der USEs.
	 * 
	 * @return Returns the sortedListOfUSEs.
	 */
	public TreeMap getListOfUSEs() {
		return sortedListOfUSEs;
	}

	/**
	 * Ermittelte Anzahl von Knoten.
	 * 
	 * @return Returns the intNodes.
	 */
	public int getIntNodes() {
		return intNodes;
	}
	/**
	 * Ermittelte Anzahl an Routes.
	 * 
	 * @return Returns the intRoutes.
	 */
	public int getIntRoutes() {
		return intRoutes;
	}
	/**
	 * Ermittelte Anzahl an Uses.
	 * 
	 * @return Returns the intUses.
	 */
	public int getIntUses() {
		return intUses;
	}
	/**
	 * Array über anzahl der Knoten und Attribute pro Ebene.
	 * 
	 * @return Returns the arrNodesAndAttributesPerLevel.
	 */
	public int[][] getArrNodesAndAttributesPerLevel() {
		return arrNodesAndAttributesPerLevel;
	}
	/**
	 * Ermittelte grösste Anzahl an Kindern für einen Knoten.
	 * 
	 * @return Returns the maxChildrenPerNode.
	 */
	public int getMaxChildrenPerNode() {
		return maxChildrenPerNode;
	}
	/**
	 * Enthält alle Route-Knoten dieser Szene.
	 * 
	 * @return Returns the routeNodes.
	 */
	public Vector getRouteNodes() {
		return routeNodes;
	}
	/**
	 * Methode sammelt alle Daten über die Knoten im Baum von startNode.
	 */
	public void fetchData() {				
		// Initialisieren der Instanzvariablen
		routeNodes = new Vector();
		sortedListOfDEFs = new TreeMap();
		sortedListOfUSEs = new TreeMap();
		
		intNodes      = 0;
		intAttributes = 0;
		intDefs       = 0;
		intUses       = 0;   
	    intRoutes     = 0;
	    
	    intTreeDepth = startNode.getDepth(); 
	    arrNodesAndAttributesPerLevel = new int[intTreeDepth + 1][2]; // array mehrdimensional, jeweil 1 grösser da dimensionieren nicht 0 basiert 	    
		
		// Zwischenspeicher für aktuelles Element
		SceneTreeNode currentNode;    	  
		String currentNodeName;
		String modifiedNodeName;
		LinkedList listOfTreePathesToUSEs;
		int childCount;
		//int attributeCount;
		
		// Breitendurchlauf über kompletten Subtree		
		Enumeration e = startNode.breadthFirstEnumeration();
				
		while (e.hasMoreElements()) {
		    currentNode = (SceneTreeNode) e.nextElement();
		    currentNodeName = currentNode.toString().toUpperCase();			    		    		    		  
		    
		    // ermitteln der grössten kinderelement anzahlen
		    // trennung nach knoten und attributen nicht leicht möglich
		    // meistens sollten es aber knoten und nicht attribute sein
		    // wenn die file nicht zu klein ist
		    
		    childCount = currentNode.getChildCount(); 
		    //attributeCount = currentNode.getLeafCount();	
		    
		    if (childCount > maxChildrenPerNode)
		    	maxChildrenPerNode = childCount;
		    //if (attributeCount > maxAttributesPerNode)
		    	//maxAttributesPerNode = attributeCount;
		    
		    
		    // etwas verschachtelt für weniger zu überprüfende Bedingungenetwas verschachtelt für weniger zu überprüfende Bedingungen:
		    if (currentNode.isLeaf()) {
		    	// Hier Blätter = Attribute
		    	arrNodesAndAttributesPerLevel[currentNode.getLevel()][1]++;
		    	
		    	intAttributes++; 
			    if (currentNodeName.startsWith("USE:")) {
		     	    // zählen aller Uses
	     	     	intUses++;
	     	     	// füllen der USEs liste
	     	     	
	     	     	//ermitteln des namens ohne "USE: "
	     	     	modifiedNodeName = currentNode.toString().substring(5);
	     	     	
	     	     	if (sortedListOfUSEs.containsKey(modifiedNodeName)) {
	     	     		listOfTreePathesToUSEs = (LinkedList)sortedListOfUSEs.get(modifiedNodeName);	
	     	     	}
	     	     	else {
	     	     		listOfTreePathesToUSEs = new LinkedList();
	     	     		sortedListOfUSEs.put(modifiedNodeName, listOfTreePathesToUSEs);
	     	     	}
	     	     	listOfTreePathesToUSEs.add(new TreePath(currentNode.getPath()))	;										
			    }
			    else if (currentNodeName.startsWith("DEF:")) {
			    	// zählen der DEFs (erstmal unabhängig von der Anzahl der Elemente in der Liste)
			    	intDefs++;
			    	// ermitteln der DEFs und speichern in TreeMap			         	     
			    	sortedListOfDEFs.put(new LexicographicalString(currentNode.toString().substring(5)), new TreePath(currentNode.getPath()));
			    }							    			    	
		    } else {
		    	// Hier Nodes = Knoten		    	
		    	if (!currentNode.isRoot() && !currentNodeName.equals("SCENE") && !currentNodeName.equals("ROUTE")) {
		    		// zählen aller Nodes der Szene, ohne Root- und Scene-Knoten (und ohne Attribute - thx@timo)
		    		intNodes++;
		    		arrNodesAndAttributesPerLevel[currentNode.getLevel()][0]++;
		    	}			    
			    if (currentNodeName.equals("ROUTE")) {
			    	// zählen aller Routes
			    	intRoutes++;
			    	// abspeichern des route knoten
			    	routeNodes.add(currentNode);
			    }     
		    	
		    }		    
		}									
	}
}
