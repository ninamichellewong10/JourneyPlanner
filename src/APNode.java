import java.util.ArrayDeque;

public class APNode {
	
    /*
     * Class for the node needed for articulation point search.
     * 
     * @param 
     * Node node
     * Integer depth
     * APNode parent
     * 
     */
	
	public int depth = 0;
	public int reachBack = 0;
	public APNode parent = null;
	public Node node = null;
	public ArrayDeque<Node> children = new ArrayDeque<Node>();
		
	public APNode(Node node, int depth, APNode parent){
		this.node = node;
		this.depth = depth;
		this.parent = parent;
		this.children = new ArrayDeque<Node>();
	}	
}