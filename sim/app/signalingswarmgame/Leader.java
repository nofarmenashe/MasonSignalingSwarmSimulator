/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.engine.*;
import sim.util.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Leader extends BaseAgent {
    public static final double Next_STEP_RATE = 1;
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

    private Map<AgentState, Map<Agent, AgentPosition>> getNextStepPossiblePositions(SignalingSwarmGame swarm, Map<Agent, AgentPosition> positionMap) {
        Map<Agent, AgentPosition> updatedAcptPositions = new HashMap<>();
        Map<Agent, AgentPosition> updatedMisuPositions = new HashMap<>();
        Map<Agent, AgentPosition> updatedNoSigPositions = new HashMap<>();

        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet())
            entry.getKey().position = entry.getValue();

        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet()) {
            Double2D acpt = AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), AgentState.AcceptedSignal);
            Double2D misu = AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), AgentState.MisunderstoodSignal);
            Double2D nosig = AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), AgentState.NoSignal);

            Map<AgentState, AgentPosition> nextPositions = new HashMap<>();
            updatedAcptPositions.put(entry.getKey(), new AgentPosition(acpt,entry.getValue().loc));
            updatedMisuPositions.put(entry.getKey(), new AgentPosition(misu,entry.getValue().loc));
            updatedNoSigPositions.put(entry.getKey(), new AgentPosition(nosig,entry.getValue().loc));
        }

        return Map.of(
                AgentState.AcceptedSignal, updatedAcptPositions,
                AgentState.MisunderstoodSignal, updatedMisuPositions,
                AgentState.NoSignal, updatedNoSigPositions);
    }

    public void step(SimState state) {
        final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

        Map<Agent, AgentPosition> agentsToCurrentPosition = getAgentsCurrentPositions(swarm);
        Pair<Double, Double> utility = getLookaheadUtility(swarm, agentsToCurrentPosition, swarm.leaderAgent.position, swarm.getStepsLookahead());
        returnAgentsPositionToPhysical(swarm);
        swarm.isLeaderSignaled = utility.fst > utility.snd;
        swarm.influencedAgents = swarm.isLeaderSignaled? AgentMovementCalculator.getAgentNeighborsByState(swarm, this, AgentState.NoSignal): null;
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

    private Pair<Double, Double> getLookaheadUtility(SignalingSwarmGame swarm, Map<Agent, AgentPosition> agentsToCurrentPosition, AgentPosition leaderPosition, int stepsLookahead) {
        if(stepsLookahead == 0) {
//            System.out.println("end");
            return new Pair<Double, Double>(0.0, 0.0);
        }

        Map<AgentState, Map<Agent, AgentPosition>> possiblePositions =   getNextStepPossiblePositions(swarm, agentsToCurrentPosition);
        leaderPosition.updatePosition(swarm.jump);

        Map<Agent, AgentPosition> nosignalPositions = possiblePositions.get(AgentState.NoSignal);
        double currStepNoSignalUtility = calculateUtility(nosignalPositions, leaderPosition);

//        System.out.println("NoSignalUtility = " + currStepNoSignalUtility + " "+ nosignalPositions +" l = " + stepsLookahead);

        Pair<Double, Double> nextStepNoSignalUtility = getLookaheadUtility(swarm, nosignalPositions, leaderPosition, stepsLookahead-1);

        double noSignalUtility =  currStepNoSignalUtility +
                (Next_STEP_RATE * Math.max(nextStepNoSignalUtility.fst, nextStepNoSignalUtility.snd));


        double signalUtility = 0;

        for (int i = 0; i < Math.pow(2,swarm.numAgents); i++) {
            boolean[] isSignalAccepted = getBinaryPermutation(i, swarm.numAgents);
            Map<Agent, AgentPosition> agentsNextPositions = getSwarmPositionsByOptions(swarm, isSignalAccepted,possiblePositions);

            Pair<Double, Double> nextStepOptionUtility = getLookaheadUtility(swarm, agentsNextPositions, leaderPosition, stepsLookahead-1);

            double currStepOptionUtility = calculateUtility(agentsNextPositions, leaderPosition);
//            System.out.println("SignalOptionUtility = " + currStepOptionUtility + " option = " + Arrays.toString(isSignalAccepted) + ", " + agentsNextPositions + " l = " + stepsLookahead);
            double optionUtility = currStepOptionUtility +
                    (Next_STEP_RATE * Math.max(nextStepOptionUtility.fst, nextStepOptionUtility.snd));

            double optionProbability = getOptionProbability(isSignalAccepted, swarm.p_signal_accecptness_v);

            signalUtility += optionProbability * optionUtility;
        }

        return new Pair<>(signalUtility, noSignalUtility);

//        if(stepsLookahead > 1) {
//            currentSignalUtility = currentSignalUtility + (Next_STEP_RATE * getLookaheadUtility(swarm, signalPositions, leaderPosition,
//                    stepsLookahead - 1).snd);
//            currentNosignalUtility = currentNosignalUtility + (Next_STEP_RATE * getLookaheadUtility(swarm, nosignalPositions, leaderPosition,
//                    stepsLookahead - 1).snd);
//        }
//
//        totalSignalUtility = currentSignalUtility;
//        totalNoSignalUtility = currentNosignalUtility;
//
//        return (currentSignalUtility > currentNosignalUtility)?
//                new Pair(true, currentSignalUtility): new Pair(false, currentNosignalUtility);
    }

    private double getOptionProbability(boolean[] isSignalAccepted, double p_signal_accecptness_v) {
        double probability = 1;

        for (int i = 0; i < isSignalAccepted.length; i++)
            probability = probability * (isSignalAccepted[i]? p_signal_accecptness_v: 1- p_signal_accecptness_v);

        return probability;
    }

    private Map<Agent, AgentPosition> getSwarmPositionsByOptions(SignalingSwarmGame swarm, boolean[] isSignalAccepted, Map<AgentState, Map<Agent, AgentPosition>> possiblePositions) {
        Map<Agent, AgentPosition> positions = new HashMap<>();

        for (int i = 0; i < swarm.numAgents; i++) {
            BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[i+1];
//            if(agent instanceof Leader) continue;
            AgentState agentState = isSignalAccepted[i]? AgentState.AcceptedSignal: AgentState.MisunderstoodSignal;
                positions.put((Agent)agent, possiblePositions.get(agentState).get(agent));
            }

        return positions;
    }

    private boolean[] getBinaryPermutation(int i, int n) {
        boolean[] permutation = new boolean[n];
        String binaryString = String.format("%"+ n +"s", Integer.toBinaryString(i));
        char[] binaryArray = binaryString.toCharArray();

        for (int j = 0; j < binaryArray.length; j++)
            permutation[j] = binaryArray[j] == '1';

        return permutation;
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
