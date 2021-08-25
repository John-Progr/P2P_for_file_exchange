import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class Tracker {


    private ServerSocket server;
    private Socket connection;
    private ObjectOutputStream outputClient;
    private ObjectInputStream inputClient;
    private Socket client;
    private ObjectInputStream inputServer;
    public ObjectOutputStream outputServer;
    public ArrayList<Account> accounts;
    public CopyOnWriteArrayList<Account> active_users;
    public Set<String> file_names;
    public ConcurrentMap<String,ArrayList<Integer>> file_token;


    public Tracker(){
        accounts=new ArrayList<Account>();
        active_users=new CopyOnWriteArrayList<Account>();
        file_names=new HashSet<String>();
        file_token=new ConcurrentHashMap<String, ArrayList<Integer>>();
    }




    public static void main(String args[]) {
        System.out.println("Server starting");
        System.out.println(args[0]);

        new Tracker().runServer(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    //Tracker runs on a specific port and has a fixed amount of peers it can take at a time
    public void runServer(int port, int queue_length) {

        try {
            server = new ServerSocket(3000, 100);

            while (true) {
                try {
                    waitForConnection();
                    System.out.println("A Peer is connected");
                    //getStreams();
                    processConnection();


                    // This thread will handle the client
                    // separately
                } catch (EOFException eofException) {
                    System.out.println("Connection to Server has been terminated");
                }
            }



            } catch(IOException ioexception){
                ioexception.printStackTrace();
            }

    }


    //Tracker waits for a peer to connect
    public void waitForConnection() throws IOException{
         connection=server.accept();

    }


    //sets up the in/out streams to send messages

    public void processConnectionC(){
        try{


            String message=(String)inputServer.readObject();
            System.out.println(message);

        }catch(IOException ioexception){
            ioexception.printStackTrace();
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }


    }
    public void getStreams() throws IOException {
        outputServer = new ObjectOutputStream(connection.getOutputStream());
        outputServer.flush();
        inputServer = new ObjectInputStream(connection.getInputStream());
    }


    //close in/out streams and terminate connection
    public void closeconnection(){
        System.out.println("Terminating Connection");

        try{
           //output.close();
           // input.close();
            connection.close();

        }catch(IOException ioException){

            ioException.printStackTrace();

        }
    }


    public void startClient(int port){
        try{
            connectToServer(port);
           // getStreams();
        }catch(EOFException eofException){
            System.out.println("Client Terminated Connection");
        }catch(IOException ioException){
            ioException.printStackTrace();
        }finally{
            closeConnectionc();
        }
    }

    public void connectToServer(int port) throws IOException{
        client=new Socket(InetAddress.getByName("127.0.0.1"),port);
        System.out.println("Connected to Tracker");


    }

    public void getStreamsClient() throws IOException{
        outputClient=new ObjectOutputStream(client.getOutputStream());
        outputClient.flush();

        inputClient=new ObjectInputStream(client.getInputStream());


    }

    private void closeConnectionc(){
        try{
            outputClient.close();
            inputClient.close();
            client.close();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void processConnection() throws IOException{
            PeerClientHandler clientSock
                    = new PeerClientHandler(connection,accounts,active_users,file_names,file_token);

            // This thread will handle the client
            // separately
            new Thread(clientSock).start();



    }







}
