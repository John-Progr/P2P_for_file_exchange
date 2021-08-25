import java.io.Serializable;
public class Info implements Serializable {
    private String info_name;
    private byte[] FileExtract;

    public Info(String info_name,byte[] FileExtract){
        this.info_name=info_name;
        this.FileExtract=FileExtract;

    }

    public byte[] getFileExtract() {
        return FileExtract;
    }
    public String getInfo_name(){
        return info_name;
    }
}
