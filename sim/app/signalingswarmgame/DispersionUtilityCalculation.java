package sim.app.signalingswarmgame;

import com.sun.tools.javac.util.Pair;
import sim.util.Double2D;

public class DispersionUtilityCalculation {
    public static Pair<Double2D, Double> getAgentUtility(SignalingSwarmGame swarm, Agent agent){
        Double2D weightedAvgDirection = new Double2D(0,0);
        double sumOfDistances, sumOfAcptDistances,sumOfMisuDistances;
        sumOfDistances = sumOfAcptDistances = sumOfMisuDistances  = 0;

        for (Agent agent2: swarm.swarmAgents) {
            if(agent == agent2) continue;
            double dist = AgentMovementCalculator.getDistanceBetweenPoints(
                    agent.position.loc, agent2.position.loc);
            Double2D direction = AgentMovementCalculator.getDirectionBetweenPoints(
                    agent.position.loc, agent2.position.loc);
            if(dist < swarm.signal_radius_v) continue;
            weightedAvgDirection = weightedAvgDirection.add(direction.multiply(1 / dist));
            sumOfDistances += dist;
        }
        Double2D nextMovementDirection = AgentMovementCalculator.getNormalizedVector(weightedAvgDirection);
        Double2D nextAcptLoc = getLocInBounderies(swarm, agent.position.loc.add(nextMovementDirection.multiply(-swarm.dt)));
        Double2D nextMisuLoc = getLocInBounderies(swarm, agent.position.loc.add(nextMovementDirection.multiply(swarm.dt)));

        for (Agent agent2: swarm.swarmAgents) {
            sumOfAcptDistances += AgentMovementCalculator.getDistanceBetweenPoints(
                    nextAcptLoc, agent2.position.loc);
            sumOfMisuDistances += AgentMovementCalculator.getDistanceBetweenPoints(
                    nextAcptLoc, agent2.position.loc);
        }
        double deltaDistancesAcpt = sumOfDistances - sumOfAcptDistances;
        double deltaDistancesMisu = sumOfDistances - sumOfMisuDistances;
        double utility = (swarm.p_signal_accecptness_v * deltaDistancesAcpt) +
                ((1 - swarm.p_signal_accecptness_v) * deltaDistancesMisu);
        Double2D location = agent.position.loc.add(nextMovementDirection.multiply(5));
        return new Pair(location, utility);
    }

    private static Double2D getLocInBounderies(SignalingSwarmGame swarm, Double2D loc) {
        double updatedLoc_x = loc.x;
        double updatedLoc_y = loc.y;
        if(loc.x < 0) updatedLoc_x = 1;
        if(loc.y < 0) updatedLoc_y = 1;
        if(loc.x > swarm.width) updatedLoc_x = swarm.width - 1;
        if(loc.y > swarm.height) updatedLoc_y = swarm.height - 1;
        return new Double2D(updatedLoc_x,updatedLoc_y);
    }
}
