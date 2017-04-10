package scenejtree;

/**
 * Diese Klasse nimmt einen String auf, sortiert ihn aber
 * mittels compareTo ohne Beachtung von Gross- / und Kleinbuchstaben.
 * Wird verwendet, um eine sortierte Füllung der ComboBox für
 * die Def-Knoten zu gewährleisten.
 * 
 * (evtl. wäre eine Implementierung über ListenModell besser)
 * 
 * Created on 07.06.2004
 * 
 * @author Frederik Suhr
 */
public class LexicographicalString implements Comparable {
	String string;
	
	/**
	 * Konstruktor für leeren String
	 */
	public LexicographicalString () {
		this ("");
	}

	/**
	 * Konstruktor für Speichern eines Strings.
	 * 
	 * @param string zum speichern
	 */
	public LexicographicalString(String string) {
		this.string=string;
	}
	/**
	 * Überschriebenes compareTo. Führt einen Vergleich ohne Beachtung der
	 * Gross-/Kleinschreibung durch.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object obj) {
		return this.string.compareToIgnoreCase(((LexicographicalString)obj).toString());
	}
	/**
	 * Gibt den abgelegten String zurück.
	 */
	public String toString() {
		return string;
	}	
}
