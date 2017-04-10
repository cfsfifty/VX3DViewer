/*
 * Created on 06.07.2004
 */ 
package printer;

/**
 * Diese Klasse berechnet und Speichert die beiden Punkte,
 * zwischen denen die Kannten des Graphen verlaufen.  
 * Die Punkte werden als zweidimensionales float-Array (childCoor, parentCoor) gespeichert
 * 
 * @author Timo Winkelvos
 */
public class EdgeStore {
	//Der Kantenansatzpunkt des Vaterknotens
	public float[] parentCoor;
	//Der Kantenansatzpunkt des Kindes
	public float[] childCoor;
	//die Ebene, auf der der Vaterknoten im Baum liegt
	public int parentNodeLevel;
	//Hier die jeweilige Position der Knoten (von Links)
	public float parentX, childX;
	public static final float NODEHEIGHT = 25, NODEWIDTH = 80;
	
	/**
	 * Der Konstruktor erstellt aus den horzontalen Positionen und der Ebene auf der der Vater liegt eine neue Kante
	 * 
	 * @param parentNodeLevel die Ebene, auf der der Vaterknoten im Baum liegt
	 * @param parentX die von links gezählte, horizontale Postition des Vaterknotens
	 * @param childX die von links gezählte, horizontale Postition des Kindknotens
	 */
	public EdgeStore(int parentNodeLevel, float parentX , float childX){
		this.parentX = parentX;
		this.childX = childX;
		this.parentNodeLevel = parentNodeLevel;
		parentCoor = new float[2];
		childCoor = new float[2];
		
		setParentCoor();
		setChildCoor();
	}
	
	/**
	 * setParentCoor
	 * diese Methode berechnet den Punkt an der Unterseite des Vaterrechtecks, an dem die Kante ansetzt,
	 * indem die halbe Rechteckbreite und die Höhe auf den RE Ursprungspunkt aufaddiert werden
	 */
	public void setParentCoor(){
		//berechne die Ursprünge des entsprechenden Rechtecks
		float xPos = 20 + parentX*(NODEWIDTH+15);
		float yPos = 20 + parentNodeLevel*(NODEHEIGHT+40);
		//verändert obige Werte, so daß die Koordinaten dem Punkt in der Mitte der Unterseite des Rechtecks entsprechen
		parentCoor[0] = xPos + (0.5f * NODEWIDTH);
		parentCoor[1] = yPos + (NODEHEIGHT);		
	}
	
	/**
	 * setParentCoor
	 * diese Methode berechnet den Punkt an der Oberseite des Kind-Rechtecks, an dem die Kante ansetzt,
	 * indem die halbe Rechteckbreite zu dem RE Ursprungspunkt aufaddiert werden.
	 */
	public void setChildCoor(){
		//berechne die Ursprünge des entsprechenden Rechtecks
		float xPos = 20 + childX*(NODEWIDTH+15);
		float yPos = 20 + (parentNodeLevel+1)*(NODEHEIGHT+40);
		//verändert obige Werte, so daß die Koordinaten dem Punkt in der Mitte der Oberseite des Rechtecks entsprechen
		childCoor[0] = xPos + (0.5f * NODEWIDTH);
		childCoor[1] = yPos;
	}
}
//
//