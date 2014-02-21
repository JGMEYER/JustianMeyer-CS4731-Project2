
import java.io.*;
import java.util.Scanner;
import java.text.DecimalFormat;

public class MyBotTester {

    // solution - run all possibilities in bot_test.sh -> "bash bot_tester.sh &> testlog.txt", then parse testlog.txt

	public static void main(String[] args) {
        int maps[] = {1, 60};
        int max_turn_time = 1000;
        int max_num_turns = 200;
        String log_title = "testlog.txt";
        
        try {
            //String[] cmd = {"bash","bot_tester.sh","&>","testlog.txt"};
            //execCmd(cmd);
            
            for (int i = 1; i <= 100; i++) {
                File map = new File("maps/map"+i+".txt");
                Scanner scan = new Scanner(map);
                String line;
                double px=0, py=0, ex=0, ey=0;
                
                while (scan.hasNext()) {
                    line = scan.nextLine();
                    String[] split = line.split(" ");
                    
                    if (split.length == 6) {
                        if (split[3].equals("1")) {
                            px = Double.parseDouble(split[1]);
                            py = Double.parseDouble(split[2]);
                        } else if (split[3].equals("2")) {
                            ex = Double.parseDouble(split[1]);
                            ey = Double.parseDouble(split[2]);
                        }
                    }
                }
                 
                double dx = px-ex;
                double dy = py-ey;
                int dist = (int)Math.ceil(Math.sqrt(dx*dx + dy*dy));
                
                System.out.printf("Map%d: %d\n", i, dist);
            }
            
            File testlog = new File(log_title);
            calcWinLoss(testlog);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
	}
	
	public static void calcWinLoss(File testlog) throws FileNotFoundException {
	    Scanner scan = new Scanner(testlog);
	    String line;
	    
	    int wins = 0;
	    int losses = 0;
	    int draws = 0;
	    int timeouts = 0;
	    int games = 0;
	    int turn = 0;
	    int winTurns = 0;
	    int lossTurns = 0;
	    int drawTurns = 0;
	    DecimalFormat df = new DecimalFormat("#.##");
	    
	    while (scan.hasNext()) {
	        line = scan.nextLine();
	        
	        int turnIndex = line.lastIndexOf("Turn ");
	        
	        if (turnIndex > -1) {
    	        String turnString = line.substring(turnIndex+5);
    	        turn = Integer.parseInt(turnString);
            }
	        
	        // Winner decided
	        if (line.contains("Player 1 Wins!") || line.contains("Player 2 Wins!") || line.contains("Draw!")) {
	            games++;
	            
	            if (line.contains("Player 1 Wins!")) {
	                wins++;
	                winTurns += turn;
	            } else  if (line.contains("Player 2 Wins!")) {
	                losses++;
	                lossTurns += turn;
	                System.out.printf("\t>map%d.txt lost\n", games);
	            } else {
    	            draws++;
    	            drawTurns += turn;
    	            System.out.printf("\t>map%d.txt draw\n", games);
	            }
	            turn = 0;
	        }
	        
	        boolean newBot = line.startsWith("BOT_");
	        
	        // New bot introduced or no more info
	        if (newBot || !scan.hasNext()) {
	            if (games > 0) {
	                System.out.printf("\tWins: %d/%d (%s)\n", wins, games, df.format((double) winTurns/wins));
	                System.out.printf("\tLosses: %d/%d (%s)\n", losses, games, df.format((double) lossTurns/losses));
	                System.out.printf("\tDraws: %d/%d (%s)\n", draws, games, df.format((double) drawTurns/draws));
	                System.out.printf("\tTimeouts: %d/%d\n", timeouts, games);
                }
	            
	            if (newBot) {
	                System.out.printf("%s BOT:\n", line.substring(4));
	            }
	            wins = 0;
	            losses = 0;
	            games = 0;
	            turn = 0;
	            winTurns = 0;
	            lossTurns = 0;
	            drawTurns = 0;
	        }
	    }
	}
	
	public static void execCmd(String[] cmd) throws java.io.IOException {
		Process proc = Runtime.getRuntime().exec(cmd);
	}

}
