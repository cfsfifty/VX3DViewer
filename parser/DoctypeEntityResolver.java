package parser;

import java.io.File;

import org.xml.sax.*;

/**
 * Diese Klasse nutzt den EntityResolver um vom Parser benötigte Referenzen auf 
 * extern vorhandene DTDs an eine andere Stelle zu leiten.
 * 
 * @author Patrick Helmholz
 */

public class DoctypeEntityResolver implements EntityResolver {
	
	// Pfade der einzelnen DTDs, die genutzt werden, relative location im projekt
	/**
	 * Pfad zur "x3d-3.0.dtd"
	 */
	private final String X3D_DTD = "./parser/dtd/x3d-3.0.dtd";
	/**
	 * Pfad zur "x3d-3.0-Web3dExtensions.dtd"
	 */
	private final String EXTENSIONS_DTD = "./parser/dtd/x3d-3.0-Web3dExtensions.dtd";
	/**
	 * Pfad zur "x3d-3.0-InputOutputFields.dtd"
	 */
	private final String FIELDS_DTD = "./parser/dtd/x3d-3.0-InputOutputFields.dtd";
	
	// sax input sourcen der DTDs, werden einmalig im konstruktor geladen
	/**
	 * Input source zu "x3d-3.0.dtd"
	 */
	private InputSource inputSource_X3D_DTD;
	/**
	 * Input source zu "x3d-3.0-Web3dExtensions.dtd"
	 */
	private InputSource inputSource_EXTENSIONS_DTD;
	/**
	 * Input source zu "x3d-3.0-InputOutputFields.dtd"
	 */
	private InputSource inputSource_FIELDS_DTD;
	
	// Konstruktor, lädt die files for
	/**
	 * Konstruktor für den neuen EntityResolver. Er legt
	 * die Inputsourcen zu den DTDs an, mittels der
	 * Hilfsmethode loadSource();
	 */
	public DoctypeEntityResolver() {		
		inputSource_X3D_DTD = loadSource(X3D_DTD);
		inputSource_EXTENSIONS_DTD = loadSource(EXTENSIONS_DTD);
		inputSource_FIELDS_DTD = loadSource(FIELDS_DTD);
	}	
	/**
	 * überschriebene Methode, um für anfragen auf DTDs die lokalen abgelegten zu liefern
	 */
	public InputSource resolveEntity(String publicID, String systemID) {

		if (systemID.endsWith("-3.0.dtd")) {
			return inputSource_X3D_DTD;
	    }
	    if (systemID.endsWith("-Web3dExtensions.dtd")) {
	    	return inputSource_EXTENSIONS_DTD;
	    }
	    if (systemID.endsWith("-InputOutputFields.dtd")) {
	    	return inputSource_FIELDS_DTD;
	    }
	    else {
	      return null; //TODO: Hier ggf. ladeversuch oder zumindest meldung
	    }	   		   
	}
	/**	
	 * Hilfsmethode
	 * wandelt den Pfad zunächst in einen absoluten Pfad um,
	 * um darauf eine SAX InputSource zurückzugeben
	 * funktioniert somit in Java 1.4 _und_ 1.5. 
	 * Zumindest in der momentan aktuellen Version von Java 1.5 schien es
	 * sonst bei InputSource zu unvorhersehbaren Fehlern zukommen.
	 * @param relativeFileName Pfad, der absolut gemacht werden soll
	 */
	public InputSource loadSource(String relativeFileName) {	
		File file = new File(relativeFileName);
		return new InputSource(file.getAbsolutePath());
	}
} 
	