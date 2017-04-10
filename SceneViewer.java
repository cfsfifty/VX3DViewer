/*
 * Created on 13.05.2004
 */
import java.io.File;

import javax.swing.SwingUtilities;

import misc.DialogFactory;

import gui.*;

/**
 * Dies ist die Startklasse f�r das gesammte Programm. Dazu laufen
 * folgende Schritte ab: 
 * - Es wird �berpr�ft, ob unter Windows gestartet wurde.
 * - Ein evtl g�ltiger Startparameter, der eine .x3d oder .wrl
 *   zum �ffnen angibt, wird ermittelt.
 * - Im SwingThread eine Instanz des Zentrums der GUI, n�mlich der Desktop
 *   Klasse erzeugt.
 * - Dieser erh�lt dann ggf. noch die File zum Laden aus den Startparametern.
 * 
 * @author SEP VRML97 / X3D Group 
 */
public class SceneViewer {
	
	public static void main(String args[]){
		
		if (isWindows()) { 
			// Programm nur unter Windows lauff�hig		

			String fileName;
			File fileToLoad = null;			
			
			// Startparameter �berpr�fen
			
			String allParameters = "";
							
			for (int i = 0; i < args.length; i++) {
				fileName = args[i].toLowerCase();				
				if (fileName.endsWith(".x3d") || fileName.endsWith(".wrl")) {
					// g�ltige Endung gefunden
					fileToLoad = new File(fileName);
					// pr�fe ob sich datei theoretisch laden l�sst:
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
					// XXX workaround, falls Pfad mit leerzeichen �bergeben wurde als
					// mehrere parameter statt diesen in anf�hrungszeichen zu kapseln
					for (int i = 0; i < args.length; i++) {
						if (i>0)
							allParameters += " ";
						allParameters += args[i];
					}
					DialogFactory df = new DialogFactory(allParameters);
					df.showDialog();
					
					if (allParameters.endsWith(".x3d") || allParameters.endsWith(".wrl")) {
						fileToLoad = new File(allParameters);
						// pr�fe ob sich datei theoretisch laden l�sst:
						if (!fileToLoad.exists()) {						
							fileToLoad = null;	
						}
					}
				}

			}
							
			final File fileToOpen = fileToLoad; // f�r �bergabe an thread			
			
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
								// Datei �ffnen ...
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
			//kein Windows, keine Programmausf�hrung
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
