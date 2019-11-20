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
    public  double prevStepRate = 0.5;
    public double width = 100;
    public double height = 100;
    public int numAgents = 2;
    public int numLeaders = 1;
    public double jump = 1;  // how far do we move in a timestep?
    public boolean isLeaderSignaled = false;
    public BaseAgent[] influencedAgents = null;
    public SwarmType swarmType = SwarmType.Flocking;

    public int sight_size_v = numAgents;
    public double p_signal_accecptness_v = 0.6;
    public char model_v = 'B';
    public double initial_alpha_v = 0;
    public boolean are_agents_independent_v = false;
    public double leader_influence_v = 1;
    public int steps_lookahead_v = 2;


    // some properties to appear in the inspector
    public double getAcceptLeadersSignalCorrectly() { return p_signal_accecptness_v;}
    public void setAcceptLeadersSignalCorrectly(double p) { p_signal_accecptness_v = p;}

    public char getModel() { return model_v;}
    public void setModel(char modelType) { model_v = modelType;}

    public double getInitialAlpha() { return initial_alpha_v;}
    public void setInitialAlpha(double initAlpha) { initial_alpha_v = initAlpha;}

    public boolean getAreAgentsIndependent() { return are_agents_independent_v;}
    public void setAreAgentsIndependent(boolean indenpendentAgents) { are_agents_independent_v = indenpendentAgents;}

    public double getLeaderInfluence() { return leader_influence_v;}
    public void setLeaderInfluence(double leaderInfluence) { leader_influence_v = leaderInfluence;}

    public int getStepsLookahead() { return steps_lookahead_v;}
    public void setStepsLookahead(int lookahead) { steps_lookahead_v = lookahead;}

    public int getSightSize() { return sight_size_v;}
    public void setSightSize(int sightSize) { sight_size_v = sightSize;}

    /** Creates a SignalingSwarmGame simulation with the given random number seed. */
    public SignalingSwarmGame(long seed)
{
    super(seed);
}

    public SignalingSwarmGame(long seed, int n, double p, int l)
    {
        super(seed);
        setAcceptLeadersSignalCorrectly(p);
        numAgents = n;
        setStepsLookahead(l);
        sight_size_v = n;
    }

    public void start()
    {
        super.start();

        // set up the agents field
        agents = new Continuous2D(width,width,height);

        if(swarmType == SwarmType.Flocking)
            AgentMovementCalculator.setInstance(new FlockingAgentMovementCalculator());

        leaderAgent = new Leader();
        
//        leaderAgent = new GatheringLeader();
        Double2D loc =  new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        Double2D lastLoc =  new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        leaderAgent.position = new AgentPosition(loc, lastLoc);
        leaderAgent.currentPhysicalPosition = new AgentPosition(loc, lastLoc);
        agents.setObjectLocation(leaderAgent,leaderAgent.position.loc);
        schedule.scheduleRepeating(schedule.EPOCH, 0, leaderAgent);


        // make a bunch of agents and schedule 'em
        for(int x=0;x<numAgents;x++)
        {
            Agent agent = new Agent();
            locateAgent(agent);
            agents.setObjectLocation(agent, agent.position.loc);
            schedule.scheduleRepeating(schedule.EPOCH, 1, agent);
        }

    }

    private void locateAgent(Agent agent){
        Double2D loc = new Double2D(random.nextDouble()*width, random.nextDouble() * height);
        double alpha = getInitialAlpha();
        Double2D lastLoc;
        if(alpha != 0.0){ // alpha is constant
            Double2D leadLoc = leaderAgent.position.loc;
            Double2D d_loc_leader = new Double2D(leadLoc.x - loc.x, leadLoc.y - loc.y);
            double theta = Math.PI + alpha;

            lastLoc = new Double2D(loc.x + (d_loc_leader.x * Math.cos(theta)) - (d_loc_leader.y * Math.sin(theta)),
                                         loc.y + (d_loc_leader.y * Math.cos(theta)) + (d_loc_leader.x * Math.sin(theta)));
        } else
            lastLoc = new Double2D(random.nextDouble()*width, random.nextDouble() * height);

        agent.position = new AgentPosition(loc, lastLoc);
        agent.currentPhysicalPosition = new AgentPosition(loc, lastLoc);
//        agent.updateLastD(jump);
    }

    public double getConvergancePercentage() {
        int alignedCounter = 0;
        for (int x=0;x<agents.allObjects.numObjs;x++) {
            if(agents.allObjects.objs[x] != leaderAgent){
                Agent agent = (Agent)(agents.allObjects.objs[x]);
                if(AgentMovementCalculator.distanceFromGoal(this, agent) <= 0.5)
                    alignedCounter++;
            }
        }
        return alignedCounter / (double)numAgents;
    }

    public double getLostPercentage() {
        int lostCounter = 0;
        for (int x=0;x<agents.allObjects.numObjs;x++) {
            if(agents.allObjects.objs[x] != leaderAgent){
                Agent agent = (Agent)(agents.allObjects.objs[x]);
                if(AgentMovementCalculator.getDistanceBetweenPoints(leaderAgent.position.loc, agent.position.loc) > 40)
                    lostCounter++;
            }
        }
        return lostCounter / (double)numAgents;
    }


    public boolean swarmReachedGoal() {
    	for (int x=0;x<agents.allObjects.numObjs;x++) {
      		 if(agents.allObjects.objs[x] != leaderAgent){
                   Agent agent = (Agent)(agents.allObjects.objs[x]);
                   if(!AgentMovementCalculator.isAgentReachedGoal(this, agent)) return false;
      		 }
       	}
    	return true;
    }
    
    public static void main(String[] args)
    {
        doLoop(SignalingSwarmGame.class, args);
        System.exit(0);
    }

}
