// Java implementation for a client
// Save file as Client.java
 
import java.io.*;
import java.net.*;
import java.util.Scanner;
 
// Client class
public class Client 
{
    static int port = 0;
    public static void main(String[] args) throws IOException 
    {
        try
        {
            Scanner scn = new Scanner(System.in);
             
            // getting localhost ip
     
            // establish the connection with server port 5056
            if(args.length!=3)
            {
                System.out.println("3 arguments required and username should not have spaces");
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
            port = Integer.parseInt(args[2]);
            InetAddress ip = InetAddress.getByName(args[1]);
            Socket s = new Socket(ip, Integer.parseInt(args[2]));
     
            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(args[0]);
            String received = dis.readUTF();
            if(received.equals("error")){
                System.out.println("this username: "+args[0]+" is already used by another user");
                try{
                    s.close();
                    dis.close();
                    dos.close();
                    System.exit(0);
                }catch (Exception e){
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            // the following loop performs the exchange of
            // information between client and client handler
            System.out.print(">>");
            Thread t = new ClientReceiving(s, dis);
            t.start();

            t = new ClientSending(s, dos);
            t.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class ClientReceiving extends Thread
{
    final DataInputStream dis;
    final Socket s;

    public ClientReceiving(Socket s, DataInputStream dis)
    {
        this.s = s;
        this.dis = dis;
    }

    @Override
    public void run() 
    {
        while (true) 
        {
            try{
                String received = this.dis.readUTF();
                // System.out.println("");
                String[] split_received  = received.split(" ");
                if(split_received[0].equals("filetcp"))
                {
                    System.out.println(" Receiving "+split_received[2]+" from "+split_received[1]);
                    FileOutputStream fos = new FileOutputStream("client_"+split_received[2]);
                    byte[] buffer = new byte[4096];
                    int filesize = this.dis.readInt();
        
                    int read = 0;
                    int totalRead = 0;
                    int remaining = filesize;
                    while((read = this.dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;
                        // System.out.println("read " + totalRead + " bytes.");
                        fos.write(buffer, 0, read);
                    }
                    fos.close();
                    // try {  
                    //     BufferedWriter bufWriter = new BufferedWriter(new FileWriter(split_received[2]));
                    //     String file_content = this.dis.readUTF();
                    //     while(!(file_content.equals("bye"))){
                    //         bufWriter.write(file_content+"\n");  
                    //         file_content = this.dis.readUTF();
                    //         // System.out.println("file:"+file_content);
                    //     } 
                    //     System.out.println(" Received "+split_received[2]+" from "+split_received[1]);
                    //     bufWriter.close();  
                    // }catch(Exception e){
                    //    e.printStackTrace();
                    //     break;
                    // }
                    System.out.println("Received file");  
                }
                else if(split_received[0].equals("fileudp"))
                {
                    System.out.println(" Receiving "+split_received[2]+" from "+split_received[1]);
                    try {  
                        BufferedWriter bufWriter = new BufferedWriter(new FileWriter("client"+split_received[2]));
                        String file_content = this.dis.readUTF();
                        while(!(file_content.equals("bye"))){
                            bufWriter.write(file_content+"\n");  
                            file_content = this.dis.readUTF();
                            // System.out.println("file:"+file_content);
                        } 
                        System.out.println(" Received "+split_received[2]+" from "+split_received[1]);
                        bufWriter.close();  
                    }catch(Exception e){
                       e.printStackTrace();
                        break;
                    }  
                }
                // if(received != null && !received.isEmpty())
                else
                    System.out.println(received);
                System.out.print(">>");   
            }catch(Exception e){
                e.printStackTrace();
                break;
            }

        }
    }
}

class ClientSending extends Thread
{
    Scanner scn = new Scanner(System.in);
    final DataOutputStream dos;
    final Socket s;
    public ClientSending(Socket s, DataOutputStream dos)
    {
        this.s = s;
        this.dos = dos;
    }

    @Override
    public void run() 
    {
        while (true) 
        {
            try{
                String tosend = scn.nextLine();
                String[] split_to_send = tosend.split(" ");
                if(split_to_send[0].equals("reply") && split_to_send[split_to_send.length-1].equals("tcp") )
                {
                    String filename = split_to_send[1];
                    Boolean check_file = new File("./", filename).exists();
                    if(!(check_file))
                       System.out.println("file:"+filename+" not found in the current directiory");
                    else{
                        this.dos.writeUTF(tosend);       
                    } 
                    try{  
                        File x = new File(filename);
                        FileInputStream fis = new FileInputStream(x);
                        byte[] buffer = new byte[4096];
                        // BufferedReader bufReader = new BufferedReader(new FileReader(filename));  
                        // String data = new String();  
                        this.dos.writeLong(x.length());
                        int read;
                        while ((read=fis.read(buffer)) !=-1){  
                            // System.out.println(data);  
                            this.dos.write(buffer,0,read);
                        } 
                        fis.close(); 
                    }catch (FileNotFoundException e) {  
                        e.printStackTrace();  
                    } 
                }
                else if(split_to_send[0].equals("reply") && split_to_send[split_to_send.length-1].equals("udp") )
                {
                    Client.port += 1;
                    String filename = split_to_send[1];
                    Boolean check_file = new File("./", filename).exists();
                    if(!(check_file))
                       System.out.println("file:"+filename+" not found in the current directiory");
                    else{
                        this.dos.writeUTF(tosend);       
                    } 
                    try{  
                       DatagramSocket ds = new DatagramSocket();
 
                        InetAddress ip = InetAddress.getLocalHost();
                        byte buf[] = null;
                 
                        // loop while user not enters "bye"
                        BufferedReader bufReader = new BufferedReader(new FileReader(filename));  
                        String data = new String(); 
                        data = bufReader.readLine(); 
                        while (data  != null) {  
                            // System.out.println(data);
                            buf = data.getBytes();  
                            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip,Client.port);
                            ds.send(DpSend);
                            data = bufReader.readLine();
                        }
                        
                        data = "bye";
                        buf = data.getBytes();  
                        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, Client.port);
                        ds.send(DpSend);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else
                {
                    this.dos.writeUTF(tosend);
                }
                // System.out.print(">>");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}   