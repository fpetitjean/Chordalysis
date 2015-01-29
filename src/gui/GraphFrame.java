/*******************************************************************************
 * Copyright (C) 2014 Francois Petitjean
 * 
 * This file is part of Chordalysis.
 * 
 * Chordalysis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * Chordalysis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Chordalysis.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;

/**
 * Creates a GUI for the visualisation of the graph
 */
public class GraphFrame extends JFrame implements mxIEventListener {
	private static final long serialVersionUID = 5394854042002348661L;
	MyGraphX xGraph;
	mxFastOrganicLayout layout;
	mxGraphComponent graphComponent;
	mxGraphOutline graphOutline;
	int delay;


	/**
	 * Constructor
	 * @param xGraph the graph to be displayed
	 * @param delay the delay between actions
	 */
	public GraphFrame(MyGraphX xGraph, int delay) {
		setSize(1000, 1000);
		setLocation(100, 100);
		this.xGraph = xGraph;
		xGraph.addListener(mxEvent.ADD_CELLS, this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		// xGraph.getView().setTranslate(new
		// mxPoint(-xGraph.getGraphBounds().getX(),-xGraph.getGraphBounds().getY()));

		graphComponent = new mxGraphComponent(xGraph);
		graphComponent.setPreferredSize(new Dimension(600, 600));
		add(graphComponent);
		graphOutline = new mxGraphOutline(graphComponent);
		graphOutline.setPreferredSize(new Dimension(100, 100));
		add(graphOutline);

		mxGraphLayout tmpLayout = new mxCircleLayout(xGraph);
		xGraph.getModel().beginUpdate();
		try {
			tmpLayout.execute(xGraph.getDefaultParent());
		} finally {
			xGraph.getModel().endUpdate();
		}

		layout = new mxFastOrganicLayout(xGraph);
		this.delay = delay;
	}

	@Override
	public void invoke(Object sender, mxEventObject evt) {
		xGraph.getModel().beginUpdate();
		try {
			layout.execute(xGraph.getDefaultParent());
//			mxRectangle bounds = graphComponent.getGraph().getGraphBounds();
//			graphComponent.getGraph().getView().setTranslate(new mxPoint(-bounds.getX(), -bounds.getY()));
			double newScale = 1;

			Dimension viewPortSize = graphComponent.getViewport().getSize();

			double gw = xGraph.getGraphBounds().getWidth();
			double gh = xGraph.getGraphBounds().getHeight();

			if (gw > 0 && gh > 0) {
				double w =  viewPortSize.getWidth();
				double h =  viewPortSize.getHeight();

				newScale = Math.min( w / gw,  h / gh);
			}

			graphComponent.zoom(newScale);
		} finally {
			mxMorphing morph = new mxMorphing(graphComponent);
			morph.addListener(mxEvent.DONE, new mxIEventListener() {
				@Override
				public void invoke(Object arg0, mxEventObject arg1) {
					xGraph.getModel().endUpdate();
				}
			});
			morph.startAnimation();
		}
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
