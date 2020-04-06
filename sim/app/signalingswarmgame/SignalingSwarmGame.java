package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

import java.util.*;
import java.util.stream.Collectors;

public class SignalingSwarmGame extends SimState {
    public static double EPSILON = 0.01;

    //region Mason Parameters

    private static final long serialVersionUID = 1;
    public double width = 500;
    public double height = 500;
    public double varianceThreshold = 15;
    public int currentStep = 0;

    //endregion

    //region Signaling Model Params

    public int numAgents = 2;
    public int numLeaders = 3;
    public double jump = 0.1;  // how far do we move in a timestep?
    public SwarmType swarmType = SwarmType.Flocking;
    public LeaderPositioningApproach leaderPositioningApproach = LeaderPositioningApproach.Random;
    public List<Double2D> leadersInitLoc;
    public List<Double2D> agentsInitLoc;
    public Double2D leadersDirection;

    public double prevStepRate = 0.5;

    public int steps_per_direction_v = 5;
    public int sight_size_v = numAgents;
    public double p_signal_accecptness_v = 0.6;
    public double initial_alpha_v = 0;
    public boolean are_agents_independent_v = false;
    public double leader_influence_v = 1;
    public int steps_lookahead_v = 2;
    public double sight_radius_v = 11.0;
    public double signal_radius_v = Double.MAX_VALUE;
    public double neighbor_discount_factor_v = 0;
    public int dt = 50;
    //endregion

    //region Simulation Fields

    public int currentStepSignalsCounter = 0;
    public double currentAreaCoverage = 0;
    public double currentAvgNearestNeighborDis = 0;

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

    public double getNeighborDiscountFactor() {
        return neighbor_discount_factor_v;
    }

    public void setNeighborDiscountFactor(double discountFactor) {
        neighbor_discount_factor_v = discountFactor;
    }

    public double getSightRadius() {
        return sight_radius_v;
    }

    public void setSightRadius(double sightRadius) {
        sight_radius_v = sightRadius;
    }

    public double getSignalRadius() {
        return signal_radius_v;
    }

    public void setSignalRadius(double signalRadius) {
        signal_radius_v = signalRadius;
    }

    public int getStepsPerDir() {
        return steps_per_direction_v;
    }

    public void setStepsPerDir(int steps) {
        steps_per_direction_v = steps;
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

    public void initSimulation(){
        System.out.println("_____Init Start_____");
        swarmAgents = new ArrayList<Agent>();
        currentStep = 0;
        // set random shared direction to leaders
        Double2D startPoint = new Double2D(random.nextDouble(),  random.nextDouble());
        Double2D endPoint = new Double2D(random.nextDouble(), random.nextDouble());
        leadersDirection = AgentMovementCalculator.getDirectionBetweenPoints(startPoint, endPoint);

        for (int x = 0; x < numAgents; x++) {
            Agent agent = new Agent();
            locateAgent(agent);
            swarmAgents.add(agent);
        }
        System.out.println("_____Init 1_____");

        switch(leaderPositioningApproach) {
            case GA:
                leadersInitLoc = LeaderPositioning.initializeLeadersPositionsGAApproach(this, leadersDirection);
                break;
            case Intersection:
            case IndirectIntersection:
                leadersInitLoc = LeaderPositioning.initializeLeadersPositionsIntersectionsApproach(this, leadersDirection);
                break;
            case Error:
                leadersInitLoc = LeaderPositioning.initializeLeadersPositionsErrorApproach(this, leadersDirection);
                break;
            case Graph:
                leadersInitLoc = LeaderPositioning.initializeLeadersPositionsGraphApproach(this, leadersDirection);
                break;
            default:
                leadersInitLoc = LeaderPositioning.initializeLeadersPositionsRandomly(this, leadersDirection);
        }
        System.out.println("_____Init End_____");
    }

    //endregion

    public void start() {
        super.start();

        // set up the agents field
        agents = new Continuous2D(width, width, height);
        leaderAgents = new ArrayList<Leader>();
        if (swarmType == SwarmType.Flocking)
            AgentMovementCalculator.setInstance(new FlockingAgentMovementCalculator());

        // make a bunch of agents
        for (int x = 0; x < numLeaders; x++) {
            Leader leader = new Leader();
//            locateLeader(leader, leadersDirection);
            leaderAgents.add(leader);
        }

        locateLeadersInPositions(leadersDirection, leadersInitLoc);


        putAndScheduleAgentsInScreen();
    }

    //region Locate Agents

    private void putAndScheduleAgentsInScreen() {
        for (Leader leader: leaderAgents) {
            agents.setObjectLocation(leader, leader.position.loc);
            schedule.scheduleRepeating(schedule.EPOCH, 1, leader);
        }
        for (Agent agent: swarmAgents) {
            agents.setObjectLocation(agent, agent.position.loc);
            schedule.scheduleRepeating(schedule.EPOCH, 1, agent);
        }
    }

    private void locateLeadersInPositions(Double2D leadersDirection, List<Double2D> selectedAgentsLoc) {
        for (int j = 0; j < numLeaders; j++) {
            Leader leader = leaderAgents.get(j);
            Double2D leadersPos = new Double2D(selectedAgentsLoc.get(j).x, selectedAgentsLoc.get(j).y);
            leader.position = new AgentPosition(
                    leadersPos, leadersPos.subtract(leadersDirection.multiply(jump)));
            leader.currentPhysicalPosition = new AgentPosition(leader.position);
        }
    }

    private void locateAgent(BaseAgent agent) {
        Double2D lastLoc = new Double2D(random.nextDouble() * width, random.nextDouble() * height);
        Double2D loc = new Double2D(random.nextDouble() * width,  random.nextDouble() * height);

        agent.position = new AgentPosition(loc, lastLoc);
        agent.currentPhysicalPosition = new AgentPosition(loc, lastLoc);
    }

//    private void locateAgent(BaseAgent agent, int x) {
//        Double2D lastLoc;
//        Double2D loc;
//        switch(x) {
//            case 0: case 1: case 2: case 3: case 4:
//                loc = new Double2D((x+3) * 10,  height / 2 - 20);
//                break;
//            default:
//                loc = new Double2D(x * 10,  height / 2 + 20);
//        }
//
//        switch(x) {
//            case 0: case 4:
//                lastLoc = new Double2D(loc.x, loc.y + 3);
//                break;
//            case 3:
//                lastLoc = new Double2D(loc.x - 2, loc.y - 1);
//                break;
//            case 1:
//                lastLoc = new Double2D(loc.x + 1, loc.y - 5);
//                break;
//            case 2:
//                lastLoc = new Double2D(loc.x + 1 , loc.y );
//                break;
//            default:
//                lastLoc = new Double2D(loc.x + 1, loc.y + 5);
//        }
//
//        agent.position = new AgentPosition(loc, lastLoc);
//        agent.currentPhysicalPosition = new AgentPosition(loc, lastLoc);
//    }

//endregion

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
            double minDistanceFromLeader = Integer.MAX_VALUE;
            for(Leader leader: leaderAgents) {
                double dis = AgentMovementCalculator.getDistanceBetweenPoints(leader.position.loc, agent.position.loc);
                if ( dis < minDistanceFromLeader)
                minDistanceFromLeader = dis;
            }
            if(minDistanceFromLeader > sight_radius_v * 2)
                lostCounter++;
        }
        return lostCounter / (double) numAgents;
    }

    public boolean swarmReachedGoal() {
        List<Double> distances = getSwarmDistances();
        Double2D[] convexHull = ConvexHull.coverPolygon(getAgentsLoc(), numAgents);
        System.out.println(getMean(distances));
        currentAreaCoverage = ConvexHull.polygonArea(convexHull) / (height * width);
        currentAvgNearestNeighborDis = getMean(distances);
        return (currentAreaCoverage >= 0.7 &&  currentAvgNearestNeighborDis >= 50);
    }

    private Double2D[] getAgentsLoc() {
        Double2D[] locations = new Double2D[numAgents];
        for(int i = 0; i < numAgents; i++)
            locations[i] = swarmAgents.get(i).position.loc;
        return locations;
    }

    public List<Double> getSwarmDistances() {
        List<Double> distances = new ArrayList<>();
        if(swarmAgents == null) return distances;
            for (Agent agent : swarmAgents) {
                double minDis = width;
                for (Agent agent2 : swarmAgents) {
                    if (agent == agent2) continue;
                    double dis = AgentMovementCalculator.getDistanceBetweenPoints(
                            agent.position.loc, agent2.position.loc);
                    if(dis < minDis) minDis = dis;
//                    distances.add(dis);
                }
                distances.add(minDis);
            }

        return distances;
    }

    double getMean(List<Double> data) {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum / data.size();
    }

    double getVariance(List<Double> data) {
        double mean = getMean(data);
        double temp = 0;
        for(double a : data)
            temp += (a - mean)*(a - mean);
        return temp / (data.size() - 1);
    }

    double getStdDev(List<Double> data) {
        return Math.sqrt(getVariance(data));
    }


    //endregion

    public static void main(String[] args) {
        doLoop(SignalingSwarmGame.class, args);
        System.exit(0);
    }

}
