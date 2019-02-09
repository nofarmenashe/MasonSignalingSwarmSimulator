/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public abstract class Agent extends BaseAgent {
	public boolean doesStopCriteriaMet = false;
	public boolean isAgentAcceptSignalCorrectly;

	public Agent() {
		super();
	}

	public void updateLastD(double jump) {
		Double2D d = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
		double dis = Math.sqrt(d.x * d.x + d.y * d.y);
		if (dis > 0) {
			lastD = new Double2D(d.x / dis * jump, d.y / dis * jump);
		}
		lastLoc = new Double2D(loc.x - lastD.x, loc.y - lastD.y);

//		Double2D d_dbg = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
//		double dis_dbg = Math.sqrt(d.x * d.x + d.y * d.y);
	}

	public abstract Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm);
	
	protected abstract Double2D acceptedSignalBehavior(SignalingSwarmGame swarm);
	protected abstract Double2D misunderstoodSignalBehavior(SignalingSwarmGame swarm);
	protected abstract Double2D noSignalBehavior(SignalingSwarmGame swarm);
	protected abstract boolean checkStopCriteria(SignalingSwarmGame swarm);
	public void step(SimState state) {
		Double2D newLoc;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

//		Double2D leaderLoc = swarm.agents.getObjectLocation(swarm.leaderAgent);
//		Double2D directionLoc = getDirectionLoc(swarm);
//
//		Double2D original_direction = getDirection(loc, directionLoc, swarm.jump);
//
		double p = swarm.getAcceptLeadersSignalCorrectly();
//		double distanceFromLeader = Math.sqrt(Math.pow(leaderLoc.x - loc.x, 2) + Math.pow(leaderLoc.y - loc.y, 2));
//
//		double agent_dis = Math.sqrt(Math.pow(original_direction.x, 2) + Math.pow(original_direction.y, 2));

		if (checkStopCriteria(swarm)) {
			doesStopCriteriaMet = true;
			return;
		}

		if (swarm.isLeaderSignaled) {
			isAgentAcceptSignalCorrectly = Math.random() < p;

			if (isAgentAcceptSignalCorrectly) // signal accepts correctly
				newLoc = acceptedSignalBehavior(swarm);
			else // signal misunderstood
				newLoc = misunderstoodSignalBehavior(swarm);
			
			// add leader's influence
			Double2D noSignalLoc = noSignalBehavior(swarm);
			double lambda = swarm.getLeaderInfluence();
			newLoc = newLoc.multiply(lambda).add(noSignalLoc.multiply(1-lambda));
			
		} else
			newLoc = noSignalBehavior(swarm);

		lastLoc = loc;
		loc = new Double2D(newLoc.x, newLoc.y);
		lastD = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
		
		swarm.agents.setObjectLocation(this, loc);

	}

//	private Double2D getNeighboursAverageDirection(SignalingSwarmGame swarm) {
//		Double2D sumLoc = new Double2D(0, 0);
//
//		for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
//			BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[x];
//			Double2D agentDirection = agent.getDirection(agent.lastLoc, agent.loc, swarm.jump);
//			sumLoc = sumLoc.add(agentDirection);
//		}
//		Double2D middlePointOfOthers = sumLoc.multiply(swarm.jump / (swarm.agents.allObjects.numObjs) * sumLoc.length());
//
//		return middlePointOfOthers;
//	}
}