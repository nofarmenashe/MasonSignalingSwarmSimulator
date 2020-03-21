package sim.app.signalingswarmgame;

import sim.util.Double2D;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LeaderPositioning {

    //region GA
    private  static double EPSILON = 0.05;
    private static int POPULATION_SIZE = 100;
    private static double CROSSOVER_PROB = 0.9;
    private static double MUTATION_PROB = 0.1;
    private static double MUTATION_JUMP = 2.0;
    private static Double2D[] directions = new Double2D[]{
            new Double2D(0, -MUTATION_JUMP),
            new Double2D(MUTATION_JUMP, -MUTATION_JUMP),
            new Double2D(MUTATION_JUMP, 0),
            new Double2D(MUTATION_JUMP, MUTATION_JUMP),
            new Double2D(0, MUTATION_JUMP),
            new Double2D(-MUTATION_JUMP, MUTATION_JUMP),
            new Double2D(-MUTATION_JUMP, 0),
            new Double2D(-MUTATION_JUMP, MUTATION_JUMP)
    };

    public static List<Double2D> initializeLeadersPositionsGAApproach(SignalingSwarmGame swarm, Double2D leadersDirection) {
        Map<Double2D[], Double> populationToFitness =new HashMap<>();
        List<Double2D[]> population = new ArrayList<>();

        double bestFitness = 0;
        double saturatedGenerations = 0;
        List<Double2D> possibleInitialPositions = getInitialPossiblePositions(swarm);

        Map<BaseAgent, Double> agentToError = new HashMap<>();
        Map<BaseAgent, List<BaseAgent>> agentToNeighbors = new HashMap<>();
        for(BaseAgent agent: swarm.swarmAgents) {
            agentToError.put(agent,
                    AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, agent.position.getMovementDirection()));
            agentToNeighbors.put(agent, AgentMovementCalculator.getAgentNeighbors(swarm, agent, true));
        }

       // Generate initial population
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Double2D[] chromosome = new Double2D[swarm.numLeaders];
            for(int j=0; j < swarm.numLeaders; j++) {
                int chosenIndex = swarm.random.nextInt(possibleInitialPositions.size());
                chromosome[j] = possibleInitialPositions.get(chosenIndex);
            }
            double fitness = calculateChromosomeFitness(swarm, chromosome, agentToError);
            populationToFitness.put(chromosome, fitness);
            population.add(chromosome);
        }

        bestFitness = populationToFitness.values().stream().sorted(Comparator.reverseOrder()).findFirst().get();

        while(saturatedGenerations < 100) {
            // Selection
            List<Double2D[]> matingPool = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                int chosenChromosomeIndex1 = swarm.random.nextInt(POPULATION_SIZE);
                int chosenChromosomeIndex2 = swarm.random.nextInt(POPULATION_SIZE);

                double fitness1 = calculateChromosomeFitness(swarm, population.get(chosenChromosomeIndex1), agentToError);
                double fitness2 = calculateChromosomeFitness(swarm, population.get(chosenChromosomeIndex2), agentToError);

                matingPool.add(population.get(fitness1 > fitness2 ? chosenChromosomeIndex1 : chosenChromosomeIndex2));
            }

            // Crossover
            List<Double2D[]> nextPopulation = new ArrayList<>();
            while (nextPopulation.size() < POPULATION_SIZE) {
                int chosenChromosomeIndex1 = swarm.random.nextInt(POPULATION_SIZE);
                int chosenChromosomeIndex2 = swarm.random.nextInt(POPULATION_SIZE);

                Double2D[] chromosome1 = matingPool.get(chosenChromosomeIndex1);
                Double2D[] chromosome2 = matingPool.get(chosenChromosomeIndex2);

                if (swarm.random.nextDouble() < CROSSOVER_PROB) {
                    Double2D[] newChromosome1 = new Double2D[swarm.numLeaders];
                    Double2D[] newChromosome2 = new Double2D[swarm.numLeaders];
                    int cuttingPoint = swarm.random.nextInt(swarm.numLeaders);

                    for (int j = 0; j < swarm.numLeaders; j++) {
                        newChromosome1[j] = (j < cuttingPoint) ? chromosome1[j] : chromosome2[j];
                        newChromosome2[j] = (j < cuttingPoint) ? chromosome2[j] : chromosome1[j];
                    }

                    chromosome1 = newChromosome1;
                    chromosome2 = newChromosome2;
                }

                nextPopulation.add(chromosome1);
                nextPopulation.add(chromosome2);
            }

            // Mutation
            for (Double2D[] chromosome : nextPopulation) {
                for (int i = 0; i < chromosome.length; i++) {
                    if (swarm.random.nextDouble() < MUTATION_PROB)
                        chromosome[i] = chromosome[i].add(directions[swarm.random.nextInt(directions.length)]);
                }
            }

            // Update
            population = nextPopulation;
            populationToFitness =new HashMap<>();
            double newBestFitness = 0;
            for (Double2D[] chromosome : population) {
                double fitness = calculateChromosomeFitness(swarm, chromosome, agentToError);
                populationToFitness.put(chromosome, fitness);
                if(fitness > newBestFitness) newBestFitness = fitness;
            }

            saturatedGenerations = ((bestFitness - EPSILON < newBestFitness) && (bestFitness < bestFitness + EPSILON))?
                    saturatedGenerations+1: 0;
            bestFitness = newBestFitness;

        }

        Double2D[] topChromosome = populationToFitness.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(kvp -> kvp.getKey()).collect(Collectors.toList()).get(0);
        return new ArrayList<>(Arrays.asList(topChromosome));
    }

    private static double calculateChromosomeFitness(SignalingSwarmGame swarm, Double2D[] chromosome, Map<BaseAgent, Double> agentToError) {
        double error = 0;
        List<Agent> influencedAgents = new ArrayList<>();
        for (int i = 0; i < chromosome.length; i++) {
            for (Agent agent: swarm.swarmAgents) {
                if (AgentMovementCalculator.getDistanceBetweenPoints(chromosome[i], agent.position.loc) <= swarm.getSightRadius()
                        && !influencedAgents.contains(agent)) {
                    influencedAgents.add(agent);
                    error += agentToError.get(agent);
                }

            }
        }
        return error;
    }

    private static List<Double2D> getInitialPossiblePositions(SignalingSwarmGame swarm) {
        List<Double2D> possiblePositions =new ArrayList<>();
        for (BaseAgent agent: swarm.swarmAgents){
            possiblePositions.add(agent.position.loc);
            for (BaseAgent agent2: swarm.swarmAgents)
                possiblePositions.add(agent.position.loc.add(agent2.position.loc).multiply(0.5)); // middle point
        }
        return  possiblePositions;
    }

    //endregion

    //region Intersection

    public static List<Double2D> initializeLeadersPositionsIntersectionsApproach(SignalingSwarmGame swarm, Double2D leadersDirection) {
        Map<BaseAgent, Double> agentToError = new HashMap<>();
        Map<BaseAgent, List<BaseAgent>> agentToNeighbors = new HashMap<>();
        for(BaseAgent agent: swarm.swarmAgents) {
            agentToError.put(agent,
                    AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, agent.position.getMovementDirection()));
            agentToNeighbors.put(agent, AgentMovementCalculator.getAgentNeighbors(swarm, agent, true));
        }

        List<List<BaseAgent>> intersectingAgentsGroups  = createIntersectingGroups(swarm, leadersDirection, agentToError, agentToNeighbors);
        List<List<Double2D>> optionalPointsPerGroup = new ArrayList<>();
        List<Double2D> selectedLoc =new ArrayList<>();
        for (List<BaseAgent> group: intersectingAgentsGroups)
            optionalPointsPerGroup.add(getPointInIntersectionArea(swarm, group, agentToError));

        int groupIndex = 0;
        List<BaseAgent> coveredAgents = new ArrayList<>();
        while(selectedLoc.size() < swarm.numLeaders){
            if(optionalPointsPerGroup.get(groupIndex).size() > 0 &&
                    (coveredAgents.containsAll(swarm.swarmAgents) ||
                            !coveredAgents.containsAll(intersectingAgentsGroups.get(groupIndex)))) {
                selectedLoc.add(optionalPointsPerGroup.get(groupIndex).remove(0));
                coveredAgents.addAll(intersectingAgentsGroups.get(groupIndex));
            }
            groupIndex = (groupIndex + 1) % intersectingAgentsGroups.size();
        }
        return selectedLoc;
    }

    private static List<Double2D> getPointInIntersectionArea(SignalingSwarmGame swarm, List<BaseAgent> group,  Map<BaseAgent, Double> agentToError) {
        Map<Double2D, Double> pointsToIntersectionSize = new HashMap<>();
        if(group.size() == 1)
            return new ArrayList<>(Arrays.asList(group.get(0).position.loc));
        for(BaseAgent a: group){
            for(BaseAgent b: group){
                if(a == b) continue;
                List<Double2D> intersect = intersectTwoCircles(a.position.loc, b.position.loc, swarm.getSightRadius());
                Double2D middlePoint = new Double2D(
                        (intersect.get(0).x + intersect.get(1).x) / 2,
                        (intersect.get(0).y + intersect.get(1).y) / 2);
                double agentsInfluencedError = 0;
                double agentsInfluencedCounter = 0;
                for(BaseAgent c: swarm.swarmAgents){
                    if (AgentMovementCalculator.getDistanceBetweenPoints(middlePoint, c.position.loc) <= swarm.getSightRadius() + swarm.EPSILON)
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

    private static List<Double2D> intersectTwoCircles(Double2D loc1, Double2D loc2, double radius) {
        double centerdx = loc1.x- loc2.x;
        double centerdy = loc1.y - loc2.y;
        double R = Math.sqrt(centerdx * centerdx + centerdy * centerdy);

        // intersection(s) should exist

        double R2 = R*R;
        double c = Math.sqrt(4 * (radius*radius) / R2 - 1);

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

    private static List<List<BaseAgent>>  createIntersectingGroups(SignalingSwarmGame swarm, Double2D leadersDirection,  Map<BaseAgent, Double> agentToError, Map<BaseAgent,
            List<BaseAgent>> agentToNeighbors) {
        List<List<BaseAgent>> intersectingAgentsGroups = new ArrayList<>();
        List<BaseAgent> testedAgents = new ArrayList<>();

        for(BaseAgent agent: swarm.swarmAgents)
        {
            testedAgents.add(agent);
            List<BaseAgent> intersectionNeighbors = AgentMovementCalculator.getAgentIntersectingNeighbors(swarm, agent, true);
            List<BaseAgent> sightNeighbors = AgentMovementCalculator.getAgentNeighbors(swarm, agent, true);
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
                if(testedAgents.contains(neighbor)) {
                    intersectingAgentsGroups.add(new ArrayList<>(Arrays.asList(agent, neighbor)));
                }

            if(intersectingAgentsGroups.isEmpty() || intersectionNeighbors.isEmpty()){
                intersectingAgentsGroups.add(new ArrayList<>(Arrays.asList(agent)));
                continue;
            }
        }
        for (Agent a: swarm.swarmAgents)
            intersectingAgentsGroups.add(new ArrayList<>(Arrays.asList(a)));
        intersectingAgentsGroups = intersectingAgentsGroups.stream()
                .map(group -> groupError(group, agentToError, agentToNeighbors, swarm.getNeighborDiscountFactor()))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(kvp -> kvp.getKey())
                .collect(Collectors.toList());

        return intersectingAgentsGroups;
    }

    private static Map.Entry<List<BaseAgent>,Double> groupError(List<BaseAgent> group,
                                                         Map<BaseAgent, Double> agentToError, Map<BaseAgent,
            List<BaseAgent>> agentToNeighbors, double discountFactor) {
        double error = 0;
        for(BaseAgent a: group){
            error += agentToError.get(a);
            if(agentToNeighbors != null){
                for(BaseAgent n: agentToNeighbors.get(a)) {
                    if (!group.contains(n)) {
                        error += discountFactor * agentToError.get(n);
                    }
                }
            }
        }

        return Map.entry(group, error);
    }

    //endregion

    //region Error

    public static List<Double2D> initializeLeadersPositionsErrorApproach(SignalingSwarmGame swarm, Double2D leadersDirection) {
        Map<BaseAgent, Double> agentsToErrorRate = new HashMap<>();
        List<BaseAgent> selectedAgents = new ArrayList<>();
        List<BaseAgent> testedAgents = new ArrayList<>();
        Map<BaseAgent, List<BaseAgent>> agentsToNeighbors = new HashMap<>();

        for(Agent agent: swarm.swarmAgents) {
            List<BaseAgent> neighbors = AgentMovementCalculator.getAgentNeighbors(swarm, agent, true);
            double error = AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, agent.position.getMovementDirection());
            for (BaseAgent neighbor : neighbors)
                error += AgentMovementCalculator.calculateAngleBetweenDirections(leadersDirection, neighbor.position.getMovementDirection());
            agentsToErrorRate.put(agent, error);
            agentsToNeighbors.put(agent, neighbors);
        }

        List<BaseAgent> orderedAgentsbyErrors = agentsToErrorRate.entrySet().stream().sorted(
                Map.Entry.comparingByValue(Comparator.reverseOrder())).map(x -> x.getKey()).collect(Collectors.toList());

        while(selectedAgents.size() < swarm.numLeaders){
            BaseAgent topErrorAgent = orderedAgentsbyErrors.remove(0);
            List<BaseAgent> neighbors = agentsToNeighbors.get(topErrorAgent);
            for (BaseAgent neighbor : neighbors) {
                if (selectedAgents.contains(neighbor) && !testedAgents.contains(topErrorAgent)) {
                    orderedAgentsbyErrors.add(topErrorAgent);
                    break;
                }
            }
            if(!orderedAgentsbyErrors.contains(topErrorAgent) || testedAgents.contains(topErrorAgent)) {
                selectedAgents.add(topErrorAgent);
            }
            testedAgents.add(topErrorAgent);
        }

        List<Double2D> selectedAgentsLoc = selectedAgents.stream().map(x -> x.position.loc).collect(Collectors.toList());

        return selectedAgentsLoc;

    }

    //endregion

    //region Graph

    public static List<Double2D> initializeLeadersPositionsGraphApproach(SignalingSwarmGame swarm, Double2D leadersDirection) {
        List<Double2D> possiblePositions = possibleLeaderLocations(swarm);
        List<List<Double2D>> allPositionsCombinations = new ArrayList<>();
        int topScore = 0;
        List<Double2D> topPositions = null;


        generateAllSubGroups(possiblePositions, swarm.numLeaders, new ArrayList<>(), allPositionsCombinations);

        for(List<Double2D> leaderLocations: allPositionsCombinations){
            int possibilityScore = getPossibilityScore(swarm, leadersDirection, leaderLocations);

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

       return topPositions;
    }

    private static int getPossibilityScore(SignalingSwarmGame swarm, Double2D leadersDirection, List<Double2D> leaderLocations) {
        List<BaseAgent> directNeighbors = new ArrayList<>();
        List<BaseAgent> indirectNeighbors = new ArrayList<>();
        List<BaseAgent> agentsToSearch = new ArrayList<>();
        int directConn = 0;
        int indirectconn = 0;


        for (int j = 0; j < swarm.numLeaders; j++) {
            Leader leader = swarm.leaderAgents.get(j);
            leader.position = new AgentPosition(
                    leaderLocations.get(j).subtract(leadersDirection.multiply(swarm.jump)), leaderLocations.get(j));
            for(BaseAgent neighbor : AgentMovementCalculator.getAgentNeighbors(swarm, leader, true)) {
                directConn++;
                if (!directNeighbors.contains(neighbor))
                    directNeighbors.add(neighbor);
            }
        }

        for(BaseAgent a :directNeighbors){
            for(BaseAgent neighbor : AgentMovementCalculator.getAgentNeighbors(swarm, a, true)) {
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
                for (BaseAgent neighbor : AgentMovementCalculator.getAgentNeighbors(swarm, a, true)) {
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

    private static void generateAllSubGroups(List<Double2D> possiblePositions, int subgroupSize, List<Double2D> currentComb, List<List<Double2D>> combinations){
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

    private static List<Double2D> possibleLeaderLocations(SignalingSwarmGame swarm) {
        List<Double2D> consideredPoints = new ArrayList<>();
        List<Agent> examinedAgents = new ArrayList<>();

        for (Agent agent: swarm.swarmAgents){
            examinedAgents.add(agent);
            consideredPoints.add(new Double2D(agent.position.loc.x + 1, agent.position.loc.y + 1));
            List<BaseAgent> neighbors = AgentMovementCalculator.getAgentNeighbors(swarm, agent,true);

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

    //endregion

    //region Random

    public static List<Double2D> initializeLeadersPositionsRandomly(SignalingSwarmGame swarm, Double2D leadersDirection) {
        List<Double2D> selectedLoc = new ArrayList<>();
        for (int i = 0; i < swarm.numLeaders; i++)
            selectedLoc.add(new Double2D(swarm.random.nextDouble() * swarm.width, swarm.random.nextDouble() * swarm.height));
        return selectedLoc;
    }
    //endregion
}
