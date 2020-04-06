/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

import java.util.List;

public class Agent extends BaseAgent {
	public boolean isAgentAcceptSignalCorrectly;
	public Leader influencingLeader;

	private double alpha;
	private double acc;
	private int directionStepCounter = 0;

	public Agent(){
	}

	@Override
	public void step(SimState state) {
		Double2D desiredDirection;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

//		if(influencingLeader != null){
//			isAgentAcceptSignalCorrectly = swarm.random.nextDouble() < swarm.getAcceptLeadersSignalCorrectly();
//			if(isAgentAcceptSignalCorrectly)
//				desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints(influencingLeader.position.loc, position.loc);
//			else
//				desiredDirection = AgentMovementCalculator.getDirectionBetweenPoints( position.loc, influencingLeader.position.loc);
//
//		}
        if(position.loc.x < 10) desiredDirection = new Double2D(1,0);
        else if(position.loc.x > swarm.width - 10) desiredDirection = new Double2D(-1,0);
        else if(position.loc.y < 10) desiredDirection = new Double2D(0,1);
        else if(position.loc.y > swarm.height - 10) desiredDirection = new Double2D(0,-1);
        else {
            //Potential Forces
            List<BaseAgent> neighbors = AgentMovementCalculator.getAgentNeighbors(swarm, this, true);
            Double2D neighborsPotentialField = new Double2D(0.0, 0.0);
            if (neighbors.size() > 0) {
                for (BaseAgent neighbor : neighbors) {
                    double euclideanDistance = AgentMovementCalculator.getDistanceBetweenPoints(neighbor.position.loc, position.loc);
                    Double2D distanceVector = neighbor.position.loc.subtract(position.loc);
                    neighborsPotentialField = neighborsPotentialField.add(distanceVector.multiply(1 / Math.pow(euclideanDistance, 3)));
                }
                desiredDirection = AgentMovementCalculator.getNormalizedVector(neighborsPotentialField.multiply(-1));
                directionStepCounter = 0;
            } else if (directionStepCounter == swarm.dt) {
                desiredDirection = getRandomDirection(swarm);
//			desiredDirection = new Double2D(0,0);
                directionStepCounter = 0;
            } else
                desiredDirection = position.getMovementDirection();

            directionStepCounter++;
        }

		//Levy Walk
//		else if(swarm.currentStep == 1 || acc >= 0.1 || (swarm.currentStep % 10 == 0 && AgentMovementCalculator.getAgentNeighbors(swarm, this, true).size() > 0))
//		{
//			acc = 0;
//			alpha = swarm.random.nextDouble(false,true);
//			desiredDirection = getRandomDirection(swarm);
//			System.out.println("change dir");
//		}
//		else
//			desiredDirection = position.getMovementDirection();
//
//		acc = acc +  alpha;
		//Random walk
//		if(swarm.currentStep % 20 == 0 ||
////				( AgentMovementCalculator.getAgentNeighbors(swarm, this, true).size() > 0)) {
////			desiredDirection = getRandomDirection(swarm);
////		}
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