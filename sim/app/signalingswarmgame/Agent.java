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
	public Leader influencingLeader;

	private double alpha;
	private double acc;

	public Agent(){
	}

	@Override
	public void step(SimState state) {
		Double2D desiredDirection;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

		if(influencingLeader != null){
			isAgentAcceptSignalCorrectly = swarm.random.nextDouble() < swarm.getAcceptLeadersSignalCorrectly();
			if(isAgentAcceptSignalCorrectly)
				desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints(influencingLeader.position.loc, position.loc);
			else
				desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints( position.loc, influencingLeader.position.loc);

		}
		//Levy Walk
		else if(swarm.currentStep == 1 || acc >= 0.1 || (swarm.currentStep % 10 == 0 && AgentMovementCalculator.getAgentNeighbors(swarm, this, true).size() > 0))
		{
			acc = 0;
			alpha = swarm.random.nextDouble(false,true);
			desiredDirection = getRandomDirection(swarm);
			System.out.println("change dir");
		}
		else
			desiredDirection = position.getMovementDirection();

		acc = acc +  alpha;
		//Random walk
//		if(swarm.currentStep % 20 == 0 ||
//				( AgentMovementCalculator.getAgentNeighbors(swarm, this, true).size() > 0)) {
//			desiredDirection = getRandomDirection(swarm);
//		}
//		else
//			desiredDirection = position.getMovementDirection();


		currentPhysicalPosition.updatePosition(getNextStepLocation(swarm, desiredDirection, currentPhysicalPosition.loc));
		position = new AgentPosition(currentPhysicalPosition);

		swarm.agents.setObjectLocation(this, currentPhysicalPosition.loc);
	}

	private Double2D getRandomDirection(SignalingSwarmGame swarm) {
		Double2D desiredDirection;
		Double2D startPoint = new Double2D(swarm.random.nextDouble(), swarm.random.nextDouble());
		Double2D endPoint = new Double2D(swarm.random.nextDouble(), swarm.random.nextDouble());
		desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints(startPoint, endPoint);
		return desiredDirection;
	}
//
//	private Double2D GetInfluencedAgentDirection(SignalingSwarmGame swarm, Double2D desiredNoSignalDirection) {
//		double p = swarm.getAcceptLeadersSignalCorrectly();
//
//		Double2D desiredDirection;
//		isAgentAcceptSignalCorrectly = Math.random() < p;
//
//		Double2D desiredSignalDirection = (isAgentAcceptSignalCorrectly)?
//				AgentMovementCalculator.getAgentNextDirectionByState(swarm, this, AgentState.AcceptedSignal):
//				AgentMovementCalculator.getAgentNextDirectionByState(swarm, this, AgentState.MisunderstoodSignal);
//
//		double leaderInfluenceRate = swarm.getLeaderInfluence();
//
//		desiredDirection = desiredSignalDirection
//				.multiply(leaderInfluenceRate)
//				.add(desiredNoSignalDirection.multiply(1-leaderInfluenceRate));
//		return desiredDirection;
//	}
}