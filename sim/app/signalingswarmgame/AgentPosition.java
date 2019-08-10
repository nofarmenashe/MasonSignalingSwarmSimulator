package sim.app.signalingswarmgame;

import sim.util.Double2D;

public class AgentPosition {
    public Double2D lastLoc = new Double2D(0, 0);
    public Double2D loc = new Double2D(0, 0);

    public AgentPosition(){}

    public AgentPosition(Double2D loc, Double2D lastLoc){
        this.loc = loc;
        this.lastLoc = lastLoc;
    }

    public AgentPosition(AgentPosition pos){
        this.loc = pos.loc;
        this.lastLoc = pos.lastLoc;
    }

    public Double2D getMovementDirection() {
        return AgentMovementCalculator.getDirectionBetweenPoints(lastLoc, loc);
    }

    public void updatePosition(Double2D nextLoc) {
        Double2D prevLoc = new Double2D(loc.x, loc.y);
        loc = nextLoc;
        lastLoc = prevLoc;
    }

    public void updatePosition(double jump) {
        Double2D prevLoc = new Double2D(loc.x, loc.y);
        loc = loc.add(getMovementDirection().multiply(jump));
        lastLoc = prevLoc;
    }
}
