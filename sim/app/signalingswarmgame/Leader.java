/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import jdk.internal.net.http.common.Pair;
import sim.engine.*;
import sim.util.*;

import java.util.HashMap;
import java.util.Map;

public class Leader extends BaseAgent {
    public static final double Next_STEP_RATE = 0.5;
    public double currentRelativeSignalingUtilities;

    public Leader() {
    }

    public Leader(Double2D loc, Double2D lastLoc) {
        super(loc, lastLoc);
    }

    private double getAgentUtility(AgentPosition agentPosition, AgentPosition leaderPosition) {
        Double2D attractionToLeaderDir = AgentMovementCalculator.getDirectionBetweenPoints(
                agentPosition.loc, leaderPosition.loc);

        Double2D leaderMovementDirection = leaderPosition.getMovementDirection();
        Double2D agentMovementDirection = agentPosition.getMovementDirection();

        double orientationAngle = AgentMovementCalculator.calculateAngleBetweenDirections(
                agentMovementDirection, leaderMovementDirection);
        double attractionAngle = AgentMovementCalculator.calculateAngleBetweenDirections(
                agentMovementDirection, attractionToLeaderDir);

        double directionUtility = 0.5 * (Math.cos(orientationAngle) + Math.cos(attractionAngle));

        return directionUtility;

    }

    private Map<Agent, AgentPosition> getNextStepPositionsByState(SignalingSwarmGame swarm, Map<Agent, AgentPosition> positionMap,
                                                                      boolean isLeaderSignal) {
        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet())
            entry.getKey().position = entry.getValue();

        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet()) {
            AgentState state = isLeaderSignal ? (
                    (Math.random() < swarm.p_signal_accecptness_v) ?
                            AgentState.MisunderstoodSignal :
                            AgentState.AcceptedSignal) : AgentState.NoSignal;
            entry.getValue().updatePosition(AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), state));
        }

        return positionMap;
    }

//    private AgentPosition getAgentNextPositionByState(SignalingSwarmGame swarm, BaseAgent agent, AgentState state) {
//        Double2D nextLoc;
//        if(agent instanceof Leader)
//            nextLoc = agent.getNextStepLocation(swarm, agent.position.loc);
//        else
//            nextLoc = AgentMovementCalculator.getAgentNextPositionByState(swarm, (Agent) agent, agent.position.loc, state);
//
//        return  new AgentPosition(nextLoc, agent.position.loc);
//    }
//
//    private void updateAgentsLookAheadPositions(SignalingSwarmGame swarm,
//                                               Map<BaseAgent, Map<AgentState, AgentPosition>> agentsStateLocations,
//                                               boolean isLeaderSignal){
//        double p = swarm.getAcceptLeadersSignalCorrectly();
//        for (BaseAgent agent: agentsStateLocations.keySet()) {
//            AgentPosition selectedPos = isLeaderSignal ? (
//                    (Math.random() < p) ?
//                            agentsStateLocations.get(agent).get(AgentState.AcceptedSignal) :
//                            agentsStateLocations.get(agent).get(AgentState.MisunderstoodSignal)) :
//                    agentsStateLocations.get(agent).get(AgentState.NoSignal);
//
//            agent.position = new AgentPosition(selectedPos);
//        }
//    }
//
//    private double getSignalingUtility(SignalingSwarmGame swarm, Map<BaseAgent, Map<AgentState, AgentPosition>>agentsStateLocations){
//        double totalSwarmUtility = 0;
//        double p = swarm.getAcceptLeadersSignalCorrectly();
//        AgentPosition leaderPos = agentsStateLocations.get(swarm.leaderAgent).get(AgentState.AcceptedSignal); //any state
//        for (BaseAgent agent: agentsStateLocations.keySet()) {
//            if(agent instanceof Leader)
//                continue;
//            double agentAcptUtility = getAgentUtility(agentsStateLocations.get(agent).get(AgentState.AcceptedSignal), leaderPos);
//            double agentMisUtility = getAgentUtility(agentsStateLocations.get(agent).get(AgentState.MisunderstoodSignal), leaderPos);
//
//            double agentSignalUtility = (p * agentAcptUtility) + ((1 - p) * agentMisUtility);
//            totalSwarmUtility += agentSignalUtility;
//        }
//        return totalSwarmUtility;
//    }
//
//    private double getNoSignalUtility(SignalingSwarmGame swarm, Map<BaseAgent, Map<AgentState, AgentPosition>>agentsStateLocations){
//        double totalSwarmUtility = 0;
//        AgentPosition leaderPos = agentsStateLocations.get(swarm.leaderAgent).get(AgentState.AcceptedSignal); //any state
//
//        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
//            BaseAgent agent = (BaseAgent)swarm.agents.allObjects.objs[i];
//            if(agent instanceof Leader)
//                continue;
//            double agentNoSignalUtility = getAgentUtility(agentsStateLocations.get(agent).get(AgentState.NoSignal), leaderPos);
//
//            totalSwarmUtility += agentNoSignalUtility;
//        }
//        return totalSwarmUtility;
//    }

    public void step(SimState state) {
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        double totalSignalUtility = 0;
        double totalUnsignalUtility = 0;
        Map<Agent, AgentPosition> agentsToCurrentPosition = getAgentsCurrentPositions(swarm);
        Pair<Boolean, Double> actionUtility = getLookaheadUtility(swarm, agentsToCurrentPosition, swarm.leaderAgent.position, swarm.stepsLookahead);

        swarm.isLeaderSignaled = actionUtility.first;
        currentPhysicalPosition.updatePosition(swarm.jump);
        position = new AgentPosition(currentPhysicalPosition);
        swarm.agents.setObjectLocation(this, position.loc);

    }

    private Pair<Boolean, Double> getLookaheadUtility(SignalingSwarmGame swarm, Map<Agent, AgentPosition> agentsToCurrentPosition, AgentPosition leaderPosition, int stepsLookahead) {
        Map<Agent, AgentPosition> signalPositions =   getNextStepPositionsByState(swarm, agentsToCurrentPosition, true);
        Map<Agent, AgentPosition> nosignalPositions =   getNextStepPositionsByState(swarm, agentsToCurrentPosition, false);

        double currentSignalUtility = calculateUtility(signalPositions, leaderPosition);
        double currentNosignalUtility =  calculateUtility(nosignalPositions, leaderPosition);

        if(stepsLookahead == 1)
            return (currentSignalUtility > currentNosignalUtility)?
                    new Pair(true, currentSignalUtility): new Pair(false, currentNosignalUtility);

        leaderPosition.updatePosition(swarm.jump);
        double signalUtility = currentSignalUtility + Next_STEP_RATE * getLookaheadUtility( swarm, signalPositions, leaderPosition,
                stepsLookahead -1).second;
        double nosignalUtility =  currentNosignalUtility + Next_STEP_RATE * getLookaheadUtility( swarm, nosignalPositions, leaderPosition,
                stepsLookahead -1).second;

        return (signalUtility > nosignalUtility)?
                new Pair(true, signalUtility): new Pair(false, nosignalUtility);
    }

    private double calculateUtility(Map<Agent, AgentPosition> agentsToCurrentPosition, AgentPosition leaderPosition) {
        double swarmUtility = 0;
        for (Map.Entry<Agent, AgentPosition> entry: agentsToCurrentPosition.entrySet())
            swarmUtility += getAgentUtility(entry.getValue(), leaderPosition);
        return swarmUtility;
    }

    private Map<Agent, AgentPosition> getAgentsCurrentPositions(SignalingSwarmGame swarm) {
        Map<Agent, AgentPosition> positionsMap = new HashMap<>();
        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
            BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[i];
            if(agent instanceof Leader)
                continue;
            positionsMap.put((Agent)agent, agent.position);
        }
        return positionsMap;
    }
}
