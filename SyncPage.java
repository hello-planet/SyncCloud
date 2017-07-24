package syncto;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SyncPage extends Page{

	private final String logo_path = "C:\\Users\\KUN\\workspace\\SyncCloud\\src\\sync.png";
	private static Timer timer = null;
	private static Task task = null;
	private String selectedMode = null;
	
	public SyncPage(int a,int b,String str){
		super(a,b,str);
		setLayout(new BorderLayout());
		// synclogo
		ThemeImage syncimage =new ThemeImage(logo_path,150,0,100,100);
		add(syncimage,BorderLayout.CENTER);
		
		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0,BoxLayout.Y_AXIS));
		// combobox
		JPanel p = new JPanel(new FlowLayout());
		JLabel syncmode = new JLabel("同步方式");
		p.add(syncmode);
//		String[] selections = {"","本地至云端","云端到本地","同步"};
		ArrayList<String> selections = new ArrayList<String>();
		selections.add("本地至云端");
		selections.add("云端至本地");
		selections.add("双同步");
		// for further use
		Object[] selec = selections.toArray();
		JComboBox selection = new JComboBox(selec);
		p.add(selection);
		p0.add(p);
		// button
		JPanel p1 = new JPanel(new FlowLayout());
		JButton syncnow = new JButton("现在同步");
		p1.add(syncnow);
		JButton synctask = new JButton("定时同步");
		p1.add(synctask);
		JButton stop = new JButton("停止同步");
		p1.add(stop);
		JButton cancel = new JButton("返回");
		p1.add(cancel);
		p0.add(p1);
		add(p0,BorderLayout.SOUTH);
		
		syncnow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				if(get_local_path() == null || get_local_path().isEmpty()){
					JOptionPane.showMessageDialog(null, "请预先设定本地目录！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				// sync now
				selectedMode = (String) selection.getSelectedItem();
//				System.out.println(selectedMode);
				if(selectedMode.equals("本地至云端")){
					sync0now();
				}else if(selectedMode.equals("云端至本地")){
					sync1now();
				}else{
					sync2now();
				}
				JOptionPane.showMessageDialog(null, "同步完成！", "提示", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		synctask.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				if(get_local_path() == null || get_local_path().isEmpty()){
					JOptionPane.showMessageDialog(null, "请预先设定本地目录！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(get_syncornot()){
					JOptionPane.showMessageDialog(null, "已经处于同步中！", "提示", JOptionPane.PLAIN_MESSAGE);
					return;
				}
				// sync by time
				JOptionPane.showMessageDialog(null, "开始同步！", "提示", JOptionPane.PLAIN_MESSAGE);
				selectedMode = (String) selection.getSelectedItem();
				tasknow();
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				if(!get_syncornot()){
					JOptionPane.showMessageDialog(null, "您没有在同步中！", "警告", JOptionPane.WARNING_MESSAGE);
					return;
				}
				// stop sync
				cancel_task();
				set_syncornot(false);
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				closeWindow();
			}
		});
	}
	
	public void sync0now() {
		try{
			HashMap<File, String> localfile = getLocalFile(get_local_path());
			HashMap<String, String> cloudfile = getCloudFile(get_server_path());
			loop: for (File fl : localfile.keySet()) {
				for (String fc : cloudfile.keySet()) {
					if (fl.getName().equals(fc)) {
						long lt = getDate(localfile.get(fl));
						long ct = getDate(cloudfile.get(fc));
						if (lt > ct) {
							upload(fl);
						} else {
							System.out.println("not uploaded!");
						}
						continue loop;
					}
				}
				upload(fl);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return;
	}
	
	public void sync1now() {
		try {
			HashMap<File, String> localfile = getLocalFile(get_local_path());
			HashMap<String, String> cloudfile = getCloudFile(get_server_path());
			loop: for (String fc : cloudfile.keySet()) {
				for (File fl : localfile.keySet()) {
					if (fc.equals(fl.getName())) {
						long lt = getDate(localfile.get(fl));
						long ct = getDate(cloudfile.get(fc));
						if (ct > lt) {
							download(fc);
						} else {
							System.out.println("not downloaded!");
						}
						continue loop;
					}
				}
				download(fc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	
	public void sync2now() {
		try {
			HashMap<File, String> localfile = getLocalFile(get_local_path());
			HashMap<String, String> cloudfile = getCloudFile(get_server_path());
			ArrayList<File> filetoUP = new ArrayList<File>();
			ArrayList<String> filetoDOWN = new ArrayList<String>();

			loop0: for (File fl : localfile.keySet()) {
				for (String fc : cloudfile.keySet()) {
					if (fl.getName().equals(fc)) {
						long lt = getDate(localfile.get(fl));
						long ct = getDate(cloudfile.get(fc));
//						if (lt > (ct + get_syncinterval() * 100)) {
						if (lt > (ct + 5)) {
							filetoUP.add(fl);
							// upload(fl);
						} else {
							System.out.println("not added to file to up!");
						}
						continue loop0;
					}
				}
				filetoUP.add(fl);
				// upload(fl);
			}

			loop1: for (String fc : cloudfile.keySet()) {
				for (File fl : localfile.keySet()) {
					if (fc.equals(fl.getName())) {
						long lt = getDate(localfile.get(fl));
						long ct = getDate(cloudfile.get(fc));
//						if (ct > (lt + get_syncinterval() * 100)) {
						if (ct > (lt + 5)) {
							filetoDOWN.add(fc);
							// download(fc);
						} else {
							System.out.println("not added to file to down!");
						}
						continue loop1;
					}
				}
				filetoDOWN.add(fc);
				// download(fc);
			}
			
			if(filetoUP != null){
				// upload and download
				for (File f : filetoUP) {
					upload(f);
				}
			}
			if(filetoDOWN != null){
				for (String s : filetoDOWN) {
					download(s);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static HashMap<File,String> getLocalFile(String str) {
		HashMap<File,String> localfile = new HashMap<File,String>();
//		String name = null;
		String date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		File localFile = new File(str);
        if(localFile.exists() && localFile.isDirectory()){
        	File[] localfiles = localFile.listFiles();
        	for(File f : localfiles){
//        		name = f.getName();
        		date = sdf.format(new Date(f.lastModified()));
        		localfile.put(f,date);
        	}
        }else{
        	JOptionPane.showMessageDialog(null, "本地路径无效！", "警告", JOptionPane.WARNING_MESSAGE);
        }
        System.out.println("localfiles are "+localfile);
        return localfile;
    }
	
	public HashMap<String,String> getCloudFile(String str) throws IOException{
		HashMap<String,String> cloudfile = new HashMap<String,String>();
		String url = geturlPre() + "getfilelist";
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("path", str));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = get_httpclient().execute(httpPost);
		String resp = null;
		String name = null,date = null;
		try {
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			resp = EntityUtils.toString(entity, "UTF-8");
			System.out.println(resp);
			StringReader sr = new StringReader(resp);
			InputSource is = new InputSource(sr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("entry");

			if (nodes != null && nodes.getLength() > 0) {
				// 创建文件列表字符串数组
				for (int i = 0; i < nodes.getLength(); i++) {
					NodeList childs = nodes.item(i).getChildNodes();
					for (int j = 0; j < childs.getLength(); j++) {
						if (childs.item(j).getNodeName().equals("name"))
							name = childs.item(j).getTextContent();
						if (childs.item(j).getNodeName().equals("modifiedsortable"))
							date = childs.item(j).getTextContent();
					}
					cloudfile.put(name,date);
				}
			}
			EntityUtils.consume(entity);
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("error!");
		}finally {
			response.close();
		}
		System.out.println("cloudfiles are "+cloudfile);
		return cloudfile;
	}
	
	public void upload(File f) {
		CloseableHttpResponse response = null;
		try {
			String url = "http://" + getIP() + ":" + getPORT() + "/upload?appname=explorer&path=" + get_server_path() + "&expandzip=0";
			HttpPost httpPost = new HttpPost(url);
			// 把文件转换成流对象FileBody并加入http请求进行上传
			FileBody bin = new FileBody(f);
			HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", bin).build();
			httpPost.setEntity(reqEntity);
			response = get_httpclient().execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 打印响应内容
				System.out.println(EntityUtils.toString(entity, "UTF-8"));
			}
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
	
	public void download(String filename) {
		try {
			String url = geturlPre() + "downloadfile";
			url = url + "?filepath=" + get_server_path() + "/" + filename + "&filename=" + filename;
			// System.out.println(url);
			String filepath = get_local_path() + "\\" + filename;
			// System.out.println("//filepath:" + filepath);
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = get_httpclient().execute(httpget);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();

			File file = new File(filepath);
			FileOutputStream fileout = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fileout.write(buffer, 0, ch);
			}
			is.close();
			fileout.flush();
			fileout.close();
			System.out.println("Download Succeeded");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public static long getDate(String str){
		long datetime = 0;
		String sortdate = "";
		for(int i=0;i<str.length();i++){
			if(Character.isDigit(str.charAt(i))){
				sortdate = sortdate + str.charAt(i);
			}
		}
//		System.out.println("the date string is:"+sortdate);
		datetime = Long.parseLong(sortdate);
//		System.out.println("the date number is:"+datetime);
		return datetime;
	}

	public void tasknow(){
		set_syncornot(true);
		if(timer== null){
			timer = new Timer();
			task = new Task();
		}
		long delay = 0;
//		long intervalPeriod = get_syncinterval()*60*1000;
		long intervalPeriod = 5000;
		timer.scheduleAtFixedRate(task,delay,intervalPeriod);
	}
	
	public static void cancel_task(){
		timer.cancel();
		timer = null;
		task.cancel();
		task = null;
		JOptionPane.showMessageDialog(null, "已停止同步！", "提示", JOptionPane.PLAIN_MESSAGE);
	}
	
	class Task extends TimerTask {
		public void run(){
			if(selectedMode.equals("本地至云端")){
				sync0now();
			}else if(selectedMode.equals("云端至本地")){
				sync1now();
			}else{
				sync2now();
			}
		}
	}
	
	public static void main(String[] args) {
		SyncPage syncpage = new SyncPage(400,200,"Sync");
//		syncpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		syncpage.setResizable(false);
		syncpage.setVisible(true);
		syncpage.setLocationRelativeTo(null);
	
	}

}