/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;

public class Agent extends BaseAgent {
	public boolean isReachedLeader = false;
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

		Double2D d_dbg = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
		double dis_dbg = Math.sqrt(d.x * d.x + d.y * d.y);
	}

	public Double2D getDirectionLoc(SignalingSwarmGame swarm) {
		Double2D direction;
		if (swarm.getAreAgentsIndependent()) {
			direction = lastD;
		} else {
			direction = getNeighboursAverageDirection(swarm);
		}

		return direction.add(loc);
	}

	public void step(SimState state) {
		double new_x = 0, new_y = 0;
		final SignalingSwarmGame swarm = (SignalingSwarmGame) state;

		Double2D leaderLoc = swarm.agents.getObjectLocation(swarm.leaderAgent);
		Double2D directionLoc = getDirectionLoc(swarm);

		Double2D original_direction = getDirection(loc, directionLoc, swarm.jump);

		double p = swarm.getAcceptLeadersSignalCorrectly();
		double dis = Math.sqrt(Math.pow(leaderLoc.x - loc.x, 2) + Math.pow(leaderLoc.y - loc.y, 2));

		double agent_dis = Math.sqrt(Math.pow(original_direction.x, 2) + Math.pow(original_direction.y, 2));

		if (dis < swarm.jump) {
//            if(printWriter != null && !isReachedLeader){
//                printWriter.write(stringBuilder.toString());
//                printWriter.close();
//            }
			isReachedLeader = true;
			return;
		}

		if (swarm.isLeaderSignaled) {
			isAgentAcceptSignalCorrectly = Math.random() < p;

			double dx = leaderLoc.x - loc.x;
			double dy = leaderLoc.y - loc.y;

			if (isAgentAcceptSignalCorrectly) // signal accepts correctly
			{
				new_x = (swarm.jump / dis) * dx + loc.x;
				new_y = (swarm.jump / dis) * dy + loc.y;
			} else { // signal misunderstood
				new_x = -((dis + swarm.jump) / dis) * dx + leaderLoc.x;
				new_y = -((dis + swarm.jump) / dis) * dy + leaderLoc.y;
			}
		} else {
			if (swarm.getModel() == 'B') {
				new_x = loc.x + original_direction.x;
				new_y = loc.y + original_direction.y;
			} else {
				double alpha = calculateAngleBetweenAgentAndDirectionToOther(directionLoc, swarm.leaderAgent, swarm);
				if (swarm.getModel() == 'A')
					alpha = alpha / 2.0;
				Double2D newloc = getDirectionWithAngleToOtherAgentLocation(original_direction, swarm.leaderAgent,
						alpha, swarm);
				new_x = newloc.x;
				new_y = newloc.y;
//                Double2D d_loc_leader = new Double2D(leaderLoc.x - loc.x, leaderLoc.y - loc.y);
//
//                double y_ratio = d_loc_leader.y / original_direction.y;
//
//                double dx = (agent_dis * Math.cos(alpha) * (dis - (y_ratio * agent_dis))) /
//                            (d_loc_leader.x - (y_ratio * original_direction.x));
//
//                new_x = loc.x + dx;
//
//                double dy = ((agent_dis * agent_dis * Math.cos(alpha)) - (original_direction.x * (new_x - loc.x))) /
//                        original_direction.y;
//
//                new_y = loc.y + dy;
			}
		}

		lastLoc = loc;
		loc = new Double2D(new_x, new_y);

		double debug_dis = Math.sqrt(Math.pow(loc.x - lastLoc.x, 2) + Math.pow(loc.y - lastLoc.y, 2));
		if (debug_dis > 1.01) {
			debug_dis = 1.0;
		}

		lastD = new Double2D(loc.x - lastLoc.x, loc.y - lastLoc.y);
		swarm.agents.setObjectLocation(this, loc);

	}

	private Double2D getNeighboursAverageDirection(SignalingSwarmGame swarm) {
		Double2D sumLoc = new Double2D(0, 0);

		for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
//            if (swarm.agents.allObjects.objs[x] != this) {
			BaseAgent agent = (BaseAgent) swarm.agents.allObjects.objs[x];
			Double2D agentDirection = agent.getDirection(agent.lastLoc, agent.loc, swarm.jump);
			sumLoc = sumLoc.add(agentDirection);

//                Double2D direction = getOriginalDirection(middlePointDirectionLoc, swarm);
//                double angle = calculateAngleBetweenAgentAndDirectionToOther(middlePointDirectionLoc, agent, swarm);
//                middlePointDirectionLoc = getDirectionWithAngleToOtherAgentLocation(direction, agent, angle / 2.0, swarm);
//                double dis = Math.sqrt(Math.pow((agent.loc.x - loc.x), 2) + Math.pow((agent.loc.y - loc.y), 2));
//                if(dis < minDistance){
//                    minDistance = dis;
//                    averagedDirectionLoc = agent.loc;
//                }
//            }
		}
		Double2D middlePointOfOthers = sumLoc.multiply(swarm.jump / (swarm.agents.allObjects.numObjs) * sumLoc.length());

		return middlePointOfOthers;
	}
}