package misc;

import java.io.File;
import java.util.Hashtable;
import javax.swing.filechooser.FileFilter;
import java.io.FilenameFilter;

/**
 * Diese Klasse stellt eine einfache Implementierung der FileFilter-Klasse dar,
 * um die Anzeige innerhalb eines JFileChooser's auf Dateien zu beschr�nken,
 * deren Dateinamens-Erweiterungen angegeben werden k�nnen.<p>

 * Beispiel: ein Filter, der alle Dateien bis auf gif and jpg in einem FileChooser herausfiltert.<p>
 * 
 * 		JFileChooser chooser = new JFileChooser();<br>
 * 		VarFileFilter filter = new VarFileFilter();<br>
 * 		filter.addExtension("gif");<br>
 * 		filter.addExtension("jpg");<br>
 * 		filter.setDescription("JPEG & GIF Bilder (*.gif, *.jpg)");<br>
 * 		chooser.setFileFilter(filter);<br>
 * 		chooser.showOpenDialog(null);<br>
 *
 *   
 * Zus�tzlich wurde das Interface FilenameFilter implementiert. Dadurch kann der 
 * Datei-Filter auch verwendet werden, um z.B. eine Liste von best. Dateien in einem
 * Verzeichnis zu erhalten. 
 *   
 * Beispie: ein Filter, der alle Dateien bis auf gif in einem Verzeichnis herausfilter und als Liste zur�ckgibt.<p>
 * 
 * 		File myDirectory = new File("./mygifs/");<br>
 *		gifFiles = myDirectory.listFiles(new VarFileFilter("gif"));<br>
 *		int gifCount=gifFiles.length;<br>
 *  
 * @author Henrik Peters, Frederik Suhr
 */
public class VarFileFilter extends FileFilter implements FilenameFilter {
	
	private Hashtable extensions = null;
	private String description = null;
	
	/**
	 * Erstellt eine Instanz der Klasse. F�r den Fall, dass kein Filter
	 * hinzugef�gt wird, werden alle Dateien angezeigt.
	 */
	public VarFileFilter() {
		this.extensions = new Hashtable();
	}
	
	/**
	 * Erstellt eine Instanz der Klasse und f�gt als Filter die angegebene
	 * Dateinamens-Erweiterung extension hinzu.<br>
	 * Beispiel: new VarFileFilter("exe");
	 * 
	 * @param extension die Dateinamens-Erweiterung
	 */
	public VarFileFilter(String extension) {
		this(extension, null);
	}
	
	/**
	 * Erstellt eine Instanz der Klasse und f�gt als Filter die angegebene
	 * Dateinames-Erweiterung extension hinzu, sowie die Beschreibung
	 * description des Filters.
	 * 
	 * @param extension die Dateinames-Erweiterung
	 * @param description die Beschreibung
	 */
	public VarFileFilter(String extension, String description) {
		this();
		if(extension != null) {
			addExtension(extension);
		}
		if(description != null) {
			setDescription(description);
		}
	}
	
	/**
	 * Erstellt eine Instanz der Klasse und f�gt als Filter die im String-Array
	 * extensions �bergebenen ein.<br>
	 * Beispiel: new VarFileFilter(new String[]{"gif", "jpg"});
	 * 
	 * @param extensions die Dateinamens-Erweiterungen
	 */
	public VarFileFilter(String[] extensions) {
		this(extensions, null);
	}
	
	/**
	 * Erstellt eine Instanz der Klasse und f�gt als Filter die im String-Array
	 * extensions �bergebenen ein, sowie die Beschreibung description des
	 * Filters.
	 * 
	 * @param extensions die Dateinamens-Erweiterungen
	 * @param description die Beschreibung
	 */
	public VarFileFilter(String[] extensions, String description) {
		this();
		for(int i = 0; i < extensions.length; i++) {
			addExtension(extensions[i]);
		}
		if(description != null) {
			setDescription(description);
		}
	}
	
	/**
	 * Gibt true zur�ck, wenn die Datei im JFileChooser angezeigt werden soll
	 * oder false, wenn nicht.<p>
	 * 
	 * Diese Methode muss wegen der abstrakten Klasse
	 * javax.swing.filechooser.FileFilter implementiert sein.
	 * 
	 * @param file die zu pr�fende Datei
	 * @return true oder false, ob die Datei angezeigt werden soll
	 */
	public boolean accept(File file) {
		
		if(file != null) {
			if(file.isDirectory()) {
				return true;
			}
			String extension = getExtension(file);
			if(extension != null && extensions.get(extension) != null) {				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gibt true zur�ck, wenn die Datei im nach dem Interface FilenameFilter die gesetzten
	 * Bedingungen erf�llt oder false, wenn nicht.<p>
	 * 
	 * Diese Methode muss wegen dem Interface
	 * java.io.FilenameFilter implementiert sein.
	 * 
	 * @param filepath der zu pr�fende Pfad der Datei
	 * @param filename die zu pr�fende Datei
	 * @return true oder false, ob die Datei die Bedingungen erf�llt
	 */
	public boolean accept(File filepath, String filename) {	
		String extension = getExtension(filename);
		if(extension != null && extensions.get(extension) != null) {
			return true;
		}		
		return false;
	}

	
	/**
	 * Gibt die Beschreibung des Filters zur�ck.<p>
	 * 
	 * Diese Methode muss wegen der abstrakten Klasse
	 * javax.swing.filechooser.FileFilter implementiert sein.
	 * 
	 * @return die Beschreibung des Filters
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * F�gt den String extension zur Liste der anzuzeigenden Dateien hinzu.
	 * 
	 * @param extension die Dateinamens-Erweiterung
	 */
	public void addExtension(String extension) {
		if(extensions == null) {
			extensions = new Hashtable(10);
		}
		extensions.put(extension.toLowerCase(), this);
	}
	
	/**
	 * Setzt den String description als lesbare Beschreibung des Filters.<br>
	 * z.B.: filter.setDescription("Executables (*.exe)");
	 * 
	 * @param description die Beschreibung des Filters
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gibt die Erweiterung des Dateinamens von File file zur�ck.
	 * Verwendung durch �berladen von getExtension(String filename).
	 * 
	 * @param file die zu bearbeitende Datei
	 * @return die Erweiterung der Datei
	 */
	public String getExtension(File file) {
		if(file != null) {
			String filename = file.getName();
			return getExtension(filename);
		}
		return null;
	}
	/**
	 * Gibt die Erweiterung des Dateinamens von String filename zur�ck.
	 * 
	 * @param filename String der zu bearbeitenden Datei
	 * @return die Erweiterung der Datei
	 */
	public String getExtension(String filename) {
		if(filename != null) {			
			int i = filename.lastIndexOf('.');
			if(i > 0 && i < filename.length()-1) {
				return filename.substring(i+1).toLowerCase();
			}
		}
		return null;
	}
}
