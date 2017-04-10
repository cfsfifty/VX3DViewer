/*
 * Created on 23.06.2004
 *
 */
package scene2dview;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * In dieser Klasse wurde eine statische Methode angelegt, die für einen Knoten
 * seine Kinder ermittelt und als Array zurückgibt.
 * 
 * @author Frederik Suhr 	- Test (nach Test-First)
 * @author Patrick Helmholz - Implementierung zum Test
 */
public class SelectedNodeFactory {
	/**
	 * Diese Methode soll ein Array zurückgeben, dass alle
	 * Kindknoten vom übergebenen Knoten enthält. Dabei sollen
	 * alle Attribute übersprungen werden. Die Sortierung soll 
	 * der im JTree entsprechen, der erste Knoten dann bei Index 0,
	 * der zweite bei [1], so dass dadurch auch die Position im 
	 * JTree erkannt werden kann.
	 * 
	 * @param node Knoten, dessen Kinder ermittelt werden sollen
	 * @return Kinder (ohne Attribute) des Knoten in einem Array
	 */
	
	public static DefaultMutableTreeNode[] getChildrenArray(DefaultMutableTreeNode node) {
		ArrayList childrenList = new ArrayList();
		DefaultMutableTreeNode[] childrenArray;
		//System.out.println(node.getChildCount());
		if (node != null) {
			for (int i = 0; i < node.getChildCount(); i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
				if (child.isLeaf() || child.toString().matches("ROUTE"))
					continue;
				else
					childrenList.add(child);
			}
			childrenArray = new DefaultMutableTreeNode[childrenList.size()];
			childrenArray = (DefaultMutableTreeNode[]) childrenList.toArray(childrenArray); 
			return childrenArray;
		}
		return null;
	}
}
