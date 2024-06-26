package Models;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Message {
    public int idSend;
    public String nameSend;
    public String message;
    public String time;
    
    public Message(int idSend, String nameSend, String message) {
        this.idSend = idSend;
        this.nameSend = nameSend;
        this.message = message;
        this.time = new SimpleDateFormat("[HH:mm:ss - dd/MM/yyyy] ").format(Calendar.getInstance().getTime());
    }
}
