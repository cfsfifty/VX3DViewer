package parser;

import misc.*;

import javax.swing.*;

import java.awt.Component;
import java.io.File;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import javax.swing.filechooser.FileFilter;
/**
 * Diese Klasse sorgt über die open-Methode für das Laden einer
 * .wrl oder .x3d in ein JDOM Document. Bei einer .wrl wird eine 
 * Konvertierung in .x3d angestossen.
 * 
 * Created on 25.05.2004
 *
 * @author Timo Winkelvos, fs, pk, ph
 * 
 */
public class SceneFileOpener {
	
	/**
	 * Zwischenspeicher für zuletzt angesurftes Verzeichnis im FileChooser
	 */
	public File lastFCDir;
	/**
	 * Aus xml-Datei per SAX ein JDom Document Bauen.
	 */
	private SAXBuilder saxBuilder;
	/**
	 * Dateiname und Pfad zur Eingabedatei.
	 */
	public String fileName, filePath;
	/**
	 * Vollst. Dateiname(inkl. Pfad)
	 */
	private String completeFileName; 
	/**
	 * laden erfolgreich abgeschlossen?
	 */
	private boolean boolFileReady=false;
	
	/**
	 * Leerer Konstruktor reicht für Laden der Klasse. Alles weitere übernimmt die
	 * open-Methode. 
	 */
	public SceneFileOpener(){	
	}
	
	/**
	 * Öffnen einer x3D-Datei, parsen in ein JDOM Document.
	 * 
	 * @param x3dFileToOpen DateiObjekt der zu öffnenden Datei.
	 * @return geparstes Dokument.
	 */
	public Document openx3dFile(File x3dFileToOpen){
		Document doc = new Document();
						
		try {
			// Parsen der gewählten X3D File
			saxBuilder = new SAXBuilder();
			saxBuilder.setEntityResolver(new DoctypeEntityResolver());
			doc = saxBuilder.build(x3dFileToOpen);			
		} 
		catch(Exception e) {
			e.printStackTrace();
			doc = null;
		}
		return doc;
	}
	/**
	 * Öffnen einer wrl-Datei, dazuu in x3d konvertieren und dann über
	 * openx3dFile öffnen.
	 * 
	 * @param wrlToOpen DateiObjekt der zu öffnenden Datei.
	 * @return geparstes Dokument.
	 */
	public Document openwrlFile(File wrlToOpen) {
		String x3dtempfile =("temp.x3d");
		//temporaere X3D Datei wird wieder geloescht
		File temp = new File(x3dtempfile);
		temp.deleteOnExit();
		
		Converter conv = new Converter(wrlToOpen.getPath(), x3dtempfile);
	    if (conv.convert)
	    	return openx3dFile(temp);
	    else
	    	return null;
	}
      
	/**
	 * Öffnen einer .x3d oder .wrl Datei, ruft die korrepsondierende Methode auf.
	 * 
	 * @param fileToOpen DateiObjekt der zu öffnenden Datei.
	 * @return geparstes Dokument.
	 */
	public Document openFile(File fileToOpen){			
		boolFileReady = false;
		Document doc = new Document();
		
		if (fileToOpen.exists()) {
			if(fileToOpen.toString().toLowerCase().endsWith(".x3d")) {			
				doc = openx3dFile(fileToOpen);
			}
			else if(fileToOpen.toString().toLowerCase().endsWith(".wrl")) {			
				doc = openwrlFile(fileToOpen);
			}
			else {
				//unbekannte endung.
				System.err.println("Unkonwn file extension on: " + fileToOpen.toString());
				doc = null;
			}
			
			if (doc != null) {
				// öffnen erfolgreich
				updateFileNameAndPath(fileToOpen);						
				boolFileReady = true;
			}
			else {
				boolFileReady = false;					
				System.err.println("Opening file failed. Failure on parsing to document.");
        		DialogFactory error = new DialogFactory(null,
        				"Error occured",
						"Opening file failed. Failure on parsing document.", DialogFactory.ERROR_DIALOG);
        		error.showDialog();        			
			}
		}
		else {
			// datei existiert nicht
			boolFileReady = false;
			doc = null;
			System.err.println("Opening file failed. File not existing: " + fileToOpen.toString());
    		DialogFactory error = new DialogFactory(null,
    				"Error occured",
					"File not existing: " + fileToOpen.toString(), DialogFactory.ERROR_DIALOG);
    		error.showDialog();        			
		}
		return doc;		
	}
	
	/**
	 * Speichern von Dateinamen und Pfad in den Instanzvariablen.
	 */
	private void updateFileNameAndPath(File fileOpened) {
		// Wurzelknoten JTree benannt nach dem Dateinamen
		fileName = fileOpened.getName();
		completeFileName = fileOpened.getPath();		
	}

	/**
	 * Öffnen über FileChooser Dialog.
	 * 
	 * @param modalParent Fenster wird modal zu dieser Komponente
	 * @return geparstes Dokument.
	 */
	public Document openFile(Component modalParent){						
        //Datei öffnen Dialog von Swing
	    JFileChooser fc = new JFileChooser();
	    fc.setDialogTitle("Select an X3D or VRML-File");
            
        //Dateiauswahl beschränken
	    VarFileFilter filterStandard = new VarFileFilter();
	    filterStandard.addExtension("wrl");
	    filterStandard.addExtension("x3d");
	    filterStandard.setDescription("VRML97-/X3D-Scene Files (*.wrl, *.x3d)");
	    
	    VarFileFilter filterX3D = new VarFileFilter("x3d");
	    filterX3D.setDescription("X3D-Scene Files (*.x3d)");
	    
	    VarFileFilter filterVRML = new VarFileFilter("wrl");
	    filterVRML.setDescription("VRML97-Scene Files (*.wrl)");
	    
	    FileFilter filterAllFiles = fc.getAcceptAllFileFilter();

        //erst *.x3d, dann *.wrl
	    fc.addChoosableFileFilter(filterStandard);
	    fc.addChoosableFileFilter(filterX3D);
	    fc.addChoosableFileFilter(filterVRML);
	    //aktueller filter:
	    fc.setFileFilter(filterStandard);
        //*.* entfernen
	    fc.removeChoosableFileFilter(filterAllFiles);
        
        
	    // Rückspeicherung aktuelles Verzeichnis, sofern gesetzt
		if (lastFCDir!=null) {                 	
			fc.setCurrentDirectory(lastFCDir);
		}
		int returnVal = fc.showDialog(modalParent, "Load");
		if (returnVal == 0) {
			//aktuelles verzeichnis zwischenspeichern
			lastFCDir=fc.getCurrentDirectory();					
			return openFile(fc.getSelectedFile());
		}
		else {
			boolFileReady=false;
			return null;			
		}
	}
	
	/**
	 * Gibt den Pfad der zuletzt geparsten Datei zurück.
	 * @return Pfad zu Datei
	 */
	public String getFilePath(){
		return completeFileName;
	}
	/**
	 * Gibt den Dateinamen der zuletzt geparsten Datei zurück.
	 * @return Dateiname
	 */
	
	public String getFileName() {
		return fileName;
	}

	/**
	 * Prüfflag, ob Laden erfolgreich war.
	 * 
	 * @return true, wenn datei erfolgreich geöffnet
	 */
	public boolean isFileReady() {
		return boolFileReady;
	}
}
