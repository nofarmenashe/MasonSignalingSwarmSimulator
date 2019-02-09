/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public class GatheringLeader extends Leader {
	public double currentRelativeSignalingUtilities;

    public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm){
        return swarm.agents.getObjectLocation(this);
    }

	@Override
	public double getSignalingUtility(Agent agent,double p) {
		return (2 * p) - 1;
	}

	@Override
	public double getUnsignalingUtility(Agent agent, SignalingSwarmGame swarm) {
		double alpha = agent.calculateAngleBetweenAgentAndDirectionToOther(agent.getNextLocInOriginalBehaviorDirection(swarm),this, swarm);
		if(swarm.getModel() == 'A') alpha = alpha / 2.0;
		return Math.cos(alpha);
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
