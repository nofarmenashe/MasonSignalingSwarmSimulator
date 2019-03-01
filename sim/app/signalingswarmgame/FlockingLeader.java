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
	private double EPSILON = 0.000000001;
	public double currentRelativeSignalingUtilities;

    public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm){
        return swarm.agents.getObjectLocation(this);
    }

	@Override
	public double getSignalingUtility(Agent agent,SignalingSwarmGame swarm) {
		double p = swarm.getAcceptLeadersSignalCorrectly();
		Double2D acceptedLoc = agent.acceptedSignalBehavior(swarm);
		Double2D misunderstoodLoc = agent.misunderstoodSignalBehavior(swarm);
		
		double acceptedAlpha = agent.calculateAngleBetweenAgentAndDirectionToOther(acceptedLoc, swarm.leaderAgent, swarm);
		double misunderstoodAlpha = agent.calculateAngleBetweenAgentAndDirectionToOther(misunderstoodLoc, swarm.leaderAgent, swarm);
		
		return (p * 1 / (acceptedAlpha == 0? EPSILON: acceptedAlpha)) + 
			   ((1 - p) * 1/(misunderstoodAlpha == 0?EPSILON: misunderstoodAlpha));
	}

	@Override
	public double getUnsignalingUtility(Agent agent, SignalingSwarmGame swarm) {
		Double2D noSignalLoc = agent.noSignalBehavior(swarm);
		
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		
		Double2D noSignalDirection = getDirectionBetweenPoints(agent.loc, noSignalLoc, swarm.jump);
		
		double noSignalAlpha = calculateAngleBetweenDirections(noSignalDirection, leaderDirection);
		
		return 1 / (noSignalAlpha == 0? EPSILON : noSignalAlpha);
	}    
}
