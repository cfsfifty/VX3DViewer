package scenejtree;

/**
 * Diese Klasse nimmt einen String auf, sortiert ihn aber
 * mittels compareTo ohne Beachtung von Gross- / und Kleinbuchstaben.
 * Wird verwendet, um eine sortierte F�llung der ComboBox f�r
 * die Def-Knoten zu gew�hrleisten.
 * 
 * (evtl. w�re eine Implementierung �ber ListenModell besser)
 * 
 * Created on 07.06.2004
 * 
 * @author Frederik Suhr
 */
public class LexicographicalString implements Comparable {
	String string;
	
	/**
	 * Konstruktor f�r leeren String
	 */
	public LexicographicalString () {
		this ("");
	}

	/**
	 * Konstruktor f�r Speichern eines Strings.
	 * 
	 * @param string zum speichern
	 */
	public LexicographicalString(String string) {
		this.string=string;
	}
	/**
	 * �berschriebenes compareTo. F�hrt einen Vergleich ohne Beachtung der
	 * Gross-/Kleinschreibung durch.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object obj) {
		return this.string.compareToIgnoreCase(((LexicographicalString)obj).toString());
	}
	/**
	 * Gibt den abgelegten String zur�ck.
	 */
	public String toString() {
		return string;
	}	
}
