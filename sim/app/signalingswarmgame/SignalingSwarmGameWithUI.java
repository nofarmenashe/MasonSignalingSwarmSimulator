/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.display.Console;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;

import javax.swing.*;
import java.awt.*;

import sim.portrayal.simple.*;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignalingSwarmGameWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
	public static PrintWriter simulationReportWriter;

	public static boolean isCurrentGameFinished;
    private String gameStatistics = "";
    
    private int currentStep;
    private int signalsCount;

    public static void main(String[] args) {
    	int n = 2;
    	int p = 5;

//		for (int p = 1; p < 10; p++) {
//			for (int n = 1; n < 20; n++) {
//				SignalingSwarmGameWithUI sgwui = new SignalingSwarmGameWithUI(n, p/10.0);
		SignalingSwarmGameWithUI sgwui = new SignalingSwarmGameWithUI(n, p / 10.0);
		Controller simConsole = sgwui.createController();  // randomizes by currentTimeMillis
    }

    public Object getSimulationInspectedObject() {
        return state;
    }  // non-volatile

    ContinuousPortrayal2D agentsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D signalsPortrayal = new ContinuousPortrayal2D();

	public SignalingSwarmGameWithUI(int n, double p) {
		super(new SignalingSwarmGame(System.currentTimeMillis(), n, p));
	}

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
        
        currentStep = 0;
		signalsCount = 0;

		String timestamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("dd_MM_yyyy HH_mm_ss"));
		File simulationReportFile = new File("Reports/simulationResults "+timestamp+".csv");

		try {
        	if(!simulationReportFile.getParentFile().exists())
                simulationReportFile.getParentFile().mkdirs();
			simulationReportWriter = new PrintWriter(simulationReportFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
      appendSimulatorParameters(state);
	  updateReportFile(state);
    }

    public void finish() {
    	super.finish();
		isCurrentGameFinished = true;
    }
    
    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }
    
    public boolean step() {
        boolean result = super.step();
        updatePortraylsColors();
        currentStep++;
        updateReportFile(super.state);
		SignalingSwarmGame swarm = (SignalingSwarmGame) super.state;
		if(((SignalingSwarmGame) state).isLeaderSignaled) signalsCount++;
		if(swarm.swarmReachedGoal() || currentStep >= 20000) {
			simulationReportWriter.close();
			finish();
		}


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

    private void appendSimulatorParameters(SimState state) {
    	SignalingSwarmGame swarm = (SignalingSwarmGame) state;
    	StringBuilder stringBuilder = new StringBuilder();

    	stringBuilder.append(String.format("Parameters\nModel,%c\nP,%.2f\nInitial Alpha,%.2f\nIndependent,%b\nLeader's Influence,%.2f\nPrev Step Weight,%.2f\n",
                                            swarm.model_v, swarm.p_signal_accecptness_v, swarm.initial_alpha_v,
                                            swarm.are_agents_independent_v, swarm.leader_influence_v, swarm.prevStepRate));

        stringBuilder.append("Step, agent, Signal?, lastX, lastY, X, Y, dirX, dirY, disFromLeader\n");
		simulationReportWriter.write(stringBuilder.toString());
    }

    private void updateReportFile(SimState state){
        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
            BaseAgent a = (BaseAgent)swarm.agents.allObjects.get(i);

            if(a instanceof Leader && currentStep != 0)
                continue;

            boolean signal = (a instanceof Leader)? swarm.isLeaderSignaled : ((Agent)a).isAgentAcceptSignalCorrectly;
            Double2D dir = a.position.getMovementDirection();
            double disFromLeader = (a instanceof Leader)? 0: AgentMovementCalculator.distanceFromGoal(swarm, (Agent)a);

            stringBuilder.append(String.format("%d,%d,%b,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f\n",
                    currentStep, i, signal, a.position.lastLoc.x, a.position.lastLoc.y,
                    a.position.loc.x, a.position.loc.y, dir.x, dir.y, disFromLeader));
        }
        simulationReportWriter.write(stringBuilder.toString());
        simulationReportWriter.flush();
    }
}
