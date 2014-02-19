import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class MyBot {

    //private static PrintWriter log;
    private static PrintStream log;
    private static boolean debug = true;
    
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

        int futureSteps = 30; // 30
        int fractions = 5; // (i.e. 5 = 1/5, 2/5, 3/5, 4/5, 5/5)

        World curWorld = new World(pw);
        curWorld.timeStep(futureSteps);

        // Have each planet expand to nearby planets
        for (Planet source : pw.MyPlanets()) {
            Planet dest = null;
            double bestScore = Double.MIN_VALUE;
            int fleetSize = 0;
            
            for (Planet neut : pw.Planets()) {
                for (int i = 1; i < fractions; i++) {
                    World w = new World(pw);
                    w.addFleet(source.PlanetID(), neut.PlanetID(), (i*source.NumShips())/fractions);
                    w.timeStep(futureSteps);
                    double score = w.score();
                
                    if (score > bestScore) {
                        bestScore = score;
                        dest = neut;
                        fleetSize = (i*source.NumShips())/fractions;
                    }
                }
            }
            
            if (source != null && dest != null && bestScore > curWorld.score() && fleetSize > 0) {
                World w = new World(pw);
                if (bestScore > w.score()) {
                    pw.IssueOrder(source, dest, fleetSize);
                }
            }
        }
    }
    
    // Output message to log file
    public static void log(Object msg) {
        if (debug) {
            logf("%s\n", msg);
        }
    }
    
    // Output error stacktrace to log file
    public static void loge(Exception e) {
        if (debug) {
            e.printStackTrace();
        }
    }
    
    // Output message to log file with formatting
    public static void logf(String format, Object... args) {
        if (debug) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            log.print(String.format("<"+timeStamp+"> " + format, args));
        }
    }

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
    
    /*
     * A class that acts as a duplicate of the game world in order to make
     * modifications and predict future outcomes. These worlds can be scored and
     * compared using a measure that will be tweaked over time to determine the
     * best possible outcomes.
     */
    private static class World {
        
        private PlanetWars pw;
        private Planet[] planets;
        private ArrayList<Fleet> fleets;
        
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
        
        public int score() {
            int[] scores = new int[3];
            
            for (Planet p : planets) {
                scores[p.Owner()] += p.NumShips();
            }
            
            for (Fleet f : fleets) {
                // scores[f.Owner()] += f.NumShips();
            }
            
            return scores[1] - scores[2];
        }
    }
    
}

