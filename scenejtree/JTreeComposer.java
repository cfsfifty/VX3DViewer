package scenejtree;

import java.awt.*;
import java.awt.event.*;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.tree.*;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.jdom.Document;


/**
 * Die Klasse erzeugt mittels Hilfklassen aus einem JDOM Document einen JTree.
 * Dessen Anzeigeoptionen werden definiert. Ereignisse für Benutzerinteraktion werden
 * gesetzt.
 *  
 * @author SEP VRML97 Group
 */

public class JTreeComposer implements TreeSelectionListener, KeyListener {   // implements ActionListener 

	/**
	 * Geparstes dokument
	 */
	public Document doc;
	/**
	 * JDom -> Jtree umwandeln
	 */
    public JDOM2JTreeConverter outputter;
    
    /**
     * Dateiname für root knoten
     */
    public String displayedFileName;

	/**
	 * root knoten
	 */
    public SceneTreeNode root;
    /**
     * erzeugter jtree
     */
    public JTree tree;
    
    /**
     * alter cursor
     */
    private Cursor oldCursor = null;    
    /**
     * hand cursor
     */
    private Cursor linkCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    /**
     * tree durchlaufen und daten ermitteln
     */
    private TreeTraverser treeTraverserData;

    /* Strings für Labels
     * 
     */
    private String strUses = "# Uses: 0";
    private String strNodes = "# Nodes: 0";    
    private String strRoutes = "# Routes: 0";
    
    /*
     * Werte aus Traverser
     */
    private String strAttributes = "# Attributes: 0";
    private String strDefs= "# Defs: 0";
    private TreeMap mapOfDEFsToTreePathes;
    private TreeMap mapOfUSEsToTreePathes;
    private int[][] arrElementsPerLevel;
    private int maxChildrenPerNode;    
    private int intNodes;        
    private DefaultComboBoxModel cbModelDEFs = new DefaultComboBoxModel();
    
    /**
     * Layout-erzeugender Renderer
     */
    private SceneGraphRenderer sceneRenderer;
    
    /** 
     * zuletzt ausgewählter knotenpfad
     */
    private TreePath previousSelectedPath;	
    
    /**
     * icon für einfache knoten
     */
    private ImageIcon nodeIcon;
    
    /**
     * Konstruktor für ein geparstes Dokument, aus dem dann der JTree gebaut wird.
     * 
     * @param doc geparstes JDOM Document
     * @param displayedFileName Dateiname für Root Knoten 
     */
    public JTreeComposer(Document doc, String displayedFileName) {
    	nodeIcon = new ImageIcon("./scenejtree/ICONS/Node.gif");
    	this.doc=doc;    	
    	this.displayedFileName=displayedFileName;
    	doFile();    	
    }
    
    /**
     * Konstruktor für dummy ansicht
     *
     */
    public JTreeComposer() {    	                
    	nodeIcon = new ImageIcon("./scenejtree/ICONS/Node.gif");
		
    	// Dummy-Wurzel für JTree erzeugen und hinzufügen
        root = new SceneTreeNode("Please load x3d/vrml file", "Root", "", "", true, nodeIcon);         
        tree = new JTree(root); 
        tree.setRootVisible(true);
        
        // Aussehen für JTree definieren (nur für Dummy Ansicht)
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
                
        renderer.setClosedIcon(nodeIcon);
        renderer.setOpenIcon(nodeIcon);
        renderer.setLeafIcon(nodeIcon);
        
        renderer.setFont(new Font("Arial",Font.BOLD,15));          
        tree.setCellRenderer(renderer);
        
        this.tree.setScrollsOnExpand(true);                  
    }
    
    /**
     * Gibt den enthaltenen JTree zurück.
     * 
     * @return gebauter JTree
     */
    public JTree getTree() {
    	return tree;
    }
    /**
     * Gibt den Root Knoten des JTrees zurück
     * 
     * @return Wurzelknoten
     */
    public DefaultMutableTreeNode getRoot() {
    	return root;
    }

	/**
	 * Anzahl Knoten (für Label).
	 * 
	 * @return Returns the strNodes.
	 */
	public String getStrNodes() {
		return strNodes;
	}
	/**
	 * Anzahl Routes (für Label).
	 * 
	 * @return Returns the strRoutes.
	 */
	public String getStrRoutes() {
		return strRoutes;
	}
	/**
	 * Anzahl USEs (für Label).
	 * 
	 * @return Returns the strUses.
	 */
	public String getStrUses() {
		return strUses;
	}
	/**
	 * Liste aller geDEFten Knoten für ComboBox.
	 * 
	 * @return Returns the cbModelDEFs.
	 */
	public DefaultComboBoxModel getCbModelDEFs() {
		return cbModelDEFs;
	}

	/**
	 * Anzahl der Attribute.
	 * 
	 * @return Returns the strAttributes.
	 */
	public String getStrAttributes() {
		return strAttributes;
	}
	/**
	 * Anzahl der geDEFten Knoten.
	 * 
	 * @return Returns the strDefs.
	 */
	public String getStrDefs() {
		return strDefs;
	}
	/**
	 * Gibt ein Array über Anzahl der Knoten und Atribute pro Ebene
	 * im Baum zurück.
	 * 
	 * @return Returns the arrElementsPerLevel.
	 */
	public int[][] getArrElementsPerLevel() {
		return arrElementsPerLevel;
	}
        
	/**
	 * Gibt die grösste auftretende Anzahl an Kindern wieder.
	 * 
	 * @return Returns the maxChildrenPerNode.
	 */
	public int getMaxChildrenPerNode() {
		return maxChildrenPerNode;
	}
	/**
	 * Gibt die Anzahl der Knoten in der Szene wieder.
	 * 
	 * @return Returns the intNodes.
	 */
	public int getIntNodes() {
		return intNodes;
	}
	/**
	 * Wird bei Änderung der Auwahl vom TreeSelectionListener aufgerufen.
	 * Speichert den zuletzt ausgewählten Pfad und den nun aktuellen Knoten
	 */
    public void valueChanged(TreeSelectionEvent e) {
    	DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();    	
    	previousSelectedPath = e.getOldLeadSelectionPath();    	
    }
    
   	/**
     * Diese Methode wandelt das JDOM Document in den JTree um.
     * Es werden Mausereignisse gesetzt, und das Layout definiert.
     */
    public void doFile() {
                                        	            	
    	// Wurzelknoten JTree benannt nach dem Dateinamen
    	ImageIcon rootIcon = nodeIcon;
    	
    	if (displayedFileName.toLowerCase().endsWith(".x3d"))
    		rootIcon =  nodeIcon = new ImageIcon("./scenejtree/x3dEditIcons/X3D.GIF");
    	else if  (displayedFileName.toLowerCase().endsWith(".wrl"))
    		rootIcon =  nodeIcon = new ImageIcon("./scenejtree/x3dEditIcons/VRML.GIF");
    	
    	root = new SceneTreeNode(displayedFileName, displayedFileName, "", "", true, rootIcon);

        // Neuen JTree anlegen
        tree = new JTree(root); 
        tree.setRootVisible(true);
        
    	// bei den tasten rauf / runter verhalten ändern:
    	tree.addKeyListener(this);
        
        // Ereignis kann verwendet werden, sofern etwas beim
        // Auswählen eines Elementes passieren soll:
        tree.addTreeSelectionListener(this);
        
        // Ereignis, wenn die Mousetaste innerhalb des JTrees gedrückt wird:
        MouseListener ml = new MouseAdapter() {
        	public void mousePressed (MouseEvent e) {
        		int selRow = tree.getRowForLocation(e.getX(),e.getY());
        		TreePath selPath = tree.getPathForLocation(e.getX(),e.getY());
        		
        		if (selRow != -1) {
        			SceneTreeNode thisNode = (SceneTreeNode)selPath.getLastPathComponent();
        			
        			if (thisNode.isSceneAttribute()) {
        				tree.setSelectionPath(new TreePath(((SceneTreeNode)(thisNode.getParent())).getPath()));
        			}
        			
        			if (thisNode.isLeaf()) {
        				// Wenn auf ein Leaf geklickt wurde ...
        				if (thisNode.toString().toUpperCase().startsWith("USE:")) { // && e.getButton()==3
            				// ... und es ein USE ist,
            				// zum DEF abschnitt springen
        					gotoDEFsParent(thisNode.toString().substring(5));
        				}
        				else if ((thisNode.toString().toUpperCase().startsWith("DEF:"))) {
            				// ... und es ein DEF ist,
            				// ggf. ein Popup der vorhandenen USEs zeigen
        					String key = thisNode.toString().substring(5);        					        					
        					        					
        					if (mapOfUSEsToTreePathes.containsKey(key)) {
        						LinkedList listUses = (LinkedList)mapOfUSEsToTreePathes.get(key);        						
        						
        						Iterator itUses = listUses.iterator(); 
        						
        						TreePath thisPath;
        						
        						JPopupMenu mnuUsesPopup = new JPopupMenu();
        						JLabel titleItem;
        						if (listUses.size() > 1)        						
        							titleItem = new JLabel(" The node '" + thisNode.toString().substring(5) +  "' is used at these nodes:", SwingConstants.CENTER);
        						else
        							titleItem = new JLabel(" The node '" + thisNode.toString().substring(5) +  "' is used at this node:", SwingConstants.CENTER);
        						        						
        						titleItem.setFont(new Font("Arial",Font.BOLD,12));
        						mnuUsesPopup.add(titleItem);        						
        						
        						mnuUsesPopup.addSeparator();
        						
        						while (itUses.hasNext()) {
        							thisPath = (TreePath)itUses.next();
        							AbstractAction a = new AbstractAction(getActionNameForPath(thisPath)) {        								
        								public void actionPerformed(ActionEvent e) {
        									gotoNodesParent((TreePath)getValue ("path"));
        								}
        							};
        							a.putValue("path",thisPath);
        							a.putValue(Action.SHORT_DESCRIPTION,thisPath.toString());
        							a.putValue(Action.SMALL_ICON,((SceneTreeNode)(thisPath.getParentPath().getLastPathComponent())).getElementIcon());
        							
        							mnuUsesPopup.add(new JMenuItem(a));
        						}        							
        						//System.out.println(listUses);
        						mnuUsesPopup.show(tree.getComponentAt(new Point(e.getX(), e.getY())),e.getX(),e.getY());
        						//tree.setSelectionPath(previousSelectedPath);
        					}
        				}
            			else if ((thisNode.toString().toUpperCase().startsWith("TONODE:"))) {
            				// hier wurde eine route geklickt:
            				// ggf. zum knoten springen    					        					
        					if (mapOfDEFsToTreePathes.containsKey(new LexicographicalString(thisNode.toString().substring(8)))) {
        						gotoDEFsParent(thisNode.toString().substring(8));
        					}
            			}    
            			else if ((thisNode.toString().toUpperCase().startsWith("FROMNODE:"))) {
            				// hier wurde eine route geklickt:
            				// ggf. zum knoten springen    					        					
        					if (mapOfDEFsToTreePathes.containsKey(new LexicographicalString(thisNode.toString().substring(10)))) {
        						gotoDEFsParent(thisNode.toString().substring(10));
        					}
            			}            				
                	        /*
        					Iterator uses = treeTraverserData.getListOfUSEs().keySet().iterator();
                	        
                	        while (uses.hasNext()) {
                	        	String key = (String)uses.next();
                	        	LinkedList ll = (LinkedList)treeTraverserData.getListOfUSEs().get(key);
                	        	System.out.println(ll);                	        	
                	        }
                	        */    						
        				
        			}        				
        		}
        	}

        	private String getActionNameForPath(TreePath thisPath) {
        		TreePath parentPath = thisPath.getParentPath();
        		SceneTreeNode parentNode = (SceneTreeNode) parentPath.getLastPathComponent();
        		
        		// ermitteln der position in der ebene:
        		int position = 1;// zahl für das zu suchende Element, damit zählung nich 0 basiert ist
        		int level = parentNode.getLevel();

        		String actionName = "";
        		
        		// <Scene> holen
        		SceneTreeNode sceneNode = (SceneTreeNode)thisPath.getPathComponent(1);        		
        		Enumeration sceneChildren = sceneNode.sceneNodeChildren();
        		SceneTreeNode sceneChild;
        		// durchlaufe alle Kinder von Scene
        		while (sceneChildren.hasMoreElements()) {
        			sceneChild = (SceneTreeNode)sceneChildren.nextElement();        			
        			if (sceneNode.getSceneNodeIndex(sceneChild) <= sceneNode.getSceneNodeIndex(((SceneTreeNode)thisPath.getPathComponent(2)))) {
	        			// liegt nicht rechts vom zu untersuchenden ast, breitendurchlauf und auf level zählen:
        				Enumeration breadthFirst = sceneChild.breadthFirstEnumeration();
	            		SceneTreeNode thisNode;
	            		while (breadthFirst.hasMoreElements()) {
	            			thisNode = (SceneTreeNode)breadthFirst.nextElement();	            				            			
	            			if (thisNode.getLevel() == level && thisNode.isSceneNode()) {	            	
	            				// knoten ist vom typ SceneNode und liegt auf dem zu addierenden level
	            				if (sceneNode.getSceneNodeIndex(sceneChild) == sceneNode.getSceneNodeIndex(((SceneTreeNode)thisPath.getPathComponent(2)))) {
	            					// auf ast vom parentNode angekommen
		            				if (thisNode == parentNode) {
		            					// beim parentNode angekommen, zahl ermittelt
										break;	            					
		            				}
	            				}
	            				// knoten auf ebene links von parentnode, 1 aufaddieren und weiter
	            				position++;
	            			} else
	            			if (thisNode.getLevel() > level) {
	            				// zu tief in baum vorgedrungen, sollte nicht passieren
	            				break;
	            			}
	            		}
        			}
	            	else {
	            		// zu weit nach rechts in baum gewandert, sollte nich passieren
	            		break;
	            	}
        		}
        		        		
        		actionName = parentNode.toString() + " [@level: " + level + ", @position in level: " + position + "]";
        		
        		return actionName;
        	}        	
        };
        tree.addMouseListener(ml);
                                
        oldCursor=tree.getCursor(); // für rücksetzen alten cursor speichern                        
        MouseMotionListener mml = new MouseMotionAdapter() {
        	public void mouseMoved (MouseEvent e) {
        		// hier mächtig redundanz, aber das mousemotion gefällt mir eh nicht
        		// lieber wäre mir etwas, was nur bei den richtigen "Component"en 
        		// im JTree passiert
        		
        		int selRow = tree.getRowForLocation(e.getX(),e.getY());
        		TreePath selPath = tree.getPathForLocation(e.getX(),e.getY());
        		if (selRow != -1) {
        			DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
        			
        			if (thisNode.isLeaf() && thisNode.toString().toUpperCase().startsWith("USE:")) {
        				// Wenn es ein Leaf ist ...
        				// ... und dabei noch ein USE
        				// Cursor Symbol in Hand ändern
                		if (tree.getCursor() != linkCursor)
                			tree.setCursor(linkCursor);
        				return;
        			}
        			else if ((thisNode.toString().toUpperCase().startsWith("DEF:"))) {
        				// ... und es ein DEF ist,
    					// mit korrespondieredem USE
        				// Cursor setzen    					        					
    					if (mapOfUSEsToTreePathes.containsKey(thisNode.toString().substring(5))) {
                    		if (tree.getCursor() != linkCursor)
                    			tree.setCursor(linkCursor);
            				return;    						
    					}
        			}    
        			else if ((thisNode.toString().toUpperCase().startsWith("TONODE:"))) {
        				// hier wurde eine route gefunden:
        				// ggf. Cursor setzen    					        					
    					if (mapOfDEFsToTreePathes.containsKey(new LexicographicalString(thisNode.toString().substring(8)))) {
                    		if (tree.getCursor() != linkCursor)
                    			tree.setCursor(linkCursor);
            				return;    						
    					}
        			}    
        			else if ((thisNode.toString().toUpperCase().startsWith("FROMNODE:"))) {
        				// hier wurde eine route gefunden:
        				// ggf. Cursor setzen    					        					
    					if (mapOfDEFsToTreePathes.containsKey(new LexicographicalString(thisNode.toString().substring(10)))) {
                    		if (tree.getCursor() != linkCursor)
                    			tree.setCursor(linkCursor);
            				return;    						
    					}
        			}    

        		}
        		// wenn wir bis hier gekommen sind, befinden wir uns wohl
        		// nicht auf so einem Blatt, also ggf. defaultcursor setzen
        		if (tree.getCursor() != oldCursor)
        			tree.setCursor(oldCursor);
        	}
        };                                                
        tree.addMouseMotionListener(mml);
		                                                    
        // Aufruf des Konverters, der den Szenengraphen aus dem
        // Document in den JTree einfügt
        //System.out.println(outputter); 
        outputter =new JDOM2JTreeConverter();
       
        outputter.output(doc, root); //Root  

        // Daten über den Tree aktualisieren:
        treeTraverserData = new TreeTraverser(root);
        
        mapOfUSEsToTreePathes = treeTraverserData.getListOfUSEs();
        
        arrElementsPerLevel = treeTraverserData.getArrNodesAndAttributesPerLevel();
        maxChildrenPerNode = treeTraverserData.getMaxChildrenPerNode();
		//maxAttributesPerNode = treeTraverserData.getMaxAttributesPerNode();
        
        this.intNodes = treeTraverserData.getIntNodes();
        
        strNodes = "# Nodes: " + treeTraverserData.getIntNodes();
        strRoutes="# Routes: " + treeTraverserData.getIntRoutes();
        strUses="# Uses: " + treeTraverserData.getIntUses();
        
        strDefs="# Defs: " + treeTraverserData.getIntDefs();
        strAttributes="# Attributes: " + treeTraverserData.getIntAttributes();
        
        // KomboBox Modell aktualisieren
        cbModelDEFs.removeAllElements();
        mapOfDEFsToTreePathes = treeTraverserData.getListOfDEFs();
        
        Set setOfDEFs = mapOfDEFsToTreePathes.keySet();        
        Iterator it = setOfDEFs.iterator();
        
        while (it.hasNext()) {
        	cbModelDEFs.addElement(it.next().toString());
        }
		        
        // schon mal die ersten zwei ebenen ausklappen
        tree.expandRow(0);
        tree.expandRow(1);		
        
        // JTree Layout nach SceneGraphRenderer
        sceneRenderer = new SceneGraphRenderer(mapOfDEFsToTreePathes, mapOfUSEsToTreePathes); // uses werden übergeben, damit nur die defs auf uses verlinkt werden, wo es die zuordnung überhaupt gibt
        DefaultTreeCellRenderer renderer = sceneRenderer;
        tree.setCellRenderer(renderer);

        //Unnötig, da Default, aber bei Patrick gings angeblich nich ohne :)
        tree.putClientProperty("JTree.lineStyle","Angled");
        
        tree.setRowHeight(tree.getRowHeight()+2);
        
        // nur einfach auswahl von echten nodes (ohne leaves) zulassen:
        tree.setSelectionModel(new FilteredTreeSelectionModel());

    }
    

    /**
     * Klappt den ganzen Baum auf, so dass alle Knoten "theoretisch" sichtbar sind.
     */
    public void expandAll(JTree tree) { 
        int row = 0; 
        while (row < tree.getRowCount()) {
        	tree.expandRow(row);
            row++;
        }
    }
    
    /**
     * Klappt den Baum bis auf die ersten 2 Ebenen wieder zu.
     */
    public void collapseAll(JTree tree) {
        int row = tree.getRowCount() - 1;
        while (row >= 0) {
            tree.collapseRow(row);
            row--;
        }
        tree.expandRow(0);
        tree.expandRow(1);       
    }
    
	
	/**
	 * Spring den Vaterknoten zu einem Def-Attribut an.
	 * 
	 * @param strDEF DEF Attribut, des anzuzeigenden knoten 
	 */
	public void gotoDEFsParent(String strDEF) {						
		LexicographicalString searchString = new LexicographicalString(strDEF);
		if (mapOfDEFsToTreePathes.containsKey(searchString)) {
			gotoNodesParent((TreePath)(mapOfDEFsToTreePathes.get(searchString)));
		}
	}
	/**
	 * Springt den Vater zu einem gegeben Knoten-Pfad-Array an.
	 * 
	 * @param path pfad, zu dessen parent gesprungen werden soll
	 */
	private void gotoNodesParent(TreePath path) {
		gotoNode(path.getParentPath());
	}
	/**
	 * Diese Methode bringt einen Knoten im JTree zur Anzeige.
	 * 
	 * Verwendet nun schöneres Scrolling, ohne die Selection zu ändern, 
	 * leider funktionierte im plaf setScrollsOnEpand() nicht.
	 * 
	 * Wird nun ersetzt durch "tree.scrollRowToVisible(tree.getRowCount()-1);",
	 * welches zuerst nach ganz unten scrollt, dadurch werden auch möglichst
	 * viele Kinder gezeigt.
	 * 
	 * @param path Pfad, der angesprungen werden soll.
	 */
	public void gotoNode(TreePath path) {
		// bildschirmaktualisierung aus, für sauber aussehendes scrolling
		tree.setVisible(false);
				
		// Pfad zum anzuzeigenden Knoten
		TreePath treePath = path; 
		
		// ausklappen des zielpfades
		tree.expandPath(treePath);
		// von unten reinscrollen, damit möglichst viel vom Subtree zu sehen ist
		tree.scrollRowToVisible(tree.getRowCount()-1);

		// scrollen auf zielpfad
		tree.scrollPathToVisible(treePath);
		// auswahl festlegen
		tree.setSelectionPath(treePath);
		
		// bildschirm wieder aktualisieren
		tree.setVisible(true);
	}

	/**
	 * Implementiert für KeyListener Interface.
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent arg0) {		
	}

    /**
     * Es wurde eine taste im jtree gedrückt. Wenn es Cursor
     * "rauf" oder "runter" war, konsumieren und selber 
     * zum nächsten(vorherigen) node gehen.
     * 
     * Wird benutzt, um Blätter zu überspringen.
     * 
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode()==KeyEvent.VK_UP) {
			// nach oben scrollen
			
			arg0.consume();
			DefaultMutableTreeNode tmpNode;
			
			for (int i=tree.getSelectionRows()[0]-1; i>=0; i--) {
				tmpNode = (DefaultMutableTreeNode) tree.getPathForRow(i).getLastPathComponent();
				if (!tmpNode.isLeaf()) { 					
					tree.scrollRowToVisible(i);
					tree.setSelectionRow(i);						
					return;
				}
			}			
		}
		else if (arg0.getKeyCode()==KeyEvent.VK_DOWN) {
			// nach unten scrollen
			arg0.consume();
			DefaultMutableTreeNode tmpNode;
			
			for (int i=tree.getSelectionRows()[0]+1; i<tree.getRowCount(); i++) {
				tmpNode = (DefaultMutableTreeNode) tree.getPathForRow(i).getLastPathComponent();												
				if (!tmpNode.isLeaf()) {					
					tree.scrollRowToVisible(i);
					tree.setSelectionRow(i);						
					return;
				}
			}	
			// wenn man bis hier her kommt, wurde nix gefunden, dann wenigstens die letzten
			// blätter reinscrollen
			tree.scrollRowToVisible(tree.getRowCount() - 1);

		}
		else {
			//do nothing special
		}
		
	}

	/**
	 * Implementiert für KeyListener Interface.
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent arg0) {
	}	
}
