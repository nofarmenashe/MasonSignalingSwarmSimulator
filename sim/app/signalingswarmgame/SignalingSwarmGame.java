package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

import java.util.ArrayList;
import java.util.List;

public class SignalingSwarmGame extends SimState {

    //region Mason Parameters

    private static final long serialVersionUID = 1;
    public double width = 100;
    public double height = 100;

    //endregion

    //region Signaling Model Params

    public int numAgents = 2;
    public int numLeaders = 1;
    public double jump = 1;  // how far do we move in a timestep?
    public SwarmType swarmType = SwarmType.Flocking;

    public double prevStepRate = 0.5;

    public int sight_size_v = numAgents;
    public double p_signal_accecptness_v = 0.6;
    public double initial_alpha_v = 0;
    public boolean are_agents_independent_v = false;
    public double leader_influence_v = 1;
    public int steps_lookahead_v = 2;
    public double sight_radius_v = 20.0;
    //endregion

    //region Simulation Fields

    public int currentStepSignalsCounter = 0;

    public Continuous2D agents;
    public List<Leader> leaderAgents;
    public List<Agent> swarmAgents;
    //endregion

    //region Get/Set Inspector Properties

    public double getAcceptLeadersSignalCorrectly() {
        return p_signal_accecptness_v;
    }

    public void setAcceptLeadersSignalCorrectly(double p) {
        p_signal_accecptness_v = p;
    }

    public double getInitialAlpha() {
        return initial_alpha_v;
    }

    public void setInitialAlpha(double initAlpha) {
        initial_alpha_v = initAlpha;
    }

    public boolean getAreAgentsIndependent() {
        return are_agents_independent_v;
    }

    public void setAreAgentsIndependent(boolean indenpendentAgents) {
        are_agents_independent_v = indenpendentAgents;
    }

    public double getLeaderInfluence() {
        return leader_influence_v;
    }

    public void setLeaderInfluence(double leaderInfluence) {
        leader_influence_v = leaderInfluence;
    }

    public int getStepsLookahead() {
        return steps_lookahead_v;
    }

    public void setStepsLookahead(int lookahead) {
        steps_lookahead_v = lookahead;
    }

    public int getSightSize() {
        return sight_size_v;
    }

    public void setSightSize(int sightSize) {
        sight_size_v = sightSize;
    }

    public double getSightRadius() {
        return sight_radius_v;
    }

    public void setSightRadius(double sightRadius) {
        sight_radius_v = sightRadius;
    }
    //endregion

    //region Ctor

    /**
     * Creates a SignalingSwarmGame simulation with the given random number seed.
     */
    public SignalingSwarmGame(long seed) {
        super(seed);
    }

    public SignalingSwarmGame(long seed, int n, double p, int l) {
        super(seed);
        setAcceptLeadersSignalCorrectly(p);
        numAgents = n;
        setStepsLookahead(l);
        sight_size_v = n;
    }
    //endregion

    public void start() {
        super.start();

        // set up the agents field
        agents = new Continuous2D(width, width, height);
        leaderAgents = new ArrayList<Leader>();
        swarmAgents = new ArrayList<Agent>();

        if (swarmType == SwarmType.Flocking)
            AgentMovementCalculator.setInstance(new FlockingAgentMovementCalculator());

        // set random shared direction to leaders
        Double2D startPoint = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D endPoint = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D leadersDirection = AgentMovementCalculator.getDirectionBetweenPoints(startPoint, endPoint);

        for (int x = 0; x < numLeaders; x++) {
            Leader leader = new Leader();
            locateLeader(leader, leadersDirection);
            leaderAgents.add(leader);
        }

        // make a bunch of agents and schedule 'em
        for (int x = 0; x < numAgents; x++) {
            Agent agent = new Agent();
            locateAgent(agent);
            swarmAgents.add(agent);
        }

    }

    private void locateAgent(BaseAgent agent) {
        Double2D lastLoc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D loc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);

        agent.position = new AgentPosition(loc, lastLoc);
        agent.currentPhysicalPosition = new AgentPosition(loc, lastLoc);

        agents.setObjectLocation(agent, agent.position.loc);
        schedule.scheduleRepeating(schedule.EPOCH, 1, agent);
    }

    private void locateLeader(BaseAgent agent, Double2D direction) {
        Double2D lastLoc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);

        agent.position = new AgentPosition(lastLoc, direction, jump);
        agent.currentPhysicalPosition = new AgentPosition(agent.position.loc, lastLoc);

        agents.setObjectLocation(agent, agent.position.loc);
        schedule.scheduleRepeating(schedule.EPOCH, 1, agent);
    }

    //region Retrieve Info to Report

    public double convergencePercentage() {
        int alignedCounter = 0;
        for (Agent agent: swarmAgents) {
            if (AgentMovementCalculator.distanceFromGoal(this, agent) <= 0.5)
                alignedCounter++;
        }
        return alignedCounter / (double) numAgents;
    }

    public double lostPercentage() {
        int lostCounter = 0;
        for (Agent agent: swarmAgents) {
                //Todo: define lost agent for multiple leader and fix condition
//                if (AgentMovementCalculator.getDistanceBetweenPoints(leaderAgent.position.loc, agent.position.loc) > 40)
                    lostCounter++;
        }
        return lostCounter / (double) numAgents;
    }

    public boolean swarmReachedGoal() {
        for (Agent agent: swarmAgents) {
                if (!AgentMovementCalculator.isAgentReachedGoal(this, agent)) return false;
        }
        return true;
    }
    //endregion

    public static void main(String[] args) {
        doLoop(SignalingSwarmGame.class, args);
        System.exit(0);
    }

}
