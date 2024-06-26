package Models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class RunnableServerFile implements Runnable{
    private final File fileTarget; 
    
    public RunnableServerFile(File fileTarget) {
        this.fileTarget = fileTarget;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(7878); 
            Socket clientSocket = serverSocket.accept(); 

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = in.readLine();

            if (request.contains("GET")) {
                //example : "GET / HTTP/1.1"
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + Files.probeContentType(fileTarget.toPath()));
                out.println();

                try(FileInputStream fileInputStream = new FileInputStream(fileTarget)){
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
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Client.[RunnableServerFile] Lỗi " + e.getMessage());
        }
    }
}
