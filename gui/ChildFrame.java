package gui;
import java.awt.*;
import javax.swing.*;

/**
 * Diese Klasse bildet eine Oberklasse für die GUI Elemente JTree (ChildFrameJTree)
 * und 2D-Ansicht (ChildFrameGraphics).
 * 
 * Created on 13.05.2004
 * @author Timo Winkelvos
 */
public abstract class ChildFrame extends JPanel {

	private Color col;

	/**
	 * Legt ein ChildFrame Panel an. Gew. Grössen und Look and Feel setzen. 
	 */
	public ChildFrame(){
		
		this.setMinimumSize(getMinimumSize());
		this.setPreferredSize(getPreferredSize());				
		
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
	}
	/**
	 * Liefert die minimale Grösse.
	 * 
	 * @return Dimension
	 */
	public Dimension getMinimumSize(){
		return new Dimension(5,400);
	}
	
	/**
	 * Liefert die gewünschte Grösse.
	 * 
	 * @return Dimension
	 */
	public Dimension getPreferredSize(){
		return new Dimension(250,600); 
	}


}
