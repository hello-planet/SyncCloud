package syncto;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SettingPage extends Page{
	
	private final String logo_path = "C:\\Users\\KUN\\workspace\\SyncCloud\\src\\setting.png";
	private final String default_server_path = "/home/mycloud/cloud/sync";
	private final String default_local_path = "C:\\Users\\KUN\\Documents\\MyCloud\\sync\\";
	private final String default_syncinterval = "5";
	
	public SettingPage(int a,int b,String str) {
		super(a,b,str);
		setLayout(new BorderLayout());
		// synclogo
		ThemeImage settingimage =new ThemeImage(logo_path,150,10,100,100);
		add(settingimage,BorderLayout.CENTER);
		
		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0,BoxLayout.Y_AXIS));
		// label and textfield , setting items
		JPanel p = new JPanel(new GridLayout(0, 2));
		JLabel localfolder = new JLabel("本地同步路径",JLabel.CENTER);
		p.add(localfolder);
		JTextField folderInput = new JTextField();
		folderInput.setText(get_localfoldersetted());
		p.add(folderInput);
		JLabel cloudfolder = new JLabel("云端同步路径",JLabel.CENTER);
		p.add(cloudfolder);
		JTextField cloudInput = new JTextField();
		cloudInput.setText(get_server_path());
		p.add(cloudInput);
		JLabel interval = new JLabel("同步周期/分钟（1~100）",JLabel.CENTER);
		p.add(interval);
		JTextField minuteInput = new JTextField();
		minuteInput.setText(get_synctimesetted());
		p.add(minuteInput);
		p0.add(p);
		// buttons
		JPanel p1 = new JPanel(new FlowLayout());
		JButton confirm = new JButton("确定");
		p1.add(confirm);
		JButton reset = new JButton("恢复默认");
		p1.add(reset);
		JButton cancel = new JButton("返回");
		p1.add(cancel);
		p0.add(p1);
		add(p0, BorderLayout.SOUTH);
		
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				String folderstr = folderInput.getText();
				String minutestr = minuteInput.getText();
				String cloudstr = cloudInput.getText();
				if(folderstr == null || folderstr.isEmpty()){
					JOptionPane.showMessageDialog(null, "请输入有效本地路径", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(cloudstr == null || cloudstr.isEmpty()){
					JOptionPane.showMessageDialog(null, "请输入有效云端路径", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(!isValidNumber(minutestr)){
					JOptionPane.showMessageDialog(null, "请输入有效时间", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				File file = new File(folderstr);
				if (!(file.exists() && file.isDirectory())) {
					JOptionPane.showMessageDialog(null, "本地路径无效！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				set_local_path(folderstr);
				set_server_path(cloudstr);
				set_syncinterval(minutestr);
				set_localfoldersetted(folderInput.getText());
				set_synctimesetted(minuteInput.getText());
				closeWindow();
			}
		});
		
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				reset(folderInput,cloudInput,minuteInput);
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				closeWindow();
			}
		});
	}
	
	public void reset(JTextField jt1,JTextField jt2,JTextField jt3){
		jt1.setText(default_local_path);
		jt2.setText(default_server_path);
		jt3.setText(default_syncinterval);
	}
	
	public boolean isValidNumber(String str){
		// 是否存在或为空
		if(str == null){
			return false;
		}
		else if(str.isEmpty()){
			return false;
		}
		// 是否是数字或非负数
		for (int i = 0; i < str.length(); i++){
		   System.out.println(str.charAt(i));
		   if (!Character.isDigit(str.charAt(i))){
			   return false;
		   }
		}
		// 是否在1-100范围内
		if(Integer.parseInt(str) > 100 || Integer.parseInt(str) == 0){
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		JFrame settingpage = new SettingPage(400,250,"Option");
		settingpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		settingpage.setResizable(false);
		settingpage.setVisible(true);
		settingpage.setLocationRelativeTo(null);
	}

}