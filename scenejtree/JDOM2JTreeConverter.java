
package scenejtree;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.jdom.Document;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Die Klasse JDOM2JTreeConverter orientiert sich an dem JTreeOutputter
 * aus dem JDOM Contrib package, angepasst an die Aufgabenstellung.
 * Sie repr�sentiert einen Adapter, der Elemente aus einem
 * JDOM Document als Knoten an einen JTree h�ngt.  
 * Dazu wird einfach die Klasse instanziiert und mittels der Methode
 * output(Decument,DefaultMutableTreeNode) mit der Wurzel beginnend
 * der ganzen Baum durchlaufen und in den Tree eingef�gt.
 * 
 * @author SEP VRML97 Group
 */
public class JDOM2JTreeConverter {

    private NodeIconLoader myNodeIconLoader;
    private ImageIcon attributeIcon;
	/**
     * Konstruktor zur Initialisierung der Klasse
     */
    public JDOM2JTreeConverter() {
    	myNodeIconLoader = new NodeIconLoader();
    	attributeIcon = new ImageIcon("./scenejtree/ICONS/ANode.gif");
    }
	
    /**
     * Methode, die das Element "Scene" aus dem Document sucht und als
     * Kind Element an die Wurzel in den SceneTreeNode Baum abbildet 
     * 
     * @param doc JDOM Dokument von X3D Datei
     * @param root Knoten , an den die Knoten geh�ngt werden sollen
     */
    public void output(Document doc, SceneTreeNode root) {
        // startet erst ab Scene
    	processElement(doc.getRootElement().getChild("Scene"), root);
    }

    /**
     * Methode, die Element und TreeNode an die Methode processElement weitergibt.
     * 
     * @param element zu bearbeitendes Element aus dem JDOM
     * @param root Knoten des JTree, an den die Knoten geh�ngt werden sollen
     */    
    public void output(Element element, SceneTreeNode root) {
        processElement(element, root);
    }

    /**
     * Diese Methode sorgt daf�r, dass das �bergebene Element als Kind
     * an den �bergebenen TreeNode geh�ngt wird. Gleichzeitig werden hier
     * die Attribute mit angeh�ngt durch den Aufruf von processAttributes.
     * Schlie�lich wird eine Rekursion f�r Kindknoten gestartet.
     * 
     * @param element zu bearbeitendes Element aus dem JDOM
     * @param root Knoten des JTree, an den die Knoten geh�ngt werden sollen
     */        
    protected void processElement(Element element, SceneTreeNode treeNode) {
        
    	String displayName;
    	String elementName;
    	    	
    	elementName = element.getName();
    	
    	// wenn DEF Attribut vorhanden an Knotennamen anf�gen:    	
    	String strDEF;
    	
    	// ggf USE speichern
        String strUSE;
    	
    	strDEF=element.getAttributeValue("DEF");
        if (strDEF == null || strDEF.equals("")) {
        	displayName = elementName;
        }
        else {
        	displayName = elementName + " (" + strDEF + ")";
        }

    	strUSE = element.getAttributeValue("USE");
        if (strUSE == null) {
        	strUSE = "";
        }

        
        // Knoten erzeugen
    	SceneTreeNode localTreeNode = 
            new SceneTreeNode(displayName, elementName, strDEF, strUSE,
					elementName.toUpperCase().equals("ROUTE") ? false : true,
					myNodeIconLoader.getIconForNodeName(elementName));        
        
    	// String elementText = element.getTextNormalize(); // kommt bei x3d nicht vor, text zwischen tags
                
        // Attribte des Knoten als Kinder hinzuf�gen
        processAttributes(element, localTreeNode);

        // Kinder durchlaufen
        Iterator iterator = element.getChildren().iterator();

        while (iterator.hasNext()) {
            Element nextElement = (Element)iterator.next();
            processElement(nextElement, localTreeNode);
        }
        
        // Knoten dem JTree hinzuf�gen
        treeNode.addSceneTreeNode(localTreeNode);
    }

    /**
     * Diese Methode sorgt daf�r, dass die Attribute des �bergebenen Elementes
     * als Kinder an den �bergebenen TreeNode geh�ngt werden.
     * 
     * @param element zu bearbeitendes Element aus dem JDOM
     * @param root Knoten des JTree, an den die Knoten geh�ngt werden sollen
     */        

    protected void processAttributes(Element element, SceneTreeNode treeNode) {
        
    	// Attribute durchlaufen
    	Iterator attributeIterator = element.getAttributes().iterator();
        while (attributeIterator.hasNext()) {
        	Attribute attribute = (Attribute)attributeIterator.next();
        	// containerField �berspringen (enth�lt keine n�tzlichen Information)
        	if (attribute.getName().equals("containerField")) continue;
            // Attribut an den Knoten anh�ngen
        	String elementName = attribute.getName();
        	String displayName = elementName + ": "+attribute.getValue();
        	
        	ImageIcon elementIcon = attributeIcon;
        	
        	if (elementName.toUpperCase().equals("DEF"))
        		elementIcon = myNodeIconLoader.getIconForNodeName(elementName);
        	else if (elementName.toUpperCase().equals("USE"))
        		elementIcon = myNodeIconLoader.getIconForNodeName(elementName);        	
        	else if (elementName.toUpperCase().equals("FROMNODE"))				    
        		elementIcon = myNodeIconLoader.getIconForNodeName("route_out"); 
        	else if (elementName.toUpperCase().equals("TONODE"))				    
        		elementIcon = myNodeIconLoader.getIconForNodeName("route_in"); 

        	SceneTreeNode node = 
                new SceneTreeNode(displayName, elementName, "", "", false, elementIcon);        	
            
            treeNode.addSceneTreeNode(node);
        }
    }
}

