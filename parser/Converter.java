package parser;

import iicm.vrml.pw.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Die Klasse Converter orientiert sich an der Klasse Vrml97ToX3d (von Qiming Wang)
 * aus dem JDOM Contrib Packet. Sie wurde modifiziert und der Aufgabenstellung 
 * angepasst.
 * Sie wandelt eine VRML Datei in eine X3D Datei um. Es werden zwei Strings 
 * uebergeben, der erste ist der Name der VRML Datei, und der zweite ist 
 * der Name der resultierenden X3D Datei.
 * 
 * @author SEP VRML97 Group, insbesondere pk
 */

public class Converter{
	/**
   	 * Umwandeln erfolgreich abgeschlossen?
   	 */
	boolean convert = false;
	
  	/**
	 * Umwandelvorgang starten.
	 *  
	 * @param wrlfile Dateiname der uebergebenden VRML Datei
	 * @param x3dfile Dateiname der resultierenden X3D Datei
	 */
  	
  public Converter (String wrlfile, String x3dfile){

  	try{    
      VRMLparser parser = new VRMLparser (Decompression.filterfile (wrlfile));
      GroupNode root = parser.readStream ();
     
      if (root != null){
      	
      	FileOutputStream file = new FileOutputStream (x3dfile);
	  
      	//wrapper
      	PrintStream os = new PrintStream (file);  // wrapper
      	
      	//writing x3d data  ===");
      	writeX3dHeader (os, x3dfile);
      	//root.writeX3dNodes (parser, os); //vorsichtig beim aus-/einkommentieren, die file war doppelt drin
      	//root.writeX3dNodes (parser, os, 1, 1);
      	root.writeX3dNodes (parser, os, 1, 0);
      	writeX3dEnd (os);

      	os.close ();
      	
      	convert = true;      	      	
      }
      else{
        System.out.println ("error on parsing " + wrlfile);
        if (parser.getVersion () == 0.0f)
          System.out.println ("unrecognized header");
      }
    }
    catch (IOException e)
    {
      System.out.println ("error on reading " + wrlfile);
      System.out.println (e.getMessage ());  
    }
  }
  
  /**
   * Schreibt den Kopf der X3D Datei. Hier kann man auch mit den Tags
   * <header> </header> zusatzinformation in die X3D Datei einfuegen 
   * 
   * @param w Der Stream in den reingeschrieben wird
   */

  public static void writeX3dHeader(PrintStream w, String x3dFileName) {
   
    w.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"); // weil hier kein unicode erzeugt wird


//  transitional doctype
    w.print("<!DOCTYPE X3D PUBLIC \"http://www.web3d.org/specifications/x3d-3.0.dtd\" \"file:///www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd\">\n");
    w.print("<X3D profile=\"Full\">\n");
    w.print("  <head>\n");
    w.print("    <meta name=\"filename\" content=\"" + x3dFileName + "\"/>\n");
    
    Date time = new Date(System.currentTimeMillis());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    w.print("    <meta name=\"created\" content=\""+ dateFormat.format(time) + "\"/>\n");
    w.print("    <meta name=\"translated\" content=\"" + dateFormat.format(time) + "\"/>\n");
    w.print("    <meta name=\"revised\"    content=\"" + dateFormat.format(time) + "\"/>\n");
    
    w.print("    <meta name=\"generator\" content=\"Vrml97ToX3dNist, http://ovrt.nist.gov/v2_x3d.html\"/>\n");
    w.print("  </head>\n");
   
    w.print("  <Scene>\n");        
  }

  
  /**
   * Hier wird der Kopf wieder geschlosen
   * 
   * @param w Der Stream in den reingeschrieben wird
   */
  public static void writeX3dEnd(PrintStream w) {
    w.print("  </Scene>\n\n");
    w.print("</X3D>\n");
  }
}

