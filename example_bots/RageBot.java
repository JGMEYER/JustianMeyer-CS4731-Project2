import java.util.*;

public class RageBot {
    public static void DoTurn(PlanetWars pw) {
	for (Planet source : pw.MyPlanets()) {
	    
	    // determine if enough ships to send
	    if (source.NumShips() < 10 * source.GrowthRate()) {
		    continue;
	    }
	    
	    // find closest enemy planet
	    Planet dest = null;
	    int bestDistance = 999999;
	    for (Planet p : pw.EnemyPlanets()) {
		    int dist = pw.Distance(source, p);
    		if (dist < bestDistance) {
    		    bestDistance = dist;
    		    dest = p;
    		}
	    }
	    
	    // send all resources to selected planet
	    if (dest != null) {
		    pw.IssueOrder(source, dest, source.NumShips());
	    }
	}
    }

    public static void main(String[] args) {
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
    }
}

