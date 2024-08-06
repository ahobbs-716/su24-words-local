package words.g0;

import org.junit.Test;
import words.core.GameConfig;
import words.core.GameEngine;
import words.core.IOController;
import words.core.Letter;

import java.io.*;
import java.util.*;

import static junit.framework.TestCase.*;
import static words.g0.Group3Player.*;

public class Group3PlayerTest {

    @Test
    public void containsAllLetters() {

        Group3Player player = new Group3Player();

       assertTrue(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A', 'N', 'D', 'O', 'N')));
       assertTrue(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A', 'N', 'D', 'O')));
       assertTrue(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A', 'N', 'D')));
       assertTrue(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A', 'N')));
       assertTrue(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A')));
       assertTrue(player.containsAllLetters("ABANDON", List.of('A', 'B')));
       assertTrue(player.containsAllLetters("ABANDON", List.of('A')));

       assertTrue(player.containsAllLetters("ABANDON", new ArrayList<>()));             //assume this is correct behaviour

        assertFalse(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A', 'N', 'D', 'O', 'N', 'R')));
        assertFalse(player.containsAllLetters("ABANDON", List.of('A', 'B', 'A', 'N', 'D', '0', 'R')));

    }


    @Test
    public void createFileTest() throws IOException {

        //prep vars
        Group3Player player = new Group3Player();
        Set<String> words = new TreeSet<>();

        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\alice\\Documents\\codeProjects1\\su24-words-local\\files\\dictionary.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\alice\\Documents\\codeProjects1\\su24-words-local\\files\\cleaned.txt"));

        String incoming;
        Optional<String> outgoing;

        //get rid of first line
        reader.readLine();

        //add to set
        while ((incoming = reader.readLine()) != null) words.add(player.cleanWord(incoming));
        reader.close();

        //at this stage, set is done
        for (Iterator<String> it = words.stream().iterator(); it.hasNext();) writer.write(buildOut(it.next()));
        writer.close();

        System.out.println("Operation complete");
        System.out.println(words.size());
        System.out.println(((float)words.size()/(float)267752)+  "% of dataset remain");

    }

}
