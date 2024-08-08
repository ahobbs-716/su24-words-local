package words.g0;

import words.core.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Level;

import static words.core.ScrabbleValues.getWordScore;


public class Group3Player extends Player {
    Map<Integer, Map<Letter, Integer>> playerCounts;             //format: Map <PlayerID <letter_type, num_letters_of_type>
    Map<Letter, Integer> absoluteCounts;                         //format: Map <Letter, num_instances_on_board>
    int totalPlayed;
    ArrayList<String> vowels;
    ArrayList<String> easyConst; // value of 4 or fewer
    ArrayList<String> hardConst; // value of 5 or higher
    String[] sortWords;

    int cashRemaining;

    public Group3Player() {
        playerCounts = new HashMap<>();
        absoluteCounts = new HashMap<>();
        totalPlayed = 0;
        vowels = new ArrayList<>(Arrays.asList("A", "E", "I", "O", "U"));
        easyConst = new ArrayList<>(Arrays.asList("B", "C", "D", "F", "G", "H", "L", "M", "N", "P", "R", "S", "T", "V", "W", "Y"));
        hardConst = new ArrayList<>(Arrays.asList("J", "K", "Q", "X", "Z"));

        cashRemaining = 100;

    }

    protected void initializeSort() {
        String line = null;
        ArrayList<String> wtmp = new ArrayList<>(55000);
        try {
            BufferedReader r = new BufferedReader(new FileReader("files/cleaned.txt"));
            while (null != (line = r.readLine())) {
                String newline = new String(line.trim());
                newline = newline.replaceAll("\\d+", "");
                wtmp.add(new String(line.trim()));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred.", e);
        }
        sortWords = wtmp.toArray(new String[0]);
    }
    @Override
    public void startNewRound(SecretState secretstate){
        totalPlayed = 0;
        myLetters.clear(); // clear the letters that I have
        // this puts the secret letters into the currentLetters List
        myLetters.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());

        playerLetters.clear(); // clear the letters that all the players have
        for (int i = 0; i < numPlayers; i++) {
            playerLetters.add(new LinkedList<Character>()); // initialize each player's list of letters
        }
        /*
        Note that although the letters that I have will be in the playerLetters List, the playerLetters
        List doesn't include my secret letters.
         */
    }

    @Override
    public void startNewGame(int playerID, int numPlayers) {
        myID = playerID; // store my ID
        initializeSort();

        initializeWordlist(); // read the file containing all the words

        this.numPlayers = numPlayers; // so we know how many players are in the game
    }

    private void recordLetter(Letter letter, int ownerID) {

        //check data
        if (letter == null) return;     //case letter is invalid
        if (ownerID < 0) return;       //case no one won this bid

        //if here, letter is valid

        // Add to the player count
        if (!playerCounts.containsKey(ownerID)) playerCounts.put(ownerID, new HashMap<Letter, Integer>());      //case never seen this player win
        else if (!playerCounts.get(ownerID).containsKey(letter)) playerCounts.get(ownerID).put(letter, 1);      //case this player has no letters of this type
        else playerCounts.get(ownerID).put(letter, (playerCounts.get(ownerID).get(letter)+1));                  //case this player already has a letter of this type

        //Add to the absolute count
        if(!absoluteCounts.containsKey(letter)) absoluteCounts.put(letter, 1);
        else absoluteCounts.put(letter, absoluteCounts.get(letter) + 1);

        //add to total seen
        totalPlayed++;

    }

    private void updateCash(int pointsSpent) {

        cashRemaining -= pointsSpent;
    }






    public static boolean containsAllLetters(String str, List<Character> letters) {
        for (char letter : letters) {
            if (str.indexOf(letter) == -1) {
                return false;
            }
        }
        return true;
    }

    public static List<Character> sorter( List<Character> letters){
        List<Character> sortedList = new ArrayList<>(letters);
        Collections.sort(sortedList);
        return sortedList;
    }


    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds, ArrayList<String> playerList, SecretState secretstate, int playerID) {
        if (playerBidList.size() > 0)
            recordLetter(playerBidList.get(playerBidList.size() - 1).getTargetLetter(), playerBidList.get(playerBidList.size() - 1).getWinnerID());

        //calculate useful vars
        String word = returnWord();
        int proposal = 0;
        int roundsLeft = numPlayers * 8 - totalPlayed;

        if (totalPlayed < 3) {
            proposal = 3;


        // THIS checks if we have a valid word of 7 or more letters. at this point, stop betting.
        // IMPROVEMENT made: if new letter adds to value of our existing word... than can bid
            // CHANGE:now goes into this position when it exceeds 100

    } else if (cashRemaining + getWordScore(returnWord()) <= 101){
            int c_count = 0;
            for(String w: sortWords){
                if((w.length() == 7) && (containsAllLetters(w, myLetters))){
                    if(w.contains(String.valueOf(bidLetter.getCharacter()))){
                        c_count ++;
                    }
                }
                if (c_count >=1){
                    proposal = 5;
                }
            }
        }


        //need to consider rounds left

        // strategy: rounds left + number of letters > 11 to bid heavily to get a 7-letter word
        // aka in the >= 4 section
        //reasoning: we will assume some we will be outbid despite big bets, or some letters dont work
        //thus, we will only pursue 7 letters if enough time


        // THIS: if we have 4 or more letters
        //count all the 7 (or more) letter words CONTAINING the letters that we already have
        //then, for all those long words ^, count how many have the new letter
        // then, bid appropriately based on how much that letter will help (probability of it being in a longer water)

        // TO DO: test these bids and thesholds => mitali just did it randomly
        // POSSIBLE LIMITATION: can all but one of these letters work?


        else if((myLetters.size() >= 4) && (1.5*(7-myLetters.size())< roundsLeft)){ //&& (
            int w_count = 0;
            int c_count = 0;
            for(String w: sortWords){
                if((w.length() >= 7) && (containsAllLetters(w, myLetters))){
                    w_count ++;
                    if(w.contains(String.valueOf(bidLetter.getCharacter()))){
                        c_count ++;
                    }
                }

            }
            if ((double) c_count / w_count > 0.9){
                proposal = 10;
            }
            if ((double) c_count / w_count > 0.7){
                proposal = 8;
            }
            if ((double) c_count / w_count > 0.5){
                proposal = 5;
            }
            proposal = 1;

        }




        // if we do not have 4 letters, bid on letterswith following strat:
        // we wnt vowels and easy constinents
        //if we cant get them, make the other teams pay for them!

        else if (vowels.contains(String.valueOf(bidLetter.getCharacter()))){
            proposal = 6;
        }
        else if (easyConst.contains(String.valueOf(bidLetter.getCharacter()))){
            proposal = 6;
        }
        else if (hardConst.contains(String.valueOf(bidLetter.getCharacter()))){
            proposal = 4;
        }

        updateCash(proposal);
        return proposal;
    }
}
