package application.DAO;


import application.models.User;

public interface UserDAO {
    User login(String username, String password);
    boolean register(User user);
    boolean checkUsernameExists(String username);
}
