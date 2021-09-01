import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Scanner;
import java.lang.Math;
public class Peer {

    private ObjectOutputStream outputClient;
    private ObjectInputStream inputClient;
    private Socket client;
    private ServerSocket server;
    private Socket connection;
    private ObjectInputStream inputServer;
    public ObjectOutputStream outputServer;
    private String directory;
    private Socket connectP;
    private ObjectInputStream inputP;
    private ObjectOutputStream outputP;


    public Peer(String directory) {
        this.directory = directory;
    }


    public static void main(String[] args) {

        Scanner myObj = new Scanner(System.in);
        Peer peer = new Peer(args[0]);

        // peer.runServer(Integer.parseInt(args[1]),100);

        while (true) {

            System.out.println("Welcome!");
            System.out.println("1.Register");
            System.out.println("2.Login");
            System.out.println("3.Exit");
            String[] credentials = new String[2];

            String choice = myObj.nextLine();


            // peer.sendMessage("kalimera");
            peer.startClient(3000);


            // peer.closeConnection();
            switch (choice) {
                case "1":

                    peer.sendMessage("1");
                    boolean reg = false;
                    while (!reg) {
                        System.out.println("REGISTER");
                        System.out.print("Give a username: ");
                        credentials[0] = myObj.nextLine();
                        System.out.println();
                        System.out.println("Give a strong password: ");
                        credentials[1] = myObj.nextLine();
                        System.out.println();
                        //peer.startClient(Integer.parseInt(args[0]));

                        //checks if the username exists in the log of tracker
                        //if it does then it returns a failure message to user
                        //to try again with different username
                        //else it creates an account

                        reg = peer.register(credentials[0], credentials[1]);
                    }
                    // peer.closeConnection();


                    break;


                case "2":
                    //peer.startClient(3000);
                    peer.sendMessage("2");
                    boolean login = false;
                    String Username = "";
                    while (!login) {
                        System.out.println("LOGIN");
                        System.out.print("Username: ");
                        credentials[0] = myObj.nextLine();
                        System.out.println();
                        System.out.print("Password: ");
                        credentials[1] = myObj.nextLine();
                        System.out.println();
                        Username = credentials[0];
                        login = peer.login(credentials[0], credentials[1]);
                    }

                    try {
                        DownloadHandler clientSock
                                = new DownloadHandler(Username, Integer.parseInt(args[1]), args[0]);

                        // This thread will handle the client
                        // separately
                        Thread t=new Thread(clientSock);
                        t.start();


                        Message token_id = peer.get_tokenid();//here it captures the random token_id
                        System.out.println("your token_id for this session is: " + token_id.getMessage_int());
                        peer.inform();//peer informs tracker for shared_directory content
                        peer.sendMessage(args[1]);
                        boolean service2 = true;
                        while (service2) {


                            System.out.println("Hello " + credentials[0] + " choose your actions!");
                            System.out.println("1.List");
                            System.out.println("2.Details");
                            System.out.println("3.Check if he is active");
                            System.out.println("4.simpleDownload");
                            System.out.println("5.Logout");
                            choice = myObj.nextLine();
                            //List
                            if (choice.equals("1")) {
                                peer.sendMessage("1");
                                ArrayList<Message> list = peer.list();
                                System.out.println("The Availiable files are: ");
                                int i = 1;
                                for (Message message : list) {
                                    System.out.println("File " + i + " " + message.getMessage());
                                    i++;
                                }
                                //Details
                            } else if (choice.equals("2")) {
                                peer.sendMessage("2");
                                System.out.print("Which file do you prefer: ");
                                choice = myObj.nextLine();

                                //String name = "Christos_statistics";
                                peer.sendMessage(choice);
                                String file_name=choice;
                                ArrayList<Account> list = peer.details(choice);
                                if (list.size() == 0) {
                                    System.out.println("This file doesnt exist in this system");
                                } else {
                                    for (Account account : list) {
                                        System.out.println("token_id: " + account.getToken_id());
                                        System.out.println("user_name: " + account.getUsername());
                                        System.out.println("count_downloads: " + account.getCount_downloads());
                                        System.out.println("count_failures: " + account.getCount_failures());
                                        System.out.println("port: " + account.getPort());

                                    }

                                }

                                System.out.println("Press 1 to download the file");
                                System.out.println("Press 2 to check if users with these files are active");
                                System.out.println("Press 3 to exit");
                                choice = myObj.nextLine();
                                if (choice.equals("1")) {
                                    if(peer.simpleDownloads(list,file_name)){
                                        peer.sendMessage("U");
                                        peer.update(token_id.getMessage_int());
                                        peer.sendMessage(file_name);
                                    }



                                } else if (choice.equals("2")) {
                                    peer.closeConnection();
                                    System.out.print("give users name");
                                    choice = myObj.nextLine();
                                    System.out.println();
                                    for (Account a : list) {
                                        if (a.getUsername().equals(choice)) {
                                            if (peer.checkActive(a)) {
                                                System.out.println("User: " + a.getUsername() + " is active!");
                                            } else {
                                                System.out.println("User: " + a.getUsername() + " is inactive!");
                                            }
                                        }
                                    }


                                }




                            } else if (choice.equals("5")) {
                                peer.sendMessage("5");
                                peer.logout(token_id.getMessage_int());
                                peer.closeConnection();
                                clientSock.serverD.close();
                                service2=false;



                            }
                        }


                    } catch (IOException ioexception) {
                        ioexception.printStackTrace();
                    } catch (ClassNotFoundException classn) {
                        classn.printStackTrace();

                    }






                    break;


                case "3":
                    peer.sendMessage("3");
                    System.out.println("looking forward to seeing you again!");
                    break;
                default:
                    System.out.println("invalid number");

            }
        }
    }

    public boolean login(String username, String password) {
        boolean login = false;
        try {
            outputClient.writeObject(new Message(username));
            outputClient.flush();
            outputClient.writeObject(new Message(password));
            outputClient.flush();
            Message reply = (Message) inputClient.readObject();
            if (reply.getMessage().equals("Successful login")) {
                System.out.println("Successful login!");
                login = true;
            } else {
                System.out.println("Unsuccesful login");
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return login;

    }


    //peer creates an account to tracker,if the username is already in use tracker return a message to try different username
    //peer sends username to server
    //if it exists then it returns a failure message to try again
    //if it doesnt it creates an account
    public boolean register(String username, String password) {
        System.out.println("edw");
        boolean registered = false;
        try {
            outputClient.writeObject(new Message(username));
            outputClient.flush();
            outputClient.writeObject(new Message(password));
            outputClient.flush();
            Message reply = (Message) inputClient.readObject();
            if (reply.getMessage().equals("Successful registration")) {
                System.out.println("Successful registration!");
                registered = true;
            } else {
                System.out.println("Unsuccesful registration.Try with different username");
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return registered;
    }




    //asks from tracker list of file names that are avauliable in the system
    public void startClient(int port) {
        try {
            connectToServer(port);
            getStreamsClient();
        } catch (EOFException eofException) {
            System.out.println("Client Terminated Connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void connectToServer(int port) throws IOException {
        client = new Socket(InetAddress.getByName("127.0.0.1"), port);
        System.out.println("Connected to Tracker");


    }

    public void getStreamsClient() throws IOException {
        outputClient = new ObjectOutputStream(client.getOutputStream());
        outputClient.flush();

        inputClient = new ObjectInputStream(client.getInputStream());


    }

    private void closeConnection() {
        try {
            outputClient.close();
            inputClient.close();
            client.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }



    public void sendMessage(String message) {
        try {
            //System.out.println("hello");
            outputClient.writeObject(new Message(message));
            outputClient.flush();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();

        }

    }


    public Message get_tokenid() throws IOException, ClassNotFoundException {
        return (Message) inputClient.readObject();
    }


    public void inform() throws IOException, ClassNotFoundException {
        List<Message> file_names = new ArrayList<Message>();
        //it creates an array of files and with listfiles its gets of files of a specific directory
        File[] files = new File(directory).listFiles();
//If this pathname does not denote a directory, then listFiles() returns null.
        for (File file : files) {
            if (file.isFile()) {
                file_names.add(new Message(file.getName()));
            }
        }


        outputClient.writeObject(file_names);
        outputClient.flush();

    }

    public ArrayList<Message> list() {
        ArrayList<Message> file_names = null;
        try {
            outputClient.writeObject(new Message("list"));

            file_names = (ArrayList<Message>) inputClient.readObject();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();

        }
        return file_names;
    }


    public ArrayList<Account> details(String name) {
        ArrayList<Account> details = null;

        try {
            outputClient.writeObject(new Message(name));
            details = (ArrayList<Account>) inputClient.readObject();

        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();

        }
        return details;

    }

    public boolean simpleDownloads(ArrayList<Account> list, String name) {
           boolean isdownloaded=true;
        //a checkActiveFunction to check if peer is active and to measure response time
        Map<Account, Double> response_times = new HashMap<Account, Double>();
        double[] times = new double[list.size()];
        int i = 0;
        System.out.println("Time to download");
        for (Account a : list) {
            double time = this.checkActiveEnhanced(a);
            response_times.put(a, time);
            times[i++] = time;
        }
        Arrays.sort(times);
        boolean not_completed = true;
        int best =0;
        while (not_completed) {
            System.out.println("Download time");
            if(best==list.size()){
                break;
            }
            for (Map.Entry<Account, Double> entry : response_times.entrySet()) {
                if (entry.getValue() == times[best]) {
                    try{
                        System.out.println("try");
                        connectP = new Socket(InetAddress.getByName("127.0.0.1"),entry.getKey().getPort());
                        outputP=new ObjectOutputStream(connectP.getOutputStream());
                        outputP.flush();
                        inputP = new ObjectInputStream(connectP.getInputStream());

                        outputP.writeObject(new Message("P"));
                        outputP.flush();
                        outputP.writeObject(new Message(name));
                        outputP.flush();
                        try {
                            this.receiveFile();
                            System.out.println("we got the file");
                            not_completed = false;
                        } catch (IOException ioexception) {
                            ioexception.printStackTrace();
                        } catch (ClassNotFoundException cnfe) {
                            cnfe.printStackTrace();
                        }


                    }catch(ConnectException unknown){
                        try{
                            System.out.println("catch");
                            connectP=new Socket(InetAddress.getByName("127.0.0.1"),3000);
                            outputP=new ObjectOutputStream(connectP.getOutputStream());
                            outputP.flush();
                            inputP=new ObjectInputStream(connectP.getInputStream());

                            outputP.writeObject(new Message("F"));
                            outputP.flush();
                            outputP.writeObject(new Message(entry.getKey().getUsername()));
                            outputP.flush();
                            System.out.println("Download failed");
                            isdownloaded=false;

                        }catch(UnknownHostException uhe){
                            uhe.printStackTrace();
                        }catch(IOException ioe) {
                            ioe.printStackTrace();
                        }
                        best++;

                    }catch(IOException ioexception){
                        best++;
                    }

                }
            }
        }
        return isdownloaded;
    }










    public double checkActiveEnhanced(Account a){


        double time=0;
        try{
            client=new Socket(InetAddress.getByName("127.0.0.1"),a.getPort());
            getStreamsClient();
            long start = System.currentTimeMillis();
            outputClient.writeObject(new Message("HTTP REQUEST"));
            Message response=(Message)inputClient.readObject();
            long end=System.currentTimeMillis();
            time=((double)(end - start) / 1000)*Math.pow(0.9,a.getCount_downloads())*Math.pow(1.2,a.getCount_failures());
        }catch(SocketException ce) {
            System.out.println("NO connection");

        }finally{
            return time;
        }

    }





    public boolean checkActive(Account account) throws UnknownHostException,IOException{
        boolean isactive=true;
        try{
            client=new Socket(InetAddress.getByName("127.0.0.1"),account.getPort());
            getStreamsClient();
            //this.sendMessage("A");

        }catch(ConnectException ce) {
            isactive = false;
        }finally{
            return isactive;
        }

    }

    //PEER AS A SERVER




    public void receiveFile() throws IOException,ClassNotFoundException{
        Message message=(Message)inputP.readObject();
        Info fileTxt=message.getFile();
        try (FileOutputStream fos = new FileOutputStream(directory+fileTxt.getInfo_name()+".txt")) {
            fos.write(fileTxt.getFileExtract());
        }


    }

    public void logout(int token) throws IOException{
        outputClient.writeObject(new Message(token));


    }

    public void update(int tokenid) throws IOException {
        outputClient.writeObject(new Message(tokenid));
    }

















}






