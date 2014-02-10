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

        /*World w = new World(pw);
        int score1 = w.scoreDifference();
        w.timeStep(10);
        int score2 = w.scoreDifference();
        logf("%d %d\n", score1, score2);*/

        // Have each planet expand to nearby planets
        for (Planet source : pw.MyPlanets()) {
            Planet dest = null;
            double bestScore = 0;
            
            for (Planet neut : pw.NeutralPlanets()) {
                //int score = neut.GrowthRate() - (int)Math.pow(pw.Distance(source.PlanetID(), neut.PlanetID()),2) - (int)Math.sqrt(neut.NumShips());
                double score = Math.pow(pw.Distance(source.PlanetID(), neut.PlanetID()),-1)*neut.GrowthRate() + Math.pow(neut.NumShips(),-1);
                if (score > bestScore) {
                    bestScore = score;
                    dest = neut;
                }
            }
            
            if (source != null && dest != null) {
                int fleetSize = source.NumShips()/10;
                
                /*int futureSteps = 20;
                int estMovesToCapture = dest.NumShips()/fleetSize;
                
                int dist = pw.Distance(source.PlanetID(), dest.PlanetID());
                int noMove = source.GrowthRate()*futureSteps;
                int move = source.GrowthRate()*futureSteps + dest.GrowthRate()*(futureSteps-dist) - dest.NumShips();
                
                if (move > noMove) {
                    pw.IssueOrder(source, dest, source.NumShips()/10);
                }*/
                
                World w = new World(pw);
                w.timeStep(10);
                
                World w2 = new World(pw);
                w2.addFleet(source.PlanetID(), dest.PlanetID(), fleetSize);
                w2.timeStep(10);
                
                if (w2.score() > w.score()) {
                    pw.IssueOrder(source, dest, fleetSize);
                }
            }
        }

	    /*
	    // (1) If we currently have a fleet in flight, just do nothing.
    	if (pw.MyFleets().size() >= 1) {
    	    return;
    	}
    	
    	// (2) Find my strongest planet.
    	Planet source = null;
    	double sourceScore = Double.MIN_VALUE;
    	for (Planet p : pw.MyPlanets()) {
    	    double score = (double)p.NumShips();
    	    if (score > sourceScore) {
    		    sourceScore = score;
        		source = p;
    	    }
    	}
    	// (3) Find the weakest enemy or neutral planet.
    	Planet dest = null;
    	double destScore = Double.MIN_VALUE;
    	for (Planet p : pw.NotMyPlanets()) {
    	    double score = 1.0 / (1 + p.NumShips());
    	    if (score > destScore) {
    		    destScore = score;
        		dest = p;
    	    }
    	}
    	// (4) Send half the ships from my strongest planet to the weakest
    	// planet that I do not own.
    	if (source != null && dest != null) {
    	    int numShips = source.NumShips() / 2;
    	    pw.IssueOrder(source, dest, numShips);
    	}
    	*/
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
        
        // Returns positive value if player is winning, negative if enemy is
        // winning, and zero if both parties share the same score. May not
        // always be an accurate measure of which party will win in the given
        // state.
        public int score() {
            int[] scores = new int[3];
            
            for (Planet p : planets) {
                scores[p.Owner()] += p.NumShips()+2*p.GrowthRate(); // value generation more
            }
            
            for (Fleet f : fleets) {
                scores[f.Owner()] += f.NumShips();
            }
            
            return scores[1] - scores[2];
        }
    }
    
}

