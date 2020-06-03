package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.util.Double2D;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DispersionUtilityCalculation {

    public static Map<Agent,Double2D> getTopUtilityPositions(SignalingSwarmGame swarm){
        Map<Double2D, Double> locationsToUtility = new HashMap<Double2D,Double>();
        Map<Agent, Double2D> agentToLocation = new HashMap<Agent,Double2D>();
        if(swarm.numLeaders == 0) return agentToLocation;
        for (Agent a: swarm.swarmAgents) {
            Pair<Double2D,Double> locationToUtility = DispersionUtilityCalculation.getAgentUtility(swarm, a);
            locationsToUtility.put(locationToUtility.fst, locationToUtility.snd);
            agentToLocation.put(a, locationToUtility.fst);
        }
        if(swarm.currentStep % 100 == 0) System.out.println("");
        List<Double> orderedUtilities = locationsToUtility.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(kvp-> kvp.getValue()).collect(Collectors.toList());
//        double threshold = (swarm.numLeaders < swarm.numAgents)?
//                3 * orderedUtilities.get(swarm.numLeaders) - (2 * orderedUtilities.get(swarm.numLeaders - 1)):
          double threshold = Math.min(swarm.getMedian(orderedUtilities), orderedUtilities.get(swarm.numLeaders - 1));
        List<Double2D> selectedLocations =  locationsToUtility.entrySet().stream()
                .filter((kvp) -> kvp.getValue() >= threshold)
                .map(kvp -> kvp.getKey()).collect(Collectors.toList());
        Map<Agent,Double2D> selectedAgents = (Map<Agent, Double2D>) agentToLocation.entrySet().stream()
                .filter(kvp -> selectedLocations.contains(kvp.getValue()))
                .collect(Collectors.toMap(kvp -> kvp.getKey(), kvp-> kvp.getValue()));
        return selectedAgents;
    }

    public static Pair<Double2D, Double> getAgentUtility(SignalingSwarmGame swarm, Agent agent){
        Double2D weightedAvgDirection = new Double2D(0,0);
        double sumOfDistances, sumOfAcptDistances,sumOfMisuDistances;
        sumOfDistances = sumOfAcptDistances = sumOfMisuDistances  = 0;

        for (Agent agent2: swarm.swarmAgents) {
            if(agent == agent2) continue;
            double dist = AgentMovementCalculator.getDistanceBetweenPoints(
                    agent.position.loc, agent2.position.loc);
//            if(dist > (3 * Math.sqrt(2) / 4) * swarm.width) continue;
            sumOfDistances += dist;
            Double2D direction = AgentMovementCalculator.getDirectionBetweenPoints(
                    agent.position.loc, agent2.position.loc);

            weightedAvgDirection = weightedAvgDirection.add(direction.multiply(
                    (dist+1)/(dist))
            );
        }

        Double2D directionToTop = new Double2D(0,-1);
        Double2D directionToBottom = new Double2D(0,1);
        Double2D directionToLeft = new Double2D(-1,0);
        Double2D directionToRight = new Double2D(1,0);
        if(weightedAvgDirection.x != 0 && weightedAvgDirection.y != 0) {
            if (agent.position.loc.x >= swarm.width - 2)
                weightedAvgDirection = weightedAvgDirection.add(directionToRight.multiply(1 / ((swarm.width - agent.position.loc.x)+1)/(swarm.width - agent.position.loc.x)));
            if (agent.position.loc.x <= 2)
                weightedAvgDirection = weightedAvgDirection.add(directionToLeft.multiply(1 / ((agent.position.loc.x)+1)/agent.position.loc.x));
            if (agent.position.loc.y >= swarm.height - 2)
                weightedAvgDirection = weightedAvgDirection.add(directionToBottom.multiply(1 / ((agent.position.loc.x)+1)/agent.position.loc.x));
            if (agent.position.loc.y <= 2)
                weightedAvgDirection = weightedAvgDirection.add(directionToTop.multiply(1 / ((swarm.height - agent.position.loc.y)+1)/(swarm.height - agent.position.loc.y)));
        }
        Double2D nextMovementDirection = AgentMovementCalculator.getNormalizedVector(weightedAvgDirection);

        Double2D nextAcptLoc = getLocInBounderies(swarm, agent.position.loc.add(nextMovementDirection.multiply(-swarm.dt * swarm.jump)));
        Double2D nextMisuLoc = getLocInBounderies(swarm, agent.position.loc.add(nextMovementDirection.multiply(swarm.dt * swarm.jump)));

//        for (Agent agent2: swarm.swarmAgents) {
//            if(agent2 == agent) continue;
//            double dist = AgentMovementCalculator.getDistanceBetweenPoints(
//                    agent.position.loc, agent2.position.loc);
//            if(dist > 2 * swarm.currentAvgNearestNeighborDis) continue;
//            sumOfAcptDistances += AgentMovementCalculator.getDistanceBetweenPoints(
//                    nextAcptLoc, agent2.position.loc);
//            sumOfMisuDistances += AgentMovementCalculator.getDistanceBetweenPoints(
//                    nextMisuLoc, agent2.position.loc);
//        }
        double avgDist = swarm.getMean(swarm.getSwarmDistances());
        agent.position.loc = nextAcptLoc;
        double avgAcptDist = swarm.getMean(swarm.getSwarmDistances());
        agent.position.loc = nextMisuLoc;
        double avgMisuDist = swarm.getMean(swarm.getSwarmDistances());
        agent.position.loc = new Double2D(agent.currentPhysicalPosition.loc.x, agent.currentPhysicalPosition.loc.y);
        double deltaDistancesAcpt = avgAcptDist - avgDist;
        double deltaDistancesMisu = avgMisuDist - avgDist;
//        double deltaDistancesAcpt = sumOfAcptDistances - sumOfDistances;
//        double deltaDistancesMisu = sumOfMisuDistances - sumOfDistances;
        double utility = (swarm.p_signal_accecptness_v * deltaDistancesAcpt) +
                ((1 - swarm.p_signal_accecptness_v) * deltaDistancesMisu);
        if(swarm.currentStep % 100 == 0) System.out.print(utility + ", ");
        Double2D location = getLocInBounderies(swarm, agent.position.loc.add(nextMovementDirection.multiply(5)));
        return new Pair(location, utility);
    }

    private static Double2D getLocInBounderies(SignalingSwarmGame swarm, Double2D loc) {
        double updatedLoc_x = loc.x;
        double updatedLoc_y = loc.y;
        if(loc.x <= 0) updatedLoc_x = 1;
        if(loc.y <= 0) updatedLoc_y = 1;
        if(loc.x >= swarm.width) updatedLoc_x = swarm.width - 1;
        if(loc.y >= swarm.height) updatedLoc_y = swarm.height - 1;
        return new Double2D(updatedLoc_x,updatedLoc_y);
    }
}
