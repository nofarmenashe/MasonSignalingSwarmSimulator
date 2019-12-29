package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

import java.util.*;
import java.util.stream.Collectors;

public class SignalingSwarmGame extends SimState {

    //region Mason Parameters

    private static final long serialVersionUID = 1;
    public double width = 100;
    public double height = 100;

    //endregion

    //region Signaling Model Params

    public int numAgents = 2;
    public int numLeaders = 3;
    public double jump = 1;  // how far do we move in a timestep?
    public SwarmType swarmType = SwarmType.Flocking;
    public LeaderPositioningAlgo leaderPositioningAlgo = LeaderPositioningAlgo.Random;

    public double prevStepRate = 0.5;

    public int sight_size_v = numAgents;
    public double p_signal_accecptness_v = 0.6;
    public double initial_alpha_v = 0;
    public boolean are_agents_independent_v = false;
    public double leader_influence_v = 1;
    public int steps_lookahead_v = 2;
    public double sight_radius_v = 20.0;
    //endregion

    //region Simulation Fields

    public int currentStepSignalsCounter = 0;

    public Continuous2D agents;
    public List<Leader> leaderAgents;
    public List<Agent> swarmAgents;
    //endregion

    //region Get/Set Inspector Properties

    public double getAcceptLeadersSignalCorrectly() {
        return p_signal_accecptness_v;
    }

    public void setAcceptLeadersSignalCorrectly(double p) {
        p_signal_accecptness_v = p;
    }

    public double getInitialAlpha() {
        return initial_alpha_v;
    }

    public void setInitialAlpha(double initAlpha) {
        initial_alpha_v = initAlpha;
    }

    public boolean getAreAgentsIndependent() {
        return are_agents_independent_v;
    }

    public void setAreAgentsIndependent(boolean indenpendentAgents) {
        are_agents_independent_v = indenpendentAgents;
    }

    public double getLeaderInfluence() {
        return leader_influence_v;
    }

    public void setLeaderInfluence(double leaderInfluence) {
        leader_influence_v = leaderInfluence;
    }

    public int getStepsLookahead() {
        return steps_lookahead_v;
    }

    public void setStepsLookahead(int lookahead) {
        steps_lookahead_v = lookahead;
    }

    public int getSightSize() {
        return sight_size_v;
    }

    public void setSightSize(int sightSize) {
        sight_size_v = sightSize;
    }

    public double getSightRadius() {
        return sight_radius_v;
    }

    public void setSightRadius(double sightRadius) {
        sight_radius_v = sightRadius;
    }
    //endregion

    //region Ctor

    /**
     * Creates a SignalingSwarmGame simulation with the given random number seed.
     */
    public SignalingSwarmGame(long seed) {
        super(seed);
    }

    public SignalingSwarmGame(long seed, int n, double p, int l) {
        super(seed);
        setAcceptLeadersSignalCorrectly(p);
        numAgents = n;
        setStepsLookahead(l);
        sight_size_v = n;
    }
    //endregion

    public void start() {
        super.start();

        // set up the agents field
        agents = new Continuous2D(width, width, height);
        leaderAgents = new ArrayList<Leader>();
        swarmAgents = new ArrayList<Agent>();

        if (swarmType == SwarmType.Flocking)
            AgentMovementCalculator.setInstance(new FlockingAgentMovementCalculator());

        // set random shared direction to leaders
        Double2D startPoint = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D endPoint = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D leadersDirection = AgentMovementCalculator.getDirectionBetweenPoints(startPoint, endPoint);

        // make a bunch of agents
        for (int x = 0; x < numLeaders; x++) {
            Leader leader = new Leader();
//            locateLeader(leader, leadersDirection);
            leaderAgents.add(leader);
        }
        for (int x = 0; x < numAgents; x++) {
            Agent agent = new Agent();
            locateAgent(agent);
            swarmAgents.add(agent);
        }

        switch(leaderPositioningAlgo) {
            case Error:
                initializeLeadersPositionsErrorApproach(leadersDirection);
                break;
            case Graph:
                initializeLeadersPositionsGraphApproach(leadersDirection);
                break;
            default:
                initializeLeadersPositionsRandomly(leadersDirection);
        }

        putAndScheduleAgentsInScreen();
    }

    //region Locate Agents

    private void putAndScheduleAgentsInScreen() {
        for (Leader leader: leaderAgents) {
            agents.setObjectLocation(leader, leader.position.loc);
            schedule.scheduleRepeating(schedule.EPOCH, 1, leader);
        }
        for (Agent agent: swarmAgents) {
            agents.setObjectLocation(agent, agent.position.loc);
            schedule.scheduleRepeating(schedule.EPOCH, 1, agent);
        }
    }

    private void initializeLeadersPositionsErrorApproach(Double2D leadersDirection) {
        Map<BaseAgent, Double> agentsToErrorRate = new HashMap<>();
        List<BaseAgent> selectedAgents = new ArrayList<>();
        List<BaseAgent> secondLevelAgents = new ArrayList<>();
        Map<BaseAgent, List<BaseAgent>> agentsToNeighbors = new HashMap<>();

        for(Agent agent: swarmAgents) {
            List<BaseAgent> neighbors = AgentMovementCalculator.getAgentNeighbors(this, agent, true);
            double error = AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, agent.position.getMovementDirection());
            for (BaseAgent neighbor : neighbors)
                error += AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, neighbor.position.getMovementDirection());
            agentsToErrorRate.put(agent, error);
            agentsToNeighbors.put(agent, neighbors);
        }

        List<BaseAgent> orderedAgentsbyErrors = agentsToErrorRate.entrySet().stream().sorted(
                Map.Entry.comparingByValue(Comparator.reverseOrder())).map(x -> x.getKey()).collect(Collectors.toList());

        while(selectedAgents.size() < numLeaders){
            if(orderedAgentsbyErrors.size() > 0) {
                BaseAgent topErrorAgent = orderedAgentsbyErrors.remove(0);
                List<BaseAgent> neighbors = agentsToNeighbors.get(topErrorAgent);
                boolean neighborChosen = false;
                for (BaseAgent neighbor : neighbors) {
                    if (selectedAgents.contains(neighbor)) {
                        neighborChosen = true;
                        secondLevelAgents.add(topErrorAgent);
                    }
                }
                if(!neighborChosen)
                    selectedAgents.add(topErrorAgent);
            }
            else {
                selectedAgents.addAll(secondLevelAgents.subList(0, numLeaders - secondLevelAgents.size()));
            }
        }

        List<Double2D> selectedAgentsLoc = selectedAgents.stream().map(x -> x.position.loc).collect(Collectors.toList());

        for (int j = 0; j < numLeaders; j++) {
            Leader leader = leaderAgents.get(j);
            Double2D leadersPos = new Double2D(selectedAgentsLoc.get(j).x + 1, selectedAgentsLoc.get(j).y + 1);
            leader.position = new AgentPosition(
                    leadersPos, leadersPos.subtract(leadersDirection.multiply(jump)));
            leader.currentPhysicalPosition = new AgentPosition(leader.position);
        }

    }

    private void initializeLeadersPositionsGraphApproach(Double2D leadersDirection) {
        List<Double2D> possiblePositions = possibleLeaderLocations();
        List<List<Double2D>> allPositionsCombinations = new ArrayList<>();
        int topScore = 0;
        List<Double2D> topPositions = null;


        generateAllSubGroups(possiblePositions, numLeaders, new ArrayList<>(), allPositionsCombinations);

        for(List<Double2D> leaderLocations: allPositionsCombinations){
            int possibilityScore = getPossibilityScore(leadersDirection, leaderLocations);

            if(possibilityScore > topScore)
            {
                topScore = possibilityScore;
                topPositions = leaderLocations;
            }
        }
//        if(topPositions == null) {
//            initializeLeadersPositionsRandomly(leadersDirection);
//            return;
//        }

        for (int j = 0; j < numLeaders; j++) {
            Leader leader = leaderAgents.get(j);
            leader.position = new AgentPosition(
                    topPositions.get(j),topPositions.get(j).subtract(leadersDirection.multiply(jump)));
            leader.currentPhysicalPosition = new AgentPosition(leader.position);

        }

    }

    private int getPossibilityScore(Double2D leadersDirection, List<Double2D> leaderLocations) {
        List<BaseAgent> directNeighbors = new ArrayList<>();
        List<BaseAgent> indirectNeighbors = new ArrayList<>();
        List<BaseAgent> agentsToSearch = new ArrayList<>();
        int directConn = 0;
        int indirectconn = 0;


        for (int j = 0; j < numLeaders; j++) {
            Leader leader = leaderAgents.get(j);
            leader.position = new AgentPosition(
                    leaderLocations.get(j).subtract(leadersDirection.multiply(jump)), leaderLocations.get(j));
            for(BaseAgent neighbor : AgentMovementCalculator.getAgentNeighbors(this, leader, true)) {
                directConn++;
                if (!directNeighbors.contains(neighbor))
                    directNeighbors.add(neighbor);
            }
        }

        for(BaseAgent a :directNeighbors){
            for(BaseAgent neighbor : AgentMovementCalculator.getAgentNeighbors(this, a, true)) {
                indirectconn++;
                if (!directNeighbors.contains(neighbor) && !indirectNeighbors.contains(neighbor)) {
                    indirectNeighbors.add(neighbor);
                    if (!agentsToSearch.contains(neighbor))
                        agentsToSearch.add(neighbor);
                }
            }
        }

        while(agentsToSearch.size() > 0){
            List<BaseAgent> nextStepAgentToSearch = new ArrayList<>();
            for (BaseAgent a: agentsToSearch) {
                for (BaseAgent neighbor : AgentMovementCalculator.getAgentNeighbors(this, a, true)) {
                    indirectconn++;
                    if (!directNeighbors.contains(neighbor) && !indirectNeighbors.contains(neighbor)) {
                        indirectNeighbors.add(neighbor);
                        nextStepAgentToSearch.add(neighbor);
                    }
                }
            }
            agentsToSearch = nextStepAgentToSearch;
        }

        return ((directNeighbors.size() + indirectNeighbors.size()) * 1000) +
                                ((directConn + indirectconn) * 100) +
                                (directConn * 10) +
                                indirectconn;
    }

    private void generateAllSubGroups(List<Double2D> possiblePositions, int subgroupSize, List<Double2D> currentComb, List<List<Double2D>> combinations){
        if(subgroupSize == 0){
            if(currentComb.size() > 0)
                combinations.add(currentComb);
            return;
        }

        for(int i = 0; i < possiblePositions.size(); i++){
            List<Double2D> newComb = new ArrayList<>(currentComb);
            newComb.add(possiblePositions.get(i));
            List<Double2D> newPossiblePositions = possiblePositions.subList(i + 1, possiblePositions.size());
            generateAllSubGroups(
                    newPossiblePositions,
                    subgroupSize - 1, newComb, combinations);
        }
    }

    private List<Double2D> possibleLeaderLocations() {
        List<Double2D> consideredPoints = new ArrayList<>();
        List<Agent> examinedAgents = new ArrayList<>();

        for (Agent agent: swarmAgents){
            examinedAgents.add(agent);
            consideredPoints.add(new Double2D(agent.position.loc.x + 1, agent.position.loc.y + 1));
            List<BaseAgent> neighbors = AgentMovementCalculator.getAgentNeighbors(this, agent,true);

            for (BaseAgent neighbor: neighbors) {
                if(examinedAgents.contains(neighbor)) continue;

                consideredPoints.add(new Double2D(
                        (agent.position.loc.x + neighbor.position.loc.x)/2,
                        (agent.position.loc.y + neighbor.position.loc.y)/2
                ));
            }

        }
        return consideredPoints;
    }

    private void initializeLeadersPositionsRandomly(Double2D leadersDirection) {
        for (Leader leader : leaderAgents)
            locateLeader(leader, leadersDirection);
    }

    private void locateAgent(BaseAgent agent) {
        Double2D lastLoc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D loc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);

        agent.position = new AgentPosition(loc, lastLoc);
        agent.currentPhysicalPosition = new AgentPosition(loc, lastLoc);
    }

    private void locateLeader(BaseAgent agent, Double2D direction) {
        Double2D lastLoc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);

        agent.position = new AgentPosition(lastLoc, direction, jump);
        agent.currentPhysicalPosition = new AgentPosition(agent.position.loc, lastLoc);
    }
//endregion

    //region Retrieve Info to Report

    public double convergencePercentage() {
        int alignedCounter = 0;
        for (Agent agent: swarmAgents) {
            if (AgentMovementCalculator.distanceFromGoal(this, agent) <= 0.5)
                alignedCounter++;
        }
        return alignedCounter / (double) numAgents;
    }

    public double lostPercentage() {
        int lostCounter = 0;
        for (Agent agent: swarmAgents) {
                //Todo: define lost agent for multiple leader and fix condition
            double minDistanceFromLeader = Integer.MAX_VALUE;
            for(Leader leader: leaderAgents) {
                double dis = AgentMovementCalculator.getDistanceBetweenPoints(leader.position.loc, agent.position.loc);
                if ( dis < minDistanceFromLeader)
                minDistanceFromLeader = dis;
            }
            if(minDistanceFromLeader > sight_radius_v * 2)
                lostCounter++;
        }
        return lostCounter / (double) numAgents;
    }

    public boolean swarmReachedGoal() {
        for (Agent agent: swarmAgents) {
                if (!AgentMovementCalculator.isAgentReachedGoal(this, agent)) return false;
        }
        return true;
    }
    //endregion

    public static void main(String[] args) {
        doLoop(SignalingSwarmGame.class, args);
        System.exit(0);
    }

}
