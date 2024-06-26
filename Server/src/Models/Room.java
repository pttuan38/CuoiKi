package Models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public static int currentID = 0;
    
    public int id;
    public String nameRoom;
    public int idOwner;
    private String password = "";
    private final List<ClientListener> members = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private final List<Attachment> attachments = new ArrayList<>();
           
    public Room(String nameRoom, int idOwner) {
        this.id = Room.currentID++;
        this.nameRoom = nameRoom;
        this.idOwner = idOwner;
    }
    
    public void addMember(ClientListener newClient){
        members.add(newClient);
    }
    
    public List<ClientListener> getMembers(){
        return this.members;
    }
    
    public List<Attachment> getAttachments(){
        return this.attachments;
    }
    
    public void removeAttachment(Attachment att){
        att.destruct();
        this.attachments.remove(att);
    }
    
    public void removeMember(ClientListener client){
        members.remove(client);
    }
    
    public int getSize(){
        return members.size();
    }
    
    public String getNameMemberByID(int id){
        if(id == -1) return "[Server]";
        String name = "Unknown";
        for(ClientListener client : members){
            if(client.idClient == id){
                return client.nameClient;
            }
        }
        return name;
    }
    
    public static Room getRoomById(int id){
        for (Room r : Service.gI().rooms) {
            if(r.id == id) return r;
        }
        return null;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    
    public void loadMessagesToNewClient(ClientListener client){
        for(Message msg: messages){
            String message = String.format("new_message|%s %s: %s", msg.nameSend,msg.time, msg.message);
            client.sendCommand(message);
        }
    }
    
    public void sendGlobalMessage(int idSend, String message){
        Message messageI = new Message(idSend,getNameMemberByID(idSend), message);
        String msg = String.format("new_message|%s %s: %s", getNameMemberByID(idSend), messageI.time, message);
        for(ClientListener client : members){
            if(idSend != client.idClient){
                client.sendCommand(msg);
            }
        }
        this.messages.add(messageI);
    }
    
    public void sendGlobalCommand(String command){
        for(ClientListener client : members){
            client.sendCommand(command);
        }
    }
    
    public void updateListUserToClients(){
        if(this != null){
            String command = "persons|";
            for(var member : this.getMembers()){
                command += member.idClient + "#" + member.nameClient + "#";
                command += member.idClient == this.idOwner ? "Chủ phòng," : "Thành viên,";
            }
            command = command.substring(0, command.length() - 1);
            sendGlobalCommand(command);
        }
    }
    
    
    public void addNewAttachment(Attachment attachment){
        this.attachments.add(attachment);
        this.sendGlobalCommand("new_attachment|null");
    }
    
    public void kickPerson(int id){
        String namePerson = getNameMemberByID(id);
        boolean hasKick = false;
        for(ClientListener client : members){
            if(id == client.idClient){
                client.sendCommand("kick|" + getNameMemberByID(this.idOwner));
                client.room = null;
                this.members.remove(client);
                hasKick = true;
                break;
            }
        }
        if(hasKick){
            String msg = String.format("Chủ phòng đã kích %s ra khỏi phòng chat !",namePerson);
            this.sendGlobalMessage(-1 , msg);
        }
    }
    
    public void kickAllPerson(){
        for(ClientListener client : members){
            client.sendCommand("roomHasBeenDeleted|null");
        }
        this.members.clear();
        this.messages.clear();
    }
    
    
    public String toSendViaNetwork(){
        return String.format("%d#%s#%s#%d", id,nameRoom, getNameMemberByID(idOwner), getSize());
    }
    
    public void freeAllResources(){
        this.kickAllPerson();
        for(Attachment att : attachments){
            att.destruct();
        }
        this.attachments.clear();
    }
}
