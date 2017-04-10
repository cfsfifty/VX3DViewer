package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import misc.DialogFactory;

import org.jdom.Document;

import scenejtree.JTreeComposer;
/**
 * In dieser Klasse wird der JTree mit seiner Datenstruktur in einem
 * JScrollPane zur Anzeige gebracht. Zusätzlich wird eine Toolbar zum Ein- /
 * und Ausklappen des ges. Baumes angebote. Eine ComboBox + Button ermöglicht
 * es, Knoten mit Def-Namen in der Szene direkt anzuspringen. In der ComboBox
 * kann durch den implementierten KeyListener mittels Enter der Knoten
 * angesprungen werden.
 * Schließlich werden ein paar statistische Angaben in einem Panel angezeigt.  
 * 
 * Created on 13.05.2004
 *
 * @author Timo Winkelvos, fs, ph
 * 
 */
public class ChildFrameJTree extends ChildFrame implements ActionListener, KeyListener{	
	/**
	 * Aus dem scenejtree paket, baut den JTree zusammen
	 */
	JTreeComposer myJTreeComposer;
	/**
	 * Buttons und ComboBox liegen in diesem Panel, welches oben angebracht wird.
	 */
	public JPanel controlPanel;
	/**
	 * In dieses, in der Mitte liegende, ScrollPane kommt der JTree selbst.
	 */
	public JScrollPane scrollPane;
	/**
	 * Auf diesem Panel, welches unter dem JTree liegen wird, kommen die statistischen Angaben.
	 */
	public JPanel summaryPanel;	
    /**
     * Diese Buttons liegen im controlPanel.
     */ 
	public JButton gotoNodeDEFButton, expandAllNodes, collapseAllNodes;
    /**
     * Diese ComboBox liegt im controlPanel.
     */ 
	public JComboBox nodesDEFComboBox;
	/**
	 * Diese Labels werden in das summaryPanel gelegt.
	 */
    public JLabel nodeCount, routeCount, useCount;    		
    /**
     * Referenz auf aktuelle Desktop-instanz. Wird verwendet um beim
     * collapse/expand all die back/forward history zu beachten. 
     */
    private Desktop desktop;
    
    /**
     * Unter Angabe der aufrufenden Desktop Instanz werden hier die 
     * GUI-Elemente für den JTree initialisiert. Es wird ein leerer
     * JTree-Dummy zur Anzeige gebracht, die Buttons zunächst deaktiviert.
     * 
     * @param desktop Referenz auf aktuellen Desktop.
     */
	public ChildFrameJTree(Desktop desktop){
		this.desktop = desktop;
		
		// Insets für die Größe der Buttons 
		Insets buttonInsets = new Insets(2, 5, 2, 5);
		
        // Buttons:
        // "Gehe zu" für ComboBox
		gotoNodeDEFButton = new JButton("Go!");
        gotoNodeDEFButton.setMargin(buttonInsets);
        gotoNodeDEFButton.addActionListener(this);
        gotoNodeDEFButton.setToolTipText("Go to selected DEF node");
        gotoNodeDEFButton.setEnabled(false);
        
        // "Alle Knoten ausklappen"
        expandAllNodes = new JButton(new ImageIcon("./scenejtree/ICONS/Expand.gif"));
        expandAllNodes.setMargin(buttonInsets);
        expandAllNodes.addActionListener(this);
        expandAllNodes.setToolTipText("Expand all nodes of the scene");
        expandAllNodes.setEnabled(false);
        
        // "Alle Knoten einklappen" (bis auf Root und Scene Knoten)
        collapseAllNodes = new JButton(new ImageIcon("./scenejtree/ICONS/Collapse.gif"));
        collapseAllNodes.setMargin(buttonInsets);
        collapseAllNodes.addActionListener(this);
        collapseAllNodes.setToolTipText("Collapse all nodes of the scene");
        collapseAllNodes.setEnabled(false);
        
    	// ComboBox für Knoten mit Def-Namen:
        nodesDEFComboBox = new JComboBox();
        nodesDEFComboBox.setEditable(false);
        nodesDEFComboBox.setAutoscrolls(true);        
        nodesDEFComboBox.setPrototypeDisplayValue("Please select DEF to jump to");
        nodesDEFComboBox.addKeyListener(this);
        nodesDEFComboBox.setToolTipText("Here you can select the DEF Node to jump to (by clicking 'Go' or by pressing enter).");
        nodesDEFComboBox.setEnabled(false);
        
        // Statistik Labels:
        // JLabel: Anzahl der Nodes 
        nodeCount = new JLabel("# Nodes: 0");
        nodeCount.setFont(new Font("Arial",Font.PLAIN,12));
        nodeCount.setEnabled(false);
        // JLabel: Anzahl der ROUTES 
        routeCount = new JLabel("# Routes: 0");
        routeCount.setFont(new Font("Arial",Font.PLAIN,12));
        routeCount.setEnabled(false);
       // JLabel: Anzahl der USEs
        useCount = new JLabel("# Uses: 0");
        useCount.setFont(new Font("Arial",Font.PLAIN,12));
        useCount.setEnabled(false);
		
        // controlPanel für Buttons und ComboBox
        controlPanel = new JPanel();        
        controlPanel.add(expandAllNodes);
        controlPanel.add(collapseAllNodes);
        controlPanel.add(nodesDEFComboBox);
        controlPanel.add(gotoNodeDEFButton);

        // unteres Panel (Statistische Übersicht)
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new FlowLayout(FlowLayout.CENTER,25,0));
        
        summaryPanel.add(nodeCount);
        summaryPanel.add(routeCount);
        summaryPanel.add(useCount);		       
        
        // scollpanel für JTree selbst
        scrollPane = new JScrollPane();                
        this.setLayout(new BorderLayout());
        this.add("North", controlPanel);
        this.add("South", summaryPanel);
        this.add("Center", scrollPane);               
        
        // leeren JTree aufbauen, sowie vorbereiten, einen "echten" JTree anzuzeigen
        myJTreeComposer =new JTreeComposer();
        // den "dummy" anzeigen
        scrollPane.getViewport().add(myJTreeComposer.getTree());
	}
	/**
	 * Methode wird verwendet, um den erfolgreichen Ladevorgang einer Szene
	 * zu signalisieren. Dazu wird das geparste JDOM Document übergeben, sowie
	 * der originale Dateiname der zugrundeliegenden Datei.
	 * 
	 * @param doc geparstes Dokument aus SceneFileOpener
	 * @param fileName Dateiname der originalen Datei (z.B. "123.wrl" oder "xyz.x3d"), unabhängig von einer ggf. durchgeführten Konvertierung in x3d.
	 */
	public void newJTree(Document doc, String fileName) {		
		nodesDEFComboBox.removeAllItems(); // alte elemente raus (evtl. nicht nötig?)
		scrollPane.getViewport().remove(myJTreeComposer.getTree());	// alten jtree aus Anzeige raus (evtl. Model ändern?)
		
		// baue neuen jtree durch neue Instanz des Composers unter Angabe von Dokument und Dateiname
		myJTreeComposer =new JTreeComposer(doc, fileName);

		// ComboBox mit Knoten inkl. Def-Namen füllen
		nodesDEFComboBox.setModel(myJTreeComposer.getCbModelDEFs());
		
		// Statistik eintragen
        nodeCount.setText(myJTreeComposer.getStrNodes());
        routeCount.setText(myJTreeComposer.getStrRoutes());
        useCount.setText(myJTreeComposer.getStrUses());		

        // JTree zur Anzeige bringen
        scrollPane.getViewport().add(myJTreeComposer.getTree());
        
        // erst jetzt die Knöpfe und Labels aktivieren (falls nicht bereits geschehen)
        if (!gotoNodeDEFButton.isEnabled()) {
        	gotoNodeDEFButton.setEnabled(true);
        	expandAllNodes.setEnabled(true);
        	collapseAllNodes.setEnabled(true);
        	nodesDEFComboBox.setEnabled(true);
        	nodeCount.setEnabled(true);
        	routeCount.setEnabled(true);
        	useCount.setEnabled(true);
        }        	        
	}
		
	/**
	 * Wurde für den ActionListener implementiert, um auf die Buttons im controlPanel
	 * reagieren zu können.
	 */
    public void actionPerformed(ActionEvent e) {
        if ( e.getSource() == expandAllNodes) {
            // JTree Komplett ausklappen angefordert
        	if (myJTreeComposer.getIntNodes() > 1000) {
        		// sehr viele Elemente = lange Dauer und hoher Speicherbedarf, also warnen
        		DialogFactory reallyExpand = new DialogFactory(this,
        				"Confirm action",
						"Do you really want to expand all " + myJTreeComposer.getIntNodes() + " nodes?\n" + 
						"This consumes a large amount of memory and\n" + 
						"may take some time. Expand all nodes?", DialogFactory.QUESTION_DIALOG);
        		if (reallyExpand.showDialog()==false)
        			return;
        	}
        	// alles ausklappen, diese Aktion aber nicht in vor und zurück navi eintragen
        	desktop.lockBackForwardNavigation = true;
        	myJTreeComposer.expandAll(myJTreeComposer.getTree());
        	desktop.lockBackForwardNavigation = false;
        } 

        if ( e.getSource() == collapseAllNodes) {
            // JTree zusammenklappen angefordert
        	// evtl. während des zusammenklappens springende auswahländerungen nicht
        	// in die vor und zurück navigation übernehmen
        	desktop.lockBackForwardNavigation = true;
        	myJTreeComposer.collapseAll(myJTreeComposer.getTree());
        	desktop.lockBackForwardNavigation = false;
        	// schließlich noch Scene auswählen
        	myJTreeComposer.gotoNode(myJTreeComposer.getTree().getPathForRow(1));
        }
        // Zu durch DEF-Namen bestimmtem Node gehen
        if ( e.getSource() == gotoNodeDEFButton || e.getSource()==nodesDEFComboBox) {        	        	
        	gotoNode();
        }              
    }
	/**
	 * Minimale Grösse, angepasst an das controlPanel
	 */
    public Dimension getMinimumSize()
	{
		return new Dimension(320, 300);
	}
	/**
	 * Wunsch-Grösse, angepasst an das controlPanel
	 */
    public Dimension getPreferredSize()
	{
		return new Dimension(320, 300);
	}

	/** 
	 * Leere Implementierung um KeyListener zu erfüllen.
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent arg0) {		
	}

	/**
	 * Leere Implementierung um KeyListener zu erfüllen.
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent arg0) {
		
	}

	/** 
	 * Wenn in der ComboBox Enter gedrückt wurde, ist dieses genauso anzusehen,
	 * als wenn auf den Go! Button geklickt wurde. Also ausgewählten Knoten anspringen.
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent arg0) {
		if (arg0.getSource() == nodesDEFComboBox && arg0.getKeyChar() == KeyEvent.VK_ENTER) {
			gotoNode();
		}		
	}

	/**
	 * Funktion für ComboBox, springt zum gewählten Node.	 
	 */
	private void gotoNode() {
    	if (nodesDEFComboBox.getSelectedItem()!=null) {        	
    		myJTreeComposer.gotoDEFsParent (nodesDEFComboBox.getSelectedItem().toString());
    	}
	}	
}
