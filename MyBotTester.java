
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
            String[] cmd = {"bash","bot_tester.sh","&>","testlog.txt"};
            execCmd(cmd);
            
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
	    int timeouts = 0;
	    int games = 0;
	    DecimalFormat df = new DecimalFormat("#.##");
	    
	    while (scan.hasNext()) {
	        line = scan.nextLine();
	        
	        // Winner decided
	        if (line.equals("Player 1 Wins!") || line.equals("Player 2 Wins!")) {
	            if (line.equals("Player 1 Wins!")) {
	                wins++;
	            } else {
	                losses++;
	                System.out.printf("\t>map%d.txt lost\n", games);
	            }
	            games++;
	        }
	        
	        boolean newBot = line.startsWith("BOT_");
	        
	        // New bot introduced or no more info
	        if (newBot || !scan.hasNext()) {
	            if (wins > 0 && games > 0) {
	                System.out.printf("\tWins: %d/%d (%s)\n", wins, games, df.format((double) wins/games));
	                System.out.printf("\tLosses: %d/%d (%s)\n", losses, games, df.format((double) losses/games));
	                System.out.printf("\tTimeouts: %d/%d (%s)\n", timeouts, games, df.format((double) timeouts/games));
                }
	            
	            if (newBot) {
	                System.out.printf("%s BOT:\n", line.substring(4));
	            }
	            wins = 0;
	            games = 0;
	        }
	    }
	}
	
	public static void execCmd(String[] cmd) throws java.io.IOException {
		Process proc = Runtime.getRuntime().exec(cmd);
		//java.io.InputStream is = proc.getInputStream();
		// java.util.Scanner s = new java.util.Scanner(is);//.useDelimiter("\\A");
		//        String val = "";
		//        if (s.hasNext()) {
		//            val = s.next();
		//            System.out.println(val);
		//        } else {
		//            val = "";
		//        }
        //BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        //StringBuffer buffer = new StringBuffer();
        // String line = null;
        //         while ((line = in.readLine()) != null) {
        //             buffer.append(line).append("\n");
        //         }
        //         System.out.println(buffer.toString());
        //         in.close();
        // return null;
	}

}
