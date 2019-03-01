/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public class FlockingAgent extends Agent {
	public boolean isReachedLeader = false;
	public boolean isAgentAcceptSignalCorrectly;

	public FlockingAgent() {
		super();
	}
	
	public boolean checkStopCriteria(SignalingSwarmGame swarm) {
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		Double2D agentDirection = getMovementDirection(swarm);
		
		double directionDis = Math.sqrt(Math.pow(leaderDirection.x - agentDirection.x, 2) + Math.pow(leaderDirection.y - agentDirection.y, 2));
		return directionDis < 0.001;
	}

	public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm) {
		Double2D direction;
		if (swarm.getAreAgentsIndependent()) {
			direction = lastD;
		} else {
			direction = getNeighboursAverageDirection(swarm);
		}

		return direction.add(loc);
	}

	public Double2D acceptedSignalBehavior(SignalingSwarmGame swarm) {
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		
		double newX = loc.x + leaderDirection.x;
		double newY = loc.y + leaderDirection.y;
		
		return new Double2D(newX, newY);
	}
	
	public Double2D misunderstoodSignalBehavior(SignalingSwarmGame swarm) {
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		
		double newX = loc.x - leaderDirection.x;
		double newY = loc.y - leaderDirection.y;
		
		return new Double2D(newX, newY);
	}
	
	public Double2D noSignalBehavior(SignalingSwarmGame swarm) {
		Double2D originalDirectionNextLoc = getNextLocInOriginalBehaviorDirection(swarm);
		Double2D originalDirection = getDirectionBetweenPoints(loc, originalDirectionNextLoc, swarm.jump);
		
		if (swarm.getModel() == 'B') {
			double newX = loc.x + originalDirection.x;
			double newY = loc.y + originalDirection.y;
			
			return new Double2D(newX, newY);
		}
		
		double alpha = calculateAngleBetweenAgentAndDirectionToOther(originalDirectionNextLoc, swarm.leaderAgent, swarm);
		alpha = alpha / 2.0;
		return getDirectionWithAngleToOtherAgentLocation(originalDirection, swarm.leaderAgent,alpha, swarm);
	}

	public Double2D getNeighboursAverageDirection(SignalingSwarmGame swarm) {
		Double2D sumDirections = new Double2D(0, 0);

		for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
			BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[x];
			Double2D agentDirection = agent.getMovementDirection(swarm);
			sumDirections = sumDirections.add(agentDirection);
		}
		int n = swarm.agents.allObjects.numObjs;
		Double2D avgDirection = new Double2D (sumDirections.x / n, sumDirections.y / n);

		return avgDirection;
	}
}