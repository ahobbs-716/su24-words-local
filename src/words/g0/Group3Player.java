package words.g0;

import words.core.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;


public class Group3Player extends Player {

    //to enable tracking
    Map<Integer, Map<Letter, Integer>> playerCounts;             //format: Map <PlayerID <letter_type, num_letters_of_type>
    Map<Letter, Integer> absoluteCounts;                         //format: Map <Letter, num_instances_on_board>
    int totalPlayed;                                             //total letters that have been purchased by any player in the game. Does not include secret letters/
    String[] sortWords;                                         //sorted list of words from main dictionary
    String[] highValueWords;


    public Group3Player() throws FileNotFoundException {

        //to enable tracking
        playerCounts = new HashMap<>();
        absoluteCounts = new HashMap<>();
        totalPlayed = 0;

    }

    //for use in initialisation
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

    //for use in player tracking
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

    //probable best move
    public float calculateExpectation(float probability, float value) {
        return probability * value;
    }
    public double calculateExpectation(float[] probabilities, float[] values) {

        float temp = 0;
        for (int i = 0; i < probabilities.length; i++) temp += calculateExpectation(probabilities[i], values[i]);
        return temp;
    }

    //next best move
    public static boolean containsAllLetters(String str, List<Character> letters) {
        for (char letter : letters) {
            if (str.indexOf(letter) == -1) {
                return false;
            }
        }
        return true;
    }
    public boolean immediateWinGuaranteed(Letter bidLetter) {

        int c_count = 0;
        for(String w: sortWords){
            if((w.length() == 7) && (containsAllLetters(w, myLetters))){
                if(w.contains(String.valueOf(bidLetter.getCharacter()))){
                    c_count ++;
                }
            }
            if (c_count >=1){
                return true;
            }
        }
        return false;

    }

    //generic helper functions
    public static List<Character> sorter( List<Character> letters){
        List<Character> sortedList = new ArrayList<>(letters);
        Collections.sort(sortedList);
        return sortedList;
    }
    public static int toNumber(Character letter) {

        letter = String.valueOf(letter).toLowerCase().charAt(0);
        return letter - 'a';

    }
    public static Character toLetter(int number) {
        return (char) (number + 'a');
    }
    
    //override functions
    @Override
    public void startNewGame(int playerID, int numPlayers) {
        myID = playerID; // store my ID
        initializeSort();
        initializeWordlist(); // read the file containing all the words
        this.numPlayers = numPlayers; // so we know how many players are in the game
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

    //main function
    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds, ArrayList<String> playerList, SecretState secretstate, int playerID) {

        //bank any new info about bids
        if (playerBidList.size() > 0) recordLetter(playerBidList.get(playerBidList.size()-1).getTargetLetter(), playerBidList.get(playerBidList.size()-1).getWinnerID());

        //set up vars
        String currBest = returnWord();
        int roundsLeft = numPlayers * 8 - totalPlayed;

        //case 1: first 3 rounds [FIXED LOW BID]
        if(totalPlayed < 3) return 3;

        //case 2: longer than 7 [ONLY BID ON AN IMMEDIATE WIN]
        if (currBest.length() >= 7) if (immediateWinGuaranteed(bidLetter)) return 5;

        //case 3: words shorter than 7

        //for each word that is potentially available...
        //calculate the expectation of making that word
        //apply a premium for words that are either highly flexible or high scoring

        return 0;

    }


}
