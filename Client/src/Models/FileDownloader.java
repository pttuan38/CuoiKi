package Models;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JOptionPane;

public class FileDownloader implements Runnable{
    private String fileId, path;
    
    public FileDownloader(String path, String fileName){
        this.fileId = fileName;
        this.path = path;
    }
    
    @Override
    public void run() {
        try {
            if(fileId.contains(" ")){
                fileId = fileId.replaceAll("\\ ", "");
            }
            URL url = new URL(String.format("http://%s:8787/%s",Session.host,fileId));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            try(InputStream inputStream = connection.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                byte[] buffer = new byte[64 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                FileOutputStream fos = new FileOutputStream(new File(path));
                outputStream.writeTo(fos);
                
                inputStream.close();
                outputStream.close();
                fos.close();
                JOptionPane.showMessageDialog(null, "Đã tải xong file tại đường dẫn " + path);
            }
        } catch (IOException e) {
            System.err.println("[FileDownloader] Lỗi " + e.getMessage());
        }
    }

}