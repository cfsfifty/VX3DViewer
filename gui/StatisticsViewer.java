package gui;

import javax.swing.*;

import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;

import scenejtree.JTreeComposer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

/**
 * Die Klasse erzeugt ein Statistikfenster zur geladenen Datei, die unter anderem
 * Informationen über die Anzahl einiger best. Knoten (Routes, Attribute, USEs, etc.)
 * enthält. Des weiteren wird die Speicherbelegung angezeigt. Schließlich gibt es
 * einen Button, mit dem ein Balkendiagramm über die Verteilung von Knoten und
 * Attributen je Ebene im Szenengraph angezeigt wird. 
 */

public class StatisticsViewer {
	
	private JFrame stats;
	private JPanel statsPanel;
	private JLabel fileName, nodeCount, attributeCount, routeCount, defCount, useCount, memoryUsage;
	private JButton elementsPerLevel;
	private String strUses = "# Uses: 0";
    private String strNodes = "# Nodes: 0";    
    private String strRoutes = "# Routes: 0";
    private String strDefs = "# Defs: 0";
    private String strAttributes = "# Attributes: 0";
    private String name = "Filename:";
    
    /**
     * Array aus JTreeComposer. Der erste Index beschreibt die Ebene,
     * der zweite für 0 = Knoten, für 1 = Attribute. Der Wert entspricht
     * dann der Anzahl der Elemente. 
     */
    private int[][] arrElementsPerLevel; 
    private int maxChildrenPerNode;
   
	/**
	 * Speichern der Parameter und anlegen eines JFrames mit allen gewünschten
	 * Angaben.
	 * 
	 * @param composer Enthält den JTree
	 * @param y_center mitte der hoehe
	 * @param x_center mitte der breite
	 */
	public StatisticsViewer(JTreeComposer composer, int x_center, int y_center) {		
		this.strAttributes = composer.getStrAttributes();
		this.strDefs = composer.getStrDefs();
		this.strNodes = composer.getStrNodes();
		this.strRoutes = composer.getStrRoutes();
		this.strUses = composer.getStrUses();
		this.name = composer.getRoot().toString();
		this.arrElementsPerLevel = composer.getArrElementsPerLevel();
		this.maxChildrenPerNode = composer.getMaxChildrenPerNode();
		init();
				
		stats.setLocation(x_center - stats.getWidth()/2, y_center - stats.getHeight()/2);
		stats.setVisible(true);		
	}
	/**
	 * Die Methode erzeugt das Fenster des StatisticViewers
	 */
	private void init() {
		stats = new JFrame("Statistics");
		stats.setIconImage(new ImageIcon("./icons/ViewerMiniIcon.gif").getImage());
		stats.setSize(300,300);
		stats.setResizable(false);		
		Container content = stats.getContentPane();
		content.add(createStatsPanel());
		stats.pack();		
	}
	/**
	 * Die Methode erstellt das Panel der Statistikanzeige
	 * 
	 * @return statsPanel
	 */
	private JPanel createStatsPanel() {
		Font fontUsedForValues = new Font("Arial",Font.PLAIN,12);
		
		statsPanel = new JPanel();
		statsPanel.setLayout(new GridLayout(11, 1));
		
        // JLabel: Dateiname
		fileName = new JLabel("Filename: " +name, SwingConstants.CENTER);
		fileName.setFont(new Font("Arial",Font.CENTER_BASELINE,12));
		// JLabel: Anzahl der Nodes 
		nodeCount = new JLabel(strNodes, new ImageIcon("./scenejtree/ICONS/Node.gif"), SwingConstants.CENTER);
		nodeCount.setFont(fontUsedForValues);
		// JLabel: Anzahl der Attributes 
		attributeCount = new JLabel(strAttributes, new ImageIcon("./scenejtree/ICONS/ANode.gif"), SwingConstants.CENTER);
		attributeCount.setFont(fontUsedForValues);		
		// JLabel: Anzahl der ROUTES 
		routeCount = new JLabel(strRoutes, new ImageIcon("./scenejtree/x3dEditIcons/ROUTE.gif"), SwingConstants.CENTER);
		routeCount.setFont(fontUsedForValues);
		// JLabel: Anzahl der DEFs
		defCount = new JLabel(strDefs, new ImageIcon("./scenejtree/x3dEditIcons/DEF.gif"), SwingConstants.CENTER);
		defCount.setFont(fontUsedForValues);	
		// JLabel: Anzahl der USEs
		useCount = new JLabel(strUses, new ImageIcon("./scenejtree/x3dEditIcons/USE.gif"), SwingConstants.CENTER);
		useCount.setFont(fontUsedForValues);	
		// Lege eine Action zum Anzeigen des Balkendiagrammes an.
		AbstractAction a = new AbstractAction("Show # of elements per level",  new ImageIcon("./icons/elements.gif")) {
			private JPanel arrToPanel() {				
					Font fontUsedForValues = new Font("Arial",Font.PLAIN,12);
					int[][] array = (int[][])this.getValue("array");
					
					JPanel arrPanel = new JPanel();
					arrPanel.setLayout(new GridLayout(array.length,array[0].length));

					JLabel tmpLabel;
					
					
					for (int i = 0; i < array.length; i++) {										
						arrPanel.add(new JLabel("Tree depth: " + i));
						arrPanel.add(new JLabel("# of Nodes: " + array[i][0]));
						arrPanel.add(new JLabel("# of Attributes: " + array[i][1]));										
					}
					
					return arrPanel;
			}
		    /**
		     * baut den diagramm datensatz.
		     * 
		     * @return gefüllter datensatz.
		     */
		    private CategoryDataset createDataset() {
		        
		        // labels für datenreihen
		        final String series1 = "Scene nodes";
		        final String series2 = "Attributes";
		        
		        // datensatz bauen
		        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

				int[][] array = (int[][])this.getValue("array");
											
				
				for (int i = 1; i < array.length - 1; i++) {
					// nicht ab 0, um root auszublenden, level 1 ist der scene node
					dataset.addValue(array[i][0], series1, "Level " + i);
					// attribute aus i+1 auslesen, damit sie quasi auf der gleichen
					// ebene wie deren nodes liegen
					// deswegen auch die letzte ebene (lenght - 1) weglassen, sie 
					// bestünde nur aus attributen, die ja nun eine ebene höher liegen
					
					dataset.addValue(array[i+1][1], series2, "Level " + i);
				}		      
		        return dataset;
		        
		    }

			
			public void actionPerformed(ActionEvent action) {
				BarChart chart = new BarChart((DefaultCategoryDataset) createDataset(),"Number of nodes + attributes per level","Tree depth", "# of elements");				
			}
			private String getMaxElements() {				
				return "Highest count of children under one node  : " + this.getValue("maxchildren");
			}			
		};
		a.putValue("array",arrElementsPerLevel);
		a.putValue("maxchildren",new Integer(maxChildrenPerNode));
		
		// zur action den Button erzeugen
		elementsPerLevel = new JButton(a);				
		elementsPerLevel.setHorizontalAlignment(SwingConstants.CENTER);
		elementsPerLevel.setFont(fontUsedForValues);		
		
		// JLabel: Speichernutzung 
		memoryUsage = new JLabel(getMemoryUsage(), SwingConstants.CENTER);
		memoryUsage.setFont(fontUsedForValues);		
		
		// Labels dem Panel hinzufügen:
		statsPanel.add(fileName);
		statsPanel.add(new JSeparator());
		statsPanel.add(nodeCount);
		statsPanel.add(attributeCount);
		statsPanel.add(routeCount);
		statsPanel.add(defCount);
		statsPanel.add(useCount);
		statsPanel.add(new JSeparator());
		statsPanel.add(elementsPerLevel);
		statsPanel.add(new JSeparator());
		statsPanel.add(memoryUsage);
		
		return statsPanel;	
	}

	/**
	 * Methode gibt den Stand der Speichernutzung als String wieder.
	 * 
	 * @return String, der die Speichernutzung beschreibt
	 */
	private String getMemoryUsage() {
		Runtime r = Runtime.getRuntime();
		long maxBytes = r.maxMemory(); 
		long currentlyAllocatedBytes = r.totalMemory();
		long currentlyFreeBytes = r.freeMemory();
		long usedBytes = currentlyAllocatedBytes - currentlyFreeBytes;
		long freeBytes = maxBytes-usedBytes;
		
		return "Using " + toMegaBytes(usedBytes) + " of available " + toMegaBytes(maxBytes) + ". " + toMegaBytes(freeBytes) + " free.";
	}

	/**
	 * Diese Methode gib für eine übergegebene Anzahl Bytes
	 * einen formatierten String der Art "64.0 MB" zurück.
	 * 
	 * @param usedBytes anzahl der Bytes
	 * @return formartierten String, der in MegaBytes konvertierten Bytes
	 */
	private String toMegaBytes(long usedBytes) {
		double dblMegaBytes = usedBytes / Math.pow(1024,2);
		DecimalFormat format = new DecimalFormat("0.0 MB"); 		
		String strMegaBytes = format.format(dblMegaBytes);
		return strMegaBytes;
	}	
}
