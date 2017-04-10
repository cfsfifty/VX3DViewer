/*
 * Created on 23.06.2004
 *
 */
package scenejtree;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/** 
 * Diese Klasse erweitert das DefaultTreeSelectionModel f�r einen JTree.
 * Es �berpr�ft, ob ein Knoten, oder ein Blatt angew�hlt werden soll.
 * Ein Blatt wird nicht ausgew�hlt, richtige Knoten hingegen ja.
 * 
 * @author Frederik Suhr
 */
public class FilteredTreeSelectionModel extends DefaultTreeSelectionModel {

	/**
	 * Einfacher Super() aufruf im Konstruktor ist ausreichend.
	 * Zus�tzlich nur Einfachauswahl erm�glichen 
	 */
	public FilteredTreeSelectionModel() {
		super();		
		// f�r klickbare ereignisse: keine mehrfach auswahl erlauben:	
		this.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);		
	}
	/**
	 * �berschriebene Methode, l�sst nur pfade, die auf kein blatt zeigen in
	 * der auswahl zu
	 */
	public void setSelectionPath(TreePath path) {
		if (isPathSelectable(path)) {			
			super.setSelectionPath(path);
		}
	}
    	
	/**
	 * �berschriebene Methode, w�hlt bei einem Array nur den letzten pfad (ggf.) aus
	 * 
	 */	
	public void setSelectionPaths(TreePath[] path) {						
		// den letzten pfad ausw�hlen
		if (isPathSelectable(path[path.length - 1]))
			super.setSelectionPaths(path);		
	}		
	
	/**
	 * Diese Hilfmethode pr�ft, ob ein Pfad der Auswahl hinzugef�gt werden darf.
	 * 
	 * @param path der zu �berpr�fen ist
	 * @return true, wenn es sich um einen node handelt, der kein leave ist
	 */
	private boolean isPathSelectable(TreePath path) {
		return !((TreeNode)(path.getLastPathComponent())).isLeaf();
	}
}
