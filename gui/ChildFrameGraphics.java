package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import scene2dview.Display;
import scene2dview.Linienizer;
import scene2dview.Node;
import scenejtree.JTreeComposer;
import scenejtree.SceneTreeNode;

/**
 * Created on 13.05.2004
 * 
 * Diese Klasse nimmt die 2D Ansicht aus dem scene2Dview Paket auf.
 *
 * @author Timo Winkelvos 
 */

public class ChildFrameGraphics extends ChildFrame implements TreeSelectionListener, MouseListener {
		
	/**
	 * Referenz auf 2D Ansicht
	 */
	private Display view2d;
	/**
	 * Referent auf JTree, für durchreichen von Auswahländerungen
	 */
	private JTreeComposer accessToJTree;
	
	/**
	 * Legt eine leere 2D Ansicht an.
	 */
	public ChildFrameGraphics(){
		super();					
		view2d = new Display();
		
		this.setLayout(new BorderLayout());                		 
        this.add("Center", view2d);                                       
	}
		
	/**
	 * Erstmaliges Festlegen des ausgewählten Knotes aus dem JTree.
	 * Alle weiteren Änderungen erfolgen über TreeSelection Ereignisse.
	 *  
	 * @param selectedNode
	 * @param accessToJTree
	 */
	public void setSelectedNode(SceneTreeNode selectedNode, JTreeComposer accessToJTree) {
		this.accessToJTree = accessToJTree;
		this.view2d.setSelectedNode(selectedNode, this);
		this.revalidate();
		
		
	}

	/**
	 * ermittelt die Wunschgrösse der 2D-Ansicht
	 * 
	 * @return Default-Grösse oder Grösse von Display 
	 */
	public Dimension getPreferredSize()
	{
		if (view2d==null){
			return new Dimension(200,200);
		}
		else {
			return view2d.getPreferredSize();
		}
	}

	/**
	 * Meldet die minimale Grösse.
	 * 
	 * @return (0,0), damit im Splitpane minimiert werden kann.
	 */
	public Dimension getMinimumSize()
	{
		return new Dimension(0, 0);
	}

	/**
	 * Wird bei einem TreeSelectionEvent aufgerufen. Reicht die geänderte Auswahl
	 * an die 2D Ansicht weiter.
	 * 
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent arg0) {
		this.view2d.setSelectedNode((SceneTreeNode)arg0.getPath().getLastPathComponent(), this);				
	}

	/**
	 * Leere Methode, um MouseListener zu implementieren
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent) 
	 */
	public void mouseClicked(MouseEvent arg0) {			
	}

	/**
	 * Wurde die Maustaste auf einem Knoten gedrückt, dieses an den JTree melden,
	 * damit er angezeigt wird. Sobald der JTree soweit ist, meldet er dies über
	 * ein TreeSelectionEvent, so dass sich dann auch die 2D-Ansicht ändert.	
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getSource() instanceof Node) { 
			accessToJTree.gotoNode(new TreePath(((Node)arg0.getSource()).getTreeNode().getPath()));						
		}

	}

	/**
	 * Leere Methode, um MouseListener zu implementieren
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
	}

	/**
	 * Leere Methode, um MouseListener zu implementieren
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
	}

	/**
	 * Leere Methode, um MouseListener zu implementieren
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
	}
	
	/**
	 * gibt die linien ebene zurück, für zugang zu einstellungen.
	 * 
	 * @return instanz vom Linienizer, aus Display geholt.
	 */
	public Linienizer getLinienizer() {
		return view2d.getLinienizer();
	}	
}
