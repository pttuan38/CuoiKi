package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.UIManager;

import Models.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class FrmDashboard extends javax.swing.JFrame {
    private final DefaultTableModel tableModel;
    private MessageListener messageListener;
    
    private void connect(String namePerson){
        try {
            Socket socket = new Socket(Session.host, 8989);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            String idPerson = reader.readLine();
            Session.gI().personID = idPerson;
            writer.println(Session.gI().personName);
            
            Session.gI().set(socket);
            Session.gI().isConnected = true;
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    private class MessageListener implements Runnable{
        private BufferedReader reader;
        private boolean flag = true;
        private final JFrame dis;
        
        public MessageListener(JFrame dis, BufferedReader reader){
            this.reader = reader;
            this.dis = dis;
        }
        
        public void interrupted(){
            flag = false;
        }
        
        @Override
        public void run() {
            while(flag){
                try {
                    String messageReceive = this.reader.readLine();
                    String function = messageReceive.split("\\|")[0];
                    String data = messageReceive.split("\\|")[1];
                    switch(function){
                        //Example : reload_rooms|null
                        case "reload_rooms":{
                            Session.gI().sendMessage("get_all_rooms-XXX");
                            break;
                        }
                        //Example: rooms|id1#name1#owner1#persons1,id2#name2#owner2#persons2
                        case "rooms" :{
                            tableModel.setRowCount(0);
                            if(data.equals("null")) break;
                            String[] arrRoomData = data.split("\\,");
                            for(var arrRoom: arrRoomData){
                                String[] dataRoom = arrRoom.split("\\#");
                                int idRoom = Integer.parseInt(dataRoom[0]);
                                String nameRoom = dataRoom[1];
                                String ownerRoom = dataRoom[2];
                                int personCount = Integer.parseInt(dataRoom[3]); 
                                tableModel.addRow(new Object[]{idRoom, nameRoom, ownerRoom, personCount});
                            }
                            break;
                        }
                        case "request_password":{
                            String password = JOptionPane.showInputDialog("Phòng này yêu cầu mật khẩu, nhập mật khẩu");
                            if(password == null || password.equals("")){
                                break;
                            }
                            String command = String.format("check_password-%s-%s",data,password);
                            Session.gI().sendMessage(command);
                            break;
                        }
                        case "password":{
                            if(!data.equals("1")){
                                JOptionPane.showMessageDialog(null, "Mật khẩu không chính xác !");
                                break;
                            }
                            else{
                                new FrmChat(data).setVisible(true);
                                dis.dispose();
                                this.interrupted();
                                break;
                            }
                        }
                        //Example accept_join|idRoom
                        case "accept_join":{
                            new FrmChat(data).setVisible(true);
                            dis.dispose();
                            this.interrupted();
                            break;
                        }
                        //Example deny_join|idRoom
                        case "deny_join":{
                            JOptionPane.showMessageDialog(null, "Bạn không thể join phòng lúc này !");
                            break;
                        }
                        //Example accept_create|idRoom
                        case "accept_create":{
                            new FrmChat(data).setVisible(true);
                            dis.dispose();
                            this.interrupted();
                            break;
                        }
                        //Example deny_create|idRoom
                        case "deny_create":{
                            JOptionPane.showMessageDialog(null, "Bạn không thể tạo phòng lúc này !");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Server đã đóng ! Lập tức ngắt kết nối khỏi server.");
                    dis.dispose();
                    break;
                }
            }
        }
        
    }
    
    public FrmDashboard(String namePerson) {
        
        this.setLocation(300, 400);
        UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
        try {
            UIManager.setLookAndFeel(lafInfo[3].getClassName());
        } catch (Exception e) {
        }
        
        if(!Session.gI().isConnected){
            connect(namePerson);
            if(Session.gI().getReader() == null){
                JOptionPane.showMessageDialog(null, "Không thể kết nối đến máy chủ ");
                System.exit(0);
            }
        }
        initComponents();
        
        if(iconApp.getIcon() != null){
            this.setIconImage(((ImageIcon)iconApp.getIcon()).getImage());
        }
        
        tableModel = (DefaultTableModel) mainTable.getModel();
        messageListener = new MessageListener(this, Session.gI().getReader());
        new Thread(messageListener).start();
        Session.gI().sendMessage("get_all_rooms-xxx");
        this.setTitle(String.format("User %s - %s", Session.gI().personID, Session.gI().personName));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnJoin = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mainTable = new javax.swing.JTable();
        btnCreate = new javax.swing.JButton();
        iconApp = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setResizable(false);

        btnJoin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/join.png"))); // NOI18N
        btnJoin.setText("Tham gia");
        btnJoin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJoinActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel1.setText("Sảnh");

        mainTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        mainTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID phòng", "Tên phòng", "Chủ phòng", "Số lượng người"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(mainTable);

        btnCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/create.png"))); // NOI18N
        btnCreate.setText("Tạo phòng");
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        iconApp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/chat.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 515, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(iconApp)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnJoin, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(50, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(iconApp, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnJoin, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(217, 217, 217))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnJoinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJoinActionPerformed
        int indexSelected = mainTable.getSelectedRow();
        if(indexSelected != -1){
            int id = (int)mainTable.getValueAt(indexSelected, 0);
            Session.gI().sendMessage("join_room-"+id);
        }
    }//GEN-LAST:event_btnJoinActionPerformed
    
    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        String nameRoom = JOptionPane.showInputDialog("Nhập tên phòng cần tạo");
        if(nameRoom == null || nameRoom.strip().equals("") || nameRoom.strip().equals("-")){
            return;
        }
        String command = String.format("create_room-%s",nameRoom.replace('-', '.'));
        Session.gI().sendMessage(command);
    }//GEN-LAST:event_btnCreateActionPerformed
    
    public static void main(String[] args) {
        new FrmDashboard("").setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnJoin;
    private javax.swing.JLabel iconApp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable mainTable;
    // End of variables declaration//GEN-END:variables
}
