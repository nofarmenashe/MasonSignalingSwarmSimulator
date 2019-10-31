/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.signalingswarmgame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.*;
import sim.util.Double2D;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignalingSwarmGameWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    public static PrintWriter simulationReportWriter;
    public static PrintWriter simulationDistReportWriter;
    public static PrintWriter simulationSetReportWriter;

    public static boolean isCurrentGameFinished;
    private String gameStatistics = "";

    private int currentStep;
    private int firstSignalStep;
    private int signalsCount;
    private Long sumStepsTime;
    private String[] agentsDistancesList;
    private String signalsList;

    public static void main(String[] args) {
//        int n = 11;
//        int p = 8;
//        int l = 1;
//        int s = 2;

        SignalingSwarmGameWithUI sgwui = new SignalingSwarmGameWithUI();
//        sgwui.setParams(n, p / 10.0, l, s);
        Controller simConsole = sgwui.createController();  // randomizes by currentTimeMillis
//        for (int i = 0; i < 5; i++) {
            for (int p = 9; p > 0; p--) {
                for (int n = 1; n <= 20; n+=3) {
                    for (int s = 1; s <= n; s+=3) {
        for (int l = 1; l <= 6; l++) {
            sgwui.setParams(n, p / 10.0, l);
            ((Console) simConsole).pressPlay();
            while (((Console) simConsole).getPlayState() != Console.PS_STOPPED) {
            }
                        }
                    }
        }
//            }
//        }
    }

    public Object getSimulationInspectedObject() {
        return state;
    }  // non-volatile

    ContinuousPortrayal2D agentsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D signalsPortrayal = new ContinuousPortrayal2D();

    public void setParams(int n, double p, int l, int s) {
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).setStepsLookahead(l);
        ((SignalingSwarmGame) state).setSightSize(s);
    }

    public void setParams(int n, double p, int l) {
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).sight_size_v = n;
        ((SignalingSwarmGame) state).setStepsLookahead(l);
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
        firstSignalStep = 0;
        signalsCount = 0;
        sumStepsTime = Long.valueOf(0);
        agentsDistancesList = new String[((SignalingSwarmGame) state).numAgents];
        signalsList = ",,,";

        createReportsPrintWriter();
        appendSimulatorParameters(state);
        updateReportFile(state);
    }

    private void createReportsPrintWriter() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd_MM_yyyy HH_mm_ss"));
        File simulationReportFile = new File("Reports/simulationResults " + timestamp + ".csv");

        try {
            if (!simulationReportFile.getParentFile().exists())
                simulationReportFile.getParentFile().mkdirs();
            simulationReportWriter = new PrintWriter(simulationReportFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            FileWriter simulationSetReportFileWriter = new FileWriter("Reports/simulationSetResults.csv", true);
            simulationSetReportWriter = new PrintWriter(simulationSetReportFileWriter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            FileWriter simulationDistReportFileWriter = new FileWriter("Reports/simulationDistResults.csv", true);
            simulationDistReportWriter = new PrintWriter(simulationDistReportFileWriter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            FileWriter simulationSetReportFileWriter = new FileWriter("Reports/simulationSetResults.csv", true);
            simulationSetReportWriter = new PrintWriter(simulationSetReportFileWriter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void finish() {
        super.finish();
        updateSimulationSetReportFile();
        updateSimulationDistReportFile();

        isCurrentGameFinished = true;
    }

    private void updateSimulationDistReportFile() {
        SignalingSwarmGame swarm = (SignalingSwarmGame) super.state;
        simulationDistReportWriter.write(signalsList + "\n");
        for (int i = 0; i < agentsDistancesList.length; i++) {
            StringBuilder sb = new StringBuilder(String.format("%d, %.2f, %d, %s\n",
                    swarm.numAgents,
                    swarm.getAcceptLeadersSignalCorrectly(),
                    i + 1,
                    agentsDistancesList[i]));
            simulationDistReportWriter.write(sb.toString());
        }
        simulationDistReportWriter.write("\n");
        simulationDistReportWriter.flush();
        simulationDistReportWriter.close();
    }

    private void updateSimulationSetReportFile() {
        SignalingSwarmGame swarm = (SignalingSwarmGame) super.state;
        long avgStepTime = sumStepsTime / currentStep;
        double convergancePercentage = swarm.getConvergancePercentage();

        StringBuilder sb = new StringBuilder(String.format("%d, %.2f, %d, %d, %d, %d, %d, %d, %.2f\n",
                swarm.numAgents,
                swarm.getAcceptLeadersSignalCorrectly(),
                swarm.getStepsLookahead(),
                swarm.getSightSize(),
                firstSignalStep,
                signalsCount,
                currentStep,
                avgStepTime,
                convergancePercentage));
        simulationSetReportWriter.write(sb.toString());
        simulationSetReportWriter.flush();
        simulationSetReportWriter.close();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public boolean step() {
        long startTime = System.nanoTime();
        //Measure execution time for this method
        boolean result = super.step();
        long endTime = System.nanoTime();

        long timeElapsed = (endTime - startTime);  //in millis
        sumStepsTime += timeElapsed;

        updatePortrayalsColors();
        currentStep++;
        updateDistFile(state);
        updateReportFile(super.state);
        SignalingSwarmGame swarm = (SignalingSwarmGame) super.state;
        if (((SignalingSwarmGame) state).isLeaderSignaled) {
            signalsCount++;
            if (firstSignalStep == 0)
                firstSignalStep = currentStep;
        }
        if (swarm.swarmReachedGoal() || currentStep >= 100000) {
            simulationReportWriter.close();
            finish();
        }


        return result;
    }

    private void updateDistFile(SimState state) {
        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        signalsList += swarm.isLeaderSignaled + ",";
        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
            BaseAgent a = (BaseAgent) swarm.agents.allObjects.get(i);
            if (!(a instanceof Leader)) {
                double disFromLeader = (a instanceof Leader) ? 0 : AgentMovementCalculator.distanceFromGoal(swarm, (Agent) a);
                agentsDistancesList[i - 1] = (agentsDistancesList[i - 1] == null) ?
                        disFromLeader + "," : agentsDistancesList[i - 1] + disFromLeader + ",";
            }
        }
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
        updatePortrayalsColors();
        // reschedule the displayer
        display.reset();

        // redraw the display
        display.repaint();
    }

    public void updatePortrayalsColors() {
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

        stringBuilder.append(String.format("Parameters\nModel,%c\nP,%.2f\nInitial Alpha,%.2f\nIndependent,%b\n" +
                        "Leader's Influence,%.2f\nPrev Step Weight,%.2f\nLookahead steps,%d\n",
                swarm.model_v, swarm.p_signal_accecptness_v, swarm.initial_alpha_v,
                swarm.are_agents_independent_v, swarm.leader_influence_v, swarm.prevStepRate,

                swarm.steps_lookahead_v));

        stringBuilder.append("Step, agent, Signal?, lastX, lastY, X, Y, dirX, dirY, disFromLeader, signal utility, no signal utility\n");
        simulationReportWriter.write(stringBuilder.toString());
    }

    private void updateReportFile(SimState state) {
        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(String.format(",,%b,,,,,,,,%.4f,%.4f\n", swarm.isLeaderSignaled,
                swarm.leaderAgent.totalSignalUtility, swarm.leaderAgent.totalNoSignalUtility));

        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
            BaseAgent a = (BaseAgent) swarm.agents.allObjects.get(i);

            if (a instanceof Leader && currentStep != 0)
                continue;

            boolean signal = (a instanceof Leader) ? swarm.isLeaderSignaled : ((Agent) a).isAgentAcceptSignalCorrectly;
            Double2D dir = a.position.getMovementDirection();
            double disFromLeader = (a instanceof Leader) ? 0 : AgentMovementCalculator.distanceFromGoal(swarm, (Agent) a);

            stringBuilder.append(String.format("%d,%d,%b,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f\n",
                    currentStep, i, signal, a.position.lastLoc.x, a.position.lastLoc.y,
                    a.position.loc.x, a.position.loc.y, dir.x, dir.y, disFromLeader));
        }
        simulationReportWriter.write(stringBuilder.toString());
        simulationReportWriter.flush();
    }
}
