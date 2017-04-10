/*
 * 
 *
 */
package scenejtree;

import java.io.File;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import misc.VarFileFilter;

/**
 * Diese Klasse l�dt alle GIF Icons (gefiltert durch VarFileFilter)
 * aus dem Unterverzeichnis x3dEditIcons in eine effizient
 * durchsuchbare (da sortierte) TreeMap.
 * 
 * Die Methode getIconForNodeName() kann dann ein vom Namen her
 * zu einem Knoten passendes Icon zur�ckgeben. Wenn es kein
 * passendes Icon gibt, wird Null zur�ckgegeben.
 * 
 * Die Icons werden dann vom JDOM2JTreeConver in die SceneTreeNodes eingef�gt
 * und im SceneGraphRenderer zur Anzeige vorbereitet.
 * 
 * Created on 30.05.2004
 * @author Frederik Suhr, Patrick Helmholz
 */
public class NodeIconLoader {
	/**
	 * Konstante f�r Speicherstelle der icons
	 */
	private final String ICON_PATH="./scenejtree/x3dEditIcons/";			
	/**
	 * cache f�r geladene Icons
	 */
	private TreeMap nodeIconTreeMap;
	/**
	 * Variable f�r ein allgemeines Node-Icon, sollte
	 * kein spezielles gefunden werden k�nnen
	 */
	private ImageIcon sceneNodeIcon; 				
	
	/**
	 * Konstruktor, initialisiert alle Node Icons.
	 */
	public NodeIconLoader() {
		loadNodeIcons();
		sceneNodeIcon = new ImageIcon("./scenejtree/ICONS/Node.gif");
	}
	/**
	 * Diese Methode l�dt die Icons in die TreeMap. Es werden nur .gif Dateien
	 * im ICON_PATH geladen.
	 */
	private void loadNodeIcons() {
		// zur�cksetzen der TreeMap
		nodeIconTreeMap=new TreeMap();
						
		// basis verzeichnis f�r knoten icons �ffnen
		File iconDirectory=openFile(ICON_PATH);

		// laden aller icons in ein File array		
		File iconFiles[] = iconDirectory.listFiles(new VarFileFilter("gif"));
						
		// durchlaufen aller Icons, den Namen formatieren und samt ImageIcon
		// in die TreeMap speichern.
		
		String tmpIconName = "";
		for (int i=0; i < iconFiles.length; i++) {
			tmpIconName = iconFiles[i].getName().toLowerCase();
			tmpIconName = tmpIconName.substring(0, tmpIconName.lastIndexOf("."));			
			nodeIconTreeMap.put(tmpIconName, new ImageIcon(iconFiles[i].getPath()));			
		}
	}
	/**
	 * Methode �ffnet eine Datei.
	 * 
	 * @param path Pfad/Dateiname der zu �ffnenden Datei
	 * @return File Objekt der Datei bei Erfolg, sonst null (oder erstmal exit();)
	 */
	private File openFile(String path) {
		try {
			return new File(path);			
		}
		catch (Exception e) {
			System.out.println("Error in opening " + path);
			System.out.println(e);
			e.printStackTrace();
			System.exit(1);
			return null;
		}		
	}
	/**
	 * Diese Methode sucht in dem Instanzarray nach einer Entsprechung
	 * zum �bergebenen Knotennamen.
	 * 
	 * @param nodeName Name des Knotens im JTree
	 * @return Icon, passend zum Nodenamen, oder allg. sceneNodeIcon, wenn keines gefunden
	 */
	public ImageIcon getIconForNodeName(String nodeName) {
        String tmpName = nodeName.toLowerCase();        
        ImageIcon foundIcon;
        
        // wenn der DEF name mittels " (..." angef�gt ist, wieder abschneiden
        int positionWhitespace = tmpName.indexOf(" ");
        
        if (positionWhitespace>=0) {
        	tmpName=tmpName.substring(0,positionWhitespace);        
        }
        
        foundIcon = (ImageIcon)nodeIconTreeMap.get(tmpName);
        if (foundIcon == null)
        	foundIcon = sceneNodeIcon;        
        return foundIcon;
	}	 
}
