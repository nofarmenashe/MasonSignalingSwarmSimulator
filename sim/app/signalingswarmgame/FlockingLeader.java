/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.util.*;

public class FlockingLeader extends Leader {

	private static final long serialVersionUID = 1L;
	public double currentRelativeSignalingUtilities;

	@Override
	public double getSignalingUtility(Agent agent, SignalingSwarmGame swarm) {
		double p = swarm.getAcceptLeadersSignalCorrectly();
		Double2D acceptedDir = agent.acceptedSignalDirection(swarm);
		Double2D misunderstoodDir = agent.misunderstoodSignalDirection(swarm);
		
		double acceptedOrientationAngle = agent.calculateAngleBetweenDirections(acceptedDir, swarm.leaderAgent.getMovementDirection());
		double acceptedAttractionAngle = agent.calculateAngleBetweenDirections(acceptedDir, agent.getDirectionToNeighbor(this));
		
		double acceptionDirectionUtility = 0.5 * (Math.cos(acceptedOrientationAngle) + Math.cos(acceptedAttractionAngle));
		
		double misunderstoodOrientationAngle = agent.calculateAngleBetweenDirections(misunderstoodDir, swarm.leaderAgent.getMovementDirection());
		double misunderstoodAttractionAngle = agent.calculateAngleBetweenDirections(misunderstoodDir, agent.getDirectionToNeighbor(this));
		
		double misunderstoodDirectionUtility = 0.5 * (Math.cos(misunderstoodOrientationAngle) + Math.cos(misunderstoodAttractionAngle));
		
		return (p * acceptionDirectionUtility) + 
			   ((1 - p) * misunderstoodDirectionUtility);
	}

	@Override
	public double getUnsignalingUtility(Agent agent, SignalingSwarmGame swarm) {
		Double2D noSignalDir = agent.noSignalDirection(swarm);
		
		double noSignalOrientationAngle = agent.calculateAngleBetweenDirections(noSignalDir, swarm.leaderAgent.getMovementDirection());
		double noSignalAttractionAngle = agent.calculateAngleBetweenDirections(noSignalDir, agent.getDirectionToNeighbor(this));
		
		double noSignalDirectionUtility = 0.5 * (Math.cos(noSignalOrientationAngle) + Math.cos(noSignalAttractionAngle));
		
		return noSignalDirectionUtility;
	}    
}
