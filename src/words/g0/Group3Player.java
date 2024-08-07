package words.g0;

import words.core.*;

import java.io.*;
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
    BufferedReader cleanReader;
    Map<String, Integer> cleanWords;
    Trie trie;
    enum TYPE {
        hard_const, easy_const, vowel
    }

    public Group3Player() throws FileNotFoundException {
        playerCounts = new HashMap<>();
        absoluteCounts = new HashMap<>();
        totalPlayed = 0;
        vowels = new ArrayList<>(Arrays.asList("A", "E", "I", "O", "U"));
        easyConst = new ArrayList<>(Arrays.asList("B", "C", "D", "F", "G", "H", "L", "M", "N", "P", "R", "S", "T", "V", "W", "Y"));
        hardConst = new ArrayList<>(Arrays.asList("J", "K", "Q", "X", "Z"));

        cleanWords = new HashMap<>();
        cleanReader = new BufferedReader(new FileReader("C:\\Users\\alice\\Documents\\codeProjects1\\su24-words-local\\files\\cleaned.txt"));
        trie = new Trie();

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

    public void buildWordMap() throws IOException {

        //populate the cleanWords data
       String incoming = null;
        while ((incoming = cleanReader.readLine()) != null) {
            String[] line = incoming.split(",");
            cleanWords.put(line[1], getWordScore(line[1]));
        }

    }

    public void buildWordTrie() {
        for (Iterator<String> it = cleanWords.keySet().stream().iterator(); it.hasNext();) trie.insert(it.next().toLowerCase());
    }

    public void visualiseTree() {
        trie.visualize();
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

        //bank any new info about bids
        if (playerBidList.size() > 0) recordLetter(playerBidList.get(playerBidList.size()-1).getTargetLetter(), playerBidList.get(playerBidList.size()-1).getWinnerID());

        //set up vars
        String currBest = returnWord();
        int roundsLeft = numPlayers * 8 - totalPlayed;

        //case 1: first 3 rounds [FIXED LOW BID]
        if(totalPlayed < 3) return 3;

        //case 2: longer than 7 [ONLY BID ON AN IMMEDIATE WIN]
        if (currBest.length() >= 7) if (immediateWinGuaranteed(bidLetter)) return 5;

        //case 3: long words, and time to make them [BID ACCORDING TO USE VALUE]
        else if((myLetters.size() >= 4) && stillTime(roundsLeft)) return sizeBid(assessUse(bidLetter));

        //case 4: limited choice [MAKE OTHER TEAMS PAY]
        else if (vowels.contains(String.valueOf(bidLetter.getCharacter()))) return 6;
        else if (easyConst.contains(String.valueOf(bidLetter.getCharacter()))) return 6;
        else if (hardConst.contains(String.valueOf(bidLetter.getCharacter()))) return 4;

        return 0;

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

    public boolean stillTime(int roundsLeft) {
        return (1.5*(7-myLetters.size()) < roundsLeft);

    }

    public int sizeBid(double useValue) {

        if (useValue > 0.9) return 10;
        else if (useValue > 0.7) return 8;
        else if (useValue > 0.5) return 5;
        else return 1;

    }

    public double assessUse(Letter bidLetter) {
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
        return c_count/w_count;
    }


}
