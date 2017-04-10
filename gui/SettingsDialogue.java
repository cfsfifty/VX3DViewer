package gui;

import javax.swing.*;
import misc.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Die Klasse SettingsDialogue erstellt einen JDialog mit JTextfield und JFileChooser , mit dem der Pfad
 * zum VRML/X3d Browser angegeben werden kann
 * @author Sven Nemeth
 */

public class SettingsDialogue extends JDialog {
	
	
	private JButton browse;
	private String browserPath = "";
	private JTextField text;
	
	/**
	 * Konstruktor der Klasse SettingsDialogue
	 * @param frame Frame von dem der Dialog abhaengt 
	 * @param name Titel des Dialogs
	 * @param bool Gibt an ob der Dialog modal sein soll oder nicht
	 */
	public SettingsDialogue(Frame frame,String name,boolean bool){
		
		super(frame,name,bool);
		int dialogWidth = 640;
		int dialogHeight = 120;
		int screenWidth = (int)getToolkit().getScreenSize().getWidth();
		int screenHeight = (int)getToolkit().getScreenSize().getHeight();
		int locationWidth = (screenWidth-dialogWidth)/2;
		int locationHeight = (screenHeight-dialogHeight)/2;
		
		
		setSize(dialogWidth,dialogHeight);
		setLocation(locationWidth,locationHeight);
		setResizable(false);
	
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Path :",null,SwingConstants.LEFT);
		panel.add(label);
		
		Settings settings = new Settings(Desktop.SETTINGS_INI);		
		browserPath = settings.getProperty("browserPath"); 
		
		text = new JTextField(60);
		text.setToolTipText("Select Path To VRML/X3D Browser");
		text.setText(browserPath);
		
		panel.add(text);
		
		
		final Component settingsDialogue = this;
		//Action für den browse - JButton
		AbstractAction browseAction = new AbstractAction("Browse",new ImageIcon("./icons/open.gif")) {
			   
			public void actionPerformed(ActionEvent event) {
			      	
					JFileChooser fileChooser = new JFileChooser();
			      	VarFileFilter filter = new VarFileFilter();
					filter.addExtension("exe");
					filter.setDescription("Browser Executeables (*.exe)");
					fileChooser.setFileFilter(filter);
					fileChooser.setDialogTitle("Select Path To VRML/X3D Browser");
				    
					
					int returnVal = fileChooser.showOpenDialog(settingsDialogue);
					
			
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
				    	browserPath = fileChooser.getSelectedFile().getPath();
				    	text.setText(browserPath);
				    }
			}
		};
		
		JButton browse = new JButton(browseAction);
		browse.setToolTipText("Browse for path");
		
		
		
		
		//Action für den ok - JButton
		AbstractAction okAction = new AbstractAction("OK") {
			   
			public void actionPerformed(ActionEvent event) {
				Settings settings = new Settings(Desktop.SETTINGS_INI);
				settings.setProperty("browserPath",browserPath);
		    	settings.store(Desktop.SETTINGS_INI);
		    	dispose();
			}
		};
		JButton ok = new JButton(okAction);					
		
		//Action für den cancel - JButton
		AbstractAction cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		};
		JButton cancel = new JButton(cancelAction);
		
		
		
		
		panel.add(browse);
		panel.add(ok);
		panel.add(cancel);
		
		getRootPane().setDefaultButton(ok); // Standardknopf setzen
		panel.setBorder(BorderFactory.createEtchedBorder());
		getContentPane().add(panel);
		panel.setVisible(true);
		setVisible(true);
	}
}


