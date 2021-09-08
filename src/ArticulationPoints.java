import java.util.*;

public class ArticulationPoints {
    public HashSet<Node> aps = new HashSet<>();
    private Collection<Node> nodesList;
    private Stack<APNode> fringe = new Stack<>();
    
    /*
     * Class to search for all of the articulation points in 
     * a graph.
     * 
     * @param 
     * collection nodes
     */

    public ArticulationPoints(Collection<Node> nodes) {
        this.nodesList = nodes;
    }
    
    public Collection<Node> getAPS(){
    	return aps;
    }  

    public void ap(Node start) {
    	start.depth = 0;
    	int subTrees = 0;
        for (Node neighbour : start.getNeighbours()) {
            if (neighbour.depth != Integer.MAX_VALUE) { 
            	continue;
            }
            iterate(neighbour, 1, new APNode(neighbour, 1, null)); //perform iterate method on all nodes.
            subTrees++;
        }
        if (subTrees > 1){
        	aps.add(start); //add the start node if subtrees is more than one.
        }
    }
    
    public void findAPs() {
        for (Node node : nodesList) {
        	ap(node); //can call method from mapper class.
        }
    }

    public void iterate(Node firstNode, int depth, APNode start) {       
		fringe.push(new APNode(firstNode, depth, start)); //initialise first node, add to the fringe.

		while(!fringe.isEmpty()){
			APNode current = fringe.peek();
			Node peekNode = current.node;
			current.node.visited = true; //important to set the visited node to true, so it is only visited once.
			if(peekNode.depth == Integer.MAX_VALUE){
				peekNode.depth = current.depth;
				current.reachBack = current.depth;
				for(Node n : peekNode.getNeighbours()){
					if(n != current.parent.node){
						current.children.add(n); //if the node is the parent, do not add to the children of n.
					}
				}
			}
			else if (!current.children.isEmpty()) {
				Node removed = current.children.poll(); //retrieve first node from the current nodes children.
				if (removed.depth < Integer.MAX_VALUE) {
					current.reachBack = Math.min(current.reachBack, removed.depth);
				}
				else {
					fringe.push(new APNode(removed, peekNode.depth + 1, current)); //add a new node to be search in the fringe.
				}	
			}			
			else{
				if(!peekNode.equals(firstNode)){	
					current.parent.reachBack = Math.min(current.reachBack, current.parent.reachBack);
					if(current.reachBack >= current.parent.depth){
						aps.add(current.parent.node); //add new articulation point found.
					}
				}
				fringe.pop(); //remove search node from fringe.
			}
		}
    }

}