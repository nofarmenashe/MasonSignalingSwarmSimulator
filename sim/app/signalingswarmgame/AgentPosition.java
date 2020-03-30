package sim.app.signalingswarmgame;

import sim.util.Double2D;

public class AgentPosition {
    public Double2D lastLoc = new Double2D(0, 0);
    public Double2D loc = new Double2D(0, 0);

    public AgentPosition(){}

    public AgentPosition(Double2D loc, Double2D lastLoc){
        this.loc = new Double2D(loc.x, loc.y);
        this.lastLoc = new Double2D(lastLoc.x, lastLoc.y);
    }

    public AgentPosition(Double2D initLoc, Double2D direction, double jump){
        lastLoc = new Double2D(initLoc.x, initLoc.y);
        loc = lastLoc.add(direction.multiply(jump));
    }

    public AgentPosition(AgentPosition pos){
        loc = new Double2D(pos.loc.x, pos.loc.y);
        lastLoc = new Double2D(pos.lastLoc.x, pos.lastLoc.y);
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
