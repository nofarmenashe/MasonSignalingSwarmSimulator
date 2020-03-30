package sim.app.signalingswarmgame;

import sim.util.Double2D;

public class FlockingLeaderUtilityCalculator extends LeaderUtilityCalculator {

    protected double getAgentUtility(AgentPosition agentPosition, AgentPosition leaderPosition) {
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

}