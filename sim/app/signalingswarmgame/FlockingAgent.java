/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import java.util.ArrayList;

import sim.engine.*;
import sim.util.*;

public class FlockingAgent extends Agent {
    public boolean isReachedLeader = false;
    public boolean isAgentAcceptSignalCorrectly;

    public FlockingAgent() {
        super();
    }

    public boolean checkStopCriteria(SignalingSwarmGame swarm) {
        Double2D leaderDirection = swarm.leaderAgent.getMovementDirection();
        Double2D agentDirection = getMovementDirection();

        double directionDis = getDistanceBetweenPoints(agentDirection, leaderDirection);

        return directionDis < 0.001;
    }

    @Override
    protected BaseAgent[] getNeighbours(SignalingSwarmGame swarm,
                                        boolean isLeaderSignaled) {
        if(swarm.getAreAgentsIndependent())
            return null;

        if(isLeaderSignaled)
            return new BaseAgent[] {swarm.leaderAgent}; // TODO: get all leaders after add relevant code

        ArrayList<BaseAgent> neighbors = new ArrayList<BaseAgent>();

        for( int i = 0; i < swarm.agents.allObjects.numObjs; i++) { //TODO: filter neighbors out of sight zone
            BaseAgent agent = (BaseAgent)swarm.agents.allObjects.get(i);
            if(agent != this)
                neighbors.add(agent);
        }
        BaseAgent[] neighborsArray = new BaseAgent[neighbors.size()];
        neighbors.toArray(neighborsArray);
        return neighborsArray;
    }

    @Override
    protected Double2D getDesiredDirection(BaseAgent[] neighbors,
                                           boolean isLeaderSignal,
                                           boolean isAcceptSignalCorrectly) {
        if(neighbors == null || neighbors.length == 0)
            return this.getMovementDirection();

        Double2D neighborsOreintationDirection = isLeaderSignal? new Double2D(0,0): this.getMovementDirection();
        Double2D neighborsAttractionDirection = new Double2D(0, 0);

        for(BaseAgent agent: neighbors) {
            if(isLeaderSignal && !isAcceptSignalCorrectly) {
                neighborsOreintationDirection = neighborsOreintationDirection.subtract(agent.getMovementDirection());
                neighborsAttractionDirection = neighborsAttractionDirection.subtract(getDirectionToNeighbor(agent));
            }
            else {
                neighborsOreintationDirection = neighborsOreintationDirection.add(agent.getMovementDirection());
                neighborsAttractionDirection = neighborsAttractionDirection.add(getDirectionToNeighbor(agent));
            }
        }

        Double2D joinedDirection = (neighborsOreintationDirection.add(neighborsAttractionDirection)).multiply(0.5);

        return getNormalizedVector(joinedDirection);
    }


//	public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm) {
//		Double2D direction;
//		if (swarm.getAreAgentsIndependent()) {
//			direction = lastD;
//		} else {
//			direction = getNeighboursAverageDirection(swarm);
//		}
//
//		return direction.add(loc);
//	}

//	public Double2D acceptedSignalBehavior(SignalingSwarmGame swarm) {
//		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
//
//		double newX = loc.x + leaderDirection.x;
//		double newY = loc.y + leaderDirection.y;
//
//		return new Double2D(newX, newY);
//	}
//
//	public Double2D misunderstoodSignalBehavior(SignalingSwarmGame swarm) {
//		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
//
//		double newX = loc.x - leaderDirection.x;
//		double newY = loc.y - leaderDirection.y;
//
//		return new Double2D(newX, newY);
//	}
//
//	public Double2D noSignalBehavior(SignalingSwarmGame swarm) {
//		Double2D originalDirectionNextLoc = getNextLocInOriginalBehaviorDirection(swarm);
//		Double2D originalDirection = getDirectionBetweenPoints(loc, originalDirectionNextLoc, swarm.jump);
//
//		if (swarm.getModel() == 'B') {
//			double newX = loc.x + originalDirection.x;
//			double newY = loc.y + originalDirection.y;
//
//			return new Double2D(newX, newY);
//		}
//
//		double alpha = calculateAngleBetweenAgentAndDirectionToOther(originalDirectionNextLoc, swarm.leaderAgent, swarm);
//		alpha = alpha / 2.0;
//		return getDirectionWithAngleToOtherAgentLocation(originalDirection, swarm.leaderAgent,alpha, swarm);
//	}

//	public Double2D getNeighboursAverageDirection(SignalingSwarmGame swarm) {
//		Double2D sumDirections = new Double2D(0, 0);
//
//		for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
//			BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[x];
//			Double2D agentDirection = agent.getMovementDirection(swarm);
//			sumDirections = sumDirections.add(agentDirection);
//		}
//		int n = swarm.agents.allObjects.numObjs;
//		Double2D avgDirection = new Double2D (sumDirections.x / n, sumDirections.y / n);
//
//		return avgDirection;
//	}
}