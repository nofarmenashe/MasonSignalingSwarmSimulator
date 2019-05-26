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
	
	protected abstract boolean checkStopCriteria(SignalingSwarmGame swarm);
	
	protected abstract BaseAgent[] getNeighbours(SignalingSwarmGame swarm, boolean isLeaderSignaled);
	
	protected abstract Double2D getDesiredDirection(BaseAgent[] neighbours, 
													boolean isLeaderSignaled, 
													boolean isAcceptedSignalCorrectly);
	
	public void step(SimState state) {
		Double2D desiredDirection;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
		double p = swarm.getAcceptLeadersSignalCorrectly();
		
		Double2D desiredNoSignalDirection = noSignalDirection(swarm);
		isAgentAcceptSignalCorrectly = false;

		if (swarm.isLeaderSignaled) {
			isAgentAcceptSignalCorrectly = Math.random() < p;
			
			Double2D desiredSignalDirection = (isAgentAcceptSignalCorrectly)? 
											  acceptedSignalDirection(swarm): 
											  misunderstoodSignalDirection(swarm);
			
			double lambda = swarm.getLeaderInfluence();
			
			desiredDirection = desiredSignalDirection
					.multiply(lambda)
					.add(desiredNoSignalDirection.multiply(1-lambda));	
		} else
			desiredDirection = desiredNoSignalDirection;

		
		lastLoc = loc;
		loc = loc.add(desiredDirection.multiply(swarm.jump));
		lastD = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
		
		swarm.agents.setObjectLocation(this, loc);
	}
	
	public Double2D acceptedSignalDirection(SignalingSwarmGame swarm) {
		BaseAgent[] signalNeighbors = getNeighbours(swarm, true);
		Double2D desiredAcceptedSignalDirection = getDesiredDirection(signalNeighbors, true, true);
		
		return desiredAcceptedSignalDirection;
	}
	
	public Double2D misunderstoodSignalDirection(SignalingSwarmGame swarm) {
		BaseAgent[] signalNeighbors = getNeighbours(swarm, true);
		Double2D desiredMisunderstoodSignalDirection = getDesiredDirection(signalNeighbors, true, false);
		
		return desiredMisunderstoodSignalDirection;
	}
	
	public Double2D noSignalDirection(SignalingSwarmGame swarm) {
		BaseAgent[] noSignalNeighbors = getNeighbours(swarm, false);
		Double2D desiredNoSignalDirection = getDesiredDirection(noSignalNeighbors, false, false);
		
		return desiredNoSignalDirection;
	}

}