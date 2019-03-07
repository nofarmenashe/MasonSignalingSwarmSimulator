/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;
import sim.engine.*;
import sim.util.*;

public abstract class BaseAgent implements Steppable, sim.portrayal.Oriented2D {
    private static final long serialVersionUID = 1;
    public static final double EPSILON = 0.000000001;

    public Double2D lastLoc = new Double2D(0, 0);
    public Double2D loc = new Double2D(0, 0);
	protected Double2D lastD = new Double2D(0, 0);
	protected double lastAlpha = 0;

    public double orientation2D() {
        return Math.atan2(loc.y - lastLoc.y, loc.x - lastLoc.x);
    }

    public BaseAgent(){
        super();
    }

    public abstract void step(SimState state);
    
    public Double2D getMovementDirection() {
    	return getDirectionBetweenPoints(lastLoc, loc);

    }
    
    public Double2D getDirectionToNeighbor(BaseAgent neighbor) {
    	Double2D directionToNeighbor = neighbor.loc.subtract(loc);
    	
    	return getNormalizedVector(directionToNeighbor); 
    }
    
    protected Double2D getDirectionBetweenPoints(Double2D fromLoc, Double2D toLoc){
      Double2D direction = new Double2D(toLoc.x - fromLoc.x, toLoc.y - fromLoc.y);
      double dis = Math.sqrt(Math.pow(direction.x, 2) + Math.pow(direction.y, 2));
      
      if (dis == 0) dis = EPSILON;
      
       return new Double2D(direction.x / dis, direction.y / dis);
   }

    public double getDistanceBetweenPoints(Double2D firstPoint, Double2D secondPoint) {
		return Math.sqrt(Math.pow(secondPoint.x - firstPoint.x, 2) + 
						 Math.pow(secondPoint.y - firstPoint.y, 2));

	}
    
    public double getVectorLength(Double2D vector) {
    	return getDistanceBetweenPoints(new Double2D(0,0), vector);
	}
    
    public Double2D getNormalizedVector(Double2D vector) {
    	double vectorLength = getDistanceBetweenPoints(new Double2D(0,0), vector);
    	
    	if(vectorLength == 0) vectorLength = EPSILON;
    	
    	return new Double2D(vector.x / vectorLength, vector.y / vectorLength);
	}
    	
//    public abstract Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm);

    public double calculateAngleBetweenDirections(Double2D direction1, Double2D direction2){
        double dotProduct = (direction1.x * direction2.x) + (direction1.y * direction2.y);

        double angle = Math.acos(dotProduct);
        return Double.isNaN(angle) ? 0 : angle;
    }
    
    public double calculateAngleBetweenAgentAndDirectionToOther(Double2D direction, BaseAgent otherAgent, SignalingSwarmGame swarm){
        Double2D otherLoc = swarm.agents.getObjectLocation(otherAgent);

        Double2D directionToOther = getDirectionBetweenPoints(loc, otherLoc);

        double angle = calculateAngleBetweenDirections(direction, directionToOther);
        
        return angle;
    }
//
//    public Double2D getDirectionWithAngleToOtherAgentLocation(Double2D original_direction, BaseAgent otherAgent, double angle, SignalingSwarmGame swarm){
//
//        Double2D other_loc = swarm.agents.getObjectLocation(otherAgent);
//        Double2D d_other = new Double2D(other_loc.x - loc.x, other_loc.y - loc.y);
//       
//        double dis = Math.sqrt(Math.pow(other_loc.x - loc.x, 2) + Math.pow(other_loc.y - loc.y, 2));
//        
//        double agent_dis = Math.sqrt(Math.pow(original_direction.x, 2) + Math.pow(original_direction.y, 2));
//
//        double y_ratio = d_other.y / original_direction.y;
//
//        double dx = (agent_dis * Math.cos(angle) * (dis - (y_ratio * agent_dis))) /
//                (d_other.x - (y_ratio * original_direction.x));
//
//        double new_x = loc.x + dx;
//
//        double dy = ((agent_dis * agent_dis * Math.cos(angle)) - (original_direction.x * (new_x - loc.x))) /
//                original_direction.y;
//
//        double new_y = loc.y + dy;
//
//        return new Double2D(new_x, new_y);
//    }
//
//    public Double2D getMovementDirection(SignalingSwarmGame swarm) {
//    	return getDirectionBetweenPoints(lastLoc, loc, swarm.jump);
//    }
//	
//    protected Double2D getDirectionBetweenPoints(Double2D fromLoc, Double2D toLoc,  double jump){
//       Double2D direction = new Double2D(toLoc.x - fromLoc.x, toLoc.y - fromLoc.y);
//       double dis = Math.sqrt(Math.pow(direction.x, 2) + Math.pow(direction.y, 2));
//
//       if (dis == 0) return new Double2D(jump, 0);
//       
//        direction = new Double2D(direction.x / dis * jump, direction.y / dis * jump);
//
//        return direction;
//    }
//	
//    public double getDistanceBetweenPoints(Double2D firstPoint, Double2D secondPoint) {
//		return Math.sqrt(Math.pow(secondPoint.x - firstPoint.x, 2) + 
//						 Math.pow(secondPoint.y - firstPoint.y, 2));
//
//	}
//    
//	public double getDistanceFromOther(BaseAgent other) {
//		return Math.sqrt(Math.pow(loc.x - other.loc.x, 2) + 
//						 Math.pow(loc.y - other.loc.y, 2));
//
//	}

}
