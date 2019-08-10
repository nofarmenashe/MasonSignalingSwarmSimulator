/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;
import sim.engine.*;
import sim.util.*;

public abstract class BaseAgent implements Steppable, sim.portrayal.Oriented2D {
    private static final long serialVersionUID = 1;

    public AgentPosition currentPhysicalPosition = new AgentPosition();
    public AgentPosition position = new AgentPosition();

    public double orientation2D() {
        return Math.atan2(position.loc.y - position.lastLoc.y, position.loc.x - position.lastLoc.x);
    }

    public BaseAgent(){
    }

    public BaseAgent(Double2D loc, Double2D lastLoc){
       position = new AgentPosition(loc, lastLoc);
       currentPhysicalPosition = new AgentPosition(loc, lastLoc);
    }

    public abstract void step(SimState state);

//    protected Double2D getNextStepLocation(SignalingSwarmGame swarm, Double2D currentLoc){
//        return getNextStepLocation(swarm, position.getMovementDirection(), currentLoc);
//    }


    protected Double2D getNextStepLocation(SignalingSwarmGame swarm, Double2D nextDirection, Double2D currentLoc){
        return currentLoc.add(nextDirection.multiply(swarm.jump));
    }
}
