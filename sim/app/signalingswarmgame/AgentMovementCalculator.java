package sim.app.signalingswarmgame;

import sim.util.Double2D;

import java.util.List;

public abstract class AgentMovementCalculator {
    public static final double EPSILON = 0.000000001;

    private static AgentMovementCalculator calculatorInstance;

    //region Abstract Method

    abstract Double2D agentNextDirectionByState(SignalingSwarmGame swarm,
                                              Agent agent,
                                              AgentState state);

    abstract List<BaseAgent> agentNeighborsByState(SignalingSwarmGame swarm,
                                                BaseAgent agent,
                                                AgentState state,
                                                boolean filterLeaders);

    abstract List<BaseAgent> agentNeighborsByIntersection(SignalingSwarmGame swarm,
                                                   BaseAgent agent,
                                                   boolean filterLeaders);

    abstract boolean checkStopCriteria(SignalingSwarmGame swarm, Agent agent);

    abstract double distanceFromLeader(SignalingSwarmGame swarm, Agent agent);

    //endregion

    //region External Methods

    public static Double2D getAgentNextDirectionByState(SignalingSwarmGame swarm, BaseAgent agent, AgentState state){
        return (agent instanceof Leader)?
                agent.position.getMovementDirection():
                getInstance().agentNextDirectionByState(swarm,(Agent)agent,state);
    }

    public static List<BaseAgent> getAgentIntersectingNeighbors(SignalingSwarmGame swarm, BaseAgent agent, boolean filterLeaders){
        return getInstance().agentNeighborsByIntersection(swarm,agent, filterLeaders);
    }

    public static List<BaseAgent> getAgentNeighbors(SignalingSwarmGame swarm, BaseAgent agent, boolean filterLeaders){
        return getInstance().agentNeighborsByState(swarm,agent,AgentState.NoSignal,filterLeaders);
    }

    public static List<BaseAgent> getAgentNeighbors(SignalingSwarmGame swarm, BaseAgent agent, AgentState state){
        return getInstance().agentNeighborsByState(swarm,agent,state,false);
    }

    public static boolean isAgentReachedGoal(SignalingSwarmGame swarm, Agent agent){
        return getInstance().checkStopCriteria(swarm, agent);
    }

    public static double distanceFromGoal(SignalingSwarmGame swarm, Agent agent){
        return getInstance().distanceFromLeader(swarm, agent);
    }

    public static Double2D getAgentNextPositionByState(SignalingSwarmGame swarm, BaseAgent agent, AgentState state){
        Double2D nextDir = getAgentNextDirectionByState(swarm, agent, state);

//        Agent nextAgent = new Agent();
//        nextAgent.position = newagent.loc;
        Double2D nextLoc = agent.position.loc.add(nextDir.multiply(swarm.jump));
//        nextAgent.lastD = new Double2D(nextAgent.loc.x - nextAgent.lastLoc.x, nextAgent.loc.y - nextAgent.lastLoc.y);

        return nextLoc;
    }

    public static Double2D getDirectionToNeighbor(BaseAgent agent, BaseAgent neighbor){
        Double2D directionToNeighbor = neighbor.position.loc.subtract(agent.position.loc);

        return getNormalizedVector(directionToNeighbor);
    }


    public static Double2D getNormalizedVector(Double2D vector) {
        double vectorLength = getDistanceBetweenPoints(new Double2D(0,0), vector);

        if(vectorLength == 0) vectorLength = EPSILON;

        return new Double2D(vector.x / vectorLength, vector.y / vectorLength);
    }

    public static double calculateAngleBetweenDirections(Double2D direction1, Double2D direction2){
        double dotProduct = (direction1.x * direction2.x) + (direction1.y * direction2.y);

        double angle = Math.acos(dotProduct);
        return Double.isNaN(angle) ? 0 : angle;
    }

    public static double getVectorLength(Double2D vector) {
        return getDistanceBetweenPoints(new Double2D(0,0), vector);
    }

//    public static double calculateAngleBetweenAgentAndDirectionToOther(Double2D direction, BaseAgent otherAgent, SignalingSwarmGame swarm){
//        Double2D otherLoc = swarm.agents.getObjectLocation(otherAgent);
//
//        Double2D directionToOther = getDirectionBetweenPoints(loc, otherLoc);
//
//        double angle = calculateAngleBetweenDirections(direction, directionToOther);
//
//        return angle;
//    }

//    public static Double2D getDirectionToNeighbor(BaseAgent neighbor) {
//        Double2D directionToNeighbor = neighbor.loc.subtract(loc);
//
//        return getNormalizedVector(directionToNeighbor);
//    }

    protected static Double2D getDirectionBetweenPoints(Double2D fromLoc, Double2D toLoc){
        Double2D direction = new Double2D(toLoc.x - fromLoc.x, toLoc.y - fromLoc.y);
        double dis = Math.sqrt(Math.pow(direction.x, 2) + Math.pow(direction.y, 2));

        if (dis == 0) dis = EPSILON;

        return new Double2D(direction.x / dis, direction.y / dis);
    }

    public static double getDistanceBetweenPoints(Double2D firstPoint, Double2D secondPoint) {
        return Math.sqrt(Math.pow(secondPoint.x - firstPoint.x, 2) +
                Math.pow(secondPoint.y - firstPoint.y, 2));

    }

    //endregion

    //region Singleton

    public static void setInstance(AgentMovementCalculator otherCalculator){
        calculatorInstance = otherCalculator;
    }

    private static AgentMovementCalculator getInstance(){
        if(calculatorInstance == null)
            calculatorInstance = new FlockingAgentMovementCalculator(); // default implementation for now

        return calculatorInstance;
    }

    //endregi
}
