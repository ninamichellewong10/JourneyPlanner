import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name, city;
	public final int oneway;
	public final Collection<Segment> components;
	public double length = 0;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.oneway = oneway;
		this.components = new HashSet<Segment>();
	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}
	
	public void setLength(double len) {
		this.length = this.length + len;
	}
	
	public String getName() {
		return name;
	}
	
	public int getOneWay(){
		return oneway;
	}
	
	public double getLength(){
		return length;
	}
}

// code for COMP261 assignments