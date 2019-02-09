/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public abstract class Leader extends BaseAgent {
	public double currentRelativeSignalingUtilities;

    public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm){
        return swarm.agents.getObjectLocation(this);
    }
    
    public abstract double getSignalingUtility(Agent agent, SignalingSwarmGame swarm);
    public abstract double getUnsignalingUtility(Agent agent, SignalingSwarmGame swarm);

    public void step(SimState state) {
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        double signalingUtilities = 0, unsignalingUtilities = 0;

        for (int x=0;x<swarm.agents.allObjects.numObjs;x++) {
            if(swarm.agents.allObjects.objs[x] != this){
                Agent agent = (Agent)(swarm.agents.allObjects.objs[x]);
                if(agent.doesStopCriteriaMet) 
                	continue;
//                double alpha = agent.calculateAngleBetweenAgentAndDirectionToOther(agent.getNextLocInOriginalBehaviorDirection(swarm),this, swarm);
//                if(swarm.getModel() == 'A') alpha = alpha / 2.0;
                signalingUtilities += getSignalingUtility(agent, swarm);
                unsignalingUtilities += getUnsignalingUtility(agent, swarm);
            }
        }
        currentRelativeSignalingUtilities = signalingUtilities - unsignalingUtilities;
        swarm.isLeaderSignaled = signalingUtilities > unsignalingUtilities;

        Double2D direction = getMovementDirection(swarm); /*meanwhile does not change*/
        lastLoc = loc;
        loc = lastLoc.add(direction);
        swarm.agents.setObjectLocation(this, loc);
    }
}
