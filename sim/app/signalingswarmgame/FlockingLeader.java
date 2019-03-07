/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.util.*;

public class FlockingLeader extends Leader {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double currentRelativeSignalingUtilities;

//    public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm){
//        return swarm.agents.getObjectLocation(this);
//    }

	@Override
	public double getSignalingUtility(Agent agent,SignalingSwarmGame swarm) {
		double p = swarm.getAcceptLeadersSignalCorrectly();
		Double2D acceptedDir = agent.acceptedSignalDirection(swarm);
		Double2D misunderstoodDir = agent.misunderstoodSignalDirection(swarm);
		
		double acceptedOrientationDelta = agent.getDistanceBetweenPoints(acceptedDir, swarm.leaderAgent.getMovementDirection());
		double acceptedAttractionDelta = agent.getDistanceBetweenPoints(loc.add(acceptedDir), swarm.leaderAgent.loc);
		
		double acceptionDirectionDelta = 0.5 * (acceptedOrientationDelta + acceptedAttractionDelta);
		
		double misunderstoodOrientationDelta = agent.getDistanceBetweenPoints(misunderstoodDir, swarm.leaderAgent.getMovementDirection());
		double misunderstoodAttractionDelta = agent.getDistanceBetweenPoints(loc.add(misunderstoodDir), swarm.leaderAgent.loc);
		
		double misunderstoodDirectionDelta = 0.5 * (misunderstoodOrientationDelta + misunderstoodAttractionDelta);
		
		return (p * 1 / (acceptionDirectionDelta == 0? EPSILON: acceptionDirectionDelta)) + 
			   ((1 - p) * 1/(misunderstoodDirectionDelta == 0? EPSILON: misunderstoodDirectionDelta));
	}

	@Override
	public double getUnsignalingUtility(Agent agent, SignalingSwarmGame swarm) {
		Double2D noSignalDir = agent.noSignalDirection(swarm);
		
		double noSignalOrientationDelta = agent.getDistanceBetweenPoints(noSignalDir, swarm.leaderAgent.getMovementDirection());
		double noSignalAttractionDelta = agent.getDistanceBetweenPoints(loc.add(noSignalDir), swarm.leaderAgent.loc);
		
		double noSignalDirectionDelta = 0.5 * (noSignalOrientationDelta + noSignalAttractionDelta);
		
		
		return 1 / (noSignalDirectionDelta == 0? EPSILON : noSignalDirectionDelta);
	}    
}
