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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SignalingSwarmGameWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    public static PrintWriter simulationReportWriter;

    public static PrintWriter simulationDistReportWriter;
    public static PrintWriter simulationSetReportWriter;

    private String gameStatistics = "";

    private int signalsCount;
    private int firstSignalStep;
    private Long sumStepsTime;
    private String[] agentsDistancesList;
    private String signalsList;

    public static int index = 129;

    public static void main(String[] args) throws InterruptedException {
//        int n = 15;
        int l = 1;
        int sight = 10;
        int dt = 30;
        while (index < 150) {
            System.out.println(index);
            SignalingSwarmGameWithUI sgwui = new SignalingSwarmGameWithUI();
            Controller simConsole = sgwui.createController();  // randomizes by currentTimeMillis
            for (int n = 5; n <= 30; n +=5) {
                for (int leaders = 1; leaders <= 5; leaders ++) {
                    sgwui.setParams(n, leaders, sight, LeaderPositioningApproach.Random, 1.0, l, dt);
                    ((Console) simConsole).pressPlay();
                    while (((Console) simConsole).getPlayState() != Console.PS_STOPPED) {
                    }
                }
//                    for (int p = 0; p <= 10; p += 2) {
//                        sgwui.setParams(n, 1, sight, LeaderPositioningApproach.Random, p / 10.0, l, dt);
//                    ((Console) simConsole).pressPlay();
//                    while (((Console) simConsole).getPlayState() != Console.PS_STOPPED) {
//                    }
            }

//                for(int p = 0; p <= 10; p +=2){
//                    sgwui.setParams(n, leaders, sight, LeaderPositioningApproach.Random, p / 10.0, l, dt);
//                    ((Console) simConsole).pressPlay();
//                    while (((Console) simConsole).getPlayState() != Console.PS_STOPPED) {
//                    }

//                    }
//                }
//            }
            System.out.println("finish round " + index);
            index++;
        }
    }

    public Object getSimulationInspectedObject() {
        return state;
    }  // non-volatile

    ContinuousPortrayal2D agentsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D signalsPortrayal = new ContinuousPortrayal2D();

    public void setParams(int n, int leaders, double sight, LeaderPositioningApproach posAlgo, double p, int l, int dt) {
        System.out.println("########Set Params#####");
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).steps_lookahead_v = l;
        ((SignalingSwarmGame) state).numLeaders = leaders;
        ((SignalingSwarmGame) state).setSightRadius(sight);
        ((SignalingSwarmGame) state).setSignalRadius(2 * sight);
        ((SignalingSwarmGame) state).leaderPositioningApproach = posAlgo;
        ((SignalingSwarmGame) state).dt = dt;
        ((SignalingSwarmGame) state).initSimulation();
        System.out.println("########End Set Params#####");


    }

    public void setParams(int n, int leaders, double sight, LeaderPositioningApproach posAlgo, double p, int l) {
        System.out.println("########Set Params#####");
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).steps_lookahead_v = l;
        ((SignalingSwarmGame) state).numLeaders = leaders;
        ((SignalingSwarmGame) state).setSightRadius(sight);
        ((SignalingSwarmGame) state).leaderPositioningApproach = posAlgo;
        ((SignalingSwarmGame) state).initSimulation();
        System.out.println("########End Set Params#####");


    }

    public void setParams(int n, int leaders, LeaderPositioningApproach posAlgo, double p, int l, double ngbrFactor) {
        System.out.println("########Set Params#####");
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).numLeaders = leaders;
        ((SignalingSwarmGame) state).setStepsLookahead(l);
        ((SignalingSwarmGame) state).leaderPositioningApproach = posAlgo;
        ((SignalingSwarmGame) state).setNeighborDiscountFactor(ngbrFactor);
        ((SignalingSwarmGame) state).initSimulation();
        System.out.println("########End Set Params#####");


    }

    public void setParams(int n, int leaders, LeaderPositioningApproach posAlgo, double p, int l) {
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).numLeaders = leaders;
        ((SignalingSwarmGame) state).setStepsLookahead(l);
        ((SignalingSwarmGame) state).leaderPositioningApproach = posAlgo;
        ((SignalingSwarmGame) state).initSimulation();

    }

    public void setParams(int n, double p, int l) {
        ((SignalingSwarmGame) state).setAcceptLeadersSignalCorrectly(p);
        ((SignalingSwarmGame) state).numAgents = n;
        ((SignalingSwarmGame) state).sight_size_v = n;
        ((SignalingSwarmGame) state).setStepsLookahead(l);
    }

    public SignalingSwarmGameWithUI() {
        super(new SignalingSwarmGame(System.currentTimeMillis()));
//        super(new SignalingSwarmGame(1462783447));
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
        firstSignalStep = 0;
        signalsCount = 0;
        sumStepsTime = Long.valueOf(0);
        agentsDistancesList = new String[((SignalingSwarmGame) state).numAgents];
        signalsList = ",,,";

        createReportsPrintWriter();

        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
//            try {
//                File snapshotFile = new File(String.format("newReports/snapshot_%d_%d_%.2f_%s.png",
//                        index, swarm.numLeaders, swarm.neighbor_discount_factor_v, swarm.leaderPositioningApproach.name()));
//                display.takeSnapshot(snapshotFile, 2);
//            } catch (IOException e) {
//                System.out.println("failed taking snapshot");
//            }
//        appendSimulatorParameters(state);
//        updateReportFile(state);
    }

    private void createReportsPrintWriter() {
//        String timestamp = LocalDateTime.now()
//                .format(DateTimeFormatter.ofPattern("dd_MM_yyyy HH_mm_ss"));
//        File simulationReportFile = new File("Reports/simulationResults "+ ((SignalingSwarmGame)state).steps_lookahead_v + " " + timestamp + ".csv");
//
//        try {
//            if (!simulationReportFile.getParentFile().exists())
//                simulationReportFile.getParentFile().mkdirs();
//            simulationReportWriter = new PrintWriter(simulationReportFile);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

//
//        try {
//            FileWriter simulationDistReportFileWriter = new FileWriter("Reports/simulationDistResults.csv", true);
//            simulationDistReportWriter = new PrintWriter(simulationDistReportFileWriter);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        try {
            FileWriter simulationSetReportFileWriter = new FileWriter("newReports/simulationSetResults.csv", true);
            simulationSetReportWriter = new PrintWriter(simulationSetReportFileWriter);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void finish() {
        super.finish();

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
        long avgStepTime = swarm.currentStep == 0 ? 0 : sumStepsTime / swarm.currentStep;
//        double convergancePercentage = swarm.convergencePercentage();
//        double lostPercentage = swarm.lostPercentage();

        StringBuilder sb = new StringBuilder(String.format("%d, %d, %d, %s, %.2f, %.2f, %d, %d, %.2f, %d, %d, %d, %d, %.2f, %.2f, %.2f\n",
                index,
                swarm.numAgents,
                swarm.numLeaders,
                swarm.leaderPositioningApproach.name(),
                swarm.getNeighborDiscountFactor(),
                swarm.getAcceptLeadersSignalCorrectly(),
                swarm.getStepsLookahead(),
                swarm.dt,
                swarm.getSightRadius() / swarm.width,
                firstSignalStep,
                signalsCount,
                swarm.currentStep,
                avgStepTime,
                swarm.currentAreaCoverage,
                swarm.currentAvgNearestNeighborDis / (1.5 * (swarm.width / Math.floor(Math.sqrt(swarm.numAgents)))),
                swarm.currentAvgNearestNeighborDis));
        simulationSetReportWriter.write(sb.toString());
//        for (Agent a: swarm.swarmAgents) {
//            StringBuilder sbAgent = new StringBuilder(String.format(" ,%.2f, %.2f\n",
//                   a.position.loc.x, a.position.loc.y));
//            simulationSetReportWriter.write(sbAgent.toString());
//        }
        simulationSetReportWriter.flush();
        simulationSetReportWriter.close();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public boolean step() {
        SignalingSwarmGame swarm = (SignalingSwarmGame) super.state;
        swarm.desiredLeaderLocations = DispersionUtilityCalculation.getTopUtilityPositions(swarm);
        long startTime = System.nanoTime();
        //Measure execution time for this method
        boolean result = super.step();
        long endTime = System.nanoTime();

        long timeElapsed = (endTime - startTime);  //in millis
        sumStepsTime += timeElapsed;

        updatePortrayalsColors();
//        if(currentStep == 10 || currentStep == 30){
//            try {
//                File snapshotFile = new File(String.format("newReports/snapshot_%d_%d_%s _%d.png",
//                        index, swarm.numLeaders, swarm.leaderPositioningApproach.name(), currentStep));
//                display.takeSnapshot(snapshotFile, 2);
//            } catch (IOException e) {
//                System.out.println("failed taking snapshot");
//            }
//        }
        swarm.currentStep++;
//        updateDistFile(state);
//        updateReportFile(super.state);

        if (swarm.currentStepSignalsCounter > 0) {
            signalsCount += swarm.currentStepSignalsCounter;

            if (firstSignalStep == 0)
                firstSignalStep = swarm.currentStep;

        }


        if (swarm.swarmReachedGoal() || swarm.currentStep >= 10000) {
            updateSimulationSetReportFile();
            try {
                File snapshotFile = new File(String.format("newReports/snapshot_%d_%d_%.2f_%.2f.png",
                        index, swarm.numAgents, swarm.sight_radius_v, swarm.p_signal_accecptness_v));
                display.takeSnapshot(snapshotFile, 2);
            } catch (IOException e) {
                System.out.println("failed taking snapshot");
            }
            finish();
        }

        // reset parameters for next step
        for (Agent a : swarm.swarmAgents)
            a.influencingLeader = null;
        swarm.currentStepSignalsCounter = 0;


        return result;
    }

    private void updateDistFile(SimState state) {
        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
        signalsList += swarm.currentStepSignalsCounter + ",";
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
            String name = swarm.agents.allObjects.objs[x].toString();
            name = name.substring(name.lastIndexOf('@'));
            int shape = OrientedPortrayal2D.SHAPE_COMPASS;

            if (swarm.agents.allObjects.objs[x] instanceof Leader)
                shape = OrientedPortrayal2D.SHAPE_KITE;

            SimplePortrayal2D basic = new TrailedPortrayal2D(
                    this,
                    new OrientedPortrayal2D(
                            new SimplePortrayal2D(), 0, 2.0, color,
                            shape), agentsPortrayal, 100);
            agentsPortrayal.setPortrayalForObject(swarm.agents.allObjects.objs[x],
                    new AdjustablePortrayal2D(
                            new MovablePortrayal2D(
                                    new CircledPortrayal2D(
                                            new LabelledPortrayal2D(basic, name), 0, swarm.agents.allObjects.objs[x] instanceof Leader? swarm.signal_radius_v * 2: swarm.sight_radius_v * 2, Color.BLUE, false))));
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
            if (swarm.agents.allObjects.objs[x] instanceof Leader) {
                shape = OrientedPortrayal2D.SHAPE_KITE;
                Leader leader = (Leader) swarm.agents.allObjects.objs[x];
                if (leader.isLeaderSignaled)
                    color = Color.green;
                else
                    color = Color.red;
            } else {
                Agent agent = (Agent) swarm.agents.allObjects.objs[x];
                if (agent.influencingLeader != null) {
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

    //Todo: need to fix to support multiple leaders

//    private void appendSimulatorParameters(SimState state) {
//        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
//        StringBuilder stringBuilder = new StringBuilder();
//
//        stringBuilder.append(String.format("Parameters\nP,%.2f\nInitial Alpha,%.2f\nIndependent,%b\n" +
//                        "Leader's Influence,%.2f\nPrev Step Weight,%.2f\nLookahead steps,%d\n",
//                swarm.p_signal_accecptness_v,
//                swarm.initial_alpha_v,
//                swarm.are_agents_independent_v,
//                swarm.leader_influence_v,
//                swarm.prevStepRate,
//                swarm.steps_lookahead_v));
//
//        stringBuilder.append("Step, agent, Signal?, lastX, lastY, X, Y, dirX, dirY, disFromLeader, signal utility, no signal utility\n");
//        simulationReportWriter.write(stringBuilder.toString());
//    }
//    private void updateReportFile(SimState state) {
//        SignalingSwarmGame swarm = (SignalingSwarmGame) state;
//        StringBuilder stringBuilder = new StringBuilder();
//
//        stringBuilder.append(String.format(",,%b,,,,,,,,%.4f,%.4f\n", swarm.currentStepSignalsCounter,
//                swarm.leaderAgent.totalSignalUtility, swarm.leaderAgent.totalNoSignalUtility));
//
//        for (int i = 0; i < swarm.agents.allObjects.numObjs; i++) {
//            BaseAgent a = (BaseAgent) swarm.agents.allObjects.get(i);
//
//            if (a instanceof Leader && currentStep != 0)
//                continue;
//
//            boolean signal = (a instanceof Leader) ? swarm.isLeaderSignaled : ((Agent) a).isAgentAcceptSignalCorrectly;
//            Double2D dir = a.position.getMovementDirection();
//            double disFromLeader = (a instanceof Leader) ? 0 : AgentMovementCalculator.distanceFromGoal(swarm, (Agent) a);
//
//            stringBuilder.append(String.format("%d,%d,%b,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f\n",
//                    currentStep, i, signal, a.position.lastLoc.x, a.position.lastLoc.y,
//                    a.position.loc.x, a.position.loc.y, dir.x, dir.y, disFromLeader));
//        }
//        simulationReportWriter.write(stringBuilder.toString());
//        simulationReportWriter.flush();
//    }

}
