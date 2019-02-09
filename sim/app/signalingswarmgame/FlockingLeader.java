/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public class FlockingLeader extends Leader {
	public double currentRelativeSignalingUtilities;

    public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm){
        return swarm.agents.getObjectLocation(this);
    }

	@Override
	public double getSignalingUtility(Agent agent,SignalingSwarmGame swarm) {
		double p = swarm.getAcceptLeadersSignalCorrectly();
		Double2D acceptedLoc = agent.acceptedSignalBehavior(swarm);
		Double2D misunderstoodLoc = agent.misunderstoodSignalBehavior(swarm);
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		
		Double2D acceptedDirection = getDirectionBetweenPoints(agent.loc, acceptedLoc, swarm.jump);
		Double2D misunderstoodDirection = getDirectionBetweenPoints(agent.loc, misunderstoodLoc, swarm.jump);
		
		double dbgAcceptDis = getDistanceBetweenPoints(agent.loc, acceptedLoc);
		double dbgMisunderstoodDis = getDistanceBetweenPoints(agent.loc, misunderstoodLoc);
		
		return 1 / (p * getDistanceBetweenPoints(acceptedDirection, leaderDirection) + 
				   (1 - p) * getDistanceBetweenPoints(misunderstoodDirection, leaderDirection));
//		return 2* p -1;
	}

	@Override
	public double getUnsignalingUtility(Agent agent, SignalingSwarmGame swarm) {
		Double2D noSignalLoc = agent.noSignalBehavior(swarm);
		
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		
		Double2D noSignalDirection = getDirectionBetweenPoints(agent.loc, noSignalLoc, swarm.jump);
		
		return 1 / getDistanceBetweenPoints(noSignalDirection, leaderDirection);
	}

//    public void step(SimState state) {
//        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
//        double signalingUtilities = 0, unsignalingUtilities = 0;
//
//        double p = swarm.getAcceptLeadersSignalCorrectly();
//
//        for (int x=0;x<swarm.agents.allObjects.numObjs;x++) {
//            if(swarm.agents.allObjects.objs[x] != this){
//                Agent agent = (Agent)(swarm.agents.allObjects.objs[x]);
//                if(agent.isReachedLeader) 
//                	continue;
//                double alpha = agent.calculateAngleBetweenAgentAndDirectionToOther(agent.getNextLocInOriginalBehaviorDirection(swarm),this, swarm);
//                if(swarm.getModel() == 'A') alpha = alpha / 2.0;
//                signalingUtilities += (2 * p) - 1;
//                unsignalingUtilities += Math.cos(alpha);
//            }
//        }
//        currentRelativeSignalingUtilities = signalingUtilities - unsignalingUtilities;
//        swarm.isLeaderSignaled = signalingUtilities > unsignalingUtilities;
//
//        /* ToDo: change leaders location */
////        lastLoc = loc;
//        swarm.agents.setObjectLocation(this, loc);
//    }
    
    
}
