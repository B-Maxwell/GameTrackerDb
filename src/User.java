/**
 * Created by Ben Maxwell on 2/28/17.
 */

import java.util.ArrayList;

public class User {
    String name;
    ArrayList<Game> games = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }
}