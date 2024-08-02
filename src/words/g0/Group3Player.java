package words.g0;

import words.core.Letter;
import words.core.Player;
import words.core.PlayerBids;
import words.core.SecretState;

import java.util.ArrayList;
import java.util.List;

public class Group3Player extends Player {

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        return 0;
    }
}
