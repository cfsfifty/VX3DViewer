/*
 * Created on 22.06.2004
 *
 */
package scene2dview;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;
/**
 * Diese Klasse baut das Ebenen-Design für die 2D-Ansicht zusammen.
 * Auf der untersten Ebene liegt der OuterCanvas mit enthaltenem InnerCanvas.
 * Dadrüber liegt der Linienizer, der somit Kanten _über_ outer zeichnen kann.
 * 
 * @author bb,hp
 */
public class Layoutizer extends JLayeredPane {
	/**
	 * referenz auf Linienebene
	 */
	public Linienizer linienizer;
	/**
	 * referenz auf outer
	 */
	public OuterCanvas outer;
	/**
	 * alle panels liegen in einem JScrollPane, referenz ablegen
	 */
	private JScrollPane scrollContainer;
	
	/**
	 * Baut die Ansicht zusammen.
	 * 
	 * @param scrollContainer Referenz auf das umgebendende JScrollPane 
	 */
	public Layoutizer(JScrollPane scrollContainer){
		this.scrollContainer = scrollContainer;
		linienizer = new Linienizer();
		outer = new OuterCanvas(linienizer, scrollContainer);	
		this.setLayout(null);
		
		// ebenen 0 und 2		
		this.add(outer, new Integer(0));
		this.add(linienizer, new Integer(2));
		
		// aussehen outer
		outer.setBackground(Color.DARK_GRAY);		
		outer.setSize(new Dimension(200,200));
		outer.setPreferredSize(new Dimension(200,200));		
		outer.setOpaque(false);

		// aussehen linienizer
		linienizer.setSize(outer.getSize());
		linienizer.setPreferredSize(outer.getPreferredSize());
		linienizer.setOpaque(false);
	}
	/**
	 * Ermittelt die Wunschgrösse.
	 * 
	 * @return enspricht der Grösse von outer. 
	 */
	public Dimension getPreferredSize() {
		return outer.getPreferredSize();
	}	
}
