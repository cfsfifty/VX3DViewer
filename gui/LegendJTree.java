package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Die von JPanel abgeleitete Klasse LegendJTree erstellt das
 * Panel mit der Legende zur 2D Ansicht
 * 
 * @author Patrick Helmholz
 */
public class LegendJTree extends JPanel {
	private JLabel nodeTextLabel, nodeLabel, nodeLabel2, attribTextLabel, otherAttrib, defAttrib, useAttrib, routeIn, routeOut; 
	// die Schriftarten der Labels
	private Font defaultFont = new Font("Arial",Font.PLAIN,12);
	private Font defaultBoldFont = new Font("Arial",Font.BOLD,12);
	// Farbe f�r den Hintergrund der �berschriften
	Color background = new Color(205,205,205);
	
	/**
	 * Konstruktor der Klasse LegendJTree
	 * Erstellt ein Panel und f�gt die Labels ein
	 */
	public LegendJTree() {
		this.setLayout(new GridLayout(9,1));
		
		// Label f�r den Node Text
		nodeTextLabel = new JLabel("Nodes:", JLabel.CENTER);
		nodeTextLabel.setFont(defaultBoldFont);
		nodeTextLabel.setBorder(BorderFactory.createEtchedBorder());
		// Label ist standartm��ig durchsichtig, deshalb ge�ndert
		nodeTextLabel.setOpaque(true);
		nodeTextLabel.setBackground(background);
		
		// Label f�r Nodes
		nodeLabel = new JLabel("Several Node Icontypes, like for Transform:", SwingConstants.CENTER);
		nodeLabel.setFont(defaultFont);
		nodeLabel.setIcon(new ImageIcon("./scenejtree/x3dEditIcons/Transform.gif"));
		nodeLabel.setHorizontalTextPosition(JLabel.LEFT);
		nodeLabel2 = new JLabel("<html><center><b><i>Dummy Nodes: </b></i>ROUTEs represent a Connection between two Nodes, they are not real Nodes!</center></html>)", SwingConstants.CENTER);
		nodeLabel2.setIcon(new ImageIcon("./scenejtree/x3dEditIcons/ROUTE.gif"));
		nodeLabel2.setHorizontalTextPosition(JLabel.LEFT);
		nodeLabel2.setFont(defaultFont);
		
		// Label f�r Attribut Text
		attribTextLabel = new JLabel("Attributes:", JLabel.CENTER);
		attribTextLabel.setFont(defaultBoldFont);
		attribTextLabel.setBorder(BorderFactory.createEtchedBorder());
		// Label ist standartm��ig durchsichtig, deshalb ge�ndert
		attribTextLabel.setOpaque(true);
		attribTextLabel.setBackground(background);
		
		// Label f�r DEF
		defAttrib = new JLabel("Attribute with the DEF Name of the Node", JLabel.LEFT);
		defAttrib.setFont(defaultFont);
		defAttrib.setIcon(new ImageIcon("./scenejtree/x3dEditIcons/DEF.gif"));
		
		// Label f�r USE
		useAttrib = new JLabel("Attribute with the DEF Name of the used Node", JLabel.LEFT);
		useAttrib.setFont(defaultFont);
		useAttrib.setIcon(new ImageIcon("./scenejtree/x3dEditIcons/USE.gif"));
		
		// Label f�r Route out
		routeOut = new JLabel("Attribute with the Name of the routed outgoing Node", JLabel.LEFT);
		routeOut.setFont(defaultFont);
		routeOut.setIcon(new ImageIcon("./scenejtree/x3dEditIcons/route_out.gif"));
		
		// Label f�r Route in
		routeIn = new JLabel("Attribute with the Name of the routed incoming Node", JLabel.LEFT);
		routeIn.setFont(defaultFont);
		routeIn.setIcon(new ImageIcon("./scenejtree/x3dEditIcons/route_in.gif"));
		
		// Label f�r andere Attribute
		otherAttrib = new JLabel("Other Attributes", JLabel.LEFT);
		otherAttrib.setFont(defaultFont);
		otherAttrib.setIcon(new ImageIcon("./scenejtree/ICONS/ANode.gif"));
		
		// Labels dem Panel hinzuf�gen
		this.add(nodeTextLabel);
		this.add(nodeLabel);
		this.add(nodeLabel2);
		this.add(attribTextLabel);
		this.add(defAttrib);
		this.add(useAttrib);
		this.add(routeOut);
		this.add(routeIn);
		this.add(otherAttrib);
	}
}
