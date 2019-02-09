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

//	public void updateLastD(double jump) {
//		Double2D d = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
//		double dis = Math.sqrt(d.x * d.x + d.y * d.y);
//		if (dis > 0) {
//			lastD = new Double2D(d.x / dis * jump, d.y / dis * jump);
//		}
//		lastLoc = new Double2D(loc.x - lastD.x, loc.y - lastD.y);
//
//		Double2D d_dbg = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
//		double dis_dbg = Math.sqrt(d.x * d.x + d.y * d.y);
//	}

	public Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm) {
		Double2D direction;
		if (swarm.getAreAgentsIndependent()) {
			direction = lastD;
		} else {
			direction = getNeighboursAverageDirection(swarm);
		}

		return direction.add(loc);
	}

//	public void step(SimState state) {
//		double new_x = 0, new_y = 0;
//		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
//
//		Double2D leaderLoc = swarm.agents.getObjectLocation(swarm.leaderAgent);
//		Double2D directionLoc = getDirectionLoc(swarm);
//
//		Double2D original_direction = getDirection(loc, directionLoc, swarm.jump);
//
//		double p = swarm.getAcceptLeadersSignalCorrectly();
//		double dis = Math.sqrt(Math.pow(leaderLoc.x - loc.x, 2) + Math.pow(leaderLoc.y - loc.y, 2));
//
//		double agent_dis = Math.sqrt(Math.pow(original_direction.x, 2) + Math.pow(original_direction.y, 2));
//
//		if (dis < swarm.jump) {
//			isReachedLeader = true;
//			return;
//		}
//
//		if (swarm.isLeaderSignaled) {
//			isAgentAcceptSignalCorrectly = Math.random() < p;
//
//			double dx = leaderLoc.x - loc.x;
//			double dy = leaderLoc.y - loc.y;
//
//			if (isAgentAcceptSignalCorrectly) // signal accepts correctly
//			{
//				new_x = (swarm.jump / dis) * dx + loc.x;
//				new_y = (swarm.jump / dis) * dy + loc.y;
//			} else { // signal misunderstood
//				new_x = -((dis + swarm.jump) / dis) * dx + leaderLoc.x;
//				new_y = -((dis + swarm.jump) / dis) * dy + leaderLoc.y;
//			}
//		} else {
//			if (swarm.getModel() == 'B') {
//				new_x = loc.x + original_direction.x;
//				new_y = loc.y + original_direction.y;
//			} else {
//				double alpha = calculateAngleBetweenAgentAndDirectionToOther(directionLoc, swarm.leaderAgent, swarm);
//				if (swarm.getModel() == 'A')
//					alpha = alpha / 2.0;
//				Double2D newloc = getDirectionWithAngleToOtherAgentLocation(original_direction, swarm.leaderAgent,
//						alpha, swarm);
//				new_x = newloc.x;
//				new_y = newloc.y;
//			}
//		}
//
//		lastLoc = loc;
//		loc = new Double2D(new_x, new_y);
//
//		double debug_dis = Math.sqrt(Math.pow(loc.x - lastLoc.x, 2) + Math.pow(loc.y - lastLoc.y, 2));
//		if (debug_dis > 1.01) {
//			debug_dis = 1.0;
//		}
//
//		lastD = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
//		swarm.agents.setObjectLocation(this, loc);
//
//	}
	
	public Double2D acceptedSignalBehavior(SignalingSwarmGame swarm) {
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		Double2D leaderLoc = swarm.agents.getObjectLocation(swarm.leaderAgent);
		
		double distanceFromLeader = Math.sqrt(Math.pow(leaderLoc.x - loc.x, 2) + Math.pow(leaderLoc.y - loc.y, 2));
		
		double newX = loc.x + leaderDirection.x;
		double newY = loc.y + leaderDirection.y;
		
		return new Double2D(newX, newY);
	}
	
	public Double2D misunderstoodSignalBehavior(SignalingSwarmGame swarm) {
		Double2D leaderDirection = swarm.leaderAgent.getMovementDirection(swarm);
		Double2D leaderLoc = swarm.agents.getObjectLocation(swarm.leaderAgent);
		
		double distanceFromLeader = Math.sqrt(Math.pow(leaderLoc.x - loc.x, 2) + Math.pow(leaderLoc.y - loc.y, 2));
		
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

	private Double2D getNeighboursAverageDirection(SignalingSwarmGame swarm) {
		Double2D sumLoc = new Double2D(0, 0);

		for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
			BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[x];
			Double2D agentDirection = agent.getDirectionBetweenPoints(agent.lastLoc, agent.loc, swarm.jump);
			sumLoc = sumLoc.add(agentDirection);
		}
		Double2D middlePointOfOthers = sumLoc.multiply(swarm.jump / (swarm.agents.allObjects.numObjs) * sumLoc.length());

		return middlePointOfOthers;
	}
}