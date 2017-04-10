/*
 * Created on 29.06.2004
 */
package printer;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import misc.ScrollPaneNavigator;
import misc.VarFileFilter;
import org.jfree.ui.ExtensionFileFilter;

/**
 * Printerframe
 * Diese Klasse bietet den Print Preview Frame, es enthällt ein ScrollPane sowie zwei Buttons 
 * und deren Eventhandler zum Speichern und Schliessen des Frames.
 *
 * @author Sep-Vrml97 Group
 * @see Printerator.java
 */

public class PrinterFrame extends JFrame implements ActionListener {
	public JScrollPane scroll;
	public JTree tree;
	public JButton close, save, plus, minus;
	public JPanel southPanel, northPanel;
	public JFileChooser chooser;
	public JFrame frame;
	public ExtensionFileFilter filter;
	public Printerator printerator;	
	public String selectedFile;
	public Container contentPane;
	
	/**
	 * In der preview maus navigation aktiviert.
	 */
	private ScrollPaneNavigator scrollPaneNavigator;
	
	/**
	 * Der Konstruktor ruft den Frame auf und bekommt einen Jtree übergeben.
	 * Setzt den Titel Des Frames auf den Namen der Datei.
	 * 
	 * @param tree Der anzuzeigende JTree
	 * 
	 */
	public PrinterFrame(JTree tree){
		//Setzt den Titel
		this("Print Preview: " + tree.getModel().getRoot().toString(), tree);
		
		// symbol für ganzes programm statt java tasse
		super.setIconImage(new ImageIcon("./icons/ViewerMiniIcon.gif").getImage());
	}
	
	/**
	 * Der Konstruktor ruft den Frame auf und bekommt einen Jtree und einen String übergeben
	 * 
	 * @param tree Der anzuzeigende JTree
	 * @param title Der Titel des Frames
	 * 
	 */
	public PrinterFrame(String title, JTree tree){
		super(title);
		this.tree = tree;
		createUI();
		setVisible(true);
		
	}

	/**
	 * createUI ist eine Hilfsmethode, die vom Konstruktor aufgerufen wird, 
	 * um den Frame zu positionieren und den Inhalt zu laden
	 *
	 */
	protected void createUI(){
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				dispose();
			}
		});
		//Positionierung...		
		setSize(700,500);
		center();
		//der Baum wird in gezeichnet
		printerator = new Printerator(tree);
		
		contentPane = getContentPane();
//		ein ScrollPane, dem der Baum sofort uebergeben wird
		scroll = new JScrollPane(printerator, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.getViewport().setBackground(Color.WHITE);	
		
		// mausnavigation aktivieren:
		scrollPaneNavigator = new ScrollPaneNavigator(scroll, this);
		scrollPaneNavigator.setScrollSpeed(ScrollPaneNavigator.SUPER_QUICK_SPEED);
		
//		 ein Panel am oberen Rand des Frames enthällt die Knöpfe fuer den Zoom
		northPanel = new JPanel();
		plus = new JButton("+");
		minus = new JButton("-");
		
		// ein Panel am unteren Rand des Frames enthällt die Knöpfe fuer Save und Close
		southPanel = new JPanel();
		save = new JButton("Save File");
		close = new JButton("Close");
		
		northPanel.add(plus);
		plus.addActionListener(this);
		northPanel.add(minus);
		minus.addActionListener(this);
		southPanel.add(save);
		save.addActionListener(this);
		southPanel.add(close);
		close.addActionListener(this);
		contentPane.add(northPanel, BorderLayout.NORTH);
		contentPane.add(scroll, BorderLayout.CENTER);
		contentPane.add(southPanel, BorderLayout.SOUTH);
		
		
	}
	
	/**
	 * center, diese Methode berechnet mittels der Bildschirmauflösung die Position des Frames, 
	 * so daß er beim Öffnen in der Mitte erscheint 
	 *
	 */
	public void center(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		int x = (screenSize.width-frameSize.width)/2;
		int y = (screenSize.height-frameSize.height)/2;
		setLocation(x,y);
	}

	/**
	 * Hier wird die Methode actionPerformed überschrieben, um den Knöpfen des Frames Sinn zu geben.
	 * 
	 * @param e der Knopfdruck, der weitergegeben wurde
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
			if(e.getSource() == close) {
				//auslösen des windowclosingevents zur benutzung des oben
				//geschriebenen windowadapter zum schliessen des 3dviewers
				this.dispatchEvent (new WindowEvent( new Window(this), WindowEvent.WINDOW_CLOSING));
			}
			
			//Macht den ZoomIn um Faktor 1.5
			if(e.getSource() == plus){
				printerator.setZoomPlus();
				printerator.setHeightWidthZoom();
				printerator.setPreferredSize(new Dimension (printerator.getWidth(), printerator.getHeight()));
				printerator.repaint();	
			}
			//macht den ZoomOut um Faktor 0.5
			if(e.getSource() == minus){
				printerator.setZoomMinus();
				printerator.setHeightWidthZoom();
				printerator.setPreferredSize(new Dimension (printerator.getWidth(), printerator.getHeight()));
				printerator.repaint();
				
			}
			
			if (e.getSource() == save ) {	
				
				//Hier werden die Filter eingebaut, die hinterher die Datei auf seine Endung prüfen
				VarFileFilter filter = new VarFileFilter();
			    filter.addExtension("eps");
			    filter.setDescription("EPS File (*.eps)");
				chooser = new JFileChooser();
				chooser.addChoosableFileFilter(filter);
				chooser.showSaveDialog(frame);
				
				//Bekommt den String den den Nutzer als zu speichernden Naman angegeben hat
	            File name = chooser.getSelectedFile();
	            
	            //Checkt ob beim Speichern abgebrochen gedrueckt wurde 
	            if(name==null){   
	            	   System.out.println("Abgebrochen");
				}
	            //Fals nicht wird aus der File ein Strin gemacht
				else{
					
					String file = chooser.getSelectedFile().toString();	
					
					//Hat der nutzer eine Endung (.eps) engegeben?
					if(file.endsWith(".eps")){
						 selectedFile = file;
					}
					else{
						 selectedFile = file+".eps";
					}
						
					
		            String fileName = name.getName()+".eps";
	
		            
		            //Name und Pfad werden an paintEPS in Printerator übergeben
		            printerator.paintEps(selectedFile, fileName);
				}
			}
		}
	
}
