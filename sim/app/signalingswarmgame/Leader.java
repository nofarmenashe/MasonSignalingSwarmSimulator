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

    public Leader(int index) {
        leaderIndex = index;
    }

    public Leader(Double2D loc, Double2D lastLoc) {
        super(loc, lastLoc);
    }

    public void step(SimState state) {
        Double2D desiredDirection;
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
//        if(leaderIndex == 0)
//            swarm.setAgentsPairsDistances();
        Double2D desiredLocation = getBestLocation(swarm);
        if(desiredLocation == null)
            desiredLocation = new Double2D(swarm.random.nextDouble() * swarm.width, swarm.random.nextDouble() * swarm.height);

        desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints(position.loc, desiredLocation);

        double dist = AgentMovementCalculator.getDistanceBetweenPoints( desiredLocation, position.loc);
        if(dist < 5) {
            isLeaderSignaled = true;
            sendSignalToInfluencedAgents(swarm);
        }
        else
            isLeaderSignaled = false;

        currentPhysicalPosition.updatePosition(getNextStepLocation(swarm, desiredDirection, currentPhysicalPosition.loc));
        position = new AgentPosition(currentPhysicalPosition);

        swarm.agents.setObjectLocation(this, currentPhysicalPosition.loc);
    }

    private Double2D getBestLocation(SignalingSwarmGame swarm) {
        double maxUtility = Double.NEGATIVE_INFINITY;
        Double2D selectedLocation = null;
        for (Agent a: swarm.swarmAgents) {
            Pair<Double2D,Double> locationToUtility = DispersionUtilityCalculation.getAgentUtility(swarm, a);
            if (maxUtility < locationToUtility.snd) {
                maxUtility = locationToUtility.snd;
                selectedLocation = locationToUtility.fst;
            }
        }
        return selectedLocation;
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
