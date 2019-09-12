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
import java.util.Map;

public class Leader extends BaseAgent {
    public static final double Next_STEP_RATE = .1;
    public double totalSignalUtility;
    public double totalNoSignalUtility;

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
        Map<Agent,AgentPosition> updatedPositions = new HashMap<>();
        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet())
            entry.getKey().position = entry.getValue();

        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet()) {
            AgentState state = isLeaderSignal ?
                            AgentState.AcceptedSignal : AgentState.NoSignal;
            AgentPosition nextPosition = new AgentPosition(
                    AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), state)
                            .multiply((2 * swarm.p_signal_accecptness_v) - 1),
                    entry.getValue().loc);

            updatedPositions.put(entry.getKey(), nextPosition);
        }

        return updatedPositions;
    }

    public void step(SimState state) {
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

        Map<Agent, AgentPosition> agentsToCurrentPosition = getAgentsCurrentPositions(swarm);
        Pair<Boolean, Double> actionUtility = getLookaheadUtility(swarm, agentsToCurrentPosition, swarm.leaderAgent.position, swarm.getStepsLookahead());
        returnAgentsPositionToPhysical(swarm);
        swarm.isLeaderSignaled = actionUtility.fst;
        currentPhysicalPosition.updatePosition(swarm.jump);
        position = new AgentPosition(currentPhysicalPosition);
        swarm.agents.setObjectLocation(this, position.loc);

    }

    private void returnAgentsPositionToPhysical(SignalingSwarmGame swarm) {
        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
            BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[i];
            if(agent instanceof Leader)
                continue;
            agent.position = new AgentPosition(agent.currentPhysicalPosition);
        }
    }

    private Pair<Boolean, Double> getLookaheadUtility(SignalingSwarmGame swarm, Map<Agent, AgentPosition> agentsToCurrentPosition, AgentPosition leaderPosition, int stepsLookahead) {
        Map<Agent, AgentPosition> signalPositions =   getNextStepPositionsByState(swarm, agentsToCurrentPosition, true);
        Map<Agent, AgentPosition> nosignalPositions =   getNextStepPositionsByState(swarm, agentsToCurrentPosition, false);

        leaderPosition.updatePosition(swarm.jump);

        double currentSignalUtility = calculateUtility(signalPositions, leaderPosition);
        double currentNosignalUtility =  calculateUtility(nosignalPositions, leaderPosition);

        if(stepsLookahead > 1) {
            currentSignalUtility = currentSignalUtility + (Next_STEP_RATE * getLookaheadUtility(swarm, signalPositions, leaderPosition,
                    stepsLookahead - 1).snd);
            currentNosignalUtility = currentNosignalUtility + (Next_STEP_RATE * getLookaheadUtility(swarm, nosignalPositions, leaderPosition,
                    stepsLookahead - 1).snd);
        }

        totalSignalUtility = currentSignalUtility;
        totalNoSignalUtility = currentNosignalUtility;

        return (currentSignalUtility > currentNosignalUtility)?
                new Pair(true, currentSignalUtility): new Pair(false, currentNosignalUtility);
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
