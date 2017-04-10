package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import misc.DialogFactory;
import misc.RegistryFactory;
import misc.Settings;
import misc.VarFileFilter;

import org.jdom.Document;

import parser.SceneFileOpener;
import printer.PrinterFrame;
import scene3dview.Scene3DViewer;
import scenejtree.SceneTreeNode;

/**
 * Die Klasse Desktop erstellt das Hauptfenster des Programms,
 * welches ein Splitpane für die Ansicht als JTree auf der linken
 * Seite und als 2D Baumdarstellung auf der rechten Seite beinhaltet.
 * 
 * Des weiteren werden eine Menü- und Buttonleiste in das Fenster eingebaut.
 * 
 * Dekstop unterstützt das Laden von Dateien über eine im Menü liegende MRU,
 * sowie per drag&drop auf das Fenster abgelegte Dateien. Während eines
 * Ladevorganges wird hier der Wartecursor gesetzt.
 * Nach einem erfolgreichen Öffnen wird die Datei zur Anzeige gebracht.
 * 
 * Zusätzlich ist hier in Desktop eine History zur Vor- und Zurücknavigation über
 * einen TreeSelectionListener auf dem jTree implementiert.
 * 
 * @author SEP VRML97 Group
 */
public class Desktop extends JFrame implements ActionListener, TreeSelectionListener, ChangeListener, DropTargetListener{
	
	/*
	 * alle GUI elemente folgen hier:
	 */
	private JSplitPane split;
	private JMenu file, view, options, help;
	private JMenuItem exitProg, statistic, legend, view3D, openFile ,setOptions , print, about, documentation;
	private JMenuBar menuBar;
	private JToolBar buttonPanel, backForwardPanel;
	private JButton viewer3D, open, statistics, printer, back, forward;
	private JSeparator buttonSeparator;
	private SettingsDialogue sD;
	private JCheckBox chkDrawLabel;
	/**
	 * element im splitpane für jtree
	 */
	public ChildFrameJTree jTree;
	/**
	 * element im splitpane für 2dview
	 */
	public ChildFrameGraphics graphics;
	/**
	 * position im splitpane: links
	 */
	public static final int LINKS = 0;
	/**
	 * position im splitpane: rechts
	 */
	public static final int RECHTS = 1;

	/*
	 * Ende GUI elemente.
	 */	
			
	/**
	 * instanz der klasse, die für ladevorgang zuständig ist
	 */
	public SceneFileOpener opener;
	/**
	 * jdom document der zuletzt geöffneten Datei
	 */
	public Document doc = null;
	
	/**
	 *  die zuletzt geöffneten Dateien werden hier gespeichert
	 */
    private File[] lastOpenedFiles = {};
    
    /**
     * hält Instanz von der den 3D Viewer verwaltenden Klasse
     */
    private Scene3DViewer browser;
    /**
     * wird gesetzt, wenn ein 3D Viewer gestartet wurde
     */
    private boolean browserStarted=false;
	

	/**
	 * Position der Settings-Datei.
	 */
    public static final String SETTINGS_INI = ".\\settings.ini";
    
    /**
     * Titel für Fenster
     */
    private static final String APP_TITLE = "**VRML / X3D Viewer**";
	
	/**
	 * Speicher für vor- und zurück navigationspositionen in Form von "TreePath"es
	 */
	private Vector backForwardNavigation = new Vector();
	/**
	 * position im vector backForwardNavigation, -1 steht für leeren vector
	 */
	private int backForwardNavigationPosition = -1;
	/**
	 * wird zwischenzeitlich auf true gesetzt, wenn über back oder forward
	 * navigiert wurde, damit die einträge nich mehrfach eingefügt werden.
	 */
	private boolean backForwardNavigationInProgress = false;
	/**
	 * maximale grösse der back / forward liste
	 */
	private final int BACK_FORWARD_LIST_LENGTH = 30;

	/**
	 * wird vom childframejtree bei expand/collapse all gesetzt;
	 *
	 */
	public boolean lockBackForwardNavigation = false;
	
	/**
	 * übernimmt drop aktionen aus explorer o.ä., um x3d und wrl files zu laden.
	 */
	private DropTarget dropper;
	/**
	 * für jtree droptarget ermöglichen.
	 * 
	 * @see "dropper"
	 */
	private DropTarget jTreeDropper;
	/**
	 * wird während des Ladevorganges auf true gesetzt
	 */
	private boolean loadingEnabled = false;
	
	/**
	 * wird verwendet, um die auf dem Glasspane über Desktop Mausereignisse abzufangen
	 */
	private MouseListener consumingMouseListener = new MouseListener() {
		public void mouseClicked(MouseEvent arg0) {
			arg0.consume();					
		}
		public void mousePressed(MouseEvent arg0) {
			arg0.consume();
		}
		public void mouseReleased(MouseEvent arg0) {
			arg0.consume();		
		}
		public void mouseEntered(MouseEvent arg0) {
			arg0.consume();
		
		}
		public void mouseExited(MouseEvent arg0) {
			arg0.consume();					
		}				
	};
	/**
	 * @see "consumingMouseListener"
	 */
	private KeyListener consumingKeyListener = new KeyListener() {
		public void keyTyped(KeyEvent arg0) {
			arg0.consume();			
		}
		public void keyPressed(KeyEvent arg0) {
			arg0.consume(); 			
		}
		public void keyReleased(KeyEvent arg0) {
			arg0.consume();			
		}
	};	
	
	/**
	 * Desktop, parameterloser Konstruktor 
	 * 
	 */		
	
	public Desktop(){
		super(APP_TITLE);
		super.setIconImage(new ImageIcon("./icons/ViewerMiniIcon.gif").getImage());
		
		initContents();		
		opener = new SceneFileOpener();
		
		dropper = new DropTarget(this, this);		
	}

	/**
	 * Diese Methode wird vom Konstruktor aufgerufen, um die 
	 * Form der grafischen Oberflaeche zu bestimmen
	 */

	private void initContents(){
		//settings laden
		Settings settings = new Settings(SETTINGS_INI);
		this.lastOpenedFiles = settings.getLastOpenedFiles();
	    Point position = new Point(Integer.parseInt(settings.getProperty("windowx","0")), Integer.parseInt(settings.getProperty("windowy","0")));				
		int width = Integer.parseInt(settings.getProperty("windowwidth","0"));
		int height = Integer.parseInt(settings.getProperty("windowheight","0"));
		int windowstate = Integer.parseInt(settings.getProperty("windowstate","" + MAXIMIZED_BOTH));
	    if (width == 0) {
	    	// keine startgrösse in der ini file
			super.setSize(600,400); // grösse wenn nicht maximieren 		
			this.setExtendedState(MAXIMIZED_BOTH); // versuch zu maximieren	    	
	    }
	    else {
	    	// alte grösse wiederherstellen
	    	super.setLocation(position);
	    	super.setSize(width,height);
	    	this.setExtendedState(windowstate);	    	
	    }
				
		settings = null;
	
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jTree, graphics);
		split.setOneTouchExpandable(true);	
		
        //Beschaffen des Content Panes
		Container contentPane = getContentPane();
		
		contentPane.add(createButtonPanel(), BorderLayout.NORTH);
		contentPane.add(split, BorderLayout.CENTER);
		
		jTree = new ChildFrameJTree(this);		
		addChild(jTree,0);
		jTreeDropper = new DropTarget(jTree.myJTreeComposer.getTree(), this);
		
		graphics = new ChildFrameGraphics();
		addChild(graphics,1);					
		
		/*anlegen eines Windowadapters der auf windosclosing reagiert,
		damit der browser für die 3d ansicht bei programmende
		geschlossen wird. eigenbtlich sollte windowClosed
		ausgeführt werden, funzt aber nicht*/
					
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				//System.out.println("schluss");
				if (browserStarted){browser.close();}

			    Settings settings = new Settings(SETTINGS_INI);			
				// Fensterposition aktualisieren in settings
				settings.setProperty("windowx","" + getLocation().x);
				settings.setProperty("windowy","" + getLocation().y);
				settings.setProperty("windowwidth","" + getWidth());
				settings.setProperty("windowheight","" + getHeight());
				settings.setProperty("windowstate","" + getExtendedState());
				settings.store(SETTINGS_INI);
				settings = null;	
			}
		});
			
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menuBar = new JMenuBar();
		
		createFileMenu();
		createOptionsMenu();
		createViewMenu();
		createHelpMenu();
		menuBar.add(file);
		menuBar.add(view);
		menuBar.add(options);
		menuBar.add(help);
		setJMenuBar(menuBar);
		
		//innerhalb des try Blocks wird das WindowsLAF gesetzt
	 
		try{

		String plaf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
				UIManager.setLookAndFeel(plaf);
				SwingUtilities.updateComponentTreeUI(this);
		}
		catch(UnsupportedLookAndFeelException e){		
			System.err.println(e.toString());
		}
		catch(ClassNotFoundException e){
			System.err.println(e.toString());
		}
		catch(InstantiationException e){
			System.err.println(e.toString());
		}
		catch(IllegalAccessException e){
			System.err.println(e.toString());
		}
		
		// den divider anpassen, und zur anzeige bringen
		split.setDividerSize(12);
		setVisible(true);
	}
	
	/**
	 * Diese Methode erstellt einen Separator für das ButtonPanel
	 * 
	 * @return buttonSeparator
	 */ 
	
	private JSeparator createSeparator(){
		buttonSeparator = new JSeparator();
		buttonSeparator.setOrientation(1); 
		buttonSeparator.setPreferredSize(new java.awt.Dimension(1,20)); 
		
		return buttonSeparator;
	}
	
	
	/**
	 * Diese Methode erstellt das Hauptmenue namens "file"
	 * 
	 * @return file das File-Menue
	 * @see initContents
	 */

	private JMenu createFileMenu(){	
		file = new JMenu("File");
		file.setMnemonic('f');
		
		openFile = new JMenuItem("Open");
		openFile.setMnemonic('o');
		openFile.addActionListener(this);

		exitProg = new JMenuItem("Exit");
		exitProg.setMnemonic('x');
		exitProg.addActionListener(this);
		
		file.add(openFile);
		
		/*
		 * Wenn das Array lastOpenedFiles Elemente enthält,
		 * hier dynamisches Menü mit möglichkeit zum öffnen der
		 * dateien integrieren:
		 */
		 
		if (lastOpenedFiles.length > 0) {
			file.add(new JSeparator());
			AbstractAction a;
			int mruPosition=1;
			for (int i = 0; i < lastOpenedFiles.length; i++) {				
				if (lastOpenedFiles[i] != null) {
					a = new AbstractAction(){	
						public void actionPerformed(ActionEvent arg0) {							
							setLoading(true);
							Thread loading = new Thread(new Runnable() {
								public void run() {									
									// Datei öffnen ...
									doc = opener.openFile((File)getValue("File"));						
									
									// ... und in der gui anzeigen
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											fileOpened();	 
										}
									});									 
								}
							});
							loading.start();
						}
					};								
					//titel der action:
					a.putValue(AbstractAction.NAME,mruPosition + " " + lastOpenedFiles[i].getName().toString());
					//shortcut setzen (_1_, _2_, ...):
					a.putValue(AbstractAction.MNEMONIC_KEY, new Integer((new String("" + (mruPosition))).charAt(0)));				
					//voller pfad als tooltip:
					a.putValue(AbstractAction.SHORT_DESCRIPTION,lastOpenedFiles[i].getAbsolutePath().toString());
					//datei objekt (fürs öffnen)
					a.putValue("File",lastOpenedFiles[i]);
								
					// action dem menü hinzufügen
					file.add(a);
					
					mruPosition++;
				}
			}
		}				
		file.add(new JSeparator());
		file.add(exitProg);
	
		return file; 
	}
	
	/**
	 * Diese Methode erstellt das Hauptmenue namens "view"
	 * 
	 * @return view das View-Menue
	 * @see initContents
	 */
	
	private JMenu createViewMenu(){	
		view = new JMenu("View");
		options.setMnemonic('V');
		
		view3D = new JMenuItem("View 3D Scene");
		view3D.setMnemonic('w');
		view3D.addActionListener(this);
		view3D.setEnabled(false);
		
		print = new JMenuItem("View Print Preview");
		print.setMnemonic('P');
		print.addActionListener(this);
		print.setEnabled(false);
		
		statistic = new JMenuItem("View Scene Statistics");
		statistic.setMnemonic('t');
		statistic.addActionListener(this);
		statistic.setEnabled(false);
		
		legend = new JMenuItem("View Legend");
		legend.setMnemonic('L');
		legend.addActionListener(this);
		
	
		view.add(view3D);
		view.add(print);
		view.add(new JSeparator());
		view.add(statistic);
		view.add(legend);
		
		return view;
	}
	
	/**
	 * Diese Methode erstellt das Hauptmenue namens "options"
	 * 
	 * @return options das options-Menue
	 * @see initContents
	 */
	
	private JMenu createOptionsMenu(){	
		options = new JMenu("Options");
		options.setMnemonic('s');
		
		setOptions = new JMenuItem("Preferences");
		setOptions.setMnemonic('P');
		setOptions.addActionListener(this);
	
		options.add(setOptions);
		return options;
	}
	
	/**
	 * Diese Methode erstellt das Hauptmenue namens "help"
	 * 
	 * @return help das options-Menue
	 * @see initContents
	 */
	
	private JMenu createHelpMenu(){	
		help = new JMenu("Help");
		help.setMnemonic('H');
		
		documentation = new JMenuItem("Documentation");
		documentation.setMnemonic('D');
		documentation.addActionListener(this);		
		
		about = new JMenuItem("About");
		about.setMnemonic('A');
		about.addActionListener(this);
	
		
		help.add(documentation);
		help.add(new JSeparator());
		help.add(about);
		return help;
	}
	
	/**
	 * Diese Methode erstellt eine Toolbar für die wichtigsten Funktionen,
	 * wie Öffnen, Druckansicht etc.
	 * 
	 * @return buttonPanel
	 * @see initContents
	 */
	
	private JToolBar createButtonPanel(){
        //Insets für die Größe der Buttons 
		Insets buttonInsets = new Insets(2, 5, 2, 5);
		
		buttonPanel = new JToolBar();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		buttonPanel.setRollover(true);
		
		//Buttons
		open = new JButton(new ImageIcon("./icons/open.gif"));
		open.setToolTipText("Open X3D-/VRML -File");
		open.setMargin(buttonInsets);
		open.addActionListener(this);
		
		viewer3D = new JButton(new ImageIcon("./icons/view3D.gif"));
		viewer3D.setToolTipText("View 3D scene with browser");
		viewer3D.setMargin(buttonInsets);
		viewer3D.addActionListener(this);
		
		viewer3D.setEnabled(false);
		
		statistics = new JButton(new ImageIcon("./icons/statistics.gif"));
		statistics.setToolTipText("Show scene statistics");
		statistics.setMargin(buttonInsets);
		statistics.addActionListener(this);
		
		statistics.setEnabled(false);
		
		printer = new JButton(new ImageIcon("./icons/printer.gif"));
		printer.setToolTipText("Show the Print Scene");
		printer.setMargin(buttonInsets);
		printer.addActionListener(this);
		
		printer.setEnabled(false);
		
		buttonPanel.add(open);
		buttonPanel.add(createSeparator());
		buttonPanel.add(viewer3D);
		buttonPanel.add(createSeparator());
		buttonPanel.add(statistics);
		buttonPanel.add(createSeparator());
		buttonPanel.add(printer);
		buttonPanel.add(createBackForwardPanel());
		buttonPanel.add(createChkDrawLabel());
		
		return buttonPanel;
	}
	
	/**
	 * Checkbox zum Einstellen von drawFromToLabels
	 */
	private JCheckBox createChkDrawLabel() {
	
		chkDrawLabel = new JCheckBox("ROUTE Labels", true);
		chkDrawLabel.setToolTipText("Draw Routes with from/to Fieldnames?");
		chkDrawLabel.setEnabled(false);
		chkDrawLabel.setFocusable(false);
		chkDrawLabel.addChangeListener(this);
	
		return chkDrawLabel;
	}
	 	
	/**
	 * Diese Methode erstellt eine Toolbar für Back und Forward Buttons
	 * 
	 * @return backForwardPanel
	 * @see initContents
	 */
	private JToolBar createBackForwardPanel() {
		 //Insets für die Größe der Buttons 
		Insets buttonInsets = new Insets(2, 5, 2, 5);
		
		backForwardPanel = new JToolBar();
		backForwardPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		backForwardPanel.setRollover(true);
		
		back = new JButton(new ImageIcon("./icons/back.gif"));
		back.setToolTipText("Go back to last selected Node");
		back.setMargin(buttonInsets);
		back.addActionListener(this);
		back.setEnabled(false);
		
		forward = new JButton(new ImageIcon("./icons/forward.gif"));
		forward.setToolTipText("Go forward to next selected Node");
		forward.setMargin(buttonInsets);
		forward.addActionListener(this);
		forward.setEnabled(false);
		
		backForwardPanel.add(back);
		backForwardPanel.add(forward);
		
		return backForwardPanel;
	}
	
	/**
	 * Hier wird ein Unterfenster erzeugt
	 * 
	 * @param child Objekt der Klasse JInternalFrame
	 * @param pos LINKS oder RECHTS für Splitpane position
	 */

	public void addChild(JPanel child, int pos){
		if (pos == 0){			
			split.setLeftComponent(child);
			
		}
		else {
			split.setRightComponent(child);
		}
	
		child.setVisible(true);
	}

	/**
	 * Die abstrakte Methode actionPerformed wird überschrieben,
	 * um den Menueeintraegen Leben zu verleihen
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
 
	public void actionPerformed(ActionEvent e){
		
		// Wenn exit betätigt wird
		if(e.getSource() == exitProg) {
			//auslösen des windowclosingevents zur benutzung des oben
			//geschriebenen windowadapter zum schliessen des 3dviewers
			this.dispatchEvent (new WindowEvent( new Window(this), WindowEvent.WINDOW_CLOSING));
			//System.exit(0);
		}
		
		//Wenn open betätigt wird
		if(e.getSource() == view3D || e.getSource() == viewer3D) {
			
			//Holen des Pfades der ini und daraus den Browserpath
			Settings settings = new Settings(SETTINGS_INI);
			String browserPath = settings.getProperty("browserPath");
			/*
			 * evtl.:
			 * der JFileChooser an dieser Stelle soll noch verschwinden, da der
			 * Code in der Form mehrmals im Programm auftauchen wird
			 */
			
			//Wenn kein Browserpath vorhanden ist, soll einer gewählt werde
			if(browserPath.equals("")) {
				
				//öffnen eine Filechoosers und hinzufügen
				//des FileFilters damit nur *.exe zur Auswahl stehen
				JFileChooser chooser = new JFileChooser();				
				VarFileFilter filter = new VarFileFilter();
				filter.addExtension("exe");
				filter.setDescription("Browser Executeables (*.exe)");
				chooser.setFileFilter(filter);
				chooser.setDialogTitle("Please select a browser with X3D/VRML Plug-In");
			    
				//abfragen, ob FileSelection erfolgreich war
				int returnVal = chooser.showOpenDialog(this);
				
				//wenn ja, dann den browserpath setzen 
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	browserPath = chooser.getSelectedFile().getPath();
			    	settings.setProperty("browserPath",browserPath);
			    	settings.store(SETTINGS_INI);
			    }
			}
			
			/*
			 * Wenn trotz Settings-Klasse und JFileChooser kein Pfad zu einem
			 * Browser angegeben ist, passiert nichts.
			 */
			if(!browserPath.equals("")) {
				// falls broswer schon gestartet, erst beenden
				if (browserStarted){
					browser.close();
					browserStarted=false;
				}
				browser = new Scene3DViewer(browserPath,opener.getFilePath());
				browser.open();
				browserStarted=true;
			}
		}
			
		//Wenn openFile betätigt wird
		if (e.getSource() == openFile || e.getSource() == open) {															
			final Component modalParent = this;
			setLoading(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doc = opener.openFile(modalParent);
					fileOpened();									
				}
			});
		}
		
		// in der navigationsliste einen schritt zurück gehen
		if (e.getSource() == back) {
			navigateBackward();
		}
		// in der navigationsliste einen schritt vor gehen
		if (e.getSource() == forward) {
			navigateForward();
		}

		//Wenn statistics betätigt wird 
		if (e.getSource() == statistics || e.getSource() == statistic) {
			StatisticsViewer statView = new StatisticsViewer(jTree.myJTreeComposer, this.getContentPane().getWidth()/2, this.getContentPane().getHeight()/2);	
		}
		//Wenn Options betätigt wird 
		if (e.getSource() == setOptions) {
			//SettingsDialogue initialisieren
		    sD = new SettingsDialogue(this,"Preferences - Select Path To VRML/X3D Browser",true);
		}
		//Hier ist das ausgelöste Event für den Printer Button
		if (e.getSource() == printer || e.getSource() == print) {
		//Ein neuer PrinterFrame wird erzeugt
			PrinterFrame printi = new PrinterFrame(jTree.myJTreeComposer.tree);
		}
		//Wenn legend betätigt wird
		if (e.getSource() == legend) {
			LegendViewer legendView = new LegendViewer(this.getContentPane().getWidth()/2, this.getContentPane().getHeight()/2); 
		}
		//Wenn about betätigt wird
		if (e.getSource() == about) {
			// Dialogfenster mit Infos zum Programm
			JDialog aboutDialog = new JDialog(this, "About Viewer", true);
			aboutDialog.setSize(205, 240);
			aboutDialog.setResizable(false);
			aboutDialog.setLocation(this.getContentPane().getWidth()/2 - aboutDialog.getWidth()/2, this.getContentPane().getHeight()/2 - aboutDialog.getHeight()/2);
			aboutDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			Container content = aboutDialog.getContentPane();
			JLabel imageLabel = new JLabel();
			imageLabel.setIcon(new ImageIcon("./icons/ViewerAbout.jpg"));
			content.add(imageLabel);
			aboutDialog.setVisible(true);
			
		}
		if (e.getSource() == documentation) {		
			//pdf befindet sich im root
			final String HELP_FILE = "VX3DViewer.pdf";
			File file = new File(HELP_FILE);
			final String HELP_FILE_PATH=file.getAbsolutePath();
			
			//pdf viewer suchen										
			String handlerName = RegistryFactory.getRegistryValue("HKCR",".pdf\\");			
			String viewerApp = RegistryFactory.getRegistryValue("HKCR",handlerName + "\\shell\\open\\command\\");
		
			
			if (viewerApp != null && viewerApp != "" && viewerApp.indexOf("%1") > 0) {
				// alles ok
				String command = viewerApp;
				//.replaceAll("%1",HELP_FILE_PATH); XXX hier workaround um "\" nich maskieren zu müssen 
				int pos=0;
				int lastpos=0;				
				pos = command.indexOf("%1", lastpos);
				while(pos >= 0) {					
					command = command.substring(0,pos) + HELP_FILE_PATH + command.substring(pos+2);
					lastpos=pos;
					pos = command.indexOf("%1", lastpos + 2);
				}
				
				try {					
					Process p = Runtime.getRuntime().exec(command);
				} catch (IOException e1) {
					DialogFactory df = new DialogFactory(this, "Failed to launch PDF-Viewer", "The PDF-Viewer could not be started.\nPlease launch '" + HELP_FILE_PATH + "' manually.", DialogFactory.ERROR_DIALOG);
					df.showDialog();
				}
			}
			else {
				// nix ok
				DialogFactory df = new DialogFactory(this, "Failed to launch PDF-Viewer", "No PDF-Viewer could not be found in windows registry.\nPlease launch '" + HELP_FILE_PATH + "' manually.", DialogFactory.ERROR_DIALOG);
				df.showDialog();
			}
		}
		/*
		else {
			System.out.println(e.getActionCommand());
		}
		*/ 
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine erfolgreich geladene Szene 
	 * zur Anzeige gebracht wird, dann wird die Settings Datei aktualisiert
	 * die noch nicht aktivierten Buttons, Menüeintrage etc. aktiviert
	 */
	public void fileOpened() {
		if (opener.isFileReady()) {
			
			// back forward initialisieren
			this.back.setEnabled(false);
			this.forward.setEnabled(false);
			this.backForwardNavigation = new Vector();
			this.backForwardNavigationInProgress = false;
			this.backForwardNavigationPosition = -1;
			
			//settings aufrufen
			Settings settings = new Settings(SETTINGS_INI);			
			// MRU aktualisieren in settings
			this.lastOpenedFiles = settings.setLastOpenedFile(opener.getFilePath());
			settings.store(SETTINGS_INI);
			settings = null;
			
			// und neues filemenu erzeugen wg MRU
			menuBar.remove(file);			
			menuBar.add(createFileMenu(),0);
			menuBar.updateUI();

			// evtl. lieber boolean mit true wenn mind. einmal erfolgreich geladen wurde?
			if (!viewer3D.isEnabled())
				viewer3D.setEnabled(true);
			if (!view3D.isEnabled())
				view3D.setEnabled(true);
			if (!statistics.isEnabled())
				statistics.setEnabled(true);
			if (!printer.isEnabled())
				printer.setEnabled(true);
			if (!statistic.isEnabled())
				statistic.setEnabled(true);
			if (!print.isEnabled())
				print.setEnabled(true);
			if (!chkDrawLabel.isEnabled())
				chkDrawLabel.setEnabled(true);

			this.jTree.newJTree(doc,opener.getFileName());
			
			this.graphics.setSelectedNode((SceneTreeNode)this.jTree.myJTreeComposer.getRoot(), this.jTree.myJTreeComposer);
			this.jTree.myJTreeComposer.getTree().addTreeSelectionListener(this.graphics);
			this.jTree.myJTreeComposer.getTree().addTreeSelectionListener(this);			
			
			// Der Dateiname wird mit im fenstertitel angezeigt
			this.setTitle(APP_TITLE + " - " + opener.getFileName());
			
			//drop auf jtree registrieren
			jTreeDropper =  new DropTarget(this.jTree.myJTreeComposer.getTree(),this);			
		}
		setLoading(false);
	}

	/**
	 * treeselection ereignisse werden beachtet, um vor zurück navi zu ermöglichen
	 * 
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent arg0) {		
		if (!lockBackForwardNavigation) {
			if (!backForwardNavigationInProgress) {
				// normal auf ein element geklickt, also in die liste legen
				addNavigation (arg0.getPath());			  
			}
			else {
				// über vor zurück hierher gekommen, also nicht speichern
				backForwardNavigationInProgress = false;
			}
			// prüfen auf enable / disable der buttons
			refreshBackForwardButtons();
		}
	}
	/**
	 * Legt die aktuelle auswahl in dir vor- zurück-Liste
	 * @param selectedPath
	 */
	private void addNavigation( TreePath selectedPath ) {
		if (backForwardNavigationPosition  != backForwardNavigation.size() -1) {
			//Wenn nicht am ende der liste, alle elemente 
		  	//nach aktueller position entfernen
			while (backForwardNavigation.size()-1 > backForwardNavigationPosition) {
				backForwardNavigation.remove(backForwardNavigation.size()-1);
			}		  	
		} 

		//speichern der aktuellen auswahl
		backForwardNavigation.add(selectedPath);

		//sicherstellen das nur BACK_FORWARD_LIST_LENGTH elemente gespeichert werden
		while (backForwardNavigation.size() > BACK_FORWARD_LIST_LENGTH) {
			backForwardNavigation.remove(0);
		}

		// aktuelle position speichern
		backForwardNavigationPosition  = backForwardNavigation.size() -1;

	}
	/**
	 * prüfen, ob vor- und zurück button aktiviert bzw. deaktiviert werden müssen
	 *
	 */
	private void refreshBackForwardButtons() {
		if (backForwardNavigationPosition  > 0) {
			back.setEnabled(true);
		}
		else {
		    back.setEnabled(false);  
		}
		if (backForwardNavigationPosition  < backForwardNavigation.size() -1) {
		    forward.setEnabled(true);
		}
		else {
		    forward.setEnabled(false);  
		}
	}
	/**
	 * in der history einen schritt nach vorne gehen
	 */
	private void navigateForward() {
		if (backForwardNavigationPosition  < backForwardNavigation.size() -1) {
		    backForwardNavigationPosition++;
		    navigate();
		}
	}
	/**
	 * in der history einen schritt zurück gehen
	 */
	private void navigateBackward() {
		if (backForwardNavigationPosition  > 0) {
		    backForwardNavigationPosition--;
		    navigate();
		}
	}
	/**
	 * position im jtree ändern	 
	 */
	private void navigate() {
		backForwardNavigationInProgress = true;
		this.jTree.myJTreeComposer.gotoNode((TreePath)backForwardNavigation.get(backForwardNavigationPosition));
	}

	/**
	 * change event von der checkbox
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {		
		if (e.getSource() == chkDrawLabel) {
			// es wurde die checkbox geändert, änderung an Linienizer melden
			graphics.getLinienizer().setDrawFromToLabels(chkDrawLabel.isSelected());
		}		
	}

	/**
	 * sobald ein drag auf diese komponente kommt, die keine files enthält,
	 * ablehnen.
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
	 */	
	public void dragEnter(DropTargetDragEvent arg0) {		
		if (!arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			arg0.rejectDrag();			
		}

	}
	/**
	 * sobald ein drag auf diese komponente kommt, die keine files enthält,
	 * ablehnen.
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent arg0) {		
		if (!arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			arg0.rejectDrag();
		}
	}

	/**
	 * wird nicht verwendet - implementiert für DropTargetListener  
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent arg0) {	
	}

	/**
	 * wird nicht verwendet - implementiert für DropTargetListener
	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent arg0) {
	}

	/**
	 * Sobald eine DnD Operation durch drop abgeschlossen wurde, prüfen, 
	 * ob es eine gültige Datei war, und wenn ja, diese Laden.
	 * 
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent arg0) {
		if (!arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			arg0.rejectDrop();
			arg0.getDropTargetContext().dropComplete(true);
			return;
		}
		try {
			arg0.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        
			List fileList = (List)(arg0.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
			Iterator files = fileList.iterator();
			File file;
			String fileName;
			while (files.hasNext()) {
				file = (File)files.next();
				fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".x3d") || fileName.endsWith(".wrl")) {
					// gültigen dateinamen gefunden, starte ladevorgang und beende drop operation
					setLoading(true);
					final File fileToOpen = file;
					arg0.getDropTargetContext().dropComplete(true);
					Thread loading = new Thread(new Runnable() {						
						public void run() {									
							// Datei öffnen ...
							doc = opener.openFile(fileToOpen);						
							
							// ... und in der gui anzeigen
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fileOpened();	 
								}
							});									 
						}
					});
					loading.start();

					return;
				}
			}
		} catch (UnsupportedFlavorException e) {			
			arg0.rejectDrop();
		} catch (IOException e) {
			arg0.rejectDrop();
		}
		// kein unterstützer dateityp zum laden
		final Desktop desktop=this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DialogFactory df = new DialogFactory("Only .x3d or .wrl files can be dropped here for loading!");
				df.setModalParent(desktop);
				df.showDialog();
			}
		});									 

	}
	/**
	 * Wird aufgerufen, wenn der Ladevorgang beginnt, oder beendet wird.
	 * 
	 * @param loadingEnabled true, wenn der Zustand auf Laden steht, false nach Beendigung des Ladevorgangs.
	 */
	public void setLoading(boolean loadingEnabled) {
		Component glassPane = this.getGlassPane();
		glassPane.setVisible(loadingEnabled);
		
		if (loadingEnabled) {
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			glassPane.addMouseListener(consumingMouseListener);
			glassPane.addKeyListener(consumingKeyListener);
		}
		else {
			glassPane.setCursor(Cursor.getDefaultCursor());
			glassPane.removeMouseListener(consumingMouseListener);
			glassPane.removeKeyListener(consumingKeyListener);
		}
		this.loadingEnabled = loadingEnabled;
	}

}