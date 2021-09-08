import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;
	
	private ArrayList<Node> nodesList = new ArrayList<Node>();
	private Node startNode = null;
	private Node targetNode = null;
	private Collection<Segment> selectedSegments = new HashSet<Segment>();
	private Collection<Node> selectedNodes = new HashSet<Node>();
	private ArrayList<Road> avoidedOneWay = new ArrayList<Road>();
	private HashMap<String, Double> roadGetter = new HashMap<String, Double>();
	
	private ArticulationPoints ap;
		
	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		redraw();
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			Collection <Node> sn = new HashSet<Node>();
			sn.add(closest);
			graph.setHighlightNodes(sn);
			getTextOutputArea().setText(closest.toString());
		}
		
		if(startNode != null && targetNode != null){
			startNode = targetNode;
			targetNode = closest;
		}
		else if(targetNode == null && startNode != null){
			targetNode = closest;
		}
		else if(startNode == null){
			startNode = closest;
			selectedNodes = new ArrayList<Node>();
			selectedNodes.add(closest);
			redraw();
		}
		
	}

	@Override
	protected void onSearch() {
		redraw();
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlightRoads(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
		for (Segment s : graph.segments) {
			Road r = s.getRoad();
			s.start.getOutSegments().add(s);
			s.end.getInSegments().add(s);
			s.start.getAdjacentRoads().add(r);
			s.start.getNeighbours().add(s.end);
			if(r.getOneWay() == 0){
				s.end.getOutSegments().add(s);
				s.start.getInSegments().add(s);
				s.end.getAdjacentRoads().add(r);
				s.end.getNeighbours().add(s.start);
			}
		}
		
		for (Node node : graph.nodes.values()) {
			nodesList.add(node);			
		}
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}

	@Override
	protected void onAStar() {	
		printAStar();
	}
	
	public Node searchAStar() {
		redraw();
		if ((startNode == null || targetNode == null) || 
			(startNode == null && targetNode == null)) {
			System.err.println("Empty search, try again.");
			return null;
		}
		
		PriorityQueue<AStarNode> fringe = new PriorityQueue<AStarNode>(); //initialise fringe.
		avoidedOneWay = new ArrayList<Road>();
		startNode.estimatedCost = startNode.estimatedCost = startNode.getLocation().distance(targetNode.getLocation()); //start estimated cost.	
		startNode.visited = true; 
		
		AStarNode startingNode = new AStarNode(startNode, targetNode, 0, startNode.estimatedCost);
		fringe.offer(startingNode);
		while (!fringe.isEmpty()) {
			AStarNode selected = fringe.poll(); //retrieve element from the fringe.
			if(!selected.startNode.visited){
				selected.startNode.visited = true;
				selected.startNode.formerNode = selected.previousNode;
				selected.startNode.currentCost = selected.currentCost;
			}
			if(selected.startNode == targetNode){
				break; //if reached target node, end loop. Path found.
			}
			for(Segment s : selected.startNode.getSegments()){
                Node neighbor = s.getNeighbour(selected.startNode);
                Road r = s.getRoad(); //if the segments road is one way, don't go down this road.
                if(r.oneway == 1) {
                    if(selected.startNode.getID() == s.getEndNode().getID() && 
                    neighbor.getID() == s.getStartNode().getID()){
                    	avoidedOneWay.add(s.getRoad()); //add road to avoided one way roads.
                        continue; 
                    }
                }
				Node neighbour = null;
				if(selected.startNode == s.getStartNode()){ neighbour = s.getEndNode(); } //retrieve neighbour to search.
				if(selected.startNode == s.getEndNode()){ neighbour = s.getStartNode(); }
				if(!neighbour.visited){
					double g = selected.currentCost + s.getLength(); //update the current cost.
					double h = g + neighbour.getLocation().distance(targetNode.getLocation()); //calculate heuristic.
					fringe.offer(new AStarNode(neighbour, selected.startNode, g, h)); //offer new node to search to fringe.
				}
			}
		}	
		return targetNode;
	}
	
	public void printAStar(){
		selectedSegments = new ArrayList<Segment>();
		selectedNodes = new ArrayList<Node>();
		Node goal = searchAStar();
		selectedNodes.add(goal);
		while(goal.formerNode != null){ //search from the target node through the former nodes to get all of the segments in the path.
				for(Segment s : graph.segments){
					if(s.getStartNode() == goal && s.getEndNode() == goal.formerNode || 
						s.getEndNode() == goal && s.getStartNode() == goal.formerNode) {
						selectedSegments.add(s); //add the segment to segments in the path.
						selectedNodes.add(goal.formerNode); //add the node to nodes in the path.
					}
				}
				goal = goal.formerNode;
			}
		
		graph.highlightedNodes.addAll(selectedNodes); 
		graph.highlightedSegments.addAll(selectedSegments); //to highlight.

		double distance = 0;
		String text = new String();			
		roadGetter = new HashMap<String, Double>();
		for(Segment s : selectedSegments) {
			distance = distance + s.getLength(); //accumulating final distance of path.
			if(roadGetter.containsKey(s.road.name)) {
				roadGetter.put(s.road.name, roadGetter.get(s.road.name)+s.length); //if the map contains the name of the road, add the segment length to the value.
			}
			else {
				roadGetter.put(s.road.name,s.length);
			}
		}
		
		for(String s : roadGetter.keySet()) {
			text = text + s + " : " + String.format("%.2f", roadGetter.get(s)) + "km"+"\n"; //formatting string for output.
		}

		getTextOutputArea().setText("The shortest distance between stop: " + startNode.getID() + " and stop: " + targetNode.getID() +
									" is " + String.format("%.2f", distance) + "km." + "\n" + "This path avoids " + avoidedOneWay.size() 
									+ " one way roads! "+ "\n" + "Roads through the path are:" + "\n"+ text);	
		
		for(Node n : nodesList){n.empty();}	
		this.startNode = null; //clear all lists and clear start and target nodes.
		this.targetNode = null;
		this.avoidedOneWay.clear();		
		redraw();
	}
	
	@Override
	protected void onAPoints() { 
        ap = new ArticulationPoints(graph.nodes.values()); //call the articulation point class.
        ap.findAPs();

        graph.highlightedNodes.addAll(ap.getAPS());
        getTextOutputArea().setText("There are " + ap.getAPS().size() + " articulation points in this map.");
        for (Node n : nodesList) { n.empty(); } //reset all nodes.
	}
}

// code for COMP261 assignments