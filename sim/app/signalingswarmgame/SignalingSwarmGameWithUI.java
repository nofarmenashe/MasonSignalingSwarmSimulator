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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignalingSwarmGameWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    
    public static PrintWriter writeToFile;
    public static StringBuilder stringBuilder = new StringBuilder();
    private String gameStatistics = "";
    
    private int currentStep;

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
        
        currentStep = 0;
		String timestamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("dd_MM_yyyy HH_mm_ss"));
		File f = new File("Reports/simulationResults "+timestamp+".csv");
        try {
        	if(!f.getParentFile().exists())
        		f.getParentFile().mkdirs();
			writeToFile = new PrintWriter(f);
			stringBuilder = new StringBuilder();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        appendSimulatorParameters(super.state);
		appendHeaders();
		updateResultFile(super.state);
    }

    public void finish() {
    	super.finish();
    	try {
			new PrintWriter(
					new File("Reports/simulationResults.csv")).write(new StringBuilder().append(this.gameStatistics + this.currentStep).toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	
    	if(writeToFile != null) {
            writeToFile.write(stringBuilder.toString());
            writeToFile.flush();
            writeToFile.close();
    	}
    }
    
    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }
    
    public boolean step() {
        boolean result = super.step();
        updatePortraylsColors();
        updateResultFile(super.state);
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
    
    private static void appendHeaders() {
    	stringBuilder.append("Step Number");
    	stringBuilder.append(",");
    	stringBuilder.append("leader signal");
    	stringBuilder.append(",");
    	stringBuilder.append("accept signal");
    	stringBuilder.append(",");
    	stringBuilder.append("loc x");
    	stringBuilder.append(",");
    	stringBuilder.append("loc y");
    	stringBuilder.append(",");
    	stringBuilder.append("direction x");
    	stringBuilder.append(",");
    	stringBuilder.append("direction y");
    	stringBuilder.append(",");
    	stringBuilder.append("oreintation angle - signal");
    	stringBuilder.append(",");
    	stringBuilder.append("attraction angle -signal");
    	stringBuilder.append(",");
    	stringBuilder.append("oreintation angle - no signal");
    	stringBuilder.append(",");
    	stringBuilder.append("attraction angle -no signal");
    	stringBuilder.append(",");
    	stringBuilder.append("signal utility");
    	stringBuilder.append(",");
    	stringBuilder.append("unsignal utility");
    	stringBuilder.append("\n");
    }
    
    private void appendSimulatorParameters(SimState state) {
    	SignalingSwarmGame swarm = (SignalingSwarmGame) state;
    	
    	this.gameStatistics = swarm.numAgents + "," + swarm.p_signal_accecptness_v + ",";
    	
    	stringBuilder.append("Parameters");
    	stringBuilder.append("\n");
    	
    	stringBuilder.append("Model");
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.model_v);
    	stringBuilder.append("\n");
    	
    	stringBuilder.append("P");
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.p_signal_accecptness_v);
    	stringBuilder.append("\n");
    	
    	stringBuilder.append("Initial Alpha");
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.initial_alpha_v);
    	stringBuilder.append("\n");
    	
    	stringBuilder.append("Independent");
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.are_agents_independent_v);
    	stringBuilder.append("\n");
    	
    	stringBuilder.append("Leader's Influence");
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.leader_influence_v);
    	stringBuilder.append("\n");
    }
    
    private void updateResultFile(SimState state) {
    	SignalingSwarmGame swarm = (SignalingSwarmGame) state;
    	
    	stringBuilder.append(currentStep++);
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.isLeaderSignaled);
    	stringBuilder.append(",");
    	stringBuilder.append("");
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.leaderAgent.loc.x);
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.leaderAgent.loc.y);
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.leaderAgent.getMovementDirection().x);
    	stringBuilder.append(",");
    	stringBuilder.append(swarm.leaderAgent.getMovementDirection().y);
    	stringBuilder.append("\n");
    	
    	for(int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
    		if(swarm.agents.allObjects.get(i) == swarm.leaderAgent) continue;
    		Agent a = (Agent)swarm.agents.allObjects.get(i);
    		stringBuilder.append("");
        	stringBuilder.append(",");
        	stringBuilder.append("");
        	stringBuilder.append(",");
        	stringBuilder.append(a.isAgentAcceptSignalCorrectly);
        	stringBuilder.append(",");
        	stringBuilder.append(a.loc.x);
        	stringBuilder.append(",");
        	stringBuilder.append(a.loc.y);
        	stringBuilder.append(",");
        	stringBuilder.append(a.getMovementDirection().x);
        	stringBuilder.append(",");
        	stringBuilder.append(a.getMovementDirection().y);
        	stringBuilder.append(",");
        	stringBuilder.append(a.calculateAngleBetweenDirections(a.getDesiredDirection(a.getNeighbours(swarm, true), true, true), swarm.leaderAgent.getMovementDirection()));
        	stringBuilder.append(",");
        	stringBuilder.append(a.calculateAngleBetweenDirections(a.getDesiredDirection(a.getNeighbours(swarm, true), true, true), a.getDirectionToNeighbor(swarm.leaderAgent)));
        	stringBuilder.append(",");
        	stringBuilder.append(a.calculateAngleBetweenDirections(a.getDesiredDirection(a.getNeighbours(swarm, false), false, false), swarm.leaderAgent.getMovementDirection()));
        	stringBuilder.append(",");
        	stringBuilder.append(a.calculateAngleBetweenDirections(a.getDesiredDirection(a.getNeighbours(swarm, false), false, false), a.getDirectionToNeighbor(swarm.leaderAgent)));
        	stringBuilder.append(",");
        	stringBuilder.append(swarm.leaderAgent.getSignalingUtility(a, swarm));
        	stringBuilder.append(",");
        	stringBuilder.append(swarm.leaderAgent.getUnsignalingUtility(a, swarm));
        	stringBuilder.append("\n");
    	}
    	
    	if(currentStep % 5 == 0) {
    		writeToFile.write(stringBuilder.toString());
    		writeToFile.flush();
    		stringBuilder = new StringBuilder();
    	}
    	if(swarm.swarmReachedGoal() && writeToFile != null) {
            writeToFile.write(stringBuilder.toString());
            writeToFile.flush();
            writeToFile.close();
            finish();
    	}
    }

}
