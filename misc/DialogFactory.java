/*
 * Created on 16.06.2004
 *
 */
package misc;

import java.awt.Dialog;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Diese Klasse gibt eine einfache Möglichkeit, 
 * 
 * - einen Informationsdialog anzuzeigen   "Nachricht + OK" (Info Symbol)
 * - einen Fragedialog anzuzeigen          "Nachricht + YES=true und NO=false" (Frage Symbol)
 * - einen Fehlermeldungsdialog anzuzeigen "Nachricht + OK" (Fehler Symbol)
 * 
 * Dazu gibt es mehrere Konstruktoren, und set Methoden, zum einstellen des Dialogen
 * 
 * aufgerufen wird der Dialog über showDialog();
 * 
 * Beispiel:
 * 
 * 		DialogFactory myDialog = new DialogFactory("Überschrift","Nachricht\n\nbla",DialogFactory.QUESTION_DIALOG);
 *		System.out.println(myDialog.showDialog());
 * 
 * @author Frederik Suhr
 */  
public class DialogFactory {
	// Möglichkeiten für die verschiedenen Dialoge 
	/**
	 * Konstante für einfachen Info-Dialog
	 */
	public static final int INFO_DIALOG = 1;
	/**
	 * Konstante für Fehlermeldungs-Dialog
	 */
	public static final int ERROR_DIALOG = 2;
	/**
	 * Konstante für Frage-Dialog
	 */
	public static final int QUESTION_DIALOG = 3;
	
	// Texte der Dialogoptionen
	private static final String OK = "OK";
	private static final String YES = "YES";
	private static final String NO = "NO";
	
	// Optionen in Arrays legen 
	private static final Object[] OK_OPTION = {OK};
	private static final Object[] YES_NO_OPTION = {YES,NO};
	
	// Instanzvariablen zum Aussehen des Dialogen
	private String title = "";
	private Object message = "";
	private Component modalParent = null;
	private int dialogType = INFO_DIALOG;
	
	/**
	 * Haupt-Konstruktor, setzt alle wichtigen Dialogeigenschaften
	 * 
	 * @param modalParent Dialog wird modal zu dieser Komponente
	 * @param title Fenstertitel
	 * @param message Nachrichtobjekt (im Normalfall vom Typ String)
	 * @param dialogType siehe Klassenvariablen (Konstanten, z.B. INFO_DIALOG)
	 */
	public DialogFactory(Component modalParent, String title, Object message, int dialogType) {
		this.modalParent = modalParent;
		this.title = title;
		this.message = message;
		this.dialogType = dialogType;
	}
	/*
	 * Alternative Konstruktoren, setzen einige default Werte für leichteres Aufrufen der Dialoge:	 
	 */
	
	/**
	 * Leerer Konstruktor, Einstellungen sollten über set-Methoden gemacht werden.
	 */
	public DialogFactory() {
		this (null,"Information","",INFO_DIALOG);
	}

	/**
	 * Einfacher modaler Info-Dialog.
	 * 
	 * @param modalParent Dialog wird modal zu dieser Komponente
	 * @param message Nachrichtobjekt (im Normalfall vom Typ String)
	 */
	public DialogFactory(Component modalParent, Object message) {
		this (modalParent,"Information","",INFO_DIALOG);
	}

	/**
	 * Einfacher nicht modaler Info-Dialog.
	 * 
	 * @param message Nachrichtobjekt (im Normalfall vom Typ String)
	 */
	public DialogFactory(Object message) {
		this (null,"Infomation",message,INFO_DIALOG);
	}
	
	/**
	 * Einfacher nicht modaler Info-Dialog.
	 * 
	 * @param title Fenstertitel
	 * @param message Nachrichtobjekt (im Normalfall vom Typ String)
	 */
	public DialogFactory(String title, Object message) {
		this (null,title,message,INFO_DIALOG);
	}
	
	/**
	 * Einfacher modaler Info-Dialog.
	 * 
	 * @param modalParent Dialog wird modal zu dieser Komponente
	 * @param title Fenstertitel
	 * @param message Nachrichtobjekt (im Normalfall vom Typ String)
	 */
	public DialogFactory(Component modalParent, String title, Object message) {
		this (modalParent,title,message,INFO_DIALOG);
	}
	
	/**
	 * Benutzerdefinierter, nicht modaler Dialog.
	 * 
	 * @param title Fenstertitel
	 * @param message Nachrichtobjekt (im Normalfall vom Typ String)
	 * @param dialogType siehe Klassenvariablen (Konstanten, z.B. INFO_DIALOG)
	 */
	public DialogFactory(String title, Object message, int dialogType) {
		this (null,title,message,dialogType);
	}
	
	/**
	 * set-Methode zum Einstellen der Dialogart.
	 *  
	 * @param dialogType Stellt den dialogType auf den Wert (siehe Klassenvariablen, z.B. INFO_DIALOG)
	 */
	public void setDialogType(int dialogType) {
		this.dialogType = dialogType;
	}
	
	/**
	 * set-Methode zum Einstellen des Dialoginhaltes.
	 * 
	 * @param message Setzt das Nachrichtobjekt (im Normalfall vom Typ String)
	 */
	public void setMessage(Object message) {
		this.message = message;
	}
	
	/**
	 * set-Methode zum Einstellen der Komponente, zu der der Dialog modal sein soll
	 * 
	 * @param modalParent Dialog wird modal zu dieser Komponente
	 */
	public void setModalParent(Component modalParent) {
		this.modalParent = modalParent;
	}
	
	/**
	 * set-Methode zum Festlegen der Überschrift im Dialog.
	 * 
	 * @param title Setzt den Fenstertitel
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Dialog zur Anzeige bringen. True, wenn OK geklickt wurde
	 * bei informativen Dialogen, true wenn YES bei Fragedialogen.
	 * False bei einem Klick auf das "X" bzw. "NO".
	 *  
	 * @return true bei OK / YES, sonst False.
	 */	
	public boolean showDialog() {
		// Variablen, die anhand der Instanzvariablen
		// die Optionen für den zu zeigenden Dialog aufbauen. 		
		JOptionPane optionPane;
		Object[] options;
		int messageType;
		Dialog dialog;
		
		// Fallunterscheidung nach dialogType;
		// setzen von Optionen, Symbolen
		switch (dialogType) {
			case QUESTION_DIALOG:
				options = YES_NO_OPTION;
				messageType = JOptionPane.QUESTION_MESSAGE;			
				break;			
			case ERROR_DIALOG:
				options = OK_OPTION;
				messageType = JOptionPane.ERROR_MESSAGE;
				break;
			default:
				options = OK_OPTION;
				messageType = JOptionPane.INFORMATION_MESSAGE;								
				break;				
		}
		
		// JOptionPane nach den Settings anlegen.
		optionPane = new JOptionPane(message,messageType);
		optionPane.setOptions(options);
		// Defaultauswahl:
		optionPane.setInitialValue(options[0]);	 	
		// aus dem JOptionPane einen Dialog konstruieren:
		dialog = optionPane.createDialog(modalParent,title);
		// Layouting finalisieren und zur Anzeige bringen.
		dialog.pack();			
		dialog.setVisible(true);
		
		// nach Schliessen des Dialoges, schauen wie er beendet wurde.
		if (optionPane.getValue() == options[0]) {
			// durch OK oder YES, true zurückgeben
			return true;
		}
		else {
			// durch NO oder "X", false zurückgeben
			return false;
		}						
	}
}
