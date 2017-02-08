package extra;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFrame;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;

import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import core.model.DecomposableModel;

public class PrintableModel {


  /**
   * Display the model in a Frame
   * 
   * @param variableNames
   *            the names of the variables
   */
  public static void display(DecomposableModel model, String[] variableNames) {
    JFrame f = new JFrame();
    f.setSize(500, 500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final mxGraph xGraph = new mxGraph();
    mxGraphComponent graphComponent = new mxGraphComponent(xGraph);
    f.getContentPane().add(BorderLayout.CENTER, graphComponent);
    f.setVisible(true);

    Object parent = xGraph.getDefaultParent();
    xGraph.getModel().beginUpdate();
    try {
      Object[] vertices = new Object[model.graph.vertexSet().size()];
      for (Integer i : model.graph.vertexSet()) {
        if (variableNames != null) {
          vertices[i] = xGraph.insertVertex(parent, null,
              variableNames[i], 0, 0, 100, 30);
        } else {
          vertices[i] = xGraph.insertVertex(parent, null, "" + i, 0,
              0, 100, 30);
        }
      }
      for (DefaultEdge edge : model.graph.edgeSet()) {
        xGraph.insertEdge(parent, null, "",
            vertices[model.graph.getEdgeSource(edge)],
            vertices[model.graph.getEdgeTarget(edge)],
            "startArrow=none;endArrow=none;");
      }

    } finally {
      xGraph.getModel().endUpdate();
    }

    // define layout
    mxOrganicLayout layout = new mxOrganicLayout(xGraph);
    layout.setOptimizeEdgeCrossing(true);
    layout.setOptimizeBorderLine(true);
    layout.setMaxIterations(10000);

    // layout using morphing
    xGraph.getModel().beginUpdate();
    try {
      layout.execute(xGraph.getDefaultParent());
    } finally {
      mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

      morph.addListener(mxEvent.DONE, new mxIEventListener() {

        @Override
        public void invoke(Object arg0, mxEventObject arg1) {
          xGraph.getModel().endUpdate();
          // fitViewport();
        }

      });

      morph.startAnimation();
    }

  }

  /**
   * Builds a graphical representation of the model (MRF)
   * 
   * @param variableNames
   *            the names of the variables
   * @return an image
   * @throws IOException
   */
  public static BufferedImage getImage(DecomposableModel model, String[] variableNames) throws IOException {
    mxGraph xGraph = new mxGraph();
    Object parent = xGraph.getDefaultParent();
    xGraph.getModel().beginUpdate();
    try {
      Object[] vertices = new Object[model.graph.vertexSet().size()];
      for (Integer i : model.graph.vertexSet()) {
        if (variableNames != null) {
          vertices[i] = xGraph.insertVertex(parent, null,
              variableNames[i], 0, 0, 100, 30);
        } else {
          vertices[i] = xGraph.insertVertex(parent, null, "" + i, 0,
              0, 100, 30);
        }
      }
      for (DefaultEdge edge : model.graph.edgeSet()) {
        xGraph.insertEdge(parent, null, "",
            vertices[model.graph.getEdgeSource(edge)],
            vertices[model.graph.getEdgeTarget(edge)],
            "startArrow=none;endArrow=none;");
      }

    } finally {
      xGraph.getModel().endUpdate();
    }

    // define layout
    mxOrganicLayout layout = new mxOrganicLayout(xGraph);
    layout.setFineTuning(true);
    layout.setOptimizeEdgeCrossing(true);
    layout.setOptimizeNodeDistribution(true);
    layout.setOptimizeEdgeLength(false);
    layout.setMaxIterations(1000);

    xGraph.getModel().beginUpdate();
    try {
      layout.execute(xGraph.getDefaultParent());
    } finally {
      xGraph.getModel().endUpdate();
    }
    return mxCellRenderer.createBufferedImage(xGraph, null, 1.0,
        Color.WHITE, false, null);
  }


  /**
   * Export a Netica representation of a BN equivalent to the MRF
   * 
   * @param file
   *            the file to save the representation to
   * @param variableNames
   *            the names of the variables
   */
  public static void exportBNNetica(File file, DecomposableModel model, String[] variableNames,String [][]outcomes) {

    mxGraph xGraph = new mxGraph();
    Object parent = xGraph.getDefaultParent();
    xGraph.getModel().beginUpdate();

    HashMap<Integer,mxCell> cells = new HashMap<Integer, mxCell>();
    try {
      for (Integer i : model.graph.vertexSet()) {
        if (variableNames != null) {
          cells.put(i, (mxCell) xGraph.insertVertex(parent, null,
                variableNames[i], 0, 0, 100, 30));
        } else {
          cells.put(i,(mxCell) xGraph.insertVertex(parent, null, "" + i, 0,
                0, 100, 30));
        }
      }
      for (DefaultEdge edge : model.graph.edgeSet()) {
        xGraph.insertEdge(parent, null, "",
            cells.get(model.graph.getEdgeSource(edge)),
            cells.get(model.graph.getEdgeTarget(edge)),
            "startArrow=none;endArrow=none;");
      }

    } finally {
      xGraph.getModel().endUpdate();
    }

    // define layout
    mxOrganicLayout layout = new mxOrganicLayout(xGraph);
    layout.setFineTuning(true);
    layout.setOptimizeEdgeCrossing(true);
    layout.setOptimizeNodeDistribution(true);
    layout.setOptimizeEdgeLength(false);
    layout.setMaxIterations(1000);

    xGraph.getModel().beginUpdate();
    try {
      layout.execute(xGraph.getDefaultParent());
    } finally {
      xGraph.getModel().endUpdate();
    }

    try {
      PrintWriter out = new PrintWriter(new FileOutputStream(file), true);
      out.println("// ~->[DNET-1]->~");
      out.println("bnet Unnamed_1 {");
      out.println("\tautoupdate = FALSE;");
      DirectedAcyclicGraph<Integer, DefaultEdge> bn = model.graph.getBayesianNetwork();
      for (Integer varIndex : bn.vertexSet()) {
        out.print("\tnode ");
        out.print("v" + varIndex);
        out.println("{");
        if (variableNames != null) {
          out.println("\t\ttitle = \"" + variableNames[varIndex] + "\";");
        }
        out.println("\t\tkind = NATURE;");
        out.println("\t\tdiscrete = TRUE;");
        out.print("\t\tstates = (");
        String outcome = "\""+outcomes[varIndex][0]+"\"";
        out.print(outcome);
        for (int i = 1; i < outcomes[varIndex].length; i++) {
          outcome = "\""+outcomes[varIndex][i]+"\"";
          out.print(","+outcome);
        }
        out.println(");");

        out.print("\t\tparents = (");
        String listOfParents = "";
        Set<DefaultEdge> edgesOfNode = bn.edgesOf(varIndex);
        for (DefaultEdge e : edgesOfNode) {
          Integer source = model.graph.getEdgeSource(e);
          if (!source.equals(varIndex)) {
            listOfParents += ",v" + source;
          }
        }
        if (listOfParents.length() > 0) {
          out.println(listOfParents.substring(1) + ");");
        } else {
          out.println(");");
        }
        out.println("\t};");
        out.println();

        //layout
        out.print("\tvisual ");
        out.print("v" + varIndex);
        out.println("{");
        double x = cells.get(varIndex).getGeometry().getX();
        double y = cells.get(varIndex).getGeometry().getY();
        double w = cells.get(varIndex).getGeometry().getWidth();
        double h = cells.get(varIndex).getGeometry().getHeight();
        out.println("\twindowpos = ("+w+","+h+","+x+","+y+")");
        out.println(");");


      }

      out.println("};");

      out.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (CycleFoundException e) {
      e.printStackTrace();
    }

  }





}
