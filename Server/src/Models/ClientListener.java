package Models;

import java.net.Socket;
import java.io.*;

public class ClientListener extends Client implements Runnable{
    private BufferedReader reader;
    private PrintWriter writer;
    protected Socket socket; 
    
    public ClientListener(Socket socket){
        this.idClient = Client.currentID++;
        
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            
            writer.println(this.idClient + "");
            this.nameClient = reader.readLine();
            System.out.println("[SOCKET CONNECT] "+ nameClient + " đã tham gia server !");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Handle listener
    @Override
    public void run() {
        while(true){
            try {
                while(true){
                    String msgFromClient = this.reader.readLine();
                    String function = msgFromClient.split("\\-")[0];
                    String data = msgFromClient.split("\\-")[1];
                    handleMessage(msgFromClient, function, data);
                }
            } catch (IOException e) {
                //e.printStackTrace();
                Service.gI().removeClient(idClient);
                if(this.room != null){
                    handleMessage(String.format("leave_room-%d-1",this.room.id), "leave_room", this.room.id + "");
                }
                System.out.println(String.format("[SOCKET CLOSED] User %d - %s đã rời khỏi server.",idClient, nameClient));
                break;
            }
        }
    }
    
    public void handleMessage(String msgFromClient, String function, String data){
        switch(function){
            case "get_all_rooms":{
                String msg = "rooms|";
                for(Room room: Service.gI().rooms){
                    msg += room.toSendViaNetwork() + ",";
                }
                if(msg.equals("rooms|")){
                    msg = "rooms|null";
                }
                else{
                    msg = msg.substring(0, msg.length() - 1);
                }
                sendCommand(msg);
                break;
            }
            case "create_room":{
                Room room = new Room(data, idClient);
                room.idOwner = idClient;
                room.addMember(this);
                this.room = room;
                Service.gI().rooms.add(room);
                this.sendCommand("accept_create|" + room.id);
                Service.gI().sendGlobalMessage("reload_rooms|null");
                break;
            }
            case "join_room":{
                boolean hasExistRoom = false;
                int id = Integer.parseInt(data);
                for(Room room: Service.gI().rooms){
                    if(room.id == id){
                        hasExistRoom = true;
                        if(room.getPassword().equals("")){
                            this.room = room;
                            room.addMember(this);
                            this.sendCommand("accept_join|" + room.id);
                            Service.gI().sendGlobalMessage("reload_rooms|null");
                            this.room.updateListUserToClients();
                        }
                        else{
                            this.sendCommand("request_password|" + id);
                        }
                        break;
                    }
                }
                if(!hasExistRoom){
                    this.sendCommand("deny_join|" + id);
                }
                break;
            }
            case "joinchat":{
                this.room.loadMessagesToNewClient(this);
                this.room.sendGlobalMessage(-1, String.format("%s đã tham gia phòng chat", nameClient));
                this.room.updateListUserToClients();
                break;
            }
            case "get_persons":{
                this.room.updateListUserToClients();
                break;
            }
            case "check_admin":{
                if(this.room != null){
                    int res = this.room.idOwner == idClient ? 1 : 0;
                    this.sendCommand("admin|" + res);
                }
                break;
            }
            case "leave_room":{
                int id = Integer.parseInt(data);
                boolean haveAskClient = Integer.parseInt(msgFromClient.split("\\-")[2]) == 1 ? true : false;
                Room r = Room.getRoomById(id);
                if(r != null){
                    if(idClient != r.idOwner){
                        r.removeMember(this);
                        this.room = null;
                        r.sendGlobalMessage(-1, nameClient + " đã rời khỏi phòng chat !");
                        this.sendCommand("accept_leave|"+r.id);
                        Service.gI().sendGlobalMessage("reload_rooms|null");
                        r.updateListUserToClients();
                        if(r.id == 0 && r.getMembers().isEmpty()){
                            System.out.println("[Free] Free resources after " + Service.delayFreeResources/1000 + "s");
                            new Thread(new Service.FreeAllResources(r, Service.delayFreeResources)).start();
                        }
                    }
                    else{
                        if(haveAskClient){
                            r.freeAllResources();
                            this.room = null;
                            Service.gI().rooms.remove(r);
                            Service.gI().sendGlobalMessage("reload_rooms|null");
                        }
                        else{
                            this.sendCommand("ask_leave|"+r.nameRoom);
                        }
                    }
                    
                }
                break;
            }
            case "send_msg":{
                int idSend = Integer.parseInt(data);
                String message = msgFromClient.split("\\-")[2];
                if(this.room != null){
                    this.room.sendGlobalMessage(idSend, message);
                }
                break;
            }
            case "kick":{
                if(this.room.idOwner == idClient){
                    int id = Integer.parseInt(data);
                    this.room.kickPerson(id);
                    this.room.updateListUserToClients();
                    Service.gI().sendGlobalMessage("reload_rooms|null");
                }
                break;
            }
            case "set_password":{
                if(this.room != null){
                    boolean isAdmin = this.room.idOwner == idClient;
                    if(isAdmin){
                        this.room.setPassword(data);
                        this.sendCommand("accept_set_password|null");
                    }else{
                        this.sendCommand("deny_set_password|null");
                    }
                }
                break;
            }
            case "check_password":{
                Room r = Room.getRoomById(Integer.parseInt(data));
                String password = msgFromClient.split("\\-")[2];
                if(r.getPassword().equals(password)){
                    this.room = r;
                    r.addMember(this);
                    this.sendCommand("accept_join|" + r.id);
                    Service.gI().sendGlobalMessage("reload_rooms|null");
                    r.updateListUserToClients();
                }
                else{
                    this.sendCommand("password|0");
                }
                break;
            }
            case "upload_file":{
                new Thread(new Service.FileDownloader(this, data)).start();
                break;
            }
            case "get_files":{
                if(this.room != null){
                    String command = "files|";
                    for(Attachment att : this.room.getAttachments()){
                        command += att.toString() +",";
                    }
                    if(this.room.getAttachments().isEmpty()){
                        command = "files|null";
                    }
                    else{
                        command = command.substring(0, command.length() - 1);
                    }
                    this.sendCommand(command);
                }
                break;
            }
            case "delete_file":{
                if(this.room != null){
                    String fileName = data.split("\\__")[1];
                    String command = "file_not_found|" + fileName;
                    for(Attachment att: this.room.getAttachments()){
                        String fileId = att.file.getName();
                        if(fileId.equals(data)){
                            if(att.idOwner == idClient){
                                this.room.removeAttachment(att);
                                this.room.sendGlobalCommand("new_attachment|null");
                                this.room.sendGlobalMessage(-1, nameClient + " đã xóa file " + fileName + " khỏi phòng chat !");
                                command = "accept_delete_file|" + fileName;
                                break;
                            }
                            else{
                                command = "not_owner|" + fileName;
                                break;
                            }
                        }
                    }
                    this.sendCommand(command);
                }
                break;
            }
        }
    }
    
    public void sendCommand(String msgCommand){
        this.writer.println(msgCommand);
    }
    
}
