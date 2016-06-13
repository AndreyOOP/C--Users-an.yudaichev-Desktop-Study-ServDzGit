
public class ParsedMessage {

    private Request type; //GET, POST
    private String path; //path to file
    private String fileName;


    public ParsedMessage(Request type, String path, String fileName) {
        this.type = type;
        this.path = path;
        this.fileName = fileName;
    }

    public Request getType() {
        return type;
    }

    public String getData() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }
}
