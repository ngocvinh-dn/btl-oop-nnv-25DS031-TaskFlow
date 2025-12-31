package application.DAO;

import application.models.Label;

import java.util.List;

public interface LabelDAO {
    List<Label> getAllLabels(int userId);
    boolean addLabel(Label label, int userId);
    boolean deleteLabel(int id, int userId);
}
