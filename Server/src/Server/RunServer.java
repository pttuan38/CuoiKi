package Server;

import Models.*;

public class RunServer {
    
    public static void main(String[] args) {
//        Properties properties = new Properties();
//        properties.setProperty("file.encoding", "UTF-8");
//        System.setProperties(properties);
//        System.out.println("Current encoding : " + System.getProperty("file.encoding"));
        new Thread(new Service.RunnableListenerCLient()).start();
        new Thread(new Service.RunnableServerFile()).start();
        Service.gI().rooms.add(new Room("Phòng chat chung", -1));
    }
}
