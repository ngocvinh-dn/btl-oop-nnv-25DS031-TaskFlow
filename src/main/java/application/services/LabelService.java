package application.services;

import application.DAO.LabelDAO;
import application.DAOImpl.LabelDAOImpl;
import application.models.Label;
import application.utils.UserSession;

import java.util.List;

public class LabelService {
    private final LabelDAO labelDAO;

    public LabelService() {
        this.labelDAO = new LabelDAOImpl();
    }

    private int getCurrentUserId() {
        if(UserSession.getInstance()!=null && UserSession.getInstance().getUser()!=null){
            return UserSession.getInstance().getUser().getId();
        }
        return -1;
    }

    public boolean addLabel(Label label) {
        return labelDAO.addLabel(label,getCurrentUserId());
    }

    public boolean deleteLabel(int id) {
        return labelDAO.deleteLabel(id,getCurrentUserId());
    }

    public List<Label> getAllLabels() {
        return labelDAO.getAllLabels(getCurrentUserId());
    }
}
