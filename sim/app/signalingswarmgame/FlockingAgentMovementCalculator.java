package sim.app.signalingswarmgame;

import sim.util.Double2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlockingAgentMovementCalculator extends AgentMovementCalculator{

    public boolean checkStopCriteria(SignalingSwarmGame swarm, Agent agent) {
        double directionDis = distanceFromLeader(swarm,agent);

        return directionDis < 0.1;
    }

    public  double distanceFromLeader(SignalingSwarmGame swarm, Agent agent){
        // does not matter which one - all leaders should have the same direction
        Double2D leaderDirection = swarm.leaderAgents.get(0).position.getMovementDirection();
        Double2D agentDirection = agent.position.getMovementDirection();

        double directionDis = AgentMovementCalculator.getDistanceBetweenPoints(agentDirection, leaderDirection);

        return  directionDis;
    }

    public Double2D agentNextDirectionByState(SignalingSwarmGame swarm, Agent agent, AgentState state){
        List<BaseAgent> neighbors = getAgentNeighbors(swarm, agent, state);

        if(neighbors == null || neighbors.size() == 0)
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

    public List<BaseAgent> agentNeighborsByState(SignalingSwarmGame swarm, BaseAgent agent, AgentState state, boolean filterLeaders) {
        if (swarm.getAreAgentsIndependent())
            return null;

        if (state != AgentState.NoSignal && ((Agent)agent).influencingLeader != null)
            return new ArrayList<>(Arrays.asList(((Agent)agent).influencingLeader));
        ArrayList<BaseAgent> neighbors = new ArrayList<>();

        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) { //TODO: filter neighbors out of sight zone
            BaseAgent otherAgent = (BaseAgent) swarm.agents.allObjects.objs[i];
//            double dist = getDistanceBetweenPoints(agent.position.loc, otherAgent.position.loc);
            if (otherAgent != agent && (!filterLeaders || otherAgent instanceof Agent))
                neighbors.add(otherAgent);
        }

        neighbors.sort((a1, a2) -> (int) (100 * (getDistanceBetweenPoints(agent.position.loc, ((BaseAgent)a1).position.loc) -
                        getDistanceBetweenPoints(agent.position.loc, ((BaseAgent)a2).position.loc))));

        List<BaseAgent> neighborsInSight =  neighbors.subList(0,
                Math.min(swarm.sight_size_v, neighbors.size()));
        return neighborsInSight;
    }
}
