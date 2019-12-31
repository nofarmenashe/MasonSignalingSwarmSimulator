package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.util.Double2D;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class LeaderUtilityCalculator {

    public static final double Next_STEP_RATE = 1;
    private static LeaderUtilityCalculator calculatorInstance;

    //region Abstract Method

    protected abstract double getAgentUtility(AgentPosition agentPosition, AgentPosition leaderPosition);

    //endregion

    //region Public Methods

    public  static Pair<Double, Double> calculateUtility(SignalingSwarmGame swarm, Leader leader, Map<Agent, AgentPosition> agentsToCurrentPosition, AgentPosition leaderPosition, int stepsLookahead) {
        Pair<Double, Double> utility = getLookaheadUtility(swarm, leader, agentsToCurrentPosition, stepsLookahead);

        returnAgentsPositionToPhysical(swarm);

        return utility;
    }

    //endregion

    //region Private Methods

    private static Pair<Double, Double> getLookaheadUtility(SignalingSwarmGame swarm, Leader leader, Map<Agent, AgentPosition> agentsToCurrentPosition, int stepsLookahead) {
        if(stepsLookahead == 0) {
            return new Pair<>(0.0, 0.0);
        }

        Map<AgentState, Map<Agent, AgentPosition>> possiblePositions =   getNextStepPossiblePositions(swarm, agentsToCurrentPosition);
        leader.position.updatePosition(swarm.jump);

        Map<Agent, AgentPosition> nosignalPositions = possiblePositions.get(AgentState.NoSignal);
        double currStepNoSignalUtility = calculateSingleStepUtility(nosignalPositions, leader.position);

        Pair<Double, Double> nextStepNoSignalUtility = getLookaheadUtility(swarm, leader, nosignalPositions, stepsLookahead-1);

        double noSignalUtility =  currStepNoSignalUtility + (Next_STEP_RATE * Math.max(nextStepNoSignalUtility.fst, nextStepNoSignalUtility.snd));

        double signalUtility = getSignalUtility(swarm, leader, stepsLookahead, possiblePositions);

        return new Pair<>(signalUtility, noSignalUtility);
    }

    private static double getSignalUtility(SignalingSwarmGame swarm, Leader leader, int stepsLookahead, Map<AgentState, Map<Agent, AgentPosition>> possiblePositions) {
        double signalUtility = 0;
        List<BaseAgent> agentsInSight = AgentMovementCalculator.getAgentNeighbors(swarm, leader, true);
        for (int optionIndex = 0; optionIndex < Math.pow(2,agentsInSight.size()); optionIndex++) {

            Map<Agent, AgentPosition> agentsNextPositions = getSwarmPositionsByOptions(swarm, optionIndex, agentsInSight, possiblePositions);

            Pair<Double, Double> nextStepOptionUtility = getLookaheadUtility(swarm, leader, agentsNextPositions, stepsLookahead-1);

            double currStepOptionUtility = calculateSingleStepUtility(agentsNextPositions, leader.position);
            double optionUtility = currStepOptionUtility +
                    (Next_STEP_RATE * Math.max(nextStepOptionUtility.fst, nextStepOptionUtility.snd));

            double optionProbability = getOptionProbability(optionIndex, agentsInSight.size(), swarm.p_signal_accecptness_v);

            signalUtility += optionProbability * optionUtility;
        }
        return signalUtility;
    }

    private static Map<AgentState, Map<Agent, AgentPosition>> getNextStepPossiblePositions(SignalingSwarmGame swarm, Map<Agent, AgentPosition> positionMap) {
        Map<Agent, AgentPosition> updatedAcptPositions = new HashMap<>();
        Map<Agent, AgentPosition> updatedMisuPositions = new HashMap<>();
        Map<Agent, AgentPosition> updatedNoSigPositions = new HashMap<>();

        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet())
            entry.getKey().position = entry.getValue();

        for (Map.Entry<Agent,AgentPosition> entry: positionMap.entrySet()) {
            Double2D acpt = AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), AgentState.AcceptedSignal);
            Double2D misu = AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), AgentState.MisunderstoodSignal);
            Double2D nosig = AgentMovementCalculator.getAgentNextPositionByState(swarm, entry.getKey(), AgentState.NoSignal);

            updatedAcptPositions.put(entry.getKey(), new AgentPosition(acpt,entry.getValue().loc));
            updatedMisuPositions.put(entry.getKey(), new AgentPosition(misu,entry.getValue().loc));
            updatedNoSigPositions.put(entry.getKey(), new AgentPosition(nosig,entry.getValue().loc));
        }

        return Map.of(
                AgentState.AcceptedSignal, updatedAcptPositions,
                AgentState.MisunderstoodSignal, updatedMisuPositions,
                AgentState.NoSignal, updatedNoSigPositions);
    }

    private static void returnAgentsPositionToPhysical(SignalingSwarmGame swarm) {
        for (Agent agent: swarm.swarmAgents)
            agent.position = new AgentPosition(agent.currentPhysicalPosition);
        for (Leader agent: swarm.leaderAgents)
            agent.position = new AgentPosition(agent.currentPhysicalPosition);
    }

    private static double getOptionProbability(int optionIndex, int optionSize, double p_signal_accecptness_v) {
        double probability = 1;
        while(optionSize > 0){
            probability = (optionIndex % 2 == 1)?
                probability *  p_signal_accecptness_v:  probability *  (1 - p_signal_accecptness_v);

            optionIndex = optionIndex / 2;
            optionSize--;
        }
        return probability;
    }

    private static Map<Agent, AgentPosition> getSwarmPositionsByOptions(SignalingSwarmGame swarm, int optionIndex, List<BaseAgent> agentsInSight, Map<AgentState, Map<Agent, AgentPosition>> possiblePositions) {
        Map<Agent, AgentPosition> positions = new HashMap<>();

        for (BaseAgent neighbor: agentsInSight) {
            boolean isSignalAccepted = (optionIndex % 2) == 1;
            AgentState agentState = isSignalAccepted? AgentState.AcceptedSignal: AgentState.MisunderstoodSignal;
            positions.put((Agent)neighbor, possiblePositions.get(agentState).get(neighbor));

            optionIndex = optionIndex / 2;
        }

        for (Agent agent : swarm.swarmAgents) {
            if (agentsInSight.contains(agent))
                continue;
            positions.put(agent, possiblePositions.get(AgentState.NoSignal).get(agent));
        }

        return positions;
    }

    private static double calculateSingleStepUtility(Map<Agent, AgentPosition> agentsToCurrentPosition, AgentPosition leaderPosition) {
        double swarmUtility = 0;
        for (Map.Entry<Agent, AgentPosition> entry: agentsToCurrentPosition.entrySet())
            swarmUtility += calculateAgentUtility(entry.getValue(), leaderPosition);
        return swarmUtility;
    }

    protected static double calculateAgentUtility(AgentPosition agentPosition, AgentPosition leaderPosition){
        return getInstance().getAgentUtility(agentPosition,leaderPosition);
    }

    //endregion

    //region Singleton

    public static void setInstance(LeaderUtilityCalculator otherCalculator){
        calculatorInstance = otherCalculator;
    }

    private static LeaderUtilityCalculator getInstance(){
        if(calculatorInstance == null)
            calculatorInstance = new FlockingLeaderUtilityCalculator(); // default implementation for now

        return calculatorInstance;
    }

    //endregion
}
