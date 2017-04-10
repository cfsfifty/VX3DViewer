/*
 * Created on 16.07.2004
 *
 */
package misc;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JScrollPane;

/**
 * Diese Klasse ermöglicht es, auf einem JScrollPane und ggf. seinen Subkomponenten
 * eine MausNavigation durch gedrückhalten der Maustaste zu aktivieren.
 * 
 * Die Geschwindigkeit kann über setScrollSpeed() eingestellt werden. Siehe hierzu Konstanten. 
 * @see #DEFAULT_SPEED
 * 
 * @author Frederik Suhr
 */
public class ScrollPaneNavigator {
	/**
	 * ScrollPane, auf dem per Mausnavigation gescrollt werden soll
	 */
	private final JScrollPane scrollPane;
	
	/**
	 * auf dieser Komponente wird der Mauscursor aktiviert, evtl = scrollPane, ggf. eher eine Komponente über dem ScrollPane
	 */
	private final Component setMouseCursorHere;
	
	/**
	 * null, solange keine navigation läuft, sonst immer die letzte koordinate, zu der gescrollt wurde (beim start aktuelle Position)
	 */
	private Point dragNavigationStartPoint;

	/**
	 * MouseMotionListener für drag event zum navigieren
	 */
	private MouseMotionListener dragNavigationMouseMotionListener;
	
	/**
	 * MouseListener für loslassen der Maustaste / beenden der navigation
	 */
	private MouseListener dragNavigationMouseListener;
	
	/**
	 * Scrollgeschwindigkeit aus SUPER_QUICK_SPEED, QUICK_SPEED, NORMAL_SPEED, SLOW_SPEED oder SUPER_SLOWSPEED
	 */
	private byte scrollSpeed;
	
	public static final byte SUPER_QUICK_SPEED = -1;
	public static final byte QUICK_SPEED = 0;
	public static final byte NORMAL_SPEED = 1;
	public static final byte SLOW_SPEED = 2;
	public static final byte SUPER_SLOW_SPEED = 3;
	
	/**
	 * Standartgeschwindgkeit für scrolling, etwas langsamer als normal.
	 */
	public static final byte DEFAULT_SPEED = SLOW_SPEED;

	/**
	 * Konstruktor, aktiviert Mausnavigation auf dem übergebenen scrollPane, setzt
	 * den entsprechenden Mauscursor auf der Komponente setMouseCursorHere.
	 * Sollen noch Subkomponenten der view im scrollPane die mausnavigation ebenfalls
	 * unterstützen, diese mit activateNavigation(Component) anmelden.
	 * 
	 * @param scrollPane auf dem gescrollt werden soll
	 * @param setMouseCursorHere hier wird der Mauscursor zwischen Default und Move cursor gewechselt.
	 */
	public ScrollPaneNavigator(JScrollPane scrollPane, Component setMouseCursorHere) {
		this.scrollPane = scrollPane;
		this.setMouseCursorHere = setMouseCursorHere;
		
		createMouseEventListeners();
		
		activateNavigation(scrollPane);
		
		setScrollSpeed(ScrollPaneNavigator.DEFAULT_SPEED);
	}

	/**
	 * legt die EventListener für Aktionsstart und Ende der Mausnavigation an.
	 */
	private void createMouseEventListeners() {		
		// für Navigationsstart StartKoordinate und Cursor setzen, Navigationsfortschritt ScrollPane verschieben  
		dragNavigationMouseMotionListener = new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {		
				Component view = scrollPane.getViewport().getView();
				Rectangle visibleRect = scrollPane.getVisibleRect();
				
				if (view.getWidth() <= visibleRect.width &&
					view.getHeight() <= visibleRect.height)					
					return; //keine drag navigation, panel zu klein				

				if (dragNavigationStartPoint == null) {
					// drag navigation starten
					dragNavigationStartPoint = e.getPoint();
					// Mauscursor initialisieren
					setMouseCursorHere.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					return;
				}	
					
				// aktuelle Position:
				Point viewPoint = scrollPane.getViewport().getViewPosition();
				
				// relative Änderung ermitteln:
				int moveX = e.getPoint().x - dragNavigationStartPoint.x;
				int moveY = e.getPoint().y - dragNavigationStartPoint.y;
								
				// geschwindigkeit erhöhen, belassen oder verlangsamen:
				switch (scrollSpeed) {
					case SUPER_QUICK_SPEED:
						moveX *= 4;
						moveY *= 4;
						break;
					case QUICK_SPEED:
						moveX *= 2;
						moveY *= 2;
						break;
					case NORMAL_SPEED:
						break;
					default:
						if (Math.abs(moveX) > 2)
							moveX /= scrollSpeed;
						if (Math.abs(moveY) > 2)
							moveY /= scrollSpeed;										
				}
				
				// relative Änderung aufaddieren
				viewPoint.translate(moveX, moveY);
				
				if (view.getWidth() < visibleRect.width)
					viewPoint.x = 0; // sollte nicht passieren
				if (view.getHeight() < visibleRect.height)
					viewPoint.y = 0; // sollte nicht passieren
				
				// obere und unter grenzen einhalten
				if (viewPoint.x<0)
					viewPoint.x=0;
				if (viewPoint.y<0)
					viewPoint.y=0;
				if (!(view.getWidth() < visibleRect.width) && viewPoint.x > view.getWidth()  - scrollPane.getViewport().getWidth())
					viewPoint.x=view.getWidth()  - scrollPane.getViewport().getWidth();
				if (!(view.getHeight() < visibleRect.height) && viewPoint.y > view.getHeight() - scrollPane.getViewport().getHeight())
					viewPoint.y=view.getHeight() - scrollPane.getViewport().getHeight();

				// abspeichern des nächsten Dragnavigations start punktes 
				dragNavigationStartPoint = e.getPoint();
				// ansicht verschieben:
				scrollPane.getViewport().setViewPosition(viewPoint);												
					
			}			
		};		

		// für Navigationsende: Navigation deaktivieren und Cursor wiederherstellen;
		dragNavigationMouseListener = new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (dragNavigationStartPoint != null) {
					dragNavigationStartPoint = null;
					setMouseCursorHere.setCursor(Cursor.getDefaultCursor());
					return;
				}				
			}
		};
	}
	/**
	 * Eine Subkomponente vom ScrollPane View, auf der ebenfalls
	 * die Navigation aktiviert werden soll.
	 * 
	 * @param comp auf der DragNavigation aktiviert werden soll.
	 */
	public void activateNavigation (Component comp) {		
		comp.addMouseMotionListener(dragNavigationMouseMotionListener);
		comp.addMouseListener(dragNavigationMouseListener);
	}
	
	/**
	 * stellt die ScrollGeschwindigkeit ein. 
	 * @param scrollSpeed Speed aus den Konstanten von ScrollPaneNavigator
	 */
	public void setScrollSpeed(byte scrollSpeed) {		
		if (scrollSpeed >= SUPER_QUICK_SPEED && scrollSpeed <= SUPER_SLOW_SPEED)
			this.scrollSpeed = scrollSpeed;
		else {
			System.err.println("Wrong scrollSpeed, assuming DEFAULT_SPEED");
			this.scrollSpeed = DEFAULT_SPEED;
		}
	}

}
