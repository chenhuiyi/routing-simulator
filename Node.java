import java.io.*;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 


/**
 * 
 * @author huiyi chen (huiyich@bu.edu)
 * CS455 PA2
 * Monday, November 21st, 2016
 *
 */
public class Node { 
    
    public static final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between node 0 and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/
    int[] mincost; 			/*Minimum cost to all the nodes */
    int[] rtable;          /* Routing table to store the next hop */
    
    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) { 
    	//print the message
    	System.out.printf("\nFunction rtinit() has been called at t=%f to initialize node %d. \n", NetworkSimulator.clocktime, nodename);
    	
    	//initial node name
    	this.nodename = nodename;
    	
    	//create new array lists
    	this.lkcost = new int[initial_lkcost.length];
    	this.costs = new int[initial_lkcost.length][initial_lkcost.length];
    	this.mincost = new int[initial_lkcost.length];
    	this.rtable = new int[initial_lkcost.length];
    	
    	//assign values to distance table 
    	for(int i = 0; i < initial_lkcost.length; i++) {
    		
    		this.lkcost[i] = initial_lkcost[i];
    		this.mincost[i] = initial_lkcost[i];
    		//initialize all values in routing table to -1
    		this.rtable[i] = -1;
    		
    		//initialize all the values with INIFINITY first
    		for(int j = 0; j < initial_lkcost.length;j++){
    			this.costs[i][j] = INFINITY;
    		}
    		
    		
			if (initial_lkcost[i] != INFINITY && i != nodename){
				
				//change some specific value in cost table
				this.costs[i][i] = initial_lkcost[i];
				
				//change the next hop in routing table
				this.rtable[i] = i;
				
				//send packet to layer2 if node i is a neighborhood  of this node with poison reverse
				initial_lkcost[i] = INFINITY;				//Temporary create a fake mincost array
				NetworkSimulator.tolayer2(new Packet(this.nodename, i, initial_lkcost));
				initial_lkcost[i] = this.mincost[i];
				
				//print the message sent to neighborhood nodes
				System.out.printf("\nA packet has been sent from node %d to node %d, contents = %d %d %d %d", nodename, i, initial_lkcost[0],initial_lkcost[1],initial_lkcost[2],initial_lkcost[3]);
				
			}
			
			
    	}
    	
    	//change the cost in distance table where from this node to this node to 0
    	this.costs[nodename][nodename] = 0;
    	
    	this.rtable[nodename] = nodename;
    	
    	//print the distance table
    	System.out.println();
    	System.out.printf("\nDistance table of node %d is: \n", nodename);
    	this.printdt();
		
    	
    }    
    

	void rtupdate(Packet rcvdpkt) { 

		
		boolean update = false;      //hold a record whether min cost if update or not
		boolean dtchange = false;		//hold a record whether distance table has been changed
		
		//update the ith column of cost table where i is the sourceid of this pkt.
		for(int i = 0; i < this.lkcost.length; i++){
			if (i != this.nodename){
				int newCost = this.costs[rcvdpkt.sourceid][rcvdpkt.sourceid]+rcvdpkt.mincost[i];
				int oldCost = this.costs[i][rcvdpkt.sourceid];
				if (newCost > INFINITY){
					newCost = INFINITY;
				} 
				if (newCost != oldCost) {
					this.costs[i][rcvdpkt.sourceid] = newCost;
					dtchange = true;
					
					//if min cost changed, update mincost array
					
					if (this.minCostTo(i) != this.mincost[i]) {
						update = true;			
						this.mincost[i] = this.minCostTo(i);
					}
				}
				
			}
		}
		
		//print whether or not the distance table is updated
		if (dtchange){
			System.out.println("Distance table has been updated.");
		} else {
			System.out.println("Distance table has not been updated.");
		}
		
		System.out.printf("\nDistance table of node %d is: \n", this.nodename);
		this.printdt();
		
		//if minimum cost array has been updated, send new packet to neighborhood nodes
		if (update){
			for(int node = 0; node < this.lkcost.length; node ++){
				if (node != this.nodename && this.lkcost[node] != INFINITY){
					
					//set a fake min cost array with poison reverse
					int[] fakemincost = new int[this.lkcost.length];
					
					for (int i = 0; i < this.lkcost.length; i++){
						if (this.rtable[i] != node){
							fakemincost[i] = this.mincost[i];
						} else {
							fakemincost[i] = INFINITY;
						}
					}
					
					NetworkSimulator.tolayer2(new Packet(this.nodename, node, fakemincost));
					//print the message sent to neighborhood nodes
					System.out.printf("\nA packet has been sent from node %d to node %d, contents = %d %d %d %d", this.nodename, node, fakemincost[0],fakemincost[1],fakemincost[2],fakemincost[3]);
		
					
				}
			}
		}
		System.out.println();
		
	}
	
	
	
	/* helper function to find the minimum cost from this node to i */
	int minCostTo(int row){
		
		int minCost = INFINITY;
		
		for (int column = 0; column < this.lkcost.length; column++){
			if (this.costs[row][column] <= minCost){
				minCost = this.costs[row][column];
				this.rtable[row] = column;
			}
		}
		
		return minCost;
	}
    
	

    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) { 
    	
    	
    	//print the message
    	System.out.printf("\nFunction linkhandler() has been called at t=%f for node %d since link cost from node %d to %d has been changed to %d.\n", 
    			NetworkSimulator.clocktime, this.nodename, this.nodename, linkid, newcost);
   
    	
    	//change the link cost of this node
    	this.lkcost[linkid] = newcost;
    	
    	//hold a record whether mincost has been updated
    	boolean update = false;
    	
    	//change the cost table related to this link
    	for (int row = 0; row < this.lkcost.length; row++){
    		if (row == linkid) {
    			this.costs[row][linkid] = newcost;
    		} else {
    			this.costs[row][linkid] = INFINITY;
    		}
    		
    		//change the mincost if mincost is changed for this row
    		int oldmincost = this.mincost[row];
    		this.mincost[row] = this.minCostTo(row);
    		if ( oldmincost != this.mincost[row]){
    			update = true;
    		}
    	}
    	
    	//print message for distance table
    	System.out.println("\nDistance table has been updated.\n");
    	System.out.printf("\nDistance table of node %d is: \n", this.nodename);
		this.printdt();
    	
    	
    	//if min cost is updated, send packets to its neighborhood
    	if (update) {
    		for(int node = 0; node < this.lkcost.length; node ++){
				if (node != this.nodename && this.lkcost[node] != INFINITY){
					
					//set a fake min cost array with poison reverse
					int[] fakemincost = new int[this.lkcost.length];
					
					for (int i = 0; i < this.lkcost.length; i++){
						if (this.rtable[i] != node){
							fakemincost[i] = this.mincost[i];
						} else {
							fakemincost[i] = INFINITY;
						}
					}
					
					NetworkSimulator.tolayer2(new Packet(this.nodename, node, fakemincost));
					//print the message sent to neighborhood nodes
					System.out.printf("\nA packet has been sent from node %d to node %d, contents = %d %d %d %d", this.nodename, node, fakemincost[0],fakemincost[1],fakemincost[2],fakemincost[3]);
	
				}
			}
    	}
    	
    }    


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }
    
}
