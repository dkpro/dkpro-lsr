package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;
import de.tudarmstadt.ukp.dkpro.lexsemresource.LexicalSemanticResource;
import de.tudarmstadt.ukp.dkpro.lexsemresource.exception.LexicalSemanticResourceException;
import de.tudarmstadt.ukp.dkpro.lexsemresource.graph.EntityGraphManager.EntityGraphType;

public class GraphVisualizerPrefuse {

    private static final String COL_NAME = "name";
    private static final String COL_TYPE = "id";

//    public static void main(String[] argv) throws WikiApiException, LexicalSemanticResourceException, UnsupportedOperationException {
//        LexicalSemanticResource wikiResource = new WikipediaArticleResource("homer.tk.informatik.tu-darmstadt.de","wikiapi_test","student","student",de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language._test);
//        GraphVisualizerPrefuse.visualizeLSR(wikiResource);
//    }

    public static void visualizeLSR(LexicalSemanticResource resource) throws LexicalSemanticResourceException {
        EntityGraph entityGraph = EntityGraphManager.getEntityGraph(resource, EntityGraphType.JGraphT);
        visualizeLSR(entityGraph);
    }

    public static void visualizeLSR(EntityGraph entityGraph) throws LexicalSemanticResourceException {

//      LexicalSemanticResource resource = new WiktionaryResource(Language.GERMAN, "../LexSemRes/resource/Wiktionary/db_word_new", "DE_word");
//      RandomWalkJGraphT randomWalk = new RandomWalkJGraphT(resource, .005);
//
//        EntityGraph entityGraph = new EntityGraphJGraphT(resource, randomWalk.entityGraph);

        // -- 1. create the graph (true => directed, false => undirected)
        Graph graph = new Graph(true);
//        Graph graph = new Graph();
        graph.addColumn(COL_NAME, String.class);
        graph.addColumn(COL_TYPE, String.class);

        Map<Entity,Node> nodeMap = new HashMap<Entity,Node>();

        // add nodes
        for (Entity sourceNode : entityGraph.getNodes()) {

            Node categoryNode = graph.addNode();
            categoryNode.setString(COL_NAME, sourceNode.toString());

            nodeMap.put(sourceNode, categoryNode);

        }

        // add edges
        for (EntityGraphEdge edge : entityGraph.getEdges()) {
            Node node1 = nodeMap.get(edge.getSource());
            Node node2 = nodeMap.get(edge.getTarget());
            graph.addEdge(node1, node2);
        }


// -- 2. the visualization --------------------------------------------

        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization vis = new Visualization();
        vis.add("graph", graph);
        vis.setInteractive("graph.edges", null, false);

        // -- 3. the renderers and renderer factory ---------------------------

        // draw the "name" label for NodeItems
        LabelRenderer r = new LabelRenderer(COL_NAME);
        r.setRoundedCorner(8, 8); // round the corners

        // draw arrows
        EdgeRenderer e = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);

        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default
        vis.setRendererFactory(new DefaultRendererFactory(r,e));
           //vis.setRendererFactory(new DefaultRendererFactory(r));


        // -- 4. the processing actions ---------------------------------------

        // create our nominal color palette
        // pink for females, baby blue for males
        int[] palette = new int[] {
            ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255), ColorLib.rgb(0,180,180)
        };
        // map nominal data values to colors using our provided palette
        DataColorAction fill = new DataColorAction("graph.nodes", COL_TYPE,
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
        // use black for node text
        ColorAction text = new ColorAction("graph.nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        // use light grey for edges
        ColorAction edges = new ColorAction("graph.edges",
                VisualItem.STROKECOLOR, ColorLib.gray(200));
        // use light grey for arrows
        ColorAction arrows = new ColorAction("graph.edges",
                VisualItem.FILLCOLOR, ColorLib.gray(200));

        // create an action list containing all color assignments
        ActionList color = new ActionList();
        color.add(fill);
        color.add(text);
        color.add(edges);
        color.add(arrows);

        // create an action list with an animated layout
//        ActionList layout = new ActionList(Activity.INFINITY);
        ActionList layout = new ActionList(1000);

        // pick one of different layout instances:

        //layout.add(new BalloonTreeLayout("graph"));
        //layout.add(new ForceDirectedLayout("graph"));
        //layout.add(new RadialTreeLayout("graph"));
        //layout.add(new SquarifiedTreeMapLayout("graph"));
        //layout.add(new FruchtermanReingoldLayout("graph"));

        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout("graph");

        // set tree orientation to top-bottom:
        //treeLayout.setOrientation(Constants.ORIENT_TOP_BOTTOM);

        // set the spacing between neighbor nodes:
        //treeLayout.setBreadthSpacing(0.1);

        // add actions to the action list:
        layout.add(treeLayout);
        layout.add(new RepaintAction());

        // add the actions to the visualization
        vis.putAction("color", color);
        vis.putAction("layout", layout);


        // -- 5. the display and interactive controls -------------------------

        Display d = new Display(vis);
        d.setSize(720, 500); // set display size
        // drag individual items around
        d.addControlListener(new DragControl());
        // pan with left-click drag on background
        d.addControlListener(new PanControl());
        // zoom with right-click drag
        d.addControlListener(new ZoomControl());

        // -- 6. launch the visualization -------------------------------------

        // create a new window to hold the visualization
        JFrame frame = new JFrame("prefuse example");
        // ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        frame.pack();           // layout components in window
        frame.setVisible(true); // show the window

        // assign the colors
        vis.run("color");
        // start up the animated layout
        vis.run("layout");
    }
}