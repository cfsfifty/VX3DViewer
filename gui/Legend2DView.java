package gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.geom.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import scene2dview.*;

/**
 * Die von JPanel abgeleitete Klasse Legend2DView erstellt das
 * Panel mit der Legende zur 2D Ansicht
 * 
 * @author Patrick Helmholz
 */
public class Legend2DView extends JPanel {
	
	private JLabel defLabel, normalLabel, useLabel, routeLabel, routeDefLabel, routeUseLabel, innerTriangle, outerTriangle;
	// die Schriftart der Labels
	private Font defaultFont = new Font("Arial",Font.PLAIN,12);
	// Größe der Nodes
	private final int NODE_WIDTH = 20;
	private final int NODE_HEIGHT = 20;
	// Größe der Textlabels
	private final Dimension size = new Dimension(220,30);
	// Position der Textlabels
	private final Point location = new Point(70,10);
	
	/**
	 * Konstruktor der Klasse Legend2DView
	 * Erstellt ein Panel und fügt die Labels ein
	 */
	public Legend2DView() {
		this.setLayout(new GridLayout(8,1));
		
		// Label für normale Nodes
		JLabel label1 = new JLabel();
		normalLabel = new JLabel("Normal Nodes", SwingConstants.LEFT);
		normalLabel.setFont(defaultFont);
		normalLabel.setSize(size);
		normalLabel.setLocation(location);
		label1.add(normalLabel);
		label1.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für DEF Nodes
		JLabel label2 = new JLabel();
		defLabel = new JLabel("Used Nodes", SwingConstants.LEFT);
		defLabel.setFont(defaultFont);
		defLabel.setSize(size);
		defLabel.setLocation(location);
		label2.add(defLabel);
		label2.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für USE Nodes
		JLabel label3 = new JLabel();
		useLabel = new JLabel("<html>Nodes instancing <br> corresponding Def-Nodes</html>", JLabel.LEFT);
		useLabel.setFont(defaultFont);
		useLabel.setSize(size);
		useLabel.setLocation(location);
		label3.add(useLabel);
		label3.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für ROUTE Nodes
		JLabel label4 = new JLabel();
		routeLabel = new JLabel("Nodes with routed Fields", JLabel.LEFT);
		routeLabel.setFont(defaultFont);
		routeLabel.setSize(size);
		routeLabel.setLocation(location);
		label4.add(routeLabel);
		label4.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für ROUTE/DEF Nodes
		JLabel label5 = new JLabel();
		routeDefLabel = new JLabel("Used Nodes with routed Fields", JLabel.LEFT);
		routeDefLabel.setFont(defaultFont);
		routeDefLabel.setSize(size);
		routeDefLabel.setLocation(location);
		label5.add(routeDefLabel);
		label5.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für ROUTE/USE Nodes
		JLabel label6 = new JLabel();
		routeUseLabel = new JLabel("<html>Nodes with routed Fields and <br> instancing corresponding Def-Nodes</html>", JLabel.LEFT);
		routeUseLabel.setFont(defaultFont);
		routeUseLabel.setSize(size);
		routeUseLabel.setLocation(location);
		label6.add(routeUseLabel);
		label6.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für Inner Triangle
		JLabel label7 = new JLabel();
		innerTriangle = new JLabel("Simplified Subtree for detailed Innerview", JLabel.LEFT);
		innerTriangle.setFont(defaultFont);
		innerTriangle.setSize(size);
		innerTriangle.setLocation(location);
		label7.add(innerTriangle);
		label7.setBorder(BorderFactory.createEtchedBorder());
		
		// Label für Innertriangle
		JLabel label8 = new JLabel();
		outerTriangle = new JLabel("Simplified Subtree for outer Area", JLabel.LEFT);
		outerTriangle.setFont(defaultFont);
		outerTriangle.setSize(size);
		outerTriangle.setLocation(location);
		label8.add(outerTriangle);
		label8.setBorder(BorderFactory.createEtchedBorder());
		
		// Labels dem Panel hinzufügen
		this.add(label1);
		this.add(label2);
		this.add(label3);
		this.add(label4);
		this.add(label5);
		this.add(label6);
		this.add(label7);
		this.add(label8);
	}
	
	/**
	 * Methode zum Zeichnen der Kreise in der Legende
	 */
	public void paint(Graphics g) { 
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		/*
		 * Antialiasing aktivieren, um allen gezeichneten Objekten 
		 * einen geglätteten Rand zu verpassen
		 */
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Transformierung: verschiebung in y Richtung
		AffineTransform at = new AffineTransform();
		at.translate(0, 47);
		
		// Kreis für die Darstellung der Nodes				
		Ellipse2D circle = new Ellipse2D.Double(20, 15, NODE_WIDTH, NODE_HEIGHT);
		
		g2.setStroke(new BasicStroke(2));
		
		// normale Node
		g2.setColor(Color.black);
		g2.draw(circle);
		
		// DEF Node
		g2.setColor(Node.USES_COLOR);
		g2.transform(at);
		g2.draw(circle);
		
		// USE Node
		g2.setColor(Node.DEF_COLOR);
		g2.transform(at);
		g2.draw(circle);
		
		// routed Node
		g2.setColor(Node.ROUTE_COLOR);
		g2.transform(at);
		g2.draw(circle);
		
		// routed DEF Node
		g2.setStroke(Node.DASHED_STROKE);
		g2.setColor(Node.ROUTE_COLOR);
		g2.transform(at);
		g2.draw(circle);
		
		g2.setStroke(Node.DASHED_STROKE_2);
		g2.setColor(Node.USES_COLOR);
		g2.draw(circle);
		
		// routed USE Node
		g2.setStroke(Node.DASHED_STROKE);
		g2.setColor(Node.ROUTE_COLOR);
		g2.transform(at);
		g2.draw(circle);
		
		g2.setStroke(Node.DASHED_STROKE_2);
		g2.setColor(Node.DEF_COLOR);
		g2.draw(circle);
		
		// Tiangle für die Darstellung der Restbäume im Inner
		GeneralPath trianglePath = new GeneralPath();
		
		// setzen des Triangle-Paths für Inner Triangles
		trianglePath.moveTo(30, 15);
		trianglePath.lineTo(45, 40);
		trianglePath.lineTo(15, 40);
		trianglePath.closePath();
		
		// Triangle für Inner Triangles zeichnen
		g2.transform(at);
		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.black);
		g2.draw(trianglePath);
		
		// Tiangle für die Darstellung der Restbäume im Outer
		GeneralPath trianglePath2 = new GeneralPath();
		
		// setzen des Triangle-Paths für Outer Triangles
		trianglePath2.moveTo(25, 15);
		trianglePath2.lineTo(35, 15);
		trianglePath2.lineTo(35, 25);
		trianglePath2.lineTo(45, 45);
		trianglePath2.lineTo(15, 45);
		trianglePath2.lineTo(25, 25);
		trianglePath2.lineTo(35, 25);
		trianglePath2.lineTo(25, 25);
		trianglePath2.closePath();
		
		// Triangle für Outer Triangles zeichnen
		g2.transform(at);
		g2.draw(trianglePath2);
	}
	
}
