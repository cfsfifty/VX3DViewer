package scene3dview;
/**
 * Klasse zum Darstellen einer Szene mit Hilfe eines Browser Plug-Ins. 
 * @author Benjamin Budgenhagen, Henrik Peters
 */
public class Scene3DViewer {
	private String execPath = "";
	private Process p;
	
	/**
	 * Erstellt eine neue Instanz von Scene3DViewer aus einem String browser
	 * und einem String scene.
	 * @param browser String mit dem Pfad zum Browser
	 * @param scene String mit dem Pfad zur darzustellenden Datei
	 */
	public Scene3DViewer(String browser, String scene) {
		this.execPath = browser + " " + "\"" +scene+"\"";
	}
	
	/**
	 * Startet den Browser als Subprocess und lädt die Szene.
	 */
	public void open() {
		try {
			this.p = Runtime.getRuntime().exec(this.execPath);			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Schliesst den Browser wieder, falls er noch läuft.
	 */
	public void close() {
		this.p.destroy();
	}
	
	/*
	public static void main(String[] args) {
		Scene3DViewer dummy = new Scene3DViewer("c:\\Programme\\Internet Explorer\\iexplore.exe","c:\\HelloWorld.x3d");
		dummy.open();
		try {
			Thread.sleep(10000);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		dummy.close();
	}
	*/
}
