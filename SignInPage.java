package syncto;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SignInPage extends Page {
		
	public SignInPage(int a,int b,String str) {
		super(a,b,str);
		setLayout(new BorderLayout());
		
		JPanel p = new JPanel(new GridLayout(0, 2));
		p.setLayout(new GridLayout(0, 2));
		// set login information items
		JLabel ipL = new JLabel("IP",JLabel.CENTER);
		p.add(ipL);
		JTextField ipInput = new JTextField();
		ipInput.setText("172.20.10.4");
		p.add(ipInput);
		JLabel portL = new JLabel("端口",JLabel.CENTER);
		p.add(portL);
		JTextField portInput = new JTextField();
		portInput.setText("10001");
		p.add(portInput);
		JLabel usernameL = new JLabel("用户名",JLabel.CENTER);
		p.add(usernameL);
		JTextField username = new JTextField();
		username.setText("mycloudofkun");
		p.add(username);
		JLabel passwordL = new JLabel("密码",JLabel.CENTER);
		p.add(passwordL);
		JPasswordField password = new JPasswordField();
		password.setText("mycloud1");
		p.add(password);
		// ?
		add(p, BorderLayout.CENTER);
		
		JPanel p1 = new JPanel(new FlowLayout());
		JButton signIn = new JButton("登录");
		p1.add(signIn);
		add(p1, BorderLayout.SOUTH);
		
		signIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ipStr = ipInput.getText();
				String portStr = portInput.getText();
				String usernameStr = username.getText();
				@SuppressWarnings("deprecation")
				String passwordStr = password.getText();
				if (ipStr == null || ipStr.isEmpty()) {
					JOptionPane.showMessageDialog(null, "请输入IP！", "警告", JOptionPane.WARNING_MESSAGE);
					// ?
					return;
				}
				if (portStr == null || portStr.isEmpty()) {
					JOptionPane.showMessageDialog(null, "请输入端口！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (usernameStr == null || usernameStr.isEmpty()) {
					JOptionPane.showMessageDialog(null, "请输入用户名！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (passwordStr == null || passwordStr.isEmpty()) {
					JOptionPane.showMessageDialog(null, "请输入密码！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				System.out.println("// info:" + ipStr + "/" + portStr + "/" + usernameStr + "/" + passwordStr);
				try {
					login(ipStr, portStr, usernameStr, passwordStr);
					if (!(getIP() == null || getIP().isEmpty())){
						MainPage mpage = new MainPage(300,300,"Mycloud");
						mpage.setVisible(true);
						mpage.setResizable(false);
						mpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						mpage.setLocationRelativeTo(null);
						closeWindow();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});
		
	}

	private String sha1(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			// ?
			byte[] digest = md.digest(str.getBytes());// 对接后的字符串进行sha1加密
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数 的 字符串
			for (int i = 0; i < digest.length; i++) {
				String shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			System.out.println(hexString.toString()); // 签名密文字符串
			return hexString.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		return null;
	}
	
	public void login(String ip, String port, String user, String password) throws Exception {
		
		String url ="http://" + ip + ":" + port + "/core/"+"loginprofile";
		System.out.println(url);
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("Profile", user));
		nvps.add(new BasicNameValuePair("Password", sha1(password)));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = null;
		String resp=null;
		try {
			// 获取响应内容
			response = get_httpclient().execute(httpPost);
//			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			resp = EntityUtils.toString(entity, "UTF-8");
			// 构造解析所需格式
			StringReader sr = new StringReader(resp);
			InputSource is = new InputSource(sr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("result");
			NodeList type = doc.getElementsByTagName("type");
			// 解析
			if(nodes != null && nodes.getLength() > 0){
				String result = nodes.item(0).getTextContent();
				if(!"0".equals(result)){
					setIP(ip);
					setPORT(port);
					seturlPre("http://" + ip + ":" + port + "/core/");
				}else{
					JOptionPane.showMessageDialog(null, "登陆失败，请重新登陆！！", "警告", JOptionPane.WARNING_MESSAGE);
				}
			}
			if(type != null && type.getLength() > 0){
				System.out.println(type.item(0).getTextContent());
			}
			EntityUtils.consume(entity);
		} catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "登陆失败，请重新登陆！！", "警告", JOptionPane.WARNING_MESSAGE);
		} finally {
			response.close();
		}
		System.out.println(resp);

		return ;

	}
	
	public static void main(String[] args) {
		JFrame signinpage = new SignInPage(400,200,"Welcome");
		signinpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		signinpage.setResizable(false);
		signinpage.setVisible(true);
		signinpage.setLocationRelativeTo(null);
	}
}