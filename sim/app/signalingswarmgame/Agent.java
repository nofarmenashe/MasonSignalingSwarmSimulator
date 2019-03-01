/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public abstract class Agent extends BaseAgent {
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
	}

	public abstract Double2D getNextLocInOriginalBehaviorDirection(SignalingSwarmGame swarm);
	
	protected abstract Double2D acceptedSignalBehavior(SignalingSwarmGame swarm);
	protected abstract Double2D misunderstoodSignalBehavior(SignalingSwarmGame swarm);
	protected abstract Double2D noSignalBehavior(SignalingSwarmGame swarm);
	protected abstract boolean checkStopCriteria(SignalingSwarmGame swarm);
	
	public void step(SimState state) {
		Double2D newLoc;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
		double p = swarm.getAcceptLeadersSignalCorrectly();

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
}