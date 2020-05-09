/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.engine.*;
import sim.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Leader extends BaseAgent {
    public boolean isLeaderSignaled;
    public int leaderIndex;
    public Agent prevFocusedAgent = null;

    public Leader(int index) {
        leaderIndex = index;
    }

    public Leader(Double2D loc, Double2D lastLoc) {
        super(loc, lastLoc);
    }

    public void step(SimState state) {
        Double2D desiredDirection;
        Double2D desiredLocation;
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

        Agent desiredAgent = getBestAgentToFocus(swarm);
        if(desiredAgent == null) {
            System.out.println("No agent selected " + swarm.desiredLeaderLocations);
            desiredLocation = new Double2D(swarm.random.nextDouble() * swarm.width, swarm.random.nextDouble() * swarm.height);
        } else {
            desiredLocation = swarm.desiredLeaderLocations.get(desiredAgent);
            swarm.desiredLeaderLocations.remove(desiredAgent);

        }
        prevFocusedAgent = desiredAgent;
        desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints(position.loc, desiredLocation);

        double dist = AgentMovementCalculator.getDistanceBetweenPoints( desiredLocation, position.loc);
        if(dist < swarm.signal_radius_v && !isLeaderInOtherDirection(desiredLocation, desiredAgent)) {
            isLeaderSignaled = true;
            sendSignalToInfluencedAgents(swarm);
        }
        else
            isLeaderSignaled = false;

        currentPhysicalPosition.updatePosition(getNextStepLocation(swarm, desiredDirection, currentPhysicalPosition.loc));
        position = new AgentPosition(currentPhysicalPosition);

        swarm.agents.setObjectLocation(this, currentPhysicalPosition.loc);
    }

    private boolean isLeaderInOtherDirection(Double2D selectedLoc, Agent a) {
        double distToAgent = AgentMovementCalculator.getDistanceBetweenPoints(a.position.loc, position.loc);
        double distToGoal = AgentMovementCalculator.getDistanceBetweenPoints(selectedLoc, position.loc);
        return distToAgent < distToGoal;
    }

    private Agent getBestAgentToFocus(SignalingSwarmGame swarm) {
        double minDistance = Double.MAX_VALUE;
        Agent selectedAgent = null;
        if(swarm.desiredLeaderLocations.containsKey(prevFocusedAgent))
            return prevFocusedAgent;
       List<Agent> prevAgents = swarm.leaderAgents.stream().map(leader -> leader.prevFocusedAgent).collect(Collectors.toList());
        for (Map.Entry<Agent, Double2D> entry: swarm.desiredLeaderLocations.entrySet()) {
            if(prevAgents.contains(entry.getKey())) continue;
            double dist = AgentMovementCalculator.getDistanceBetweenPoints(entry.getValue(), position.loc);
            if (dist < minDistance) {
                minDistance = dist;
                selectedAgent = entry.getKey();
            }
        }
        return selectedAgent;
    }

    private void sendSignalToInfluencedAgents(SignalingSwarmGame swarm) {
        List<BaseAgent> agentsInSight = AgentMovementCalculator.getAgentNeighbors(swarm, this, true);

        for (BaseAgent agent: agentsInSight) {
            if(isCurrentLeaderClosestInfluencer((Agent)agent))
                ((Agent) agent).influencingLeader = this;
        }
    }

    private boolean isCurrentLeaderClosestInfluencer(Agent agent) {
        if(agent.influencingLeader == null) return true;

        double currentLeaderDist = AgentMovementCalculator.getDistanceBetweenPoints(position.loc, agent.position.loc);
        double currentInfluencerDist = AgentMovementCalculator.getDistanceBetweenPoints(agent.influencingLeader.position.loc, agent.position.loc);

        return currentLeaderDist < currentInfluencerDist;
    }
//
//    private Map<Agent, AgentPosition> getAgentsCurrentPositions(SignalingSwarmGame swarm) {
//        Map<Agent, AgentPosition> positionsMap = new HashMap<>();
//        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
//            BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[i];
//            if(agent instanceof Leader)
//                continue;
//            positionsMap.put((Agent)agent, agent.position);
//        }
//        return positionsMap;
//    }
}
