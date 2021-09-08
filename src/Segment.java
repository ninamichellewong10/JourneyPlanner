import java.awt.Graphics;
import java.awt.Point;

/**
 * A Segment is the most interesting class making up our graph, and represents
 * an edge between two Nodes. It knows the Road it belongs to as well as the
 * Nodes it joins, and contains a series of Locations that make up the length of
 * the Segment and can be used to render it.
 * 
 * @author tony
 */
public class Segment {

	public final Road road;
	public final Node start, end;
	public final double length;
	public final Location[] points;
	public final String roadName;

	public Segment(Graph graph, int roadID, double length, int node1ID,
			int node2ID, double[] coords) {

		this.road = graph.roads.get(roadID);
		this.start = graph.nodes.get(node1ID);
		this.end = graph.nodes.get(node2ID);
		this.length = length;
		this.roadName = road.getName();

		points = new Location[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			points[i] = Location
					.newFromLatLon(coords[2 * i], coords[2 * i + 1]);
		}

		this.road.addSegment(this);
		this.start.addSegment(this);
		this.end.addSegment(this);
	}
	
	public Node getStartNode(){
		return this.start;
	}
	
	public Node getEndNode(){
		return this.end;
	}
	
	public double getLength(){
		return this.length;
	}
	
	public Road getRoad(){
		return this.road;
	}
	
    public Node getNeighbour(Node node){
        for (Node n : node.getNeighbours()) {
            for (Segment segment : n.getSegments()) {
                if(segment == this)
                    return n;
            }
        }
        return null;
    }

	public void draw(Graphics g, Location origin, double scale) {
		for (int i = 1; i < points.length; i++) {
			Point p = points[i - 1].asPoint(origin, scale);
			Point q = points[i].asPoint(origin, scale);
			g.drawLine(p.x, p.y, q.x, q.y);
		}
	}
}

// code for COMP261 assignments