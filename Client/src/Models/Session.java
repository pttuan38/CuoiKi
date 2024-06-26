package Models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class Session{
    public static String host = "127.0.0.1";
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    public boolean isConnected = false;
    public final Object monitor = new Object();
    
    public String personName;
    public String personID;
    
    private final static Session session = new Session();
    
    public static Session gI(){
        return Session.session;
    }
    
    public void set(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(),"UTF-8"));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);
        } catch (IOException e) {
            
        }
    }
    
    public void sendMessage(String message){
        try {
            System.out.println("[Command] " + message);
            this.writer.println(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public BufferedReader getReader() {
        return reader;
    }
    
    public void sendFile(File file){
        new Thread(new RunnableServerFile(file)).start();
        this.sendMessage("upload_file-" + file.getName().replaceAll("\\-", "_"));
    }
    
}
