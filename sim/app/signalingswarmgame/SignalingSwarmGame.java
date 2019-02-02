/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class SignalingSwarmGame extends SimState
{
    private static final long serialVersionUID = 1;

    public Continuous2D agents;
    public Leader leaderAgent;

    public double width = 100;
    public double height = 100;
    public int numAgents = 2;
    public double jump = 1;  // how far do we move in a timestep?
    public boolean isLeaderSignaled = false;
    
    public double p_signal_accecptness_v = 0.55;
    public char model_v = 'B';
    public double initial_alpha_v = 0;
    public boolean are_agents_independent_v = false;

    // some properties to appear in the inspector
    public double getAcceptLeadersSignalCorrectly() { return p_signal_accecptness_v;}
    public void setAcceptLeadersSignalCorrectly(double p) { p_signal_accecptness_v = p;}

    public char getModel() { return model_v;}
    public void setModel(char modelType) { model_v = modelType;}

    public double getInitialAlpha() { return initial_alpha_v;}
    public void setInitialAlpha(double initAlpha) { initial_alpha_v = initAlpha;}

    public boolean getAreAgentsIndependent() { return are_agents_independent_v;}
    public void setAreAgentsIndependent(boolean indenpendentAgents) { are_agents_independent_v = indenpendentAgents;}

    /** Creates a SignalingSwarmGame simulation with the given random number seed. */
    public SignalingSwarmGame(long seed)
    {
        super(seed);
    }

    public void start()
    {
        super.start();

        // set up the agents field
        agents = new Continuous2D(width,width,height);


        leaderAgent = new Leader();
        leaderAgent.loc =  new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        leaderAgent.lastLoc =  new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        agents.setObjectLocation(leaderAgent,leaderAgent.loc);
        schedule.scheduleRepeating(schedule.EPOCH, 0, leaderAgent);


        // make a bunch of agents and schedule 'em
        for(int x=0;x<numAgents;x++)
        {
            Agent agent = new Agent();
            locateAgent(agent);
            agents.setObjectLocation(agent, agent.loc);
            schedule.scheduleRepeating(schedule.EPOCH, 1, agent);
        }

    }

    private void locateAgent(Agent agent){
        agent.loc = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        double alpha = getInitialAlpha();
        if(alpha != 0.0){ // alpha is constant
            Double2D leadLoc = leaderAgent.loc;
            Double2D d_loc_leader = new Double2D(leadLoc.x - agent.loc.x, leadLoc.y - agent.loc.y);
            double theta = Math.PI + alpha;

            agent.lastLoc = new Double2D(agent.loc.x + (d_loc_leader.x * Math.cos(theta)) - (d_loc_leader.y * Math.sin(theta)),
                                         agent.loc.y + (d_loc_leader.y * Math.cos(theta)) + (d_loc_leader.x * Math.sin(theta)));
        } else{
            agent.lastLoc = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        }
        agent.updateLastD(jump);
    }

    public int numAgentsAcceptSignal() {
    	int counter = 0;
    	if(!isLeaderSignaled) return -1;
    	for (int x=0;x<agents.allObjects.numObjs;x++) {
    		 if(agents.allObjects.objs[x] != leaderAgent){
                 Agent agent = (Agent)(agents.allObjects.objs[x]);
                 if(agent.isAgentAcceptSignalCorrectly) counter++;
    		 }
    	}
    	return counter;
    }
    
    public double calculateAgentAvgDistanceFromLeader() {
    	double sumOfDistances = 0;
    	for (int x=0;x<agents.allObjects.numObjs;x++) {
   		 if(agents.allObjects.objs[x] != leaderAgent){
                Agent agent = (Agent)(agents.allObjects.objs[x]);
                sumOfDistances += agent.getDistanceFromOther(leaderAgent);
   		 }
    	}
    	return sumOfDistances / numAgents;
    }
    
    public double calculateAgentAvgAngleFromLeader() {
    	double sumOfAngles = 0;
    	for (int x=0;x<agents.allObjects.numObjs;x++) {
   		 if(agents.allObjects.objs[x] != leaderAgent){
                Agent agent = (Agent)(agents.allObjects.objs[x]);
                sumOfAngles += agent.calculateAngleBetweenAgentAndDirectionToOther(
		                		agent.getDirectionLoc(this), 
		                		leaderAgent, 
		                		this);
   		 }
    	}
    	return sumOfAngles / numAgents;
    }
    
    public int numOfCurrentMovingAgent() {
    	int counter = 0;
    	for (int x=0;x<agents.allObjects.numObjs;x++) {
      		 if(agents.allObjects.objs[x] != leaderAgent){
                   Agent agent = (Agent)(agents.allObjects.objs[x]);
                   if(!agent.isReachedLeader) counter++;
      		 }
       	}
    	return counter;
    }
    
    public static void main(String[] args)
    {
        doLoop(SignalingSwarmGame.class, args);
        System.exit(0);
    }
}
