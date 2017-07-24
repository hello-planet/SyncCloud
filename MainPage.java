package syncto;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;



import javax.swing.JButton;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;

import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MainPage extends Page {
	
	private final String logo_path = "C:\\Users\\KUN\\workspace\\SyncCloud\\src\\Mycloud.png";
	
	public MainPage(int a,int b,String str) {
		super(a,b,str);
		setLayout(new BorderLayout());
		
		// LogoImage
		ThemeImage logoimage = new ThemeImage(logo_path,71,15,150,57);
		this.add(logoimage,BorderLayout.CENTER);
		// Button
		JPanel p1 = new JPanel(new GridLayout(0,1,0,3));
		JButton upLoad = new JButton("上传文件");
		p1.add(upLoad);
		JButton browse = new JButton("浏览文件");
		p1.add(browse);
		JButton localFolder = new JButton("本地文件夹");
		p1.add(localFolder);
		JButton sync = new JButton("同步文件");
		p1.add(sync);
		JButton setting = new JButton("设置");
		p1.add(setting);
		JButton signout = new JButton("登出");
		p1.add(signout);
		add(p1, BorderLayout.SOUTH);
		
		upLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// upload
				upLoad(upLoad);
			}
		});
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// open cloudfile page
				JFrame browsepage = new BrowsePage(300,300,"CloudFile");
				browsepage.setVisible(true);
				browsepage.setResizable(false);
				browsepage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				browsepage.setLocationRelativeTo(null);
			}
		});
		
		localFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(get_local_path() == null || get_local_path().isEmpty()){
					JOptionPane.showMessageDialog(null, "请预先设定本地目录！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				try{
					// open local folder
					openfolder();
				}catch(IOException nolocalfolder){
					nolocalfolder.printStackTrace();
//					JOptionPane.showMessageDialog(null, "请保证文件目录存在！", "警告", JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		sync.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// open sync page
				JFrame syncpage = new SyncPage(400,200,"Sync");
				syncpage.setVisible(true);
				syncpage.setResizable(false);
				syncpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				syncpage.setLocationRelativeTo(null);
			}
		});
		setting.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// open setting page
				JFrame settingpage = new SettingPage(400,250,"Option");
				settingpage.setVisible(true);
				settingpage.setResizable(false);
				settingpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				settingpage.setLocationRelativeTo(null);
			}
		});
		signout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// shutdown synctask
				if(get_syncornot()){
					SyncPage.cancel_task();
					set_syncornot(false);
				}
				// sign out
				try{
					signout(getIP(),getPORT());
					closeWindow();
					JFrame signinpage = new SignInPage(400,200,"Welcome");
					signinpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					signinpage.setResizable(false);
					signinpage.setVisible(true);
					signinpage.setLocationRelativeTo(null);
				}catch (Exception invalidIP){
					invalidIP.printStackTrace();
				}
			}
		});
	}
	
	public void upLoad(JButton btn) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(btn);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// 得到选择的文件
			File[] arrfiles = chooser.getSelectedFiles();
			if (arrfiles == null || arrfiles.length == 0) {
				return;
			}
			try {
				for (File f : arrfiles) {
					CloseableHttpResponse response = null;
					try {
						String url = "http://" + getIP() + ":" + getPORT() + "/upload?appname=explorer&path=" + get_server_path()
								+ "&expandzip=0";
						HttpPost httpPost = new HttpPost(url);
						// 把文件转换成流对象FileBody并加入http请求实体进行上传
						FileBody bin = new FileBody(f);
						HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", bin).build();
						httpPost.setEntity(reqEntity);
						response = get_httpclient().execute(httpPost);
						// System.out.println("The response value of token:" +
						// response.getFirstHeader("token"));
						// 获取响应实体
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							// // 打印响应长度
							// System.out.println("Response content length: " +
							// resEntity.getContentLength());
							// 打印响应内容
							// System.out.println(EntityUtils.toString(entity,
							// Charset.forName("UTF-8")));
							System.out.println(EntityUtils.toString(entity, "UTF-8"));
						}
						// 销毁
						EntityUtils.consume(entity);
					} catch (Exception nocloudfolder) {
						nocloudfolder.printStackTrace();
					} finally {
						try {
							if (response != null) {
								response.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				JOptionPane.showMessageDialog(null, "上传成功！", "提示", JOptionPane.PLAIN_MESSAGE);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "上传失败！", "提示", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
		
	public void openfolder() throws IOException {
		Runtime.getRuntime().exec("explorer.exe " + get_local_path());
	}

	public void signout(String ip, String port) throws Exception {
		String url = "http://" + ip + ":" + port + "/core/" + "logoutprofile";
		System.out.println(url);
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpResponse response = null;
		String resp = null;
		try {
			// 获取响应内容
			response = get_httpclient().execute(httpPost);
			System.out.println(response.getStatusLine());
			HttpEntity entity2 = response.getEntity();
			resp = EntityUtils.toString(entity2, "UTF-8");
			// do something useful with the response body
			// 构造解析所需格式
			StringReader sr = new StringReader(resp);
			// inputsource
			InputSource is = new InputSource(sr);
			// 解析
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("result");
			NodeList type = doc.getElementsByTagName("type");

			if (nodes != null && nodes.getLength() > 0) {
				System.out.println(nodes.getLength());
				System.out.println(nodes.item(0).getTextContent());
				String result = nodes.item(0).getTextContent();
				if ("1".equals(result)) {
					setIP(null);
					setPORT(null);
					seturlPre(null);
					set_server_path("/home/mycloud/cloud/sync");
					set_localfoldersetted("C:\\Users\\KUN\\Documents\\MyCloud\\sync\\");
					set_synctimesetted("5");
				}
			}
			if (type != null && type.getLength() > 0) {
				System.out.println(nodes.getLength());
				System.out.println(type.item(0).getTextContent());
			}
			EntityUtils.consume(entity2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.close();
		}
		System.out.println(resp);
	}
	
	public static void main(String[] args) {
		JFrame myFrame1 = new MainPage(300, 300, "Mycloud");
		myFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame1.setResizable(false);
		myFrame1.setVisible(true);
		myFrame1.setLocationRelativeTo(null);
	}
	
}