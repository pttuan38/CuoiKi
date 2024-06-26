package Models;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Service {
    public static long delayFreeResources = 3000;
    
    
    public List<ClientListener> clients = new ArrayList<>();
    public List<Room> rooms = new ArrayList<>();
    public List<Attachment> attachments = new ArrayList<>();
    
    public static String pathResources = "src/files/";
    private final static Service service = new Service();
    
    public static Service gI(){
        return service;
    }
    
    public void sendGlobalMessage(String command){
        for(ClientListener client: this.clients){
            client.sendCommand(command);
        }
    }
    
    public void removeClient(int id){
        for(ClientListener client: this.clients){
            if(client.idClient == id){
                this.clients.remove(client);
                break;
            }
        }
    }
    
    public static class RunnableListenerCLient  implements Runnable{
        @Override
        public void run(){
            try {
                ServerSocket ss = new ServerSocket(8989, 10000);
                System.out.println("[RunnableListenerCLient] Running on port 8989 ...");
                while(true){
                    Socket clientSocket = ss.accept();
                    ClientListener clientListener = new ClientListener(clientSocket);
                    Service.gI().clients.add(clientListener);
                    new Thread(clientListener).start();
                }
            } catch (IOException e) {
                System.err.println("[RunnableListenerCLient] Can't start serversocket RunnableListenerCLient ! "+e.getMessage());
            }
        }
    }
    
    public static class RunnableServerFile implements Runnable{
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(8787); 
                System.out.println("[RunnableServerFile] Running on port 8787 ...");

                while (true) {
                    Socket clientSocket = serverSocket.accept(); 

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String request = in.readLine();
                    
                    if (request.contains("GET")) {
                        //example : "GET /image.png HTTP/1.1"
                        String fileName = request.split("\\ ")[1].replaceAll("\\/", "");
                        File file = new File(Service.pathResources + fileName);
                        
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: " + Files.probeContentType(file.toPath()));
                        out.println();

                        try(FileInputStream fileInputStream = new FileInputStream(file)){
                            byte[] buffer = new byte[64 * 1024];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                            }
                            fileInputStream.close();
                        }
                    } else {
                        out.println("HTTP/1.1 404 Not Found");
                    }

                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("[RunnableServerFile] Error " + e.getMessage());
            }
        }
        
    }
    
    public static class FileDownloader implements Runnable{
        private final ClientListener client;
        private final String fileName;

        public FileDownloader(ClientListener client, String fileName){
            this.client = client;
            this.fileName = client.nameClient.replaceAll("\\ ", "") + "__" + fileName;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(String.format("http://%s:7878",client.socket.getInetAddress().getHostAddress()));

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                try(InputStream inputStream = connection.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    if(!Files.exists(new File(Service.pathResources).toPath())){
                        Files.createDirectory(new File(Service.pathResources).toPath());
                    }
                    File file = new File(Service.pathResources + fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    outputStream.writeTo(fos);

                    inputStream.close();
                    outputStream.close();
                    fos.close();
                    client.sendCommand("accept_upload_file|" +fileName);
                    try{
                        Attachment attach = new Attachment(client, file);
                        client.room.addNewAttachment(attach);
                        Service.gI().attachments.add(attach);
                    }
                    catch(IOException x){
                        System.err.println("[Service].FileDownloader Error " + x.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("[Service].FileDownloader Error " + e.getMessage());
                client.sendCommand("deny_upload_file|" +fileName);
            }
        }
    }
    
    public static class FreeAllResources implements Runnable{
        private final Room room;
        private final long delay;

        public FreeAllResources(Room room, long delay) {
            this.room = room;
            this.delay = delay;
        }
        
        @Override
        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.err.println("[Service].FreezeAllResources Error " + e.getMessage());
            }
            finally{
                if(this.room.getMembers().isEmpty()){
                    System.out.println("[Free] Free resources in room " + room.nameRoom);
                    this.room.freeAllResources();
                }
            }
        }
        
    }
}
