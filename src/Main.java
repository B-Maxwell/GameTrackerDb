
import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS games (id IDENTITY, game_name VARCHAR, genre VARCHAR, platform VARCHAR, release_year VARCHAR )");
        stmt.execute();

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());

                    HashMap m = new HashMap<>();
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    }
                    else {
                        return new ModelAndView(user, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }

                    Session session = request.session();
                    session.attribute("userName", name);
                    user.games = selectGames(conn);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-game",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }

                    String gameName = request.queryParams("gameName");
                    String gameGenre = request.queryParams("gameGenre");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.valueOf(request.queryParams("gameYear"));
//                    Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);
//                    user.games.add(game);

                    insertGame(conn, gameName, gameGenre, gamePlatform, gameYear);
                    user.games = selectGames(conn);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post("/delete-game", (request, response) -> {
            User user = getUserFromSession(request.session());
//            String name = session.attribute("userName");
            int gameNumber = Integer.valueOf(request.queryParams("deleteNumber"));
            deleteGame(conn, gameNumber);
            user.games = selectGames(conn);

            response.redirect("/");
            return "";
        });

        Spark.post(
                "/edit-game",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }

                    int gameNumber = Integer.valueOf(request.queryParams("editNumber"));
                    String gameName = request.queryParams("editName");
                    String gameGenre = request.queryParams("editGenre");
                    String gamePlatform = request.queryParams("editPlatform");
                    int gameYear = Integer.valueOf(request.queryParams("editYear"));
//

                    updateGame(conn, gameNumber, gameName, gameGenre, gamePlatform, gameYear);
                    user.games = selectGames(conn);
                    response.redirect("/");
                    return "";
                })
        );
    }

    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return users.get(name);
    }

    static void insertGame(Connection conn, String gameName, String genre, String platform, int releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES (NULL, ?, ?, ?, ?)");
        stmt.setString(1, gameName);
        stmt.setString(2, genre);
        stmt.setString(3,platform);
        stmt.setInt(4, releaseYear);
        stmt.execute();

    }

    static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games ORDER BY game_name, release_year ");
        ResultSet results = stmt.executeQuery();

        while (results.next()) {
            int id = results.getInt("id");
            String gameName = results.getString("game_name");
            String genre = results.getString("genre");
            String platform = results.getString("platform");
            int releaseYear = results.getInt("release_year");
            games.add(new Game(id, gameName, genre, platform, releaseYear));
        }

        return games;
    }

    static void deleteGame(Connection conn, int gameNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE id = ?");
        stmt.setInt(1, gameNum);
        stmt.execute();
    }

    static void updateGame(Connection conn, int gameNum, String gameName, String genre, String platform, int releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE games SET game_name = ?, genre = ?, platform = ?, release_year = ? WHERE id = ?");
        stmt.setString(1, gameName);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.setInt(5, gameNum);
        stmt.execute();
    }

}