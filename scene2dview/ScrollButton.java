/*
 * Created on 05.07.2004
 *
 */
package scene2dview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author fs
 *
 * Diese Klasse liefert einen Scrollknopf für inner. Er wird standartmäßig 
 * für links erzeugt, kann dann aber gedreht werden für einen ScrollKnopf
 * nach rechts.
 */
public class ScrollButton extends JComponent {
    
	/**
	 * true, normaler ScrollButton, bei false spiegeln.
	 */
	private boolean leftScrollButton;
	
	/**
	 * anzuzeigender Wert der in dieser Richtung unsichtbaren Elemente.
	 */
	private int displayValue = 0;	
	/**
	 * Label um displayValue anzuzeigen.
	 */
	private JLabel displayLabel = new JLabel(String.valueOf(displayValue));
	
	/*
	 * Farben für ScrollKnopf
	 */
	private static final Color ACTIVATED_OUTLINE = Color.BLACK;
	private static final Color ACTIVATED_FILL = Color.GREEN;
	private static final Color DEACTIVATED_OUTLINE = Color.GRAY;
	private static final Color DEACTIVATED_FILL = Color.LIGHT_GRAY;
	
	/**
	 * Linienpfad für ScrollKnopf
	 */
	private GeneralPath scrollPath;
	
	/**
	 * Der Konstruktor legt einen Scrollknopf mit Label an.
	 * 
	 * @param leftScrollButton true, wenn Scrollknopf nach links zeigen soll, false für nach rechts.
	 */
	public ScrollButton (boolean leftScrollButton) {
		this.leftScrollButton = leftScrollButton;		
		this.add(displayLabel);
		displayLabel.setVerticalAlignment(SwingConstants.CENTER);
		displayLabel.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * Setzt den anzuzeigenden Wert.
	 * 
	 * @param displayValue The displayValue to set.
	 */
	public void setDisplayValue(int displayValue) {
		this.displayValue = displayValue;
		displayLabel.setText(String.valueOf(displayValue));
	}
	/**
	 * Zeichnen des Buttons. Bei Werten über 0 aktiviert, sonst im deaktivierten
	 * Stil.
	 */
	public void paintComponent(Graphics g) { 						
		super.paintComponent(g);             
		Graphics2D g2 = (Graphics2D)g;
				 
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
										
        // strichbreite 2 pixel
        g2.setStroke(new BasicStroke(2.0f));
        
        if (displayValue > 0) {
        	// aussehen enabled
        	g2.setPaint(ScrollButton.ACTIVATED_FILL);
        	g2.fill(scrollPath);
        	g2.setPaint(ScrollButton.ACTIVATED_OUTLINE);
        	g2.draw(scrollPath);        	
        } 
        else {
        	// aussehen disabled
        	g2.setPaint(ScrollButton.DEACTIVATED_FILL);
        	g2.fill(scrollPath);
        	g2.setPaint(ScrollButton.DEACTIVATED_OUTLINE);
        	g2.draw(scrollPath);        	        	
        }
	}
	/**
	 * überschriebene methode, um label an componentgrösse anzupassen
	 */
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x,y,w,h);
		if (leftScrollButton)
			displayLabel.setBounds(w/6,0,w*5/6,h);
		else
			displayLabel.setBounds(0,0,w*5/6,h);
		
		// scrollknopfpfad für ein linkes Dreieck
		scrollPath = new GeneralPath();
		
        // startpunkt links mitte
		scrollPath.moveTo(1, this.getHeight() / 2);
        // nach oben 1/3
		scrollPath.lineTo(this.getWidth() / 3, 1);
		// nach rechts oben
		scrollPath.lineTo(this.getWidth() - 2, 1);
		// nach rechts unten
		scrollPath.lineTo(this.getWidth() - 2, this.getHeight() - 2);
		// nach unten 1/3
		scrollPath.lineTo(this.getWidth() / 3, this.getHeight() - 2);
		// und abschliessen
        scrollPath.closePath();

        // wenn rechtes dreieck, einmal drehen und zurückverschieben
        if (!leftScrollButton) {
        	AffineTransform at = new AffineTransform();
        	// um (0;0) um 180 grad drehen
        	at.rotate(Math.toRadians(180));        
        	// nach rechts unten auf ursprüngliche position verschieben
        	at.translate(-this.getWidth()+1,-this.getHeight()+1);
        	scrollPath.transform(at);
        }        
	}
	/**
	 * überschribene methode, um bei disabled den wert zu löschen
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		displayLabel.setEnabled(enabled);
		
		if (!enabled) {
			setDisplayValue(0);					
		}
			
		// reicht zum neuzeichnen
		this.revalidate();
	}
}
