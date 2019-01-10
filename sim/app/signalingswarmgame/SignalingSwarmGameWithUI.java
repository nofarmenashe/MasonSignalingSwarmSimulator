/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;

import javax.swing.*;
import java.awt.*;

import sim.portrayal.simple.*;
import sim.portrayal.SimplePortrayal2D;


public class SignalingSwarmGameWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;

    public static void main(String[] args) {
        new SignalingSwarmGameWithUI().createController();  // randomizes by currentTimeMillis
    }

    public Object getSimulationInspectedObject() {
        return state;
    }  // non-volatile

    ContinuousPortrayal2D agentsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D signalsPortrayal = new ContinuousPortrayal2D();

    public SignalingSwarmGameWithUI() {
        super(new SignalingSwarmGame(System.currentTimeMillis()));
    }

    public SignalingSwarmGameWithUI(SimState state) {
        super(state);
    }

    public static String getName() {
        return "The Signaling Swarm Game";
    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public boolean step() {
        boolean result = super.step();
        updatePortraylsColors();
        return result;
    }

    public void setupPortrayals() {
        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        // obstacle portrayal needs no setup
        agentsPortrayal.setField(swarm.agents);
        trailsPortrayal.setField(swarm.agents);
        signalsPortrayal.setField(swarm.agents);
        Color color = Color.black;

        for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
            int shape = OrientedPortrayal2D.SHAPE_COMPASS;

            if (swarm.agents.allObjects.objs[x] == swarm.leaderAgent)
                shape = OrientedPortrayal2D.SHAPE_KITE;

            SimplePortrayal2D basic = new TrailedPortrayal2D(
                    this,
                    new OrientedPortrayal2D(
                            new SimplePortrayal2D(), 0, 2.0, color,
                            shape), agentsPortrayal, 100);
            agentsPortrayal.setPortrayalForObject(swarm.agents.allObjects.objs[x], new AdjustablePortrayal2D(new MovablePortrayal2D(basic)));
            trailsPortrayal.setPortrayalForObject(swarm.agents.allObjects.objs[x], basic);
            signalsPortrayal.setPortrayalForObject(swarm.agents.allObjects.objs[x], new OvalPortrayal2D(color));

        }
        updatePortraylsColors();
        // reschedule the displayer
        display.reset();

        // redraw the display
        display.repaint();
    }


    public void updatePortraylsColors() {
        SignalingSwarmGame swarm = (SignalingSwarmGame) state;

        for (int x = 0; x < swarm.agents.allObjects.numObjs; x++) {
            Color color = Color.black;
            int shape = OrientedPortrayal2D.SHAPE_COMPASS;
            if (swarm.agents.allObjects.objs[x] == swarm.leaderAgent) {
                shape = OrientedPortrayal2D.SHAPE_KITE;

                if (swarm.isLeaderSignaled)
                    color = Color.green;
                else
                    color = Color.red;
            } else {
                Agent agent = (Agent) swarm.agents.allObjects.objs[x];
                if (swarm.isLeaderSignaled) {
                    if (agent.isAgentAcceptSignalCorrectly)
                        color = Color.green;
                    else
                        color = Color.red;
                }
            }

            signalsPortrayal.setPortrayalForObject(swarm.agents.allObjects.objs[x], new OvalPortrayal2D(color, 2.0));
        }
        // reschedule the displayer
        display.reset();

        // redraw the display
        display.repaint();
    }

    public void init(Controller c) {
        super.init(c);

        // make the displayer
        display = new Display2D(500, 500, this);
        display.setClipping(false);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Swarmers");
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list

        displayFrame.setVisible(true);
        display.attach(trailsPortrayal, "Trails");
        display.attach(agentsPortrayal, "Swarmers");

        display.attach(signalsPortrayal, "Signals");

        display.setBackdrop(Color.white);
    }

    public void quit() {
        super.quit();

        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

}
