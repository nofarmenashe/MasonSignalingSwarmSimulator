/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public class Agent extends BaseAgent {
	public boolean isAgentAcceptSignalCorrectly;

	public Agent(){	}

	@Override
	public void step(SimState state) {
		Double2D desiredDirection;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;
		double p = swarm.getAcceptLeadersSignalCorrectly();

//		position = new AgentPosition(currentPhysicalPosition);

		Double2D desiredNoSignalDirection =
				AgentMovementCalculator.getAgentNextDirectionByState(swarm,this, AgentState.NoSignal);

		if (swarm.isLeaderSignaled && isCurrentAgentInfluencedByLeader(swarm)) {
			isAgentAcceptSignalCorrectly = Math.random() < p;
			
			Double2D desiredSignalDirection = (isAgentAcceptSignalCorrectly)?
					AgentMovementCalculator.getAgentNextDirectionByState(swarm, this, AgentState.AcceptedSignal):
					AgentMovementCalculator.getAgentNextDirectionByState(swarm, this, AgentState.MisunderstoodSignal);
			
			double leaderInfluenceRate = swarm.getLeaderInfluence();
			
			desiredDirection = desiredSignalDirection
					.multiply(leaderInfluenceRate)
					.add(desiredNoSignalDirection.multiply(1-leaderInfluenceRate));
		} else
			desiredDirection = desiredNoSignalDirection;

		currentPhysicalPosition.updatePosition(getNextStepLocation(swarm, desiredDirection, currentPhysicalPosition.loc));
		position = new AgentPosition(currentPhysicalPosition);

		swarm.agents.setObjectLocation(this, currentPhysicalPosition.loc);
	}
}