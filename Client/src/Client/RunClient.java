package Client;

import Models.Session;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.UIManager;


public class RunClient {

    
    public static void main(String[] args) {
        
        
        UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
        try {
            UIManager.setLookAndFeel(lafInfo[3].getClassName());
        } catch (Exception e) {
        }
        String name = JOptionPane.showInputDialog("Nhập tên của bạn để tham gia ");
        if(name == null || name.equals("")){
            return;
        }
        Session.gI().personName = name;
        new FrmDashboard(name).setVisible(true);
    }
    
}
