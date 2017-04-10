/*
 * Created on 25.06.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package scenejtree;

import junit.framework.TestCase;

/**
 * @author Privat
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SceneTreeNodeTest extends TestCase {

	public final void testGetSceneNodesDescendantsCount() {
		SceneTreeNode root = new SceneTreeNode ("root", "root", "", "", true, null);													
		
		SceneTreeNode node1 = new SceneTreeNode ("node1", "node1", "", "", true, null);		
		root.addSceneTreeNode(node1);
		root.addSceneTreeNode(new SceneTreeNode ("node2", "node2", "", "", true, null));
		root.addSceneTreeNode(new SceneTreeNode ("node3", "node3", "", "", true, null));
		
		
		SceneTreeNode subNode = new SceneTreeNode ("node4", "node4", "", "", true, null);
		node1.addSceneTreeNode(subNode);		
		subNode.addSceneTreeNode(new SceneTreeNode ("attribut1", "attribut1", "", "", false, null));
		
		
		assertEquals(3, root.getSceneNodeChildCount());
		assertEquals(4, root.getSceneNodesDescendantsCount());
		
		
		
		
	}

	public final void testGetSceneNodeChildCount() {
		//TODO Implement getSceneNodeChildCount().
	}

}
