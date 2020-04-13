package emgsignal.v3.Database;

public class UserFormat {
    private String name, birthday;
    private String height, weight, body_res;
    private String id;

    public  UserFormat() {};
    public UserFormat(String name, String birthday, String height, String weight, String body_res, String id) {
        this.name = name;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
        this.body_res = body_res;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBody_res() {
        return body_res;
    }

    public void setBody_res(String body_res) {
        this.body_res = body_res;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
