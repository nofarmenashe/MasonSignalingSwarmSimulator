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

                double signalingUtility = getSignalingUtility(agent, swarm);
                double unsignalingUtility = getUnsignalingUtility(agent, swarm);
                double lambda = swarm.getLeaderInfluence();
                signalingUtilities += (lambda * signalingUtility) + ((1-lambda) * unsignalingUtility);
                unsignalingUtilities += unsignalingUtility;
            }
        }
        currentRelativeSignalingUtilities = signalingUtilities - unsignalingUtilities;
        swarm.isLeaderSignaled = signalingUtilities > unsignalingUtilities;

        Double2D direction = getMovementDirection(); /*does not change*/
        lastLoc = loc;
        loc = lastLoc.add(direction.multiply(swarm.jump));
        swarm.agents.setObjectLocation(this, loc);
    }
}
