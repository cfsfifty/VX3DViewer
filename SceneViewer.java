/*
 * Created on 13.05.2004
 */
import java.io.File;

import javax.swing.SwingUtilities;

import misc.DialogFactory;

import gui.*;

/**
 * Dies ist die Startklasse für das gesammte Programm. Dazu laufen
 * folgende Schritte ab: 
 * - Es wird überprüft, ob unter Windows gestartet wurde.
 * - Ein evtl gültiger Startparameter, der eine .x3d oder .wrl
 *   zum Öffnen angibt, wird ermittelt.
 * - Im SwingThread eine Instanz des Zentrums der GUI, nämlich der Desktop
 *   Klasse erzeugt.
 * - Dieser erhält dann ggf. noch die File zum Laden aus den Startparametern.
 * 
 * @author SEP VRML97 / X3D Group 
 */
public class SceneViewer {
	
	public static void main(String args[]){
		
		if (isWindows()) { 
			// Programm nur unter Windows lauffähig		

			String fileName;
			File fileToLoad = null;			
			
			// Startparameter überprüfen
			
			String allParameters = "";
							
			for (int i = 0; i < args.length; i++) {
				fileName = args[i].toLowerCase();				
				if (fileName.endsWith(".x3d") || fileName.endsWith(".wrl")) {
					// gültige Endung gefunden
					fileToLoad = new File(fileName);
					// prüfe ob sich datei theoretisch laden lässt:
					if (!fileToLoad.exists()) {						
						fileToLoad = null;	
					}
					else {						
						// alles ok, fertig zum laden
						break;
					}
				}				
			}
			
			if (fileToLoad == null) {
				if (args.length>1) {
					// XXX workaround, falls Pfad mit leerzeichen übergeben wurde als
					// mehrere parameter statt diesen in anführungszeichen zu kapseln
					for (int i = 0; i < args.length; i++) {
						if (i>0)
							allParameters += " ";
						allParameters += args[i];
					}
					DialogFactory df = new DialogFactory(allParameters);
					df.showDialog();
					
					if (allParameters.endsWith(".x3d") || allParameters.endsWith(".wrl")) {
						fileToLoad = new File(allParameters);
						// prüfe ob sich datei theoretisch laden lässt:
						if (!fileToLoad.exists()) {						
							fileToLoad = null;	
						}
					}
				}

			}
							
			final File fileToOpen = fileToLoad; // für übergabe an thread			
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//erzeugen des Desktops
					final Desktop desktop = new Desktop();
					desktop.setVisible(true);
					
					// soll eine Datei geladen werden:
					if (fileToOpen != null) {
						desktop.setLoading(true); // wartecursor
						Thread loading = new Thread(new Runnable() {						
							public void run() {									
								// Datei öffnen ...
								desktop.doc = desktop.opener.openFile(fileToOpen);						
								
								// ... und in der gui anzeigen
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										desktop.fileOpened();	 
									}
								});									 
							}
						});
						loading.start();
					}
				}		
			});			
		}
		else {
			//kein Windows, keine Programmausführung
			System.err.println("This programm is intendend to run under Windows only!");
			System.exit(1);
		}
	}
	/**
	 * Methode ermittelt, ob die Anwendung unter windows gestartet wurde.
	 * 
	 * @return true, wenn das Betriebsystem Windows ist
	 */
	private static boolean isWindows() {
		String osName = System.getProperty("os.name");				
		return osName.toLowerCase().startsWith("windows");		
	}

}
