/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -----------------
 * BarChart.java
 * -----------------
 * 
 */

package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

/**
 * Diese Klasse liefert ein Einfaches Balkendiagramm, definiert durch
 * ein CategoryDataset, einen Titel und der X- und Y-Achsenbezeichnung.
 *
 */
public class BarChart extends JFrame {
	
	private final DefaultCategoryDataset dataset;
	private final String title;
	private final String label_x_axis;
	private final String label_y_axis;

	/**
     * Diagramm erstellen und anzeigen
     *
     * @param dataset Datensätze für das Diagramm
     * @param title Überschrift Diagramm und Frame
     * @param label_x_axis Beschriftung x Achse
     * @param label_y_axis Beschriftung y Achse
     * 
     */
    public BarChart(final DefaultCategoryDataset dataset, final String title, final String label_x_axis, final String label_y_axis) {

        super(title);
        super.setIconImage(new ImageIcon("./icons/ViewerMiniIcon.gif").getImage());
        
        this.dataset = dataset;
        this.title = title;
        this.label_x_axis = label_x_axis;
        this.label_y_axis = label_y_axis;
        
        final JFreeChart chart = createChart();
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
        
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
    
    /**
     * Zusammenbauen des Diagrammes.
     * 
     * @return diagramm
     */
    private JFreeChart createChart() {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            "",         				// überschrift
            label_x_axis,               // x label
            label_y_axis,               // y label
            dataset,                    // datensätze
            PlotOrientation.VERTICAL,   // vertikale balken
            true,                       // mit legende
            true,                       // mit tooltips
            false                       // URLs???
        );

        // Layoutanpassungen im Folgende:

        // allg. Hintergrundfarbe
        chart.setBackgroundPaint(Color.white);                        

        // Referenz auf Zeichnung:
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setNoDataMessage("NO DATA!");
        
        // aussehen des eigentlichen diagrammes:
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);        
        
        // skaleneinteilung:
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperMargin(0.1);

        // kategorien renderer einstellungen verändern:                
        final CategoryItemRenderer categories = plot.getRenderer();
        // casten in bar renderer:
        final BarRenderer bars = (BarRenderer) categories;        
                
        // werte im diagramm anzeigen:
        categories.setLabelGenerator(new StandardCategoryLabelGenerator());                
        categories.setItemLabelsVisible(true);
        final ItemLabelPosition p = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER, TextAnchor.CENTER, - Math.PI / 6 
            );
        
        categories.setPositiveItemLabelPosition(p);
        categories.setNegativeItemLabelPosition(p);        
        
        plot.setRenderer(categories);
        
        // farbverlauf der serien 1 und 2
        final GradientPaint gp0 = new GradientPaint(
            0.0f, 0.0f, new Color(127,127,255), 
            0.0f, 0.0f, new Color(127,127,127)
        );
        final GradientPaint gp1 = new GradientPaint(
            0.0f, 0.0f, new Color(255,127,127), 
            0.0f, 0.0f, new Color(127,127,127)
        );
        bars.setSeriesPaint(0, gp0);
        bars.setSeriesPaint(1, gp1);
        // keine umrandung der balken
        bars.setDrawBarOutline(false);
        
        // abstand der zahlen vom balken
        bars.setItemLabelAnchorOffset(13);
        
        // x achsenbeschriftung
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );        
        
        return chart;
        
    }
}
