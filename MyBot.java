import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class MyBot {

    private static PrintWriter log;
    
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
    }
    
    // Output message to log file
    public static void log(String msg) {
        logf("%s\n", msg);
    }
    
    // Output message to log file with formatting
    public static void logf(String format, Object... args) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        log.print(String.format("<"+timeStamp+"> " + format, args));
    }

    public static void main(String[] args) {
        try {
            log = new PrintWriter(new FileWriter("mylog.txt"));
        } catch (Exception e) {
            // Can't output
        }
        
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
    	    // Owned.
    	}
    	
    	try {
    	    log.close();
    	} catch (Exception e) {
    	    // Can't output
    	}
    }
}

