/*
 * Created on 23.06.2004
 *
 */
package scenejtree;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/** 
 * Diese Klasse erweitert das DefaultTreeSelectionModel für einen JTree.
 * Es überprüft, ob ein Knoten, oder ein Blatt angewählt werden soll.
 * Ein Blatt wird nicht ausgewählt, richtige Knoten hingegen ja.
 * 
 * @author Frederik Suhr
 */
public class FilteredTreeSelectionModel extends DefaultTreeSelectionModel {

	/**
	 * Einfacher Super() aufruf im Konstruktor ist ausreichend.
	 * Zusätzlich nur Einfachauswahl ermöglichen 
	 */
	public FilteredTreeSelectionModel() {
		super();		
		// für klickbare ereignisse: keine mehrfach auswahl erlauben:	
		this.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);		
	}
	/**
	 * überschriebene Methode, lässt nur pfade, die auf kein blatt zeigen in
	 * der auswahl zu
	 */
	public void setSelectionPath(TreePath path) {
		if (isPathSelectable(path)) {			
			super.setSelectionPath(path);
		}
	}
    	
	/**
	 * Überschriebene Methode, wählt bei einem Array nur den letzten pfad (ggf.) aus
	 * 
	 */	
	public void setSelectionPaths(TreePath[] path) {						
		// den letzten pfad auswählen
		if (isPathSelectable(path[path.length - 1]))
			super.setSelectionPaths(path);		
	}		
	
	/**
	 * Diese Hilfmethode prüft, ob ein Pfad der Auswahl hinzugefügt werden darf.
	 * 
	 * @param path der zu überprüfen ist
	 * @return true, wenn es sich um einen node handelt, der kein leave ist
	 */
	private boolean isPathSelectable(TreePath path) {
		return !((TreeNode)(path.getLastPathComponent())).isLeaf();
	}
}
