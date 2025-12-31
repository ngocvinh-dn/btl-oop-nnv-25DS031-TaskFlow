package application.models;

public class Label {

    private int id;
    private int userId;
    private String title;
    private String colorCode;

    public Label(int id, int userId, String title, String colorCode) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.colorCode = colorCode;
    }

    public Label() {}

    public Label(String title, String colorCode) {
        this.title = title;
        this.colorCode = colorCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    @Override
    public String toString() {
        return title;
    }
}
