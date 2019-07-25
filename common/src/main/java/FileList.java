import java.util.ArrayList;

public class FileList extends AbstractMessage {

    ArrayList<String> list = new ArrayList<>();

    public FileList(ArrayList<String> list) {
        this.list = list;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }
}