/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public class Leader extends BaseAgent {

    public Double2D getDirectionLoc(SignalingSwarmGame swarm){
        return swarm.agents.getObjectLocation(this);
    }

    public void step(SimState state) {
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        double signalingUtilities = 0, unsignalingUtilities = 0;

        double p = swarm.getAcceptLeadersSignalCorrectly();

        for (int x=0;x<swarm.agents.allObjects.numObjs;x++) {
            if(swarm.agents.allObjects.objs[x] != this){
                Agent agent = (Agent)(swarm.agents.allObjects.objs[x]);
                double alpha = agent.calculateAngleBetweenAgentAndDirectionToOther(agent.getDirectionLoc(swarm),this, swarm);
                if(swarm.getModel() == 'A') alpha = alpha / 2.0;
                signalingUtilities += (2 * p) - 1;
                unsignalingUtilities += Math.cos(alpha);
            }
        }

        swarm.isLeaderSignaled = signalingUtilities > unsignalingUtilities;

        /* ToDo: change leaders location */
        lastLoc = loc;
        swarm.agents.setObjectLocation(this, loc);
    }

//    public double calculateAngleBetweenAgentAndLeader(Agent agent, SignalingSwarmGame swarm){
//        Double2D leaderLoc = swarm.agents.getObjectLocation(this);
//        Double2D directionLoc = agent.getDirectionLoc(swarm);
//        Double2D agentDirection = calculateDirectionVector(agent.loc, directionLoc);
//        Double2D directionToLeader = calculateDirectionVector(agent.loc, leaderLoc);
//
//        double dotProduct = (agentDirection.x * directionToLeader.x) + (agentDirection.y * directionToLeader.y);
//
//
//        double angle = Math.acos(dotProduct);
//        angle = Double.isNaN(angle) ? 0 : Math.PI - angle;
//        return swarm.getModel() == 'A'? (angle / 2.0) : angle;
//    }

}
