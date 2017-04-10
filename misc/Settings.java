package misc;
import java.util.*;
import java.io.*;

/**
 * Klasse zum Laden, Speichern und zur Verfügung stellen der in einer Datei
 * gespeicherten Einstellungen. Alle benötigten Einstellungen liegen als
 * Klassenvariablen mit default-Werten vor und werden für den Fall, dass die
 * Einstellungs-Datei nicht vorhanden ist bzw. Einstellungen fehlen, verwendet.
 *  
 * @author Benjamin Budgenhagen, Henrik Peters
 */
public class Settings extends Properties {
	
	//Klassenvariablen für Einstellungen
	private String pathToIni = "";
	private String browserPath = "";
	private int MRUAnzahl = 4;
	private ArrayList lastOpenedFiles = new ArrayList();
	
	/**
	 * Erstellt eine neue Instanz von Settings aus einem String pathToIni.
	 * 
	 * @param pathToIni Pfad zur Ini-Datei 
	 */
	public Settings(String pathToIni) {
		super();
		this.pathToIni = pathToIni;
		this.load(pathToIni);
	}
	
	/**
	 * Lädt die Ini-Datei und setzt ggf. Default-Werte.
	 * 
	 * @param pathToIni Pfad zur Ini-Datei
	 */
	public void load(String pathToIni) {
		try {
			FileInputStream iniFile = new FileInputStream(pathToIni);
			super.load(iniFile);
			
			/*
			 * Es wird überprüft, ob überhaupt alle benötigten Einstellungen
			 * vorhanden sind.
			 */
			
			/*
			 * Einstellung: browserPath
			 * 
			 * this.getProperty("browserPath") == null
			 * this.getProperty("browserPath").equals("")
			 */
			if(this.getProperty("browserPath") == null ||
					this.getProperty("browserPath").equals("")) {
				/*
				 * Falls in der .ini-Datei keine Einstellung für browserPath
				 * gefunden wird, wird versucht, den Default-Browser aus der
				 * Registry auszulesen.
				 */
				RegistryFactory registry = new RegistryFactory();
				String browserPathFromRegistry = registry.getBrowserPath();
				if(browserPathFromRegistry != null) {
					/*
					 * War das Auslesen aus der Registry erfolgreich, wird
					 * der Default-Browser in die .ini-Datei geschrieben.
					 */
					this.setProperty("browserPath",browserPathFromRegistry);
				}
				else {
					/*
					 * War das Auslesen aus der Registry nicht erfolgreich,
					 * wird der Default-Wert der Klasse in die .ini-Datei
					 * geschrieben.
					 */
					this.setProperty("browserPath",browserPath);
				}
			}
			else {
				File browser = new File(this.getProperty("browserPath"));
				if(!browser.exists()) {
					/*
					 * Abarbeiten der RegistryFactory-Methoden (s.o.)
					 */
					RegistryFactory registry = new RegistryFactory();
					String browserPathFromRegistry = registry.getBrowserPath();
					if(browserPathFromRegistry != null) {
						this.setProperty("browserPath",browserPathFromRegistry);
					}
					else {
						this.setProperty("browserPath",browserPath);
					}
				}
			}
			
			/*
			 * Einstellung: MRUAnzahl
			 */
			if(this.getProperty("MRUAnzahl") == null) {
				/*
				 * Falls in der .ini-Datei keine Einstellung für MRUAnzahl
				 * gefunden wird, wird die Standardgroesse gesetzt.
				 */
				this.setProperty("MRUAnzahl",""+this.MRUAnzahl);
			}
			else {
				/*
				 * Der Wert aus der .ini-Datei wird verwendet.
				 */
				this.MRUAnzahl = Integer.parseInt(this.getProperty("MRUAnzahl"));
			}
			
			/*
			 * Einstellung: lastOpenedFiles
			 */
			for(int i = 1; i <= this.MRUAnzahl; i++) {
				if(getFileFromIni("mru"+i) != null) {
					if(!this.lastOpenedFiles.contains(getFileFromIni("mru"+i))) {
						this.lastOpenedFiles.add(getFileFromIni("mru"+i));
					}
				}
			}
		}
		catch(FileNotFoundException e) {
			this.setProperty("browserPath",browserPath);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert die Ini-Datei.
	 * 
	 * @param pathToIni Pfad zur Ini-Datei
	 */
	public void store(String pathToIni) {
		try {
			FileOutputStream iniFile = new FileOutputStream(pathToIni);
			super.store(iniFile,pathToIni);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gibt die aktuelle MRU-Liste zurück. 
	 * 
	 * @return das Array mit der MRU-Liste
	 */
	public File[] getLastOpenedFiles() {
		return (File[])this.lastOpenedFiles.toArray(new File[lastOpenedFiles.size()]);
	}
	
	/**
	 * Die Methode fügt der MRU-Liste eine neue Datei hinzu und gibt das neue
	 * Array mit der MRU-Liste zurück
	 * 
	 * @param file String mit dem vollständigen Pfad zur Datei
	 * @return das Array mit der MRU-Liste
	 */
	public File[] setLastOpenedFile(String file) {
		File addFile = new File(file);
		
		if(!this.lastOpenedFiles.contains(addFile)) {
			this.lastOpenedFiles.add(0,addFile);
		}
		else {
			this.lastOpenedFiles.remove(this.lastOpenedFiles.indexOf(addFile));
			this.lastOpenedFiles.add(0,addFile);
		}
		
		if(this.lastOpenedFiles.size() > this.MRUAnzahl) {
			this.lastOpenedFiles.remove(this.lastOpenedFiles.size()-1);
		}
		
		for(int i = 1; i <= this.lastOpenedFiles.size(); i++) {
			this.setProperty("mru"+i, this.lastOpenedFiles.get(i-1).toString());
			
		}
		
		this.store(this.pathToIni);
		return getLastOpenedFiles(); 
	}
	
	/**
	 * Die interne Methode wird verwendet, um Pfad-Angaben zu Dateien
	 * automatisch auf ihre Korrektheit hin zu überpruefen (ist die
	 * Einstellung überhaupt gesetzt, existiert die Datei, etc.)
	 * 
	 * @param propname Name der Einstellung in der .ini-Datei
	 * @return File Objekt vom Typ File, falls Datei korrekt
	 */
	private File getFileFromIni(String propname) {
		if(this.getProperty(propname) == null ||
				this.getProperty(propname).equals("")) {
			/*
			 * Falls die Einstellung in der .ini-Datei nicht gefunden wird,
			 * wird null zurückgegeben.
			 */
			return null;
		}
		else {
			/*
			 * Wird die Einstellung gefunden, wird überprüft, ob die Datei
			 * existiert.
			 */
			File file = new File(this.getProperty(propname));
			if(!file.exists()) {
				return null;
			}
			else {
				return file;
			}
		}
	}
}
