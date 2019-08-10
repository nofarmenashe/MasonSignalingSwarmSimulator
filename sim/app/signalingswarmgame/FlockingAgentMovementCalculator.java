package sim.app.signalingswarmgame;

import sim.util.Double2D;

import java.util.ArrayList;

public class FlockingAgentMovementCalculator extends AgentMovementCalculator{

    public boolean checkStopCriteria(SignalingSwarmGame swarm, Agent agent) {
        double directionDis = distanceFromLeader(swarm,agent);

        return directionDis < 0.1;
    }

    public  double distanceFromLeader(SignalingSwarmGame swarm, Agent agent){
        Double2D leaderDirection = swarm.leaderAgent.position.getMovementDirection();
        Double2D agentDirection = agent.position.getMovementDirection();

        double directionDis = AgentMovementCalculator.getDistanceBetweenPoints(agentDirection, leaderDirection);

        return  directionDis;
    }

    public Double2D agentNextDirectionByState(SignalingSwarmGame swarm, Agent agent, AgentState state){
        BaseAgent[] neighbors = getAgentNeighborsByState(swarm, agent, state);

        if(neighbors == null || neighbors.length == 0)
            return agent.position.getMovementDirection();

        Double2D totalOrientationDir = new Double2D(0, 0);
        Double2D totalAttractionDir = new Double2D(0, 0);

        for(BaseAgent neighbor: neighbors) {
            Double2D attractionDirection = getDirectionToNeighbor(agent, neighbor);
            Double2D orientationDirection = neighbor.position.getMovementDirection();
            
            if(state == AgentState.MisunderstoodSignal) {
                totalOrientationDir = totalOrientationDir.subtract(orientationDirection);
                totalAttractionDir = totalAttractionDir.subtract(attractionDirection);
            }
            else {
                totalOrientationDir = totalOrientationDir.add(orientationDirection);
                totalAttractionDir = totalAttractionDir.add(attractionDirection);
            }
        }

        Double2D joinedDirection = (totalOrientationDir.add(totalAttractionDir)).multiply(0.5);

        if(state == AgentState.NoSignal){
            Double2D prevDirInfluence = agent.position.getMovementDirection().multiply(swarm.prevStepRate);
            joinedDirection = joinedDirection.add(prevDirInfluence);
        }

        return getNormalizedVector(joinedDirection);
    }

    public BaseAgent[] agentNeighborsByState(SignalingSwarmGame swarm, Agent agent, AgentState state) {
        if (swarm.getAreAgentsIndependent())
            return null;

        if (state != AgentState.NoSignal)
            return new BaseAgent[]{swarm.leaderAgent}; // TODO: get all leaders after add relevant code

        ArrayList<BaseAgent> neighbors = new ArrayList<BaseAgent>();

        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) { //TODO: filter neighbors out of sight zone
            BaseAgent otherAgent = (BaseAgent) swarm.agents.allObjects.get(i);
            if (otherAgent != agent)
                neighbors.add(otherAgent);
        }

        BaseAgent[] neighborsArray = new BaseAgent[neighbors.size()];
        neighbors.toArray(neighborsArray);
        return neighborsArray;
    }
}
