package syncto;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class BrowsePage extends Page{

	private final String logo_path = null;
	private String[] files = null;
	
	public BrowsePage(int a,int b,String str) {
		super(a,b,str);
		setLayout(new BorderLayout());
		
		try{
			files = getfile();
			JList list = new JList(files);
//			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			add(list, BorderLayout.CENTER);
			JPanel p = new JPanel(new FlowLayout());
			JButton download = new JButton("下载");
			p.add(download);
			JButton delete = new JButton("删除");
			p.add(delete);
			JButton cancel = new JButton("返回");
			p.add(cancel);
			add(p,BorderLayout.SOUTH);
			
			download.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (list.isSelectionEmpty()){
						JOptionPane.showMessageDialog(null, "请选择文件！", "提示", JOptionPane.PLAIN_MESSAGE);
						return;
					}	
					String filename = (String) list.getSelectedValue();
//					System.out.println("//value:" + filename);
					String filepath = null;
					JFileChooser chooser = new JFileChooser();
					chooser.setMultiSelectionEnabled(false);
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = chooser.showSaveDialog(download);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						/** 得到选择的文件* */
						File arrfiles = chooser.getSelectedFile();
						if (arrfiles == null)
							return;
						filepath = arrfiles.getPath() + "\\" + filename;
//						System.out.println("//filepath:" + filepath);
						String url = geturlPre() + "downloadfile";
						System.out.println(url);
						url = url + "?filepath=" + get_server_path() + "/" + filename + "&filename=" + filename;
						// download the file
						download(url, filepath);
						JOptionPane.showMessageDialog(null, "下载成功！", "提示", JOptionPane.PLAIN_MESSAGE);
					}
				}
			});
			
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (list.isSelectionEmpty()){
						JOptionPane.showMessageDialog(null, "请选择文件！", "提示", JOptionPane.PLAIN_MESSAGE);
						return;
					}
					String value = (String)list.getSelectedValue();
					String url = null;
					url = "http://"+getIP()+":"+getPORT()+"/app/explorer/deletefile?path="+get_server_path()+"&name="+value;
//					url = url + "?path="+server_path+"&name="+value;
					try {
							delete(url);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
				}
			});
			
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeWindow();
				}
			});
			
		}catch(Exception exc){
			exc.printStackTrace();
			System.out.println("abc");
			JOptionPane.showMessageDialog(null, "未成功获取云端文件", "警告", JOptionPane.ERROR_MESSAGE);
		}
	}

	public String[] getfile() throws Exception {
		String[] cloudfile = null;
		String url = geturlPre() + "getfilelist";
		System.out.println(url);
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("path", get_server_path()));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response = get_httpclient().execute(httpPost);
		String resp = null;
		try {
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			resp = EntityUtils.toString(entity, "UTF-8");
			StringReader sr = new StringReader(resp);
			InputSource is = new InputSource(sr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("entry");

			if (nodes != null && nodes.getLength() > 0) {
				// 创建文件列表字符串数组
				cloudfile = new String[nodes.getLength()];
				for (int i = 0; i < nodes.getLength(); i++) {
					NodeList childs = nodes.item(i).getChildNodes();
					for (int j = 0; j < childs.getLength(); j++) {
						if (childs.item(j).getNodeName().equals("name"))
							cloudfile[i] = childs.item(j).getTextContent();
					}
				}
			}
			EntityUtils.consume(entity);
		} finally {
			response.close();
		}
		System.out.println(resp);
		return cloudfile;
	}
	
	public void download(String url, String filepath) {
		try {
			if(filepath == null)
				return ;
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = get_httpclient().execute(httpget);
			HttpEntity entity = response.getEntity();
			// container to read the content
			InputStream is = entity.getContent();
			File file = new File(filepath);
			// container to write the content
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fos.write(buffer, 0, ch);
			}
			is.close();
			fos.flush();
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public void delete(String url) throws IOException {
		CloseableHttpResponse response = null;
		String resp=null;
		try{
			HttpPost httpPost = new HttpPost(url);
			// delete 并获取相应信息
			response = get_httpclient().execute(httpPost);
			// 构造解析形式
			HttpEntity entity = response.getEntity();
			resp = EntityUtils.toString(entity, "UTF-8");
			StringReader sr = new StringReader(resp);
			InputSource is = new InputSource(sr);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("result");
			// 解析
			if(nodes != null && nodes.getLength() > 0){
				System.out.println(nodes.item(0).getTextContent());
				String result = nodes.item(0).getTextContent();
				if("1".equals(result)){
					JOptionPane.showMessageDialog(null, "删除成功！", "提示",JOptionPane.PLAIN_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(null, "删除失败！", "警告", JOptionPane.WARNING_MESSAGE);
				}
			}
			System.out.println(resp);
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "删除失败！", "警告", JOptionPane.WARNING_MESSAGE);
		}finally {
			response.close();
		}
		return;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame browsepage = new BrowsePage(300,300,"CloudFile");
		browsepage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		browsepage.setResizable(false);
		browsepage.setVisible(true);
		browsepage.setLocationRelativeTo(null);
	}

}