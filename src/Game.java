/**
 * Created by Ben Maxwell on 2/28/17.
 */

public class Game {
    int id;
    String gameName;
    String genre;
    String platform;
    int releaseYear;

    public Game(){}

    public Game(int id, String gameName, String genre, String platform, int releaseYear) {
        this.id = id;
        this.gameName = gameName;
        this.genre = genre;
        this.platform = platform;
        this.releaseYear = releaseYear;
    }
}
