import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author tony
 */
public class Node{

	public final int nodeID;
	public final Location location;
	public final HashSet<Segment> segments;
	
	private ArrayList<Segment> inSegments;
	private ArrayList<Segment> outSegments;
	
	private HashSet<Road> adjacentRoads;
	private ArrayList <Node> neighbours;
	
	public boolean visited = false;
	public Node currentNode = this;
	public Node formerNode = null;

	public double estimatedCost = 0; 
	public double currentCost = 0; 

	public int depth = Integer.MAX_VALUE;
    private int count = Integer.MAX_VALUE;
    private int reachBack;

	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<>();
		this.inSegments = new ArrayList<Segment>();
		this.outSegments = new ArrayList<Segment>();
		this.adjacentRoads = new HashSet<Road>();
		this.neighbours = new ArrayList<>();
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}
	
	public HashSet<Segment> getSegments() {
		return this.segments;
	}
	
	public ArrayList<Segment> getInSegments() {
		return this.inSegments;
	}
	
	public ArrayList<Segment> getOutSegments() {
		return this.outSegments;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public int getID() {
		return nodeID;
	}
	
	public HashSet<Road> getAdjacentRoads(){
		return this.adjacentRoads;
	}
	
	public int getCount() {
		return count;
	}

	public int getReachBack() {
		return reachBack;
	}

	
	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
	
	public ArrayList<Node> getNeighbours() {
		for (Segment s : segments) {
			if (!neighbours.contains(s.start) && currentNode != s.start) {
				neighbours.add(s.start);
			}
			if (!neighbours.contains(s.end) && currentNode != s.end) {
				neighbours.add(s.end);
			}
		}
		return neighbours;
	}
	
	public void empty(){
		visited = false;
		estimatedCost = 0;
		currentCost = 0;
		formerNode = null;
		depth = Integer.MAX_VALUE;
	}
	
}

// code for COMP261 assignments