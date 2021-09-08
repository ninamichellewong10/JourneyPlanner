public class AStarNode implements Comparable<AStarNode>{
	public Node startNode;
	public Node previousNode;
	public double currentCost;
	public double cost;
	
	/*
	 * Class for a fringe element for aStar search.
	 * is made comparable for the use of comparing the cost of each node.
	 * 
	 */
	
	public AStarNode(Node start, Node from, double currentCost, double cost) {
		this.startNode = start;
		this.previousNode = from;
		this.currentCost = currentCost;
		this.cost = cost;
	}

	@Override
	public int compareTo(AStarNode o) {
		if (this.cost < o.cost) { return -1; } //comparative method, to compare the costs.
		else if (this.cost > o.cost) { return 1; }
		else { return 0; }
	}
}
