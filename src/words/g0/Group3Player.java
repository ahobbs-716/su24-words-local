package words.g0;

import words.core.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import static words.core.ScrabbleValues.*;


public class Group3Player extends Player {

    //track players [format: Map <PlayerID <letter_type, num_letters_of_type>]
    Map<Integer, Map<Character, Integer>> playerCounts;

    //track letters
    int[] absoluteCounts;
    int[] remainingCounts;
    int[] values;
    int[] probabilities;

    //track my progress
    String currentBest;
    String currentTarget;
    int cashRemaining;

    //track total played (i.e. successfully auctioned) letters (currently does not include secret letters)
    int totalAuctioned;

    //dictionaries
    String[] sortWords;                                            //sorted list of words from main dictionary
    String[] highValueWords;


    public Group3Player() {

        playerCounts = new HashMap<>();
        absoluteCounts = new int[26];

        totalAuctioned = 0;
        cashRemaining = 100;

        initialiseRemaining();
        initialiseValues();

        currentBest = "";
        currentTarget = "aeimnorsstty";

    }


    //for use in initialisation
    protected void initializeSort() {
        String line;
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
    private void initialiseRemaining() {
        remainingCounts = new int[26];
        for (int i = 0; i < 26; i++) remainingCounts[i] = getLetterFrequency((char) (toLetter(i)));
    }
    private void initialiseValues() {
        values = new int[26];
        for (int i = 0; i < 26; i++) {
            Character character = toLetter(i);
            values[i] = letterScore(character);
        }
    }


    //for use in player tracking
    private void updateTrackers(Letter letter, int ownerID) {

        //check data
        if (letter == null) return;     //case letter is invalid
        if (ownerID < 0) return;       //case no one won this bid

        //if here, letter is valid
        Character character = letter.getCharacter();
        int index = toIndex(character);

        // Add to player tracking
        if (!playerCounts.containsKey(ownerID)) playerCounts.put(ownerID, new HashMap<>());      //case never seen this player win
        else if (!playerCounts.get(ownerID).containsKey(character)) playerCounts.get(ownerID).put(character, 1);      //case this player has no letters of this type
        else playerCounts.get(ownerID).put(character, (playerCounts.get(ownerID).get(character)+1));                  //case this player already has a letter of this type

        //Add to letter tracking
        absoluteCounts[index] = absoluteCounts[index] + 1;
        remainingCounts[index] = remainingCounts[index] - 1;
        updateProbabilities();
        totalAuctioned++;

        //track banked progress
        currentBest = returnWord();

    }
    private void updateProbabilities() {

        //set up vars
        float[] probabilities = new float[26];
        int totalRemaining = 26 - totalAuctioned;

        //populate new array with probabilities
        for (int i = 0; i < 26; i++) probabilities[i] = (float) remainingCounts[i] /totalRemaining;

    }
    private void updateCash(int pointsSpent) {

        cashRemaining -= pointsSpent;
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
    public int valueLetter(Character character) {

        character = toLowerCase(character);

        //case useful
        for (int i = 0; i < currentTarget.length(); i++) if (currentTarget.charAt(i) == character) {

            switch (remainingCounts[toIndex(character)]) {
                case 1: return 7;
                case 2: return 6;
                case 3: return 5;
                default: return 4;
            }
        };

        //case not useful
        return 0;

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
    public static int toIndex(Character letter) {

        letter = String.valueOf(letter).toLowerCase().charAt(0);
        return letter - 'a';

    }
    public static Character toLetter(int number) {
        return (char) (number + 'a');
    }
    public static Character toLowerCase(Character character) {
        return String.valueOf(character).toLowerCase().charAt(0);
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
        totalAuctioned = 0;
        myLetters.clear(); // clear the letters that I have
        // this puts the secret letters into the currentLetters List
        myLetters.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());

        playerLetters.clear(); // clear the letters that all the players have
        for (int i = 0; i < numPlayers; i++) {
            playerLetters.add(new LinkedList<>()); // initialize each player's list of letters
        }
        /*
        Note that although the letters that I have will be in the playerLetters List, the playerLetters
        List doesn't include my secret letters.
         */
    }


    //main function
    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds, ArrayList<String> playerList, SecretState secretstate, int playerID) {

        int proposal = 0;

        //update tracking [LETTER COUNTS, PROBABILITIES, BEST WORDS AVAILABLE]
        if (!playerBidList.isEmpty()) updateTrackers(playerBidList.get(playerBidList.size()-1).getTargetLetter(), playerBidList.get(playerBidList.size()-1).getWinnerID());

        //case 1: if we haven't yet achieved 100 [PRIORITISE FLEXIBILITY]
        if (cashRemaining + getWordScore(currentBest) <= 101) proposal = valueLetter(bidLetter.getCharacter());

        //case 2: longer than 7 [PRIORITISE IMMEDIATE WIN]
        else if (immediateWinGuaranteed(bidLetter)) proposal = 5;

        //update tracking [CASHFLOW]
       updateCash(proposal);

       System.out.println(proposal);
       return proposal;

    }


}
