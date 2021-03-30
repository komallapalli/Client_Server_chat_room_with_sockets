// Java implementation of  Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java
 
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
 
// Server class
public class Server 
{
    // public static List<ClientHandler> clients;
    public static HashMap<String, Socket> clients = new HashMap<String, Socket>();
    public static HashMap<String, String> clientsroom = new HashMap<String, String>();
    // public static HashMap<String, Socket> clientsroom1 = new HashMap<String, Socket>();
    static int clientcount = 0;
    static int port = 0;

    public static void main(String[] args) throws IOException 
    {
        // server is listening on port 5056
        if(args.length!=3)
        {
            System.out.println("3 arguments required and username should not have spaces");
            System.exit(0);
        }
        try{
            Integer.parseInt(args[0]);
        } catch (NumberFormatException ex){
            System.out.println("first argeument should be an integer");
            System.exit(0);
        }
        try{
            Integer.parseInt(args[2]);
        } catch (NumberFormatException ex){
            System.out.println("third argeument should be an integer");
            System.exit(0);
        }
        try{
            InetAddress ip = InetAddress.getByName(args[1]);
        } catch (NumberFormatException ex){
            System.out.println("second argeument should be an ip address");
            System.exit(0);
        }
        ServerSocket ss = new ServerSocket(Integer.parseInt(args[2]));
        port = Integer.parseInt(args[2]);
        int max_limit = Integer.parseInt(args[0]);
        // System.out.println(max_limit);
        // running infinite loop for getting
        // client request
        while (clientcount<max_limit) 
        {
            System.out.println("waiting for client to join...");
            Socket s = null;
            try
            {
                // socket object to receive incoming client requests
                s = ss.accept();
                System.out.println("A new client is connected : " + s);
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                System.out.println("Assigning new thread for this client");

                String username = dis.readUTF();
                // Foo value = clients.get(username);
                System.out.println(clients.get(username));
                Boolean flag = true; 
                if(clients.get(username)!=null){
                    dos.writeUTF("error");
                    flag = false;
                    try{
                        s.close();
                        dis.close();
                        dos.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    continue;
                }
                else{
                    dos.writeUTF("no-error");   
                }
                if(flag){
                    clients.put(username, s);
                    clientsroom.put(username, Integer.toString(-1));
                    // clientsroom1.put(Integer.toString(-1), s); 
                    clientcount += 1;
                    Thread t = new ClientHandler(username, s, dis, dos);
                    t.start();
                }
                 
            }
            catch (Exception e){
                System.out.println("Closed");
                s.close();
                e.printStackTrace();
            }
        }
    }
}
 
// ClientHandler class
class ClientHandler extends Thread 
{
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    final String username;
     
 
    // Constructor
    public ClientHandler(String username, Socket s, DataInputStream dis, DataOutputStream dos) 
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.username = username;
    }

    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
 
    @Override
    public void run() 
    {
        String received;
        while (true) 
        {
            try {
                received = this.dis.readUTF();
                System.out.println(received);
                String user_room = new String();
                List<Socket> receiver_list = new ArrayList<Socket>();
                List<Socket> user_list = new ArrayList<Socket>();
                user_list.add(this.s);
                for (Map.Entry<String, String> room : Server.clientsroom.entrySet()){
                    if(this.username.equals(room.getKey())){
                        user_room = room.getValue();
                        break;
                    }
                }
                String[] command = received.split(" ");
                if(command[0].equals("leave"))
                {
                    System.out.println("Leave command from user "+this.username);
                    System.out.println("clients "+Server.clients);
                    System.out.println("clientsroom "+Server.clientsroom);
                    if(command.length!=1)
                    {
                        Thread t = new MsgHandler("Msg not found", user_list);
                        t.start();  
                    }
                    else if(user_room.equals("-1"))
                    {
                        Thread t = new MsgHandler("You are not in any chatroom to leave",user_list);
                        t.start();
                    }
                    else
                    {
                        for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                            if(user_room.equals(entry1.getValue())){
                                for (Map.Entry<String, Socket> entry2 : Server.clients.entrySet()){
                                    if((entry1.getKey()).equals(entry2.getKey())){
                                        Socket x = entry2.getValue();
                                        receiver_list.add(x);
                                    }
                                }
                            }
                        }
                        for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                            if(this.username.equals(entry1.getKey()))
                            {
                                String original = entry1.getValue();
                                Server.clientsroom.put(entry1.getKey(), "-1");
                                Thread t = new MsgHandler("You are left from chatroom "+original, user_list);
                                t.start();
                            }
                        }
                        receiver_list.remove(this.s);
                        Thread t = new MsgHandler(this.username+" left chatroom", receiver_list);
                        t.start();
                    }
                }
                else if(command[0].equals("reply") && command[command.length-1].equals("tcp"))
                {
                    System.out.println("reply tcp command from user "+this.username);
                    for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                        if(user_room.equals(entry1.getValue()) && (!(user_room.equals("-1")))){
                            for (Map.Entry<String, Socket> entry2 : Server.clients.entrySet()){
                                if((entry1.getKey()).equals(entry2.getKey())){
                                    Socket x = entry2.getValue();
                                    receiver_list.add(x);
                                }
                            }
                        }
                    }
                    receiver_list.remove(this.s);
                    String message = "filetcp "+this.username+" "+command[1];
                    // for(int i=1;i<command.length;i++)
                        // message += command[i] + " ";
                    Thread t = new MsgHandler(message, receiver_list);
                    t.start();
                    Long size = this.dis.readLong();
                    int filesize = size.intValue();
                    for(int i=0;i < receiver_list.size(); ++i){
                        Socket recv = receiver_list.get(i);
                        try{
                            DataOutputStream dos = new DataOutputStream(recv.getOutputStream());
                            dos.writeInt(filesize);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        // dos.close();
                    }
                    

                    byte[] buffer = new byte[4096];
                    int read = 0;
                    int totalRead = 0;
                    int remaining = filesize;
                    while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;
                        System.out.println("read " + totalRead + " bytes.");
                        for(int i=0;i < receiver_list.size(); ++i){
                            Socket recv = receiver_list.get(i);
                            try{
                                DataOutputStream dos = new DataOutputStream(recv.getOutputStream());
                                dos.write(buffer,0,read);
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            // dos.close();
                        }
                    }
                    System.out.println("done sending file :)");
                    Thread tt = new MsgHandler("Your file has benn sent to all users in chat room", user_list); 
                    tt.start();
                }
                else if(command[0].equals("reply") && command[command.length-1].equals("udp"))
                {
                    Server.port += 1;
                    System.out.println("reply udp command from user "+this.username);
                    for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                        if(user_room.equals(entry1.getValue()) && (!(user_room.equals("-1")))){
                            for (Map.Entry<String, Socket> entry2 : Server.clients.entrySet()){
                                if((entry1.getKey()).equals(entry2.getKey())){
                                    Socket x = entry2.getValue();
                                    receiver_list.add(x);
                                }
                            }
                        }
                    }
                    receiver_list.remove(this.s);
                    String message = "fileudp "+this.username+" "+command[1];
                    // for(int i=1;i<command.length;i++)
                        // message += command[i] + " ";
                    Thread t = new MsgHandler(message, receiver_list);
                    t.start();

                    DatagramSocket ds = new DatagramSocket(Server.port);
                    byte[] receive = new byte[65535];
                    DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);
                    ds.receive(DpReceive);
                    // String file_content = data(receive).toString();
                    while(!(data(receive).toString().equals("bye"))){
                        // System.out.println("file:"+data(receive));
                        t = new MsgHandler(data(receive).toString(), receiver_list);
                        t.start();
                        receive = new byte[165535];
                        DpReceive = new DatagramPacket(receive, receive.length);
                        ds.receive(DpReceive);
                        // System.out.println("file:"+data(receive));
                        // file_content = data(receive).toString();
                    }
                    // String file_content = this.dis.readUTF();
                    // while(file_content!="bye"){
                    //     t = new MsgHandler(file_content, receiver_list);
                    //     t.start();
                    //     file_content = this.dis.readUTF();
                    // }
                    System.out.println("Done sending file :)");
                    t = new MsgHandler("bye", receiver_list);
                    t.start();
                    Thread tt = new MsgHandler("Your file has benn sent to all users in chat room", user_list); 
                    tt.start();
                }
                else if(command[0].equals("reply"))
                {
                    if(user_room.equals("-1"))
                    {
                        Thread t = new MsgHandler("You are not in any chatroom",user_list);
                        t.start();
                        continue;
                    }
                    System.out.println("Reply command from user "+this.username);
                    System.out.println("clients "+Server.clients);
                    System.out.println("clientsroom "+Server.clientsroom);
                    for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                        if(user_room.equals(entry1.getValue())){
                            for (Map.Entry<String, Socket> entry2 : Server.clients.entrySet()){
                                if((entry1.getKey()).equals(entry2.getKey())){
                                    Socket x = entry2.getValue();
                                    receiver_list.add(x);
                                }
                            }
                        }
                    }
                    String out = this.username+"-->";
                    receiver_list.remove(this.s);
                    for(int i=1;i<command.length;i++)
                    {
                        out = out + command[i]+" "; 
                    }
                    Thread t = new MsgHandler(out, receiver_list);
                    t.start();
                    Thread tt = new MsgHandler("Your message has benn sent to all users in chat room", user_list); 
                    tt.start();
                }
                else if(command[0].equals("create") && command[1].equals("chatroom"))
                {
                    System.out.println("Create chatroom command from user "+this.username);
                    System.out.println("clients "+Server.clients);
                    System.out.println("clientsroom "+Server.clientsroom);
                    receiver_list.add(this.s);
                    if(command.length!=3)
                    {
                        receiver_list.add(this.s);
                        Thread t = new MsgHandler("Chatroom should not have spaces", receiver_list);
                        t.start();  
                    }
                    else
                    {
                        for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                            if(command[2].equals(entry1.getValue()) && this.username.equals(entry1.getKey())){
                                Thread t = new MsgHandler("Chatroom should not have spaces", receiver_list);
                                t.start();
                            }
                            else if(this.username.equals(entry1.getKey()))
                            {
                                Server.clientsroom.put(entry1.getKey(), command[2]);
                                Thread t = new MsgHandler("Created and Joined chatroom "+command[2], receiver_list);
                                t.start();
                            }
                        }
                    }
                }
                else if(command[0].equals("list") && command[1].equals("users") && command.length==2)
                {
                    if(user_room.equals("-1"))
                    {
                        Thread t = new MsgHandler("You are not in any chatroom",user_list);
                        t.start();
                        continue;
                    }
                    String out = "users-->";
                    receiver_list.add(this.s);
                    for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                        if(user_room.equals(entry1.getValue())){
                            out += entry1.getKey()+",";
                        }
                    }
                    Thread t = new MsgHandler(out, receiver_list);
                    t.start();
                }
                else if(command[0].equals("list") && command[1].equals("chatrooms") && command.length==2)
                {
                    Set<String> chatrooms = new HashSet<String>();
                    receiver_list.add(this.s);
                    for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                        chatrooms.add(entry1.getValue());
                    }
                    String out = "chatrooms-->";
                    for (String s : chatrooms) {
                        out += s+",";
                    }
                    Thread t = new MsgHandler(out, receiver_list);
                    t.start();
                }
                else if(command[0].equals("join"))
                {
                    System.out.println("join chatroom "+ command[1]+" command from user "+this.username);
                    System.out.println("clients "+Server.clients);
                    System.out.println("clientsroom "+Server.clientsroom);
                    if(command.length!=2){
                        Thread t = new MsgHandler("Chatroom not found and should not have spaces", user_list);
                        t.start();
                    }
                    else
                    {
                        Boolean found_room = false;
                        Boolean same_room = false;
                        for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                            if(command[1].equals(entry1.getValue()))
                            {
                                found_room = true;
                            }
                            if(command[1].equals(entry1.getValue()) && this.username.equals(entry1.getKey()))
                            {
                                same_room = true;
                            }
                        }
                        if(same_room){
                            Thread t = new MsgHandler("You are already in that chatroom", user_list);
                            t.start();
                        }
                        else if(!(found_room))
                        {
                            Thread t = new MsgHandler("Chatroom not found", user_list);
                            t.start();
                        }
                        else{
                            for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                                if(this.username.equals(entry1.getKey()))
                                {
                                    Server.clientsroom.put(entry1.getKey(), command[1]);
                                    Thread t = new MsgHandler("You are joined joined in the chatroom-->"+command[1], user_list);
                                    t.start();
                                    break;
                                }
                            }
                            for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                                if(command[1].equals(entry1.getValue())){
                                    for (Map.Entry<String, Socket> entry2 : Server.clients.entrySet()){
                                        if((entry1.getKey()).equals(entry2.getKey())){
                                            Socket x = entry2.getValue();
                                            receiver_list.add(x);
                                        }
                                    }
                                }
                            }
                            receiver_list.remove(this.s);
                            System.out.println(receiver_list);
                            Thread t = new MsgHandler(this.username + " added to Chatroom-->"+command[1], receiver_list);
                            t.start();
                        }
                    }
                }
                else if(command[0].equals("add"))
                {
                    Boolean found_user = false;
                    String user_old_chatroom = new String();
                    String user_new_chatroom = new String();
                    for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                        if(command[1].equals(entry1.getKey())){
                            found_user = true;
                            user_old_chatroom = entry1.getValue();
                        }
                        if(this.username.equals(entry1.getKey()))
                            user_new_chatroom = entry1.getValue();
                    }
                    if(!(found_user)){
                        Thread t = new MsgHandler("user is not available", user_list);
                        t.start();
                    }
                    else if(user_new_chatroom.equals(user_old_chatroom)){
                        Thread t = new MsgHandler(command[1]+" is already in chatroom-->"+user_old_chatroom, user_list);
                        t.start();   
                    }
                    else{
                        Server.clientsroom.put(command[1], user_new_chatroom);
                        Thread t = new MsgHandler(command[1]+" is added to  chatroom-->"+user_new_chatroom, user_list);
                        t.start();
                        for (Map.Entry<String, String> entry1 : Server.clientsroom.entrySet()){
                            if(user_new_chatroom.equals(entry1.getValue())){
                                for (Map.Entry<String, Socket> entry2 : Server.clients.entrySet()){
                                    if((entry1.getKey()).equals(entry2.getKey())){
                                        Socket x = entry2.getValue();
                                        receiver_list.add(x);
                                    }
                                }
                            }
                        }
                    }
                    receiver_list.remove(this.s);
                    Thread t = new MsgHandler(command[1] + " is added to Chatroom-->"+user_new_chatroom, receiver_list);
                    t.start();
                }
                else
                {
                    receiver_list.add(this.s);
                    Thread t = new MsgHandler("Msg not found", receiver_list);
                    t.start();
                }
               
            }catch(IOException e){
                e.printStackTrace();  
                break;          
            }
        }
        try
        {
            this.dis.close();
            this.dos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

class MsgHandler extends Thread 
{
    String message;
    List<Socket> receiver_list = new ArrayList<Socket>();
    public MsgHandler(String message, List<Socket> receiver_list) 
    {
        this.message = message;
        this.receiver_list = receiver_list;
    }
    
    @Override
    public void run() 
    {
        // String[] split_msg = this.message.split(" ");
        // if(split_msg[0].equals("file"))
        // {
            // System.out.println(this.message); 
            for(int i=0;i < this.receiver_list.size(); ++i){
                Socket recv = this.receiver_list.get(i);
                try{
                    DataOutputStream dos = new DataOutputStream(recv.getOutputStream());
                    dos.writeUTF(this.message);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
        // }
        // else
        // {
        //     for(int i=0;i < this.receiver_list.size(); ++i){
        //         Socket recv = this.receiver_list.get(i);
        //         try{
        //             DataOutputStream dos = new DataOutputStream(recv.getOutputStream());
        //             dos.writeUTF(this.message);
        //         }
        //         catch (IOException e) {
        //             e.printStackTrace();
        //         }

        //     }
        // }
    }
}