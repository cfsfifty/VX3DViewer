package scenejtree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Angelehnt an SUN's Java Tutorials. Diese Klasse liefert f�r einen JTREE,
 * der einen Szenengraph enth�lt, das passende Layout.
 * So zeigt er u.a. Custom Icons f�r die Knoten an.
 * 
 * @author Patrick Helmholz, Frederik Suhr
 */
public class SceneGraphRenderer extends DefaultTreeCellRenderer{		
	
	/**
	 * Speicher f�r DEF Namen der USEs und einer LinkedList der
	 * Pfade zu den Use-Attributen.
	 */
	private TreeMap mapOfUSEsToTreePathes;
	/**
	 * Speicher f�r DEF Knoten und dem Pfad zum Def-Attribut,
	 * wird zur Sortierung mit "LexicographicalString"'s gef�llt.
	 */ 
 	private TreeMap mapOfDEFsToTreePathes;
			
	/*
	 * Fonts f�r Wurzel, normale Knoten, normale Bl�tter und verlinkte Bl�tter.
	 */
 	Font rootFont = new Font("Arial",Font.BOLD,14);
	Font nodeFont = new Font("Arial",Font.PLAIN,14);
	Font leafFont = new Font("Arial",Font.ITALIC,11);
	Font linkFont = new Font("Arial",Font.ITALIC | Font.BOLD,11);
	
	/*
	 * Icons f�r Knoten (aus- und zugeklappt), sowie Attribute. 
	 */
    ImageIcon openIcon = new ImageIcon("./scenejtree/ICONS/Node.gif");
	ImageIcon closedIcon = openIcon;
	ImageIcon leafIcon = new ImageIcon("./scenejtree/ICONS/ANode.gif");			
	
	/**
	 * Dieser Konstruktor wird f�r einen leeren Renderer angeboten.
	 *  
	 */	
	
    public SceneGraphRenderer() {		
        this (new TreeMap(), new TreeMap());
	}
	
	/**
	 * Der Konstruktor initialisert das Objekt, setzt Standarts 
	 * der �bergeordneten Klasse und l�dt alle Icons in ein Array.
	 * 
	 * @param mapOfDEFsToTreePathes Speicher f�r defs
	 * @param mapOfUSEsToTreePathes Speicher f�r uses
	 */
	public SceneGraphRenderer(TreeMap mapOfDEFsToTreePathes, TreeMap mapOfUSEsToTreePathes) {
        super();
        
    	int iconCount;
		File iconFiles[];
								
		// wenn wir f�r einen Knoten kein Symbol haben, folgende 
		// verwenden		
		super.setClosedIcon(closedIcon);
        super.setOpenIcon(openIcon);
        super.setLeafIcon(leafIcon);                	                      
        
        //myNodeIconLoader = new NodeIconLoader();

        this.mapOfDEFsToTreePathes = mapOfDEFsToTreePathes;
		this.mapOfUSEsToTreePathes = mapOfUSEsToTreePathes;		
	}

	/**
	 * Methode Component wird �berschrieben um eigenen TreeCellRenderer zu
	 * implementieren. Hier werden durch Fallunterscheidungen unterschieden:
	 * - Rootknoten
	 * - Knoten 
	 * - Bl�tter mit besonderer Eigenschaft
	 * - Bl�tter
	 * 
	 * Es werden so z.B. die Icons gesetzt, oder Linksfonts definiert.
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {			
		// den gelben rand, wenn das objekt den focus erh�lt, deaktivieren
		if (hasFocus) {
			hasFocus=false;
		}

		// von der Oberklasse Eigenschaften und Objekte holen:
		Component comp = super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);						
		
		if (!leaf) { 
		    // nur f�r nodes symbole vergeben
			if (((DefaultMutableTreeNode)value).isRoot()) {
		    	super.setFont(rootFont);		    	
		    } else {
		    	super.setFont(nodeFont);
		    }
		}
		else {
			// F�r Bl�tter schauen nach besonderen, hier zun�chst USE 						
			if (value.toString().startsWith("USE: ")) {	        	
	        	super.setForeground(new Color(0,0,255));
	        	super.setFont(linkFont);
	        	super.setText("<html><u>" + value.toString() + "</u></html>");	        	
			}
			else if (value.toString().startsWith("DEF: ")) {				    							
				if (mapOfUSEsToTreePathes.containsKey(value.toString().substring(5))) {
					super.setForeground(new Color(0,0,255));
			        super.setFont(linkFont);
			        super.setText("<html><u>" + value.toString() + "</u></html>");				    	
				}
				else {
					super.setFont(leafFont);
				}
			}
			else if (value.toString().toUpperCase().startsWith("TONODE: ")) {				    						
				if (mapOfDEFsToTreePathes.containsKey(new LexicographicalString(value.toString().substring(8)))) {
					super.setForeground(new Color(0,0,255));
			        super.setFont(linkFont);
			        super.setText("<html><u>" + value.toString() + "</u></html>");				    	
				}
				else {
					super.setFont(leafFont);
				}				
			}
			else if (value.toString().toUpperCase().startsWith("FROMNODE: ")) {				    				 				
				if (mapOfDEFsToTreePathes.containsKey(new LexicographicalString(value.toString().substring(10)))) {
					super.setForeground(new Color(0,0,255));
			        super.setFont(linkFont);
			        super.setText("<html><u>" + value.toString() + "</u></html>");				    	
				}
				else {
					super.setFont(leafFont);
				}				
			}			
			else {
	        	// kein spezielles Blatt, also normalen Font
	        	super.setFont(leafFont);
	        }		
		}		
		this.setIcon(((SceneTreeNode)value).getElementIcon());
		return comp;
	}	
}

