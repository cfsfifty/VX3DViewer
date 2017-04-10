package misc;

import com.ice.jni.registry.*;
/**
 * Diese Klasse nutzt Methoden der Klasse Registry aus com.ice.jni.registry, um
 * diverse Operationen auf der Windows-Registry auszuführen.<p>
 * 
 * Bisher implementiert ist das Auslesen des in Windows eingestellten
 * Standard-Browsers. 
 * 
 * @author Benjamin Budgenhagen, Henrik Peters
 */
public class RegistryFactory {
	private String browserPath = null;
	
	/**
	 * Erstellt eine Instanz der Klasse. Bei jedem Instanziieren wird
	 * automatisch die Klassenvariable browserPath gesetzt, welche über die
	 * Methode getBrowserPath(); abgefragt werden kann.
	 */
	public RegistryFactory() {
		this.browserPath = getBrowserPathFromRegistry();
	}
	
	/**
	 * Gibt die Klassenvariable browserPath zurück, welche beim Instanziieren
	 * der Klasse aus der Registry eingelesen wurde.
	 * 
	 * @return browserPath String mit dem Pfad zum Standard-Browser
	 */
	public String getBrowserPath() {
		return this.browserPath;
	}
	
	/**
	 * Gibt den Wert eines Keys aus der Registry zurück.<p>
	 * 
	 * z.B.:<br>
	 * String dummy = getRegistryValue(<br>
	 * "HKCU",<br>
	 * "software\\microsoft\\windows\\currentversion\\explorer\\fileexts\\.ex\\openwithlist",<br>
	 * "a"); 
	 * 
	 * @param topKeyName der Name eines TOP-Level-Keys
	 * @param subKeyName der Name eines Sub-Keys
	 * @param valueName der Name des abzufragenden Wertes
	 * @return value String mit dem Wert
	 */
	public static String getRegistryValue(String topKeyName, String subKeyName, String valueName) {
		String ret = null;
		RegistryKey topKey;
		RegistryKey subKey;
		topKey = Registry.getTopLevelKey(topKeyName);
		subKey = Registry.openSubkey(topKey, subKeyName, RegistryKey.ACCESS_READ);
		if(subKey == null) {
			return ret;
		}
		
		try {
			ret = subKey.getStringValue(valueName);
		}
		catch(RegistryException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Gibt, falls vorhanden, den Default-Wert eines Keys aus der Registry
	 * zurück.<p>
	 * 
	 * z.B.:<br>
	 * String dummy = getRegistryValue(<br>
	 * "HKLM",<br>
	 * "software\\classes\\http\\shell\\open\\command\\");
	 * 
	 * @param topKeyName der Name eines TOP-Level-Keys
	 * @param subKeyName der Name eines Sub-Keys
	 * @return value String mit dem Wert
	 */
	public static String getRegistryValue(String topKeyName, String subKeyName) {
		String ret = null;
		RegistryKey topKey;
		RegistryKey subKey;
		topKey = Registry.getTopLevelKey(topKeyName);
		subKey = Registry.openSubkey(topKey, subKeyName, RegistryKey.ACCESS_READ);
		if(subKey == null) {
			return ret;
		}
		
		try {
			if(subKey.hasDefaultValue()) {
				ret = subKey.getDefaultValue();
			}
		}
		catch(RegistryException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Gibt einen String mit dem Pfad zum vom System gesetzten
	 * Default-Browser zurück, welcher über den Registry-Key
	 * HKEY_LOCAL_MACHINE\Software\Classes\http\shell\open\command
	 * ausgelesen wird.
	 * 
	 * @return String mit dem Pfad zum Default-Browser
	 */
	private String getBrowserPathFromRegistry() {
		String browserPath = "";
		
		browserPath = getRegistryValue(
				"HKLM",
				"software\\classes\\http\\shell\\open\\command\\");
		browserPath = browserPath.toLowerCase();
		browserPath = browserPath.replaceAll("\"", "");
		int i = browserPath.indexOf(".exe");
		browserPath = browserPath.substring(0,i+4);
		
		return browserPath;
	}
}
