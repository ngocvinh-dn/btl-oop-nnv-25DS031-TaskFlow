package application.DAO;


import application.models.User;

public interface UserDAO {
    User loginWithHash(String username, String password);
    User login(String username, String password);
    boolean register(User user);
    boolean checkUsernameExists(String username);
}
