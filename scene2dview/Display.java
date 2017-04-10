package scene2dview;
import java.awt.*;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import scenejtree.SceneTreeNode;
/**
 *
 * Diese Klasse baut die verschiedenen Layer der 2D Ansicht zusammen
 * und bringt sie in einem ScrollPane zur Ansicht.
 * 
 * @author SEP
 */

public class Display extends JPanel{
	private Layoutizer layers; 
	private JScrollPane scrollContainer;
	private SceneTreeNode selectedNode;
	
	public Display() {
		// scrollbare innere Ansicht
		scrollContainer = new JScrollPane();

		layers = new Layoutizer(scrollContainer);
		layers.setBackground(Color.DARK_GRAY);
		
		scrollContainer.setViewportView(layers);
		scrollContainer.getViewport().setBackground(Color.WHITE);
		final int UNIT_INCREMENT = 20;
		scrollContainer.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		scrollContainer.getVerticalScrollBar().setBlockIncrement(2*UNIT_INCREMENT);
		scrollContainer.getHorizontalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		scrollContainer.getHorizontalScrollBar().setBlockIncrement(2*UNIT_INCREMENT);
		
		this.setLayout(new BorderLayout());
		this.add("Center", scrollContainer);
		
	}
	/**
	 * Wird vom TreeSelectionListener in Graphics aufgerufen. Speichert den
	 * Knoten in einer Instanzvariable, und wenn es kein ROUTE ist, wird
	 * dieser zur Anzeige gebracht
	 * 
	 * @param selectedNode im JTree gewählter Knoten
	 * @param componentToReceiveMouseEvents hier sollen Mausklicks etc. auf Nodes ankommen
	 */
	public void setSelectedNode(SceneTreeNode selectedNode, MouseListener componentToReceiveMouseEvents) {
		
		if (!selectedNode.isROUTE()) {
			this.selectedNode = selectedNode;
			
			// Knoten weitergeben, grössen ermitteln und setzen.
			layers.outer.setSelectedNode(selectedNode, componentToReceiveMouseEvents);
			layers.outer.setPreferredSize(layers.outer.getSize());
			layers.linienizer.setSize(layers.outer.getSize());
			layers.linienizer.setPreferredSize(layers.outer.getSize());
			layers.setSize(layers.outer.getSize());
			layers.setPreferredSize(layers.outer.getSize());
			
			scrollContainer.validate(); // wichtig, erstellt ggf. scrollbars
			
			// position von inner ggf. anpassen, damit der selectedNode auch sichtbar ist
			
			// dazu: ermitteln der Grösse von inner und der davon sichtbaren view.
			Rectangle innerRectangle = layers.outer.inner.getBounds();
			Rectangle view = scrollContainer.getViewport().getVisibleRect();
			
			if (scrollContainer.getVisibleRect().width < innerRectangle.width) {
				// selectedNode ist das wichtigste zum reinscrollen, also zentrieren
				scrollContainer.getViewport().setViewPosition(new Point(innerRectangle.x + (layers.outer.inner.getWidth() - scrollContainer.getVisibleRect().width) / 2, innerRectangle.y));				
			} 
			else {
				// komplett anzeigen, passt rein
				Point newViewPosition = scrollContainer.getViewport().getViewPosition();
				// horizontal zentrieren
				newViewPosition.x = innerRectangle.x + (layers.outer.inner.getWidth() - scrollContainer.getVisibleRect().width) / 2;
				
				// wenn nicht komplett drin, vertikale position anpassen
				if (!view.contains(innerRectangle)) {															
					if (innerRectangle.y + innerRectangle.height > view.y + view.height)
						newViewPosition.y = innerRectangle.y + innerRectangle.height - view.height; // + 22
					else if (innerRectangle.y < view.y )						
						newViewPosition.y = innerRectangle.y;																									
					
				}
				// errechnete Position setzen
				scrollContainer.getViewport().setViewPosition(newViewPosition);
					
			}						 										 
		}
	}
	
	/**
	 * Die Wunschgrösse von Display hängt vom der entsprechenden Grösse
	 * der enthalten layers ab.
	 * @return gewünschte Grösse
	 */
	public Dimension getPreferredSize()
	{
		return layers.getPreferredSize();
	}
	
	/**
	 * Gibt die Linien-Ebene zurück, für Zugang zu Einstellungen. So können
	 * Linien gezeigt werden, oder alte Linien deaktiviert etc.
	 * 
	 * @return Instanz vom Linienizer
	 */
	public Linienizer getLinienizer() {
		return layers.linienizer;
	}

}