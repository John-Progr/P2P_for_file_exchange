import java.io.Serializable;
public class Account implements Serializable {

    private String username;
    private String password;
    private int count_downloads;
    private int count_failures;
    private int token_id;
    private int port;

    public  Account(String username,String password){
        this.username=username;
        this.password=password;
        setCount_downloads(0);
        setCount_failures(0);
    }
    public  Account(String username,String password,int count_downloads,int count_failures,int port){
        this.username=username;
        this.password=password;
        this.count_failures=count_failures;
        this.count_downloads=count_downloads;
        this.port=port;
    }

    public  Account(int token_id,String username,int port,int count_downloads,int count_failures){
        this.token_id=token_id;
        this.username=username;
        this.port=port;
        this.count_failures=count_failures;
        this.count_downloads=count_downloads;
        this.port=port;


    }

    public int getCount_downloads() {
        return count_downloads;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int getCount_failures() {
        return count_failures;
    }

    public void setCount_downloads(int count_downloads) {
        this.count_downloads = count_downloads;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCount_failures(int count_failures) {
        this.count_failures = count_failures;
    }

    public int getToken_id() {
        return token_id;
    }

    public void setToken_id(int token_id) {
        this.token_id = token_id;
    }

    public int getPort() {
        return port;
    }
}
