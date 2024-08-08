package words.g0;

import words.core.Letter;
import words.core.Player;
import words.core.PlayerBids;
import words.core.SecretState;
import words.core.ScrabbleValues;

import java.util.*;

public class albertPlayer3 extends Player {

    private Map<Character, Integer> letterFrequency;
    private Set<String> sevenLetterWords;

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretState, int playerID) {
        char letter = bidLetter.getCharacter();
        int letterValue = bidLetter.getValue();
        int currentScore = secretState.getScore();
        int roundsLeft = totalRounds - playerBidList.size() / (playerList.size() * 8);

        // Start with a more nuanced base bid calculation
        int baseBid = (int) (100.0 / (ScrabbleValues.letterScore(letter) * Math.sqrt(ScrabbleValues.getLetterFrequency(letter))));

        // Adjust bid for high-value or rare letters
        if ("BCMPKJQXZ".indexOf(letter) != -1) {
            baseBid += 4;
        }

        // Adjust bid for vowels based on current hand
        if ("AEIOU".indexOf(letter) != -1) {
            int vowelCount = countVowels();
            if (vowelCount < 2) {
                baseBid += 2;
            } else if (vowelCount > 3) {
                baseBid = (int) (baseBid * 0.7);
            }
        }

        // Check for potential word improvement
        String currentBestWord = returnWord();
        myLetters.add(letter);
        String potentialBestWord = returnWord();
        myLetters.remove(myLetters.size() - 1);  // Remove the added letter

        int wordImprovement = ScrabbleValues.getWordScore(potentialBestWord) - ScrabbleValues.getWordScore(currentBestWord);

        if (potentialBestWord.length() >= 7) {
            baseBid = Math.max(baseBid, letterValue + 3);  // Bid more aggressively for 7-letter words
        } else if (wordImprovement > baseBid / 2) {
            baseBid = (int) (baseBid * 1.3);  // Increase bid if it significantly improves our word
        } else if (wordImprovement > 0) {
            baseBid = (int) (baseBid * 1.1);  // Slight increase for any improvement
        } else {
            baseBid = (int) (baseBid * 0.8);  // Decrease bid if it doesn't improve our word
        }

        // Adjust bid based on game progress
        if (roundsLeft <= 2) {
            baseBid = (int) (baseBid * 1.4);  // More aggressive in final rounds
        } else if (roundsLeft >= totalRounds - 2) {
            baseBid = (int) (baseBid * 0.9);  // Slightly more conservative in early rounds
        }

        // Adjust bid based on our score
        if (stopBetting(currentScore)) {
            baseBid = (int) (baseBid * 1.1);  // Bid more if we're potentially last
        }

        // Ensure bid is within valid range and we keep at least 45 points
        int maxBid = Math.min(currentScore - 45, currentScore - 1);
        if (baseBid >= 10) {
            baseBid = Math.toIntExact((ScrabbleValues.getWordScore(potentialBestWord)+7)/7 + 6);
        }
        return Math.max(1, Math.min(baseBid, maxBid));
    }

    private int countVowels() {
        return (int) myLetters.stream().filter(letter -> "AEIOU".indexOf(letter) != -1).count();
    }

    private boolean stopBetting(int currentScore) {
        // Assume we're leading if our score is above a certain threshold
        // This is a simplification since we don't have other players' scores
        return currentScore < 40;  // You can adjust this threshold as needed
    }
}