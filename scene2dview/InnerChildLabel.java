/*
 * Created on 06.07.2004
 */
package scene2dview;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/** 
 * Diese Klasse liefert ein einfaches Label für die Kindknoten im Inner.
 * Das Label bekommt einen weissen Rand, um den String besser vom 
 * Hintergrund zu trennen.
 * 
 * Eine Ableitung von JLabel wäre hier sehr schwer gewesen, deswegen eine 
 * Eigenimplementierung (es hätte viel überschrieben werden müssen).
 * 
 * @author fs
 */
public class InnerChildLabel extends JComponent {
	/**
	 * Speicher für den anzuzeigenden String
	 */
	private String displayString;
	/** 
	 * Grösse des Randes
	 */
	private final int outlineWidth = 2;
	
	/**
	 * Initialisiert das Label mit dem String, und macht
	 * es durchsichtig.
	 * 
	 * @param displayString zu zeichnender String 
	 */
	public InnerChildLabel(String displayString) {
		this.displayString = displayString;
		this.setOpaque(false);
	}
	/**
	 * Konstruktor für leeres Label.
	 */
	public InnerChildLabel() {
		this("");
	}
	/**
	 * Ermitteln des angezeigten Strings.
	 * 
	 * @return Returns the displayString.
	 */
	public String getDisplayString() {
		return displayString;
	}
	/**
	 * @param displayString The displayString to set.
	 */
	public void setDisplayString(String displayString) {
		this.displayString = displayString;
		this.repaint();
	}
	/**
	 * überschriebenes paintComponent() zum Zeichnen des Labels.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);             
		Graphics2D g2 = (Graphics2D)g;
		
		/* 
		 * Antialiasing aktivieren, um allen gezeichneten Objekten 
	     * einen geglätteten Rand zu verpassen
		 */
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
		
		String textToRender = displayString;
		
		int labelWidth = this.getWidth();		
		FontMetrics fm = g2.getFontMetrics();
		int textWidth = fm.stringWidth(displayString);
		// breite, anpassen und / oder zentrieren
		// höhe, string einfach oben ausgeben			
		
		// wenn der Text breiter als das Label ist, kürzen und "auspunkten"
		if (textWidth > labelWidth - 2 * outlineWidth) { //etwas kleiner machen
			String clipString = "...";
			int totalWidth = fm.stringWidth(clipString);
			int nChars;
			for(nChars = 0; nChars < displayString.length(); nChars++) {
				totalWidth += fm.charWidth(displayString.charAt(nChars));
				if (totalWidth > labelWidth - 2 * outlineWidth) { //etwas kleiner machen
					break;
				}
			}
			textToRender = textToRender.substring(0, nChars) + clipString;	    			
			textWidth = fm.stringWidth(textToRender);
		}
		int y = outlineWidth + fm.getHeight()*3/5;
		int x = (labelWidth - textWidth) / 2 +outlineWidth;
		
		g2.setColor(Color.white);
		
		// zu allen diagonalen etwas verschieben in weiss, um so eine Outline zu erzeugen		
		for (int outlineX = x - outlineWidth; outlineX <= x + outlineWidth; outlineX++) {
			for (int outlineY = y - outlineWidth; outlineY <= y + outlineWidth; outlineY++) {
				if (outlineX != x && outlineY != y) {
					g2.drawString(textToRender,outlineX,outlineY);
				}
			}
		}
						
		//den eigentlichen String mittig zeichnen
		g2.setColor(Color.BLACK);
		g2.drawString(textToRender, x, y);						
	}
}
