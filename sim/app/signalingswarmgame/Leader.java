/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.engine.*;
import sim.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Leader extends BaseAgent {
    public boolean isLeaderSignaled;

    public Leader() {
    }

    public Leader(Double2D loc, Double2D lastLoc) {
        super(loc, lastLoc);
    }

    public void step(SimState state) {
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        double maxNegihbors = 0;
        Agent selectedAgent = null;
        for (Agent agent : swarm.swarmAgents) {
            List<BaseAgent> neighbors = AgentMovementCalculator.getAgentNeighbors(swarm, agent, true);
            if(neighbors.size() > maxNegihbors){
                maxNegihbors = neighbors.size();
                selectedAgent = agent;
            }
        }
        if(selectedAgent == null) selectedAgent = swarm.swarmAgents.get(swarm.random.nextInt(swarm.numLeaders));
        Double2D desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints(position.loc, selectedAgent.position.loc);

        if(AgentMovementCalculator.getDistanceBetweenPoints(selectedAgent.position.loc, position.loc) < 10) {
            isLeaderSignaled = true;
            sendSignalToInfluencedAgents(swarm);
        }
        else{
            isLeaderSignaled = false;
        }
//        Map<Agent, AgentPosition> agentsToCurrentPosition = getAgentsCurrentPositions(swarm);
//        Pair<Double, Double> utility = LeaderUtilityCalculator.calculateUtility(swarm, this, agentsToCurrentPosition, position, swarm.getStepsLookahead());
//
//        isLeaderSignaled = utility.fst >= utility.snd;
//        if(isLeaderSignaled) {
//            sendSignalToInfluencedAgents(swarm);
//            swarm.currentStepSignalsCounter++;
//        }

        currentPhysicalPosition.updatePosition(getNextStepLocation(swarm, desiredDirection, currentPhysicalPosition.loc));
        position = new AgentPosition(currentPhysicalPosition);

        swarm.agents.setObjectLocation(this, currentPhysicalPosition.loc);

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
