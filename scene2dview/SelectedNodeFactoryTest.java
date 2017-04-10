/*
 * Created on 23.06.2004
 *
 */
package scene2dview;

import javax.swing.tree.DefaultMutableTreeNode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Frederik Suhr
 *
 * Dieser Test untersucht die Methoden der gleichnamigen Klasse
 * SelectedNodeFactory. Die Tests werden jeweil vor den zu
 * entwickelnden Methoden geschrieben.
 */
public class SelectedNodeFactoryTest extends TestCase {

	// variablen zum dran arbeiten
	DefaultMutableTreeNode[] testNodes;
	DefaultMutableTreeNode nodeToAnalyze;
	DefaultMutableTreeNode subNode, node1, node2, node3;
	
	// einfacher konstruktor
	public SelectedNodeFactoryTest (String name) {
		super (name);
	}
	
	// zusammenstellen einer test suite mit allen tests
	public static Test suite() {
		TestSuite suite = new TestSuite ("SelectedNodeFactoryTest");
		suite.addTest(new SelectedNodeFactoryTest("testGetChildrenArray1"));
		suite.addTest(new SelectedNodeFactoryTest("testGetChildrenArray2"));
		suite.addTest(new SelectedNodeFactoryTest("testGetChildrenArray3"));
		suite.addTest(new SelectedNodeFactoryTest("testGetChildrenArray4"));
		suite.addTest(new SelectedNodeFactoryTest("testGetChildrenArray5"));
		suite.addTest(new SelectedNodeFactoryTest("testGetChildrenArray6"));
		return suite;
	}
	
	// variablen vor jeder benutzung auf null setzen
	protected void setUp() {
		testNodes = null;
		nodeToAnalyze = null;
		subNode = null;
		node1 = null;
		node2 = null;
		node3 = null;
	}

	public final void testGetChildrenArray1() {
		
		// kein knoten, keine kinder
		testNodes = SelectedNodeFactory.getChildrenArray(null);
		assertTrue("es sollte null zurückgegeben werden",testNodes == null );
	}

	public final void testGetChildrenArray2() {
				
		// ein knoten, keine kinder, sollte aber schon ein leeres array statt null sein
		testNodes = SelectedNodeFactory.getChildrenArray(new DefaultMutableTreeNode("root"));
		assertTrue("ein leeres array erwartet", testNodes.length == 0);

	}
	
	public final void testGetChildrenArray3() {
		// ein knoten, ein kind (attribut weil ist ja ein blatt)
		nodeToAnalyze = new DefaultMutableTreeNode("root");
		nodeToAnalyze.add(new DefaultMutableTreeNode("child is leave = attribute"));
		testNodes = SelectedNodeFactory.getChildrenArray(nodeToAnalyze);
		assertTrue("ein leeres array erwartet", testNodes.length == 0 );
	}
	
	public final void testGetChildrenArray4() {
		// ein knoten, ein kind (node) mit einem kind (attribut weil ist ja ein blatt)
		nodeToAnalyze = new DefaultMutableTreeNode("root");		
		subNode = new DefaultMutableTreeNode("subnode");		
		subNode.add(new DefaultMutableTreeNode("child is leave = attribute"));
		nodeToAnalyze.add(subNode);
		
		testNodes = SelectedNodeFactory.getChildrenArray(nodeToAnalyze);
		assertTrue("array sollte ein element enthalten", testNodes.length == 1 );
		assertEquals("array sollte ein element, nämlich dieses enthalten: " + subNode,subNode,testNodes[0]);

	}
	public final void testGetChildrenArray5() {
		// ein knoten, vier kinder, davon einmal mit kind und enkel, einmal ohne kinder (blatt = attribut) ,die letzten beiden nur mit kind
		nodeToAnalyze = new DefaultMutableTreeNode("root");		
		node1 = new DefaultMutableTreeNode("node1");
		subNode = new DefaultMutableTreeNode("subnode");
		subNode.add(new DefaultMutableTreeNode("subchild"));
		node1.add(subNode);							
		
		node2 = new DefaultMutableTreeNode("node2");
		node2.add(new DefaultMutableTreeNode("subnode"));
		
		node3 = new DefaultMutableTreeNode("node3");
		node3.add(new DefaultMutableTreeNode("subnode"));		
				
		nodeToAnalyze.add(node1);
		nodeToAnalyze.add(new DefaultMutableTreeNode("child is leave = attribute"));
		nodeToAnalyze.add(node2);
		nodeToAnalyze.add(node3);
						
		testNodes = SelectedNodeFactory.getChildrenArray(nodeToAnalyze);
		assertTrue("4 kinder davon 3 echte nodes erwartet, also ein array mit 3 elementen: elementanzahl: " + testNodes.length, testNodes.length == 3 );
		assertEquals("erste element soll dieses sein: " + node1, node1, testNodes[0]);
		assertEquals("zweite element soll dieses sein: " + node2, node2, testNodes[1]);
		assertEquals("dritte element soll dieses sein: " + node3, node3, testNodes[2]);		
	}
	public final void testGetChildrenArray6() {
		// ein knoten, ein kind (ROUTE) mit einem kind (attribut weil ist ja ein blatt)
		nodeToAnalyze = new DefaultMutableTreeNode("root");		
		subNode = new DefaultMutableTreeNode("ROUTE");		
		subNode.add(new DefaultMutableTreeNode("child is leave = attribute"));
		nodeToAnalyze.add(subNode);
		
		testNodes = SelectedNodeFactory.getChildrenArray(nodeToAnalyze);
		assertTrue("array sollte kein element enthalten, da routes nicht angezeigt werden sollen", testNodes.length == 0 );		

	}

}

