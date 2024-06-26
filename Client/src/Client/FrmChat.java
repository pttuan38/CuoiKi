package Client;

import Models.FileDownloader;
import Models.ImageUtils;
import Models.RunnableServerFile;
import Models.Session;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.nio.file.Files;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;


public class FrmChat extends javax.swing.JFrame {
    private final String idRoom ; 
    private boolean isAdmin = false;
    private DefaultTableModel tableModel;
    
    private class MessageListener implements Runnable{
        private final BufferedReader reader;
        private boolean flag = true;
        private final FrmChat dis;
        
        public MessageListener(FrmChat dis, BufferedReader reader){
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
                        case "admin":{
                            dis.isAdmin = Integer.parseInt(data) == 1 ? true : false;
                            if(!dis.isAdmin){
                                btnKickPerson.setEnabled(false);
                                btnKickPerson.setVisible(false);
                                
                                btnSetPassword.setVisible(false);
                                btnSetPassword.setVisible(false);
                                
                                btnRemove.setVisible(false);
                                btnRemove.setEnabled(false);
                            }
                            break;
                        }
                        //Example: new_message|message
                        case "new_message" :{
                            addMessage(data);
                            break;
                        }
                        case "accept_leave":{
                            new FrmDashboard("").setVisible(true);
                            dis.dispose();
                            flag = false;
                            break;
                        }
                        case "ask_leave":{
                            int askOwnerDeleteroom = JOptionPane.
                                    showConfirmDialog(null, "Xác nhận rời phòng?\nThao tác này sẽ xóa phòng và kick tất cả người dùng khác trong phòng", "Xác nhận rời phòng", JOptionPane.YES_NO_CANCEL_OPTION);
                            switch (askOwnerDeleteroom) {
                                case JOptionPane.YES_OPTION:{
                                    String command = String.format("leave_room-%s-1", idRoom);
                                    Session.gI().sendMessage(command);
                                    break;
                                }
                                default:{
                                    break;
                                }
                            }
                            break;
                        }
                        case "kick":{
                            JOptionPane.showMessageDialog(null, "Bạn đã kick khỏi phòng bởi " + data);
                            new FrmDashboard("").setVisible(true);
                            dis.dispose();
                            flag = false;
                            break;
                        }
                        case "roomHasBeenDeleted" :{
                            JOptionPane.showMessageDialog(null, "Phòng đã bị xóa khỏi server bởi chủ phòng");
                            new FrmDashboard("").setVisible(true);
                            dis.dispose();
                            flag = false;
                            break;
                        }
                        case "persons":{
                            tableModel = (DefaultTableModel) tableUser.getModel();
                            tableModel.setRowCount(0);
                            String[] person_arr = data.split(",");
                            for(String person_data: person_arr){
                                String []arr = person_data.split("\\#");
                                tableModel.addRow(new Object[]{arr[0], arr[1], arr[2]});
                            }
                            break;
                        }
                        case "deny_set_password":{
                            JOptionPane.showMessageDialog(null, "Không thể đặt mật khẩu lúc này !");
                            break;
                        }
                        case "deny_upload_file":{
                            JOptionPane.showMessageDialog(null, "Có lỗi trong quá trình tải file!");
                            break;
                        }
                        case "new_attachment":{
                            Session.gI().sendMessage("get_files-null");
                            break;
                        }
                        case "files":{
                            if(data.equals("null")) break;
                            DefaultTableModel model = (DefaultTableModel)tableFiles.getModel();
                            model.setRowCount(0);
                            
                            String[] files = data.split("\\,");
                            for(String dataFile: files){
                                String[] tmp = dataFile.split("\\#");
                                model.addRow(new Object[]{tmp[0], tmp[1], tmp[2], tmp[3]});
                            }
                            break;
                        }
                        case "accept_delete_file":{
                            JOptionPane.showMessageDialog(null,
                                    "Xóa file thành công " + data);
                            break;
                        }
                        case "deny_delete_file":{
                            JOptionPane.showMessageDialog(null,
                                    "Xóa file thất bại " + data);
                            break;
                        }
                        case "file_not_found":{
                            JOptionPane.showMessageDialog(null,
                                    "Không tìm thấy file " + data);
                            break;
                        }
                        case "not_owner":{
                            JOptionPane.showMessageDialog(null,
                                    "Bạn không có quyền xóa file " + data);
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
    
    //handle GUI
    private void handleSelectionChanged(){
        ListSelectionModel selectionModel = tableFiles.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho phép chọn một hàng
        
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = tableFiles.getSelectedRow();
                    if (selectedRow != -1) { // Đảm bảo có hàng được chọn
                        String fileType = (String)tableFiles.getValueAt(selectedRow, 2);
                        if(fileType.equals("image/jpeg")){
                            String owner = (String)tableFiles.getValueAt(selectedRow, 3);
                            String fileName = (String)tableFiles.getValueAt(selectedRow, 0);
                            new Thread(new ImageUtils.SetIconForLabel(img, Session.host, owner.replaceAll("\\ ", "") + "__" + fileName)).start();
                        }
                        else{
                            new Thread(new ImageUtils.SetIconForLabel(img, Session.host, "image_template.png")).start();
                        }
                    }
                }
            }
        });
    }
    
    private void addMessage(String message){
        String current = mainTextArea.getText();
        mainTextArea.setText(current + "\n" + message);
    }
    
    public FrmChat(String idRoom) {
        this.setLocation(300, 400);
        UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
        try {
            UIManager.setLookAndFeel(lafInfo[3].getClassName());
        } catch (Exception e) {
        }
        initComponents();
        new Thread(new MessageListener(this, Session.gI().getReader())).start();
        Session.gI().sendMessage("joinchat-"+idRoom);
        this.setTitle(String.format("User %s - %s", Session.gI().personID, Session.gI().personName));
        handleSelectionChanged();
        
        if(iconApp.getIcon() != null){
            this.setIconImage(((ImageIcon)iconApp.getIcon()).getImage());
        }
        
        this.idRoom = idRoom;
        
        //Check admin 
        Session.gI().sendMessage("check_admin-null");
        //Load user
        Session.gI().sendMessage("get_persons-null");
        //Load file
        Session.gI().sendMessage("get_files-null");
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainTextArea = new javax.swing.JTextArea();
        btnSend = new javax.swing.JButton();
        btnSendImage = new javax.swing.JButton();
        btnSendFile = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        lbNamePhong = new javax.swing.JLabel();
        btnRemove = new javax.swing.JButton();
        txtInput = new javax.swing.JTextField();
        btnSetPassword = new javax.swing.JButton();
        btnKickPerson = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableUser = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        img = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableFiles = new javax.swing.JTable();
        btnDownload = new javax.swing.JButton();
        btnRemoveFile = new javax.swing.JButton();
        iconApp = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        mainTextArea.setEditable(false);
        mainTextArea.setColumns(20);
        mainTextArea.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        mainTextArea.setRows(5);
        jScrollPane1.setViewportView(mainTextArea);

        btnSend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/send.png"))); // NOI18N
        btnSend.setText("Gửi tin nhắn");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        btnSendImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image.png"))); // NOI18N
        btnSendImage.setText("Gửi ảnh");
        btnSendImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendImageActionPerformed(evt);
            }
        });

        btnSendFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/attachment.png"))); // NOI18N
        btnSendFile.setText("Gửi file");
        btnSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFileActionPerformed(evt);
            }
        });

        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/exit.png"))); // NOI18N
        btnBack.setText("Rời phòng");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        lbNamePhong.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        lbNamePhong.setText("Phòng chat");

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/remove.png"))); // NOI18N
        btnRemove.setText("Xóa phòng");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        txtInput.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInputActionPerformed(evt);
            }
        });

        btnSetPassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/lock.png"))); // NOI18N
        btnSetPassword.setText("Đặt mật khẩu");
        btnSetPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetPasswordActionPerformed(evt);
            }
        });

        btnKickPerson.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/delete.png"))); // NOI18N
        btnKickPerson.setText("Kick người dùng");
        btnKickPerson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKickPersonActionPerformed(evt);
            }
        });

        tableUser.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID người dùng", "Tên người dùng", "Vai trò"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tableUser);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Chức năng");

        img.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image_template.png"))); // NOI18N

        tableFiles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tên tập tin", "Dung lượng", "Loại", "Người gửi"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
        tableFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableFilesMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tableFiles);

        btnDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/download.png"))); // NOI18N
        btnDownload.setText("Tải xuống");
        btnDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownloadActionPerformed(evt);
            }
        });

        btnRemoveFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/file_delete.png"))); // NOI18N
        btnRemoveFile.setText("Xóa file");
        btnRemoveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveFileActionPerformed(evt);
            }
        });

        iconApp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/chat.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(iconApp)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbNamePhong))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(12, 12, 12)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnDownload, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(14, 14, 14)
                                            .addComponent(img))))
                                .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnSendImage)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnSendFile))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(btnSetPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnKickPerson, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnRemoveFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnBack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)))))
                .addGap(0, 22, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(iconApp)
                    .addComponent(lbNamePhong))
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendImage, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendFile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(btnKickPerson, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSetPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRemoveFile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(img)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDownload, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(56, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        //15
        String msg = txtInput.getText();
        if(msg == null || msg.strip().equals("")){
            return;
        }
        String message = Session.gI().personName + " : " + msg;
        String command = String.format("send_msg-%s-%s", Session.gI().personID, msg.replaceAll("\\-", "?"));
        Session.gI().sendMessage(command);
        addMessage(message);
        txtInput.setText("");
    }//GEN-LAST:event_btnSendActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        Session.gI().sendMessage("leave_room-" + idRoom +"-0");
    }//GEN-LAST:event_btnBackActionPerformed
    
    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int choice = JOptionPane.
                showConfirmDialog(null, "Xác nhận xóa phòng và kick hết người dùng? Không thể hoàn tác tác vụ này.", "Xác nhận xóa phòng", JOptionPane.YES_NO_CANCEL_OPTION);
        switch(choice){
            case JOptionPane.YES_OPTION:{
                Session.gI().sendMessage("leave_room-"+idRoom+"-1");
                break;
            }
            default:{
                break;
            }
        }
    }//GEN-LAST:event_btnRemoveActionPerformed
    
    private void txtInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInputActionPerformed
        btnSendActionPerformed(evt);
    }//GEN-LAST:event_txtInputActionPerformed
    
    private void btnKickPersonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKickPersonActionPerformed
        int index = tableUser.getSelectedRow();
        if(index != -1){
            String idKick = (String)tableUser.getValueAt(index, 0);
            Session.gI().sendMessage("kick-" + idKick);
        }
    }//GEN-LAST:event_btnKickPersonActionPerformed
    
    private void btnSendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFileActionPerformed
        JFileChooser fc = new JFileChooser("./");
        fc.showOpenDialog(null);
        File file = fc.getSelectedFile();
        if(file != null){
            Session.gI().sendFile(file);
        }
    }//GEN-LAST:event_btnSendFileActionPerformed
    
    private void btnDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloadActionPerformed
        int index = tableFiles.getSelectedRow();
        if(index == -1) return;
        String owner = (String)tableFiles.getValueAt(index, 3);
        String fileName = (String)tableFiles.getValueAt(index, 0);
        
        File file = new File("./" + fileName);
        JFileChooser fc = new JFileChooser("./");
        fc.setSelectedFile(file);
        fc.showSaveDialog(null);
        file = fc.getSelectedFile();
        if(file != null && Files.exists(file.toPath())){
            JOptionPane.showMessageDialog(null, "File đã tồn tại, vui lòng thử tên khác!");
            return;
        }
        
        if(file != null){
            new Thread(new FileDownloader(file.getPath(), owner + "__" + fileName)).start();
        }
    }//GEN-LAST:event_btnDownloadActionPerformed
    
    private void btnSetPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetPasswordActionPerformed
        String password = JOptionPane.showInputDialog("Nhập mật khẩu cần đặt cho phòng ");
        if(password != null){
            if(password.contains(" ")){
                JOptionPane.showMessageDialog(null, "Mật khẩu không chứa dấu cách !");
                return;
            }
            Session.gI().sendMessage("set_password-"+password);
        }
    }//GEN-LAST:event_btnSetPasswordActionPerformed
    
    private void btnSendImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendImageActionPerformed
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image (.png, .jpg or .jpeg)", "png", "jpg", "jpeg");
        JFileChooser fc = new JFileChooser("./");
        fc.setFileFilter(filter);
        fc.showOpenDialog(null);
        File file = fc.getSelectedFile();
        if(file != null){
            new FrmPreviewImage(file).setVisible(true);
        }
    }//GEN-LAST:event_btnSendImageActionPerformed

    private void tableFilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableFilesMouseClicked
        
    }//GEN-LAST:event_tableFilesMouseClicked
    
    private void btnRemoveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveFileActionPerformed
        int index = tableFiles.getSelectedRow();
        if(index == -1) return;
        int choice = JOptionPane.
                showConfirmDialog(null, "Xác nhận xóa ? Không thể hoàn tác tác vụ này.", "Xác nhận", JOptionPane.YES_NO_CANCEL_OPTION);
        if(choice != JOptionPane.YES_OPTION) return;
        
        String owner = (String)tableFiles.getValueAt(index, 3);
        String fileName = (String)tableFiles.getValueAt(index, 0);
        
        String command = "delete_file-" + owner + "__" + fileName;
        Session.gI().sendMessage(command);
    }//GEN-LAST:event_btnRemoveFileActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnDownload;
    private javax.swing.JButton btnKickPerson;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRemoveFile;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JButton btnSendImage;
    private javax.swing.JButton btnSetPassword;
    private javax.swing.JLabel iconApp;
    private javax.swing.JLabel img;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lbNamePhong;
    private javax.swing.JTextArea mainTextArea;
    private javax.swing.JTable tableFiles;
    private javax.swing.JTable tableUser;
    private javax.swing.JTextField txtInput;
    // End of variables declaration//GEN-END:variables
}
