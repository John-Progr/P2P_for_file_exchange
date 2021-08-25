import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeerClientHandler implements Runnable{
    private Socket peer;
    private ObjectInputStream input;
    public ObjectOutputStream output;
    public ArrayList<Account> accounts;
    public CopyOnWriteArrayList<Account> active_users;
    public Set<String> file_names ;
    public ConcurrentMap<String,ArrayList<Integer>> file_token;
    private Socket client;
    private ObjectOutputStream outputClient;
    private ObjectInputStream inputClient;
    private Socket clientCheck;




    public PeerClientHandler(Socket peer, ArrayList<Account> accounts,CopyOnWriteArrayList<Account> active_users,Set<String> file_names,ConcurrentMap<String,ArrayList<Integer>> file_token){
        this.peer=peer;
        this.accounts=accounts;
        this.active_users=active_users;
        this.file_names=file_names;
        this.file_token=file_token;
    }







    public void run() {
        try {
            getStreams();

        } catch (IOException ioexception){
            ioexception.printStackTrace();

        }
        boolean service1=true;

        while (service1) {


            try {


                Message message = (Message) input.readObject();
               //if user gives unique username he can connect to the system
                if (message.getMessage().equals("1")) {
                    while (reply_register()) {

                    }


                } else if (message.getMessage().equals("2")) {

                    try {
                        Message username=(Message) input.readObject();
                        Message password = (Message) input.readObject();
                        //user gives username and password if his credentials are right he can have access to the system
                        while (!reply_login(username.getMessage(), password.getMessage())) {
                            username= (Message) input.readObject();
                            password = (Message) input.readObject();


                        }
                        System.out.println("User "+username.getMessage()+" is connected");
                        int token = new Random().nextInt();
                        output.writeObject(new Message(Math.abs(token)));//sends token to peer
                        output.flush();
                         //informs token list and matches token_id with each file name
                        ArrayList<Message> inform = reply_inform();
                        Message port_peer=(Message)input.readObject();

                        update_active(Math.abs(token), inform);
                        //updates active users list
                        update_account(Math.abs(token),username.getMessage(),Integer.parseInt(port_peer.getMessage()));


                    } catch (IOException ioexception) {
                        ioexception.printStackTrace();
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                    }


                    //sends the randomly generated token_id to the peer

                    boolean service2=true;
                    while(service2) {
                        Message choice = (Message) input.readObject();
                        if (choice.getMessage().equals("1")) {
                            reply_list();

                        } else if (choice.getMessage().equals("2")) {
                            Message name_file = (Message) input.readObject();
                            reply_details(name_file.getMessage());

                        } else if (choice.getMessage().equals("5")) {
                            Message response = (Message) input.readObject();
                            //if user choose logout then he disconnects from the tracker
                            reply_logout(response.getMessage_int());
                            System.out.println("User with token_id: " + response.getMessage_int() + " is leaving!");
                            service1=false;
                            service2=false;
                            closeConnection();



                        }
                    }


                }else if(message.getMessage().equals("F")){
                    //fail request from peer's side concerning download
                    System.out.println("FAIL");
                    message=(Message) input.readObject();
                    updateAccount1(message.getMessage());

                }else if(message.getMessage().equals("S")) {
                    //success request from peer's side concerning download
                    Message update = (Message) input.readObject();
                    updateAccount2(update.getMessage());
                }else if(message.getMessage().equals("U")){
                    //if peer receives the files then  tracker updates the data structures
                    Message token=(Message)input.readObject();
                    Message name=(Message)input.readObject();

                    for (Map.Entry<String,ArrayList<Integer>> entry : file_token.entrySet()) {
                        if(entry.getKey().equals(name.getMessage())){
                            entry.getValue().add(token.getMessage_int());
                    }
                    }




                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
















    public synchronized boolean reply_register() {
        boolean reg=false;
        try{
            System.out.println("edw");
            Message username=(Message)input.readObject();
            Message password=(Message)input.readObject();
           System.out.println("username: "+username.getMessage());
           System.out.println("password: "+password.getMessage());
//            if(accounts.size()==0){accounts.add(new Account(username.getMessage(), password.getMessage(),0,0));}
            //System.out.println("LISTA");
            for(Account acc: accounts){

                //System.out.println("Username:"+acc.getUsername());
                if(acc.getUsername().equals(username.getMessage())){
                    reg=true;
                    //System.out.println("come on");
                }
            }
            if(!reg) {
                output.writeObject(new Message("Successful registration"));
                output.flush();
                Random rand=new Random();
                accounts.add(new Account(username.getMessage(), password.getMessage(),Math.abs(rand.nextInt(20)),Math.abs(rand.nextInt(20)),123));
            }else{
                output.writeObject(new Message("Unsuccessful registration"));
                output.flush();

            }
        }catch(IOException ioexception){
            ioexception.printStackTrace();

        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }

        return reg;










    }

    public synchronized boolean reply_login(String username,String password){
        boolean login=false;
        System.out.println(username);
        System.out.println(password);
        try{
            for(Account acc: accounts){

                //System.out.println("Username:"+acc.getUsername());
                if(acc.getUsername().equals(username) && acc.getPassword().equals(password)){
                    login=true;

                    //System.out.println("come on");
                }
            }
            if(login) {
                output.writeObject(new Message("Successful login"));
                output.flush();

               // accounts.add(new Account(username.getMessage(), password.getMessage(),0,0));
            }else{
                output.writeObject(new Message("Unsuccessful registration,Try again!"));
                output.flush();

            }


        }catch(IOException ioexception){
            ioexception.printStackTrace();
        }
        return login;


    }


    public synchronized void reply_list()throws IOException{
       // ArrayList<Message> list=(ArrayList<Message>)input.readObject();
        //
        ArrayList<Message> files=new ArrayList<Message>();

        for(String filenames:file_names){
            files.add(new Message(filenames));
        }

        output.writeObject(files);


    }

    public synchronized void reply_details(String name) throws IOException,ClassNotFoundException{
        //Message message=(Message)input.readObject();//takes name of file as input and finds the list of users having this file
        ArrayList<Account> information=new ArrayList<Account>();
        //first reply_details looks up in file_token
        System.out.println(name);
        ArrayList<Integer> token=file_token.get(name+".txt");
        System.out.println(token.size());


        //sends a checkactive request to all peers with this file.if it fails then tracker updates the proper data_structures and cancels token_id


        for(Integer tokens: token ){
            //information.add()
            System.out.println(token);
            for(Account au:active_users){
                if(au.getToken_id()==tokens){
                    System.out.println("here");
                    if(checkActive(au)) {
                        information.add(au);
                    }
                }
            }
        }

          output.writeObject(information);
          output.flush();//sends all the accounts who have this specific file


    }

    public synchronized ArrayList<Message> reply_inform(){
        ArrayList<Message> content=null;
        try{
             content=(ArrayList<Message>)input.readObject();

             for(Message message:content){
                 file_names.add(message.getMessage());
             }
        }catch(IOException ioexception){
            ioexception.printStackTrace();

        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();

        }

        return content;


    }



    public void getStreams() throws IOException {
        output = new ObjectOutputStream(peer.getOutputStream());
        //System.out.println("ela egine to output");
        output.flush();
        input = new ObjectInputStream(peer.getInputStream());
    }

    public  void update_active(int token,ArrayList<Message> inform){
        for(Message var:inform){
            if (file_token.get(var.getMessage()) == null) {
                file_token.put(var.getMessage(), new ArrayList<Integer>());
            }
            file_token.get(var.getMessage()).add(token);

        }



    }

    public synchronized void update_account(int token,String username,int port){

        for(Account a:accounts){
            if(a.getUsername().equals(username)){
               active_users.add(new Account(token,username,port,a.getCount_downloads(),a.getCount_failures()));

            }
        }


    }

    public  void reply_logout(int token){
        for(Account a:active_users){
            if(a.getToken_id()==token){
                active_users.remove(a);
            }
        }


    }

   public void closeConnection(){

       try{
           output.close();
            input.close();
           peer.close();

       }catch(IOException ioException){

           ioException.printStackTrace();

       }
   }

    public boolean checkActive(Account account) throws UnknownHostException,IOException{
        boolean isactive=true;
        try{
            clientCheck=new Socket(InetAddress.getByName("127.0.0.1"),account.getPort());
            getStreamsClient();
            outputClient.writeObject(new Message("T"));
            Message response=(Message)inputClient.readObject();

            //this.sendMessage("A");

        }catch(ConnectException ce) {
            isactive = false;
        }finally{
            closeClientConnection();
            return isactive;
        }

    }


    public void getStreamsClient() throws IOException{
        outputClient=new ObjectOutputStream(clientCheck.getOutputStream());
        outputClient.flush();
        inputClient=new ObjectInputStream(clientCheck.getInputStream());


    }

    public void closeClientConnection(){
        try{
            outputClient.close();
            inputClient.close();
        }catch(IOException ioexception){
            ioexception.printStackTrace();
        }
    }

    public synchronized void updateAccount1(String username){
        for(Account a: accounts){
            if(a.getUsername().equals(username)){
                a.setCount_failures(a.getCount_failures()+1);

            }
        }
        for (Account a: active_users){
            if(a.getUsername().equals(username)){
                a.setCount_failures(a.getCount_failures()+1);

            }

        }
    }
    public synchronized void updateAccount2(String username){
        for(Account a: accounts){
            if(a.getUsername().equals(username)){
                a.setCount_downloads(a.getCount_downloads()+1);

            }
        }
        for (Account a: active_users){
            if(a.getUsername().equals(username)){
                a.setCount_failures(a.getCount_downloads()+1);

            }

        }
    }










}
