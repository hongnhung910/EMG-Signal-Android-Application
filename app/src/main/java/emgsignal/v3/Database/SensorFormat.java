package emgsignal.v3.Database;

public class SensorFormat {
    String type, resMid, resEnd, resRef, id;

    public SensorFormat(String type, String resMid, String resEnd, String resRef, String id) {
        this.type = type;
        this.resMid = resMid;
        this.resEnd = resEnd;
        this.resRef = resRef;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResMid() {
        return resMid;
    }

    public void setResMid(String resMid) {
        this.resMid = resMid;
    }

    public String getResEnd() {
        return resEnd;
    }

    public void setResEnd(String resEnd) {
        this.resEnd = resEnd;
    }

    public String getResRef() {
        return resRef;
    }

    public void setResRef(String resRef) {
        this.resRef = resRef;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
