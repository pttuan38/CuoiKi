package Models;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public abstract class ImageUtils {
    
    public static ImageIcon fitImageFromFile(JLabel label, File file){
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("[ImageUtils] Lỗi convert tỉ lệ ảnh " + e.getMessage());
        }
        if(img != null){
            Image dimg = img.getScaledInstance(label.getWidth(), label.getHeight(),
            Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(dimg);
            return imageIcon;
        }
        return null;
    }
    
    public static class SetIconForLabel implements Runnable{
        private final String host, fileName;
        private final JLabel label;
        public SetIconForLabel(JLabel label,String host, String fileName){
            this.host = host;
            this.fileName = fileName;
            this.label = label;
        }
        @Override
        public void run() {
            try {
                URL url = new URL(String.format("http://%s:8787/%s",host,fileName));

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                try(InputStream inputStream = connection.getInputStream()){
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    byte[] buffer = new byte[2 * 1024];
//                    int bytesRead;
//                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                        outputStream.write(buffer, 0, bytesRead);
//                    }

//                    byte[] imageBytes = outputStream.toByteArray();
                    BufferedImage img = ImageIO.read(inputStream);
                    Image dimg = img.getScaledInstance(label.getWidth(), label.getHeight(),
                    Image.SCALE_SMOOTH);
                    ImageIcon imageIcon = new ImageIcon(dimg);
                    this.label.setIcon(imageIcon);
                    inputStream.close();
//                    outputStream.close();
                }
            } catch (IOException e) {
                System.err.println("[ImageUtils].ImageDownloader Lỗi  " + e.getMessage());
            }
        }
        
    }
}
