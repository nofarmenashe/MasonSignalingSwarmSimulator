package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

import java.util.*;
import java.util.stream.Collectors;

public class SignalingSwarmGame extends SimState {
    public static double EPSILON = 0.01;
    //region Mason Parameters

    private static final long serialVersionUID = 1;
    public double width = 100;
    public double height = 100;

    //endregion

    //region Signaling Model Params

    public int numAgents = 2;
    public int numLeaders = 3;
    public double jump = 0.1;  // how far do we move in a timestep?
    public SwarmType swarmType = SwarmType.Flocking;
    public LeaderPositioningAlgo leaderPositioningAlgo = LeaderPositioningAlgo.Random;

    public double prevStepRate = 0.5;

    public int sight_size_v = numAgents;
    public double p_signal_accecptness_v = 0.6;
    public double initial_alpha_v = 0;
    public boolean are_agents_independent_v = false;
    public double leader_influence_v = 1;
    public int steps_lookahead_v = 2;
    public double sight_radius_v = 10.0;
    public double neighbor_discount_factor_v = 0;
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

    public double getNeighborDiscountFactor() {
        return neighbor_discount_factor_v;
    }

    public void setNeighborDiscountFactor(double discountFactor) {
        neighbor_discount_factor_v = discountFactor;
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
           case Intersection:
           case IndirectIntersection:
                initializeLeadersPositionsIntersectionsApproach(leadersDirection);
                break;
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

    private void initializeLeadersPositionsIntersectionsApproach(Double2D leadersDirection) {
        Map<BaseAgent, Double> agentToError = new HashMap<>();
        Map<BaseAgent, List<BaseAgent>> agentToNeighbors = new HashMap<>();

        for(BaseAgent agent: swarmAgents) {
            agentToError.put(agent,
                    AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, agent.position.getMovementDirection()));
            agentToNeighbors.put(agent, AgentMovementCalculator.getAgentNeighbors(this, agent, true));
        }

        List<List<BaseAgent>> intersectingAgentsGroups  = createIntersectingGroups(leadersDirection, agentToError, agentToNeighbors);
        List<List<Double2D>> optionalPointsPerGroup = new ArrayList<>();
        List<Double2D> selectedLoc =new ArrayList<>();
        for (List<BaseAgent> group: intersectingAgentsGroups)
            optionalPointsPerGroup.add(getPointInIntersectionArea(group, agentToError));

        int groupIndex = 0;
        List<BaseAgent> coveredAgents = new ArrayList<>();
        while(selectedLoc.size() < numLeaders){
            if(optionalPointsPerGroup.get(groupIndex).size() > 0 &&
                    (coveredAgents.containsAll(swarmAgents) ||
                            !coveredAgents.containsAll(intersectingAgentsGroups.get(groupIndex)))) {
                selectedLoc.add(optionalPointsPerGroup.get(groupIndex).remove(0));
                coveredAgents.addAll(intersectingAgentsGroups.get(groupIndex));
            }
            groupIndex = (groupIndex + 1) % intersectingAgentsGroups.size();
        }

        LocateLeadersInPositions(leadersDirection, selectedLoc);

    }

    private List<Double2D> getPointInIntersectionArea(List<BaseAgent> group,  Map<BaseAgent, Double> agentToError) {
        Map<Double2D, Double> pointsToIntersectionSize = new HashMap<>();
        if(group.size() == 1)
            return new ArrayList<>(Arrays.asList(group.get(0).position.loc));
        for(BaseAgent a: group){
            for(BaseAgent b: group){
                if(a == b) continue;
                List<Double2D> intersect = intersectTwoCircles(a.position.loc, b.position.loc);
                Double2D middlePoint = new Double2D(
                        (intersect.get(0).x + intersect.get(1).x) / 2,
                        (intersect.get(0).y + intersect.get(1).y) / 2);
                double agentsInfluencedError = 0;
                double agentsInfluencedCounter = 0;
                for(BaseAgent c: swarmAgents){
                    if (AgentMovementCalculator.getDistanceBetweenPoints(middlePoint, c.position.loc) <= sight_radius_v + EPSILON)
                        agentsInfluencedError += agentToError.get(c);
                    agentsInfluencedCounter ++;
                }
                pointsToIntersectionSize.put(middlePoint,
                        (10 * agentsInfluencedCounter) + agentsInfluencedError);
            }
        }

        return pointsToIntersectionSize.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(kvp -> kvp.getKey()).collect(Collectors.toList());
    }

    private List<Double2D> intersectTwoCircles(Double2D loc1, Double2D loc2) {
        double centerdx = loc1.x- loc2.x;
        double centerdy = loc1.y - loc2.y;
        double R = Math.sqrt(centerdx * centerdx + centerdy * centerdy);

        // intersection(s) should exist

        double R2 = R*R;
        double c = Math.sqrt(4 * (sight_radius_v*sight_radius_v) / R2 - 1);

        double fx = (loc1.x + loc2.x) / 2;
        double gx = c * (loc2.y - loc1.y) / 2;
        double ix1 = fx + gx;
        double ix2 = fx - gx;

        double fy = (loc1.y+loc2.y) / 2;
        double gy = c * (loc1.x - loc2.x) / 2;
        double iy1 = fy + gy;
        double iy2 = fy - gy;

        // note if gy == 0 and gx == 0 then the circles are tangent and there is only one solution
        // but that one solution will just be duplicated as the code is currently written
        return new ArrayList<>(Arrays.asList(new Double2D(ix1,iy1),
                new Double2D(ix2, iy2)));
    }

    private List<List<BaseAgent>>  createIntersectingGroups(Double2D leadersDirection,  Map<BaseAgent, Double> agentToError, Map<BaseAgent,
            List<BaseAgent>> agentToNeighbors) {
        List<List<BaseAgent>> intersectingAgentsGroups = new ArrayList<>();
        List<BaseAgent> testedAgents = new ArrayList<>();
        for(BaseAgent agent: swarmAgents)
        {
            testedAgents.add(agent);
            List<BaseAgent> intersectionNeighbors = AgentMovementCalculator.getAgentIntersectingNeighbors(this, agent, true);
            List<BaseAgent> sightNeighbors = AgentMovementCalculator.getAgentNeighbors(this, agent, true);
            List<BaseAgent> neighborsLeft = new ArrayList<>(intersectionNeighbors);

            for(List<BaseAgent> group: intersectingAgentsGroups){
                if(group.contains(agent)) {
                    neighborsLeft.removeAll(group);
                }
                if(intersectionNeighbors.containsAll(group)) {
                    group.add(agent);
                    neighborsLeft.removeAll(group);
                }
           }

            for (BaseAgent neighbor : neighborsLeft)
                if(testedAgents.contains(neighbor))
                    intersectingAgentsGroups.add(new ArrayList<>(Arrays.asList(agent, neighbor)));

            if(intersectingAgentsGroups.isEmpty() || intersectionNeighbors.isEmpty()){
                intersectingAgentsGroups.add(new ArrayList<>(Arrays.asList(agent)));
                continue;
            }
        }

        intersectingAgentsGroups = intersectingAgentsGroups.stream()
                .map(group -> groupError(group, agentToError, agentToNeighbors))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(kvp -> kvp.getKey())
                .collect(Collectors.toList());

        return intersectingAgentsGroups;
    }

    private Map.Entry<List<BaseAgent>,Double> groupError(List<BaseAgent> group,
                                                         Map<BaseAgent, Double> agentToError, Map<BaseAgent,
                                                         List<BaseAgent>> agentToNeighbors) {
        double error = 0;
        for(BaseAgent a: group){
            error += agentToError.get(a);
            if(agentToNeighbors != null){
                for(BaseAgent n: agentToNeighbors.get(a))
                    if(!group.contains(n))
                        error += getNeighborDiscountFactor() * agentToError.get(n);
            }
        }

        return Map.entry(group, error);
    }

    private void LocateLeadersInPositions(Double2D leadersDirection, List<Double2D> selectedAgentsLoc) {
        for (int j = 0; j < numLeaders; j++) {
            Leader leader = leaderAgents.get(j);
            Double2D leadersPos = new Double2D(selectedAgentsLoc.get(j).x, selectedAgentsLoc.get(j).y);
            leader.position = new AgentPosition(
                    leadersPos, leadersPos.subtract(leadersDirection.multiply(jump)));
            leader.currentPhysicalPosition = new AgentPosition(leader.position);
        }
    }

    private void initializeLeadersPositionsErrorApproach(Double2D leadersDirection) {
        Map<BaseAgent, Double> agentsToErrorRate = new HashMap<>();
        List<BaseAgent> selectedAgents = new ArrayList<>();
        List<BaseAgent> testedAgents = new ArrayList<>();
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
            BaseAgent topErrorAgent = orderedAgentsbyErrors.remove(0);
            List<BaseAgent> neighbors = agentsToNeighbors.get(topErrorAgent);
            for (BaseAgent neighbor : neighbors) {
                if (selectedAgents.contains(neighbor) && !testedAgents.contains(topErrorAgent)) {
                    orderedAgentsbyErrors.add(topErrorAgent);
                    break;
                }
            }
            if(!orderedAgentsbyErrors.contains(topErrorAgent))
                selectedAgents.add(topErrorAgent);
            testedAgents.add(topErrorAgent);
        }

        List<Double2D> selectedAgentsLoc = selectedAgents.stream().map(x -> x.position.loc).collect(Collectors.toList());

        locateLeadersInPositions(leadersDirection, selectedAgentsLoc);

    }

    private void locateLeadersInPositions(Double2D leadersDirection, List<Double2D> selectedAgentsLoc) {
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
        Double2D loc = new Double2D(random.nextDouble() * width,  height / 2);

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
