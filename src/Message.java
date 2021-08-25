import java.io.Serializable;


public class Message implements Serializable{
    private String message;
    private int message_int;
    private Info file;
    private Account account;


    public Message(String message){
        this.message=message;
    }

    public Message(int message_int){
        this.message_int=message_int;
    }

    public Message(Info file){
        this.file=file;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getMessage_int() {
        return message_int;
    }

    public void setMessage_int(int message_int) {
        this.message_int = message_int;
    }

    public Info getFile(){
        return file;
    }

    public void setFile(Info file) {
        this.file = file;
    }


}
