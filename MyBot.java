import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class MyBot {

    // Strategies to consider:
    // - check distance of enemy planet at start; if closer than any other
    //      planet, ragebot! (5, 6, 11, 5, 7) [SUCCESS]
    // - figure out a closing condition to finish off the enemy when
    //      you're way ahead [FAILED]
    // - reenforce planets closer to enemies (safely)
    // - wait out first turn to avoid getting ragebotted [FAILED]
    // - have planets determine ragedefend/greedysearch independently [FAILED]

    // Debug variables
    private static PrintStream log;
    private static boolean debug = true;
    
    // Useful variables (these may be removed later)
    private static int turn = 1;
    private static List<List<Integer>> closePlanets;
    
    // Start the game with rageDefend off as default
    private static boolean rageDefend = false;
    
    // Normal behavior variables
    private static final int FUTURE_STEPS = 30;
    
    // Minimax behavior variables
    private static final int MM_FUTURE_STEPS = 10;
    private static final int MY_FRAC = 5;
    private static final int ENEMY_FRAC = 3;
    
    // The DoTurn function is where your code goes. The PlanetWars object
    // contains the state of the game, including information about all planets
    // and fleets that currently exist. Inside this function, you issue orders
    // using the pw.IssueOrder() function. For example, to send 10 ships from
    // planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
    //
    // There is already a basic strategy in place here. You can use it as a
    // starting point, or you can throw it out entirely and replace it with
    // your own. Check out the tutorials and articles on the contest website at
    // http://www.ai-contest.com/resources.
    public static void DoTurn(PlanetWars pw) {
        
        int myProduction = 0;
        int enProduction = 0;
        
        // Engage "Rage Defend" (minimax) if planets start within 10 steps
        if (turn == 1) {
            int dist = pw.Distance(pw.MyPlanets().get(0).PlanetID(), pw.EnemyPlanets().get(0).PlanetID());
            if (dist < 10) rageDefend = true;
        }
        
        // Determine useful metrics for switching between states
        for (Planet p : pw.Planets()) {
            if (p.Owner() == 1) myProduction += p.GrowthRate();
            if (p.Owner() == 2) enProduction += p.GrowthRate();
        }
        boolean moreShips = (pw.NumShips(1) > pw.NumShips(2));
        boolean moreProd = (myProduction > enProduction);
        
        // Disengage "Rage Defend" if we gain an advantage over the opponent
        if (moreShips) {
            rageDefend = false;
        }
        
        // RAGE DEFEND
        // Use a modified minimax with alpha-beta pruning to determine the best
        // move for scenarios where both players start nearby each other
        if (rageDefend) {        
            try {
                World w = new World(pw);
            
                // Evaluate best action for each of the player's planets
                for (Planet myP : w.myPlanets()) {
                    MiniMaxNode mmn = minimax(w, myP, 1);
                    Action bestAction = mmn.action();
            
                    // Don't send fleets of size 0
                    if (bestAction.numShips > 0) {
                        pw.IssueOrder(bestAction.src().PlanetID(), bestAction.dest().PlanetID(), bestAction.numShips());
                    }
                }
            
            } catch (Exception e) {
                loge(e);
            }
        
        // GREEDY SEARCH
        // Have each of the player's planets simulate the sending some fractions
        // of its units to all other planets, then evaluate FUTURE_STEPS steps
        // into the future. The action, or lack thereof, that produces the
        // highest future score is chosen.
        } else {
            
            int fractions = 5; // (i.e. 5 = 1/5, 2/5, 3/5, 4/5, 5/5)

            World curWorld = new World(pw);
            int curScore = curWorld.score();
            logf("Turn: %d, Score:%d\n", turn, curScore);

            curWorld.timeStep(FUTURE_STEPS);

            // Have each planet expand to most advantageous planet
            for (Planet source : pw.MyPlanets()) {
                Planet dest = null;
                double bestScore = Double.MIN_VALUE;
                int fleetSize = 0;
                
                // Determine sending to which planet produces the highest score
                // FUTURE_STEPS into the future.
                for (Planet p : pw.Planets()) {
                    for (int i = 1; i < fractions; i++) {
                        World w = new World(pw);
                        w.addFleet(source.PlanetID(), p.PlanetID(), (i*source.NumShips())/fractions);

                        w.timeStep(FUTURE_STEPS);
                        double score = w.score();

                        if (score > bestScore) {
                            bestScore = score;
                            dest = p;
                            fleetSize = (i*source.NumShips())/fractions;
                        }
                    }
                }

                if (source != null && dest != null && bestScore > curWorld.score() && fleetSize > 0) {
                    pw.IssueOrder(source, dest, fleetSize);
                }
            }
        }
        
        turn++;
    }
    
    // Output message to log file
    private static void log(Object msg) {
        if (debug) {
            logf("%s\n", msg);
        }
    }
    
    // Output error stacktrace to log file
    private static void loge(Exception e) {
        if (debug) {
            e.printStackTrace();
        }
    }
    
    // Output message to log file with formatting
    private static void logf(String format, Object... args) {
        if (debug) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            log.print(String.format("<"+timeStamp+"> " + format, args));
        }
    }

    // Main logic loop
    public static void main(String[] args) {
        // Prepare log file for debug mode
        if (debug) {
            try {
                File file = new File("mylog.txt");
                
                PrintWriter writer = new PrintWriter(file); // clear file
                writer.close();
                
        		FileOutputStream fos = new FileOutputStream(file);
        		log = new PrintStream(fos);
    		
        		System.setErr(log);
            } catch (Exception e) {
            }
        }
        
        // GAME LOGIC, DO NOT TOUCH!
        int[][] forces = new int[9][3];
        log(forces[3][2]);
        
    	String line = "";
    	String message = "";
    	int c;
    	try {
    	    while ((c = System.in.read()) >= 0) {
    		switch (c) {
    		case '\n':
    		    if (line.equals("go")) {
    			    PlanetWars pw = new PlanetWars(message);
        			DoTurn(pw);
        		    pw.FinishTurn();
    			    message = "";
    		    } else {
        			message += line + "\n";
    		    }
    		    line = "";
    		    break;
    		default:
    		    line += (char)c;
    		    break;
    		}
    	    }
    	} catch (Exception e) {
    	}
    	
    	// Close debug log
    	if (debug) {
        	try {
        	    log.close();
        	} catch (Exception e) {
        	}
	    }
    }
    
    // Initiates modified minimax on specific planet
    private static MiniMaxNode minimax(World w, Planet myPlanet, int steps) {
        return minimax(w, myPlanet, null, 0, steps*2, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    // Performs a modified version of minimax that leverages alpha-beta pruning.
    // The method looks at all the possible movements a player's planet could
    // take from 0% to 100% of its ships by a step size of 1/MY_FRAC. Reactions
    // for each of the enemies planets are then taken into consideration on a
    // step size of 1/ENEMY_FRAC. Once a terminal node is reached, the model
    // looks MM_FUTURE_STEPS subtracted by maxDepth/2 steps (the steps already
    // evaluated) and calculates a score to be sent back up the tree. Every two
    // depth layers of the tree represent one step in the game, since both
    // players execute moves at the same time. Alpha-beta pruning is implemented
    // normally as a way to drastically reduce runtime.
    private static MiniMaxNode minimax(World w, Planet myPlanet, Action a, int depth, final int maxDepth, boolean maximizing, int alpha, int beta) {
        // Clone the world so we can work independently of other branches
        w = w.clone();
        
        // If terminal node, apply last action and score the modeled world
        if (depth == maxDepth) {
            w.addFleet(a);
            w.timeStep(MM_FUTURE_STEPS-maxDepth/2);
            return new MiniMaxNode(a, w.score());   
        }
        
        // Timestep world when we move forward a step in the tree
        if (depth > 0 && (depth % 2) == 0) {
            w.timeStep();
        }
        
        // MAX: Determine player's best action by maximixing player score
        if (maximizing) {
            Action bestAction = null;
            w.addFleet(a);
            
            maxloop:
            for (Planet otherP : w.notMyPlanets()) {
                for (int frac = 0; frac <= MY_FRAC; frac++) {
                    Action trialAction = new Action(myPlanet, otherP, (frac*myPlanet.NumShips())/MY_FRAC);
                    int trialScore = minimax(w, myPlanet, trialAction, depth+1, maxDepth, false, alpha, beta).score();
                    
                    if (trialScore > alpha) {
                        alpha = trialScore;
                        bestAction = trialAction;
                    }
                    
                    if (beta <= alpha) {
                        break maxloop;
                    }
                }
            }
            return new MiniMaxNode(bestAction, alpha);
        
        // MIN: Determine enemy's best action by minimizing player score
        } else {
            Action worstAction = null;
            w.addFleet(a);
            
            minloop:
            for (Planet enP : w.enemyPlanets()) {
                for (Planet otherP : w.notEnemyPlanets()) {
                    for (int frac = 0; frac <= ENEMY_FRAC; frac++) {
                        Action trialAction = new Action(enP, otherP, (frac*enP.NumShips())/ENEMY_FRAC);
                        int trialScore = minimax(w, myPlanet, trialAction, depth+1, maxDepth, true, alpha, beta).score();
                        
                        if (trialScore < beta) {
                            beta = trialScore;
                            worstAction = trialAction;
                        }
                        
                        if (beta <= alpha) {
                            break minloop;
                        }
                    }
                }
            }
            return new MiniMaxNode(worstAction, beta);
        }
    }
    
    /* //////////////////////////////////////////////////////////////////////
     * 
     * Action:
     * 
     * A class that stores basic information about a fleet action. Allows for
     * basic fleet manipulation without taking distances into account. Travel
     * times are calculated when attempting to execute the action on a World
     * model.
     * 
     * ////////////////////////////////////////////////////////////////////// */
    private static class Action {
        private Planet src;
        private Planet dest;
        private int numShips;
        
        public Action (Planet src, Planet dest, int numShips) { //throws IllegalActionException {
            this.src = src;
            this.dest = dest;
            this.numShips = numShips;
            
            /*if (numShips > src.NumShips() || src.Owner() == 0) {
                throw new IllegalActionException("Cannot send " + numShips
                    + " ships from planet " + src.PlanetID());
            }*/
        }
        
        public Planet src() {
            return src;
        }
        
        public Planet dest() {
            return dest;
        }
        
        public int numShips() {
            return numShips;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Src: " + src.PlanetID() + " ");
            sb.append("Dest: " + dest.PlanetID() + " ");
            sb.append("Ships: " + numShips);
            return sb.toString();
        }
    }
    
    /* //////////////////////////////////////////////////////////////////////
     * 
     * IllegalActionException:
     * 
     * Gets thrown whenever the player attempts to simulate sending ships from
     * a neutral planet or from a planet that has insufficient ships to complete
     * the request.
     * 
     * ////////////////////////////////////////////////////////////////////// */
    private static class IllegalActionException extends Exception {
       public IllegalActionException() { super(); }
       public IllegalActionException(String message) { super(message); }
       public IllegalActionException(String message, Throwable cause) { super(message, cause); }
       public IllegalActionException(Throwable cause) { super(cause); }
    }
    
    /* //////////////////////////////////////////////////////////////////////
     * 
     * MiniMaxNode:
     * 
     * A class that works as a means for passing data through a minimax
     * calculation. Does not represent a min/max node in the traditional sense.
     * 
     * ////////////////////////////////////////////////////////////////////// */
    private static class MiniMaxNode {
        private Action action;
        private int score;
        
        public MiniMaxNode(Action action, int score) {
            this.action = action;
            this.score = score;
        }
        
        public Action action() {
            return action;
        }
        
        public int score() {
            return score;
        }
    }
    
    /* //////////////////////////////////////////////////////////////////////
     * 
     * World:
     * 
     * A class that acts as a duplicate of the game world in order to make
     * modifications and predict future outcomes. These worlds can be scored and
     * compared using a measure that will be tweaked over time to determine the
     * best possible outcomes.
     * 
     * ////////////////////////////////////////////////////////////////////// */
    private static class World {
        
        private PlanetWars pw;
        private Planet[] planets;
        private ArrayList<Fleet> fleets;
        
        // Generates simulated world from a shallow copy of a PlanetWars object
        public World(PlanetWars pw) {
            this.pw = pw;
            
            planets = new Planet[pw.Planets().size()];
            fleets = new ArrayList<Fleet>();
            
            for (Planet p : pw.Planets()) {
                planets[p.PlanetID()] = (Planet) p.clone();
            }
            
            for (Fleet f : pw.Fleets()) {
                fleets.add((Fleet) f.clone());
            }
        }
        
        // Generates simulated world from a shallow copy of a World object
        private World(PlanetWars pw, Planet[] planets, ArrayList<Fleet> fleets) {
            this.pw = pw;
            
            this.planets = new Planet[planets.length];
            this.fleets = new ArrayList<Fleet>();
            
            for (Planet p : planets) {
                this.planets[p.PlanetID()] = (Planet) p.clone();
            }
            
            for (Fleet f : fleets) {
                this.fleets.add((Fleet) f.clone());
            }
        }
        
        // TODO error check this?
        // Adds a fleet to the world to simulate a possible move that may be
        // considered by the AI.
        public void addFleet(int srcPlanet, int destPlanet, int numShips) {
            int owner = planets[srcPlanet].Owner();
            int tripLength = pw.Distance(srcPlanet, destPlanet);
            
            Fleet f = new Fleet(owner, numShips, srcPlanet, destPlanet,
                                tripLength, tripLength);
            
            fleets.add(f);
            planets[srcPlanet].RemoveShips(numShips); // (needs error check)
        }
        
        public void addFleet(Action a) {
            if (a != null) { // null is valid input
                addFleet(a.src.PlanetID(), a.dest.PlanetID(), a.numShips);
            }
        }
        
        // Returns a shallow copy of the world
        public World clone() {
            return new World(pw, planets, fleets);
        }
        
        // Returns list of player's planets
        public List<Planet> myPlanets() {
    	    List<Planet> planets = new ArrayList<Planet>();
    	    for (Planet p : this.planets) {
    	        if (p.Owner() == 1) {
    	            planets.add(p);
    	        }
    	    }
    	    return planets;
    	}
    	
    	// Returns list of enemy's planets
    	public List<Planet> enemyPlanets() {
    	    List<Planet> planets = new ArrayList<Planet>();
    	    for (Planet p : this.planets) {
    	        if (p.Owner() >= 2) {
    	            planets.add(p);
    	        }
    	    }
    	    return planets;
    	}
    	
    	// Returns list of neutral and player planets
    	public List<Planet> notEnemyPlanets() {
    	    List<Planet> planets = new ArrayList<Planet>();
    	    for (Planet p : this.planets) {
    	        if (p.Owner() < 2) {
    	            planets.add(p);
    	        }
    	    }
    	    return planets;
    	}
    	
    	// Returns list of neutral and enemy planets
    	public List<Planet> notMyPlanets() {
    	    List<Planet> planets = new ArrayList<Planet>();
    	    for (Planet p : this.planets) {
    	        if (p.Owner() != 1) {
    	            planets.add(p);
    	        }
    	    }
    	    return planets;
    	}
    	
    	// Returns list of all planets
    	public List<Planet> planets() {
    	    List<Planet> planets = new ArrayList<Planet>();
    	    for (Planet p : this.planets) {
	            planets.add(p);
    	    }
    	    return planets;
    	}
        
        // Iterate world one turn into the future
        public void timeStep() {
            try {
                int[][] forces = new int[planets.length][3];
            
                // Generate new ships
                for (Planet p : planets) {
                    if (p.Owner() > 0) {
                        p.AddShips(p.GrowthRate());
                    }
                }
                
                // Update planet forces
                for (Planet p : planets) {
                    forces[p.PlanetID()][p.Owner()] += p.NumShips();
                }
                
                // Step fleets and apply fleet forces
                for (Iterator<Fleet> iterator = fleets.iterator(); iterator.hasNext(); ) {
                    Fleet f = iterator.next();
                    f.TimeStep();
                    if (f.TurnsRemaining() == 0) {
                        forces[f.DestinationPlanet()][f.Owner()] += f.NumShips();
                        iterator.remove();
                    }
                }
                
                // Handle Planet Captures
                for (int p = 0; p < planets.length; p++) {
                    int secondLargestIndex = 0;
                
                    if (forces[p][0] > forces[p][1]) {
                        if (forces[p][1] > forces[p][2]) {
                            secondLargestIndex = 1; // pl
                        } else if (forces[p][0] > forces[p][2]) {
                            secondLargestIndex = 2; // en
                        } else {
                            secondLargestIndex = 0; // ne
                        }
                    } else {
                        if (forces[p][0] > forces[p][2]) {
                            secondLargestIndex = 0; // ne
                        } else if (forces[p][1] > forces[p][2]) {
                            secondLargestIndex = 2; // en
                        } else {
                            secondLargestIndex = 1; // pl
                        }
                    }
                
                    int secondLargestForce = forces[p][secondLargestIndex];
                    forces[p][0] = Math.max(forces[p][0]-secondLargestForce, 0);
                    forces[p][1] = Math.max(forces[p][1]-secondLargestForce, 0);
                    forces[p][2] = Math.max(forces[p][2]-secondLargestForce, 0);
                
                    int largest = forces[p][0];
                    int winner = 0;
                    for (int i = 1; i < 3; i++) {
                        if (forces[p][i] > largest) {
                            winner = i;
                            largest = forces[p][i];
                        }
                    }
                    
                    if (forces[p][winner] != 0) {
                        planets[p].Owner(winner); // new owner
                    }
                    
                    planets[p].NumShips(largest);
                }
            
            } catch (Exception e) {
                loge(e);
            }
        }
        
        // Iterate world n turns into the future.
        public void timeStep(int n) {
            for (int i = 0; i < n; i++) {
                timeStep();
            }
        }
        
        // Return a score for this world instance.
        public int score() {
            int[] scores = new int[3];
            
            for (Planet p : planets) {
                scores[p.Owner()] += p.NumShips();
            }
            
            for (Fleet f : fleets) {
                scores[f.Owner()] += f.NumShips();
            }
            
            return scores[1] - scores[2];
        }
    }
    
}

