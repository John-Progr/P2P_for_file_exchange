import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class DownloadHandler implements Runnable {
    public ServerSocket serverD;
    private Socket connection;
    private ObjectInputStream inputServer;
    public ObjectOutputStream outputServer;
    private int port;
    private String directory;
    private ObjectInputStream inputClient;
    private ObjectOutputStream outputClient;
    private Socket client;
    private String user_name;


    public DownloadHandler(String user_name,int port,String directory){
        this.user_name=user_name;
        this.port=port;
        this.directory=directory;
    }





    public void run() {

        try {
            serverD = new ServerSocket(port,100);

            while (waitForConnection()) {
                try {

                    if(getServerStreams()){ System.out.println("ela"); processConnection();}




                    // This thread will handle the client
                    // separately
                } catch (EOFException eofException) {
                    System.out.println("Connection to Server has been terminated");
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
            }



        } catch(IOException ioexception){
            ioexception.printStackTrace();
        }

    }

    public boolean waitForConnection() throws IOException {
        boolean run=true;
          try{
              connection=serverD.accept();

          }catch(SocketException se){
              run=false;
          }


        return run;



    }

    public boolean getServerStreams(){
        boolean isit=true;
        try{
            outputServer=new ObjectOutputStream(connection.getOutputStream());
            outputServer.flush();
            inputServer=new ObjectInputStream(connection.getInputStream());

        }catch(IOException ioe){
            isit=false;

        }catch(NullPointerException np){
            isit=false;
        }
        return isit;

    }

    public void processConnection() throws IOException,ClassNotFoundException {
        boolean isit=true;
        Message message = (Message) inputServer.readObject();
        if(message.getMessage().equals("HTTP REQUEST")) {

            Thread thread = new Thread();
            Random rand = new Random();
            long number=rand.nextInt(10000);
            try {
                thread.sleep(number);
                outputServer.writeObject(new Message("ACK"));
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }





        }else if(message.getMessage().equals("P")){
            message=(Message)inputServer.readObject();
            sendFile(message.getMessage());
            client = new Socket(InetAddress.getByName("127.0.0.1"), 3000);
            getStreams();
            notifyTracker();
            //client.close();



        }else if(message.getMessage().equals("T")){
            System.out.println("OK");
            outputServer.writeObject(new Message("OK"));


        }


    }

    public void sendFile(String name) throws IOException{

        //it reads file and makes an info object
        Path path = Paths.get(directory+name+".txt");
        // Load as binary:
        byte[] bytes = Files.readAllBytes(path);

        outputServer.writeObject(new Message(new Info(name,bytes)));
        System.out.println("we sent the file");

    }

    public void notifyTracker() throws  IOException{
        outputClient.writeObject(new Message("S"));
        outputClient.flush();
        System.out.println(user_name);
        outputClient.writeObject(new Message(user_name));
        outputClient.flush();



    }

    public void getStreams() throws IOException{
        outputClient=new ObjectOutputStream(client.getOutputStream());
        outputClient.flush();
        inputClient=new ObjectInputStream(client.getInputStream());

    }



}
