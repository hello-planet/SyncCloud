package syncto;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public abstract class Page extends JFrame{
	
	private static String IP;
	private static String PORT;
	private static String urlPre;
	
	private int DEFAULT_WIDTH;
	private int DEFAULT_HEIGHT;
	private String name;
	
	private static String server_path = "/home/mycloud/cloud/sync";
	private static String local_path = null;
	private static String localfoldersetted = "C:\\Users\\KUN\\Documents\\MyCloud\\sync\\";
	private static int syncinterval = 5;
	private static String synctimesetted = "5";
	private static CloseableHttpClient httpclient = HttpClients.createDefault();
	
	private static boolean syncornot = false;
	
	public Page(int a,int b,String str){
		this.DEFAULT_WIDTH = a;
		this.DEFAULT_HEIGHT = b;
		this.name = str;
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setTitle(name);
	}
	
	public void closeWindow(){
		dispose();
	}
	
	public String getIP(){
		return IP;
	}
	
	public void setIP(String str){
		IP = str;
	}
	
	public String getPORT(){
		return PORT;
	}
	
	public void setPORT(String str){
		PORT = str;
	}
	
	public String geturlPre(){
		return urlPre;
	}
	
	public void seturlPre(String str){
		urlPre = str;
	}
	
	public String get_server_path(){
		return server_path;
	}
	
	public void set_server_path(String str){
		server_path = str;
	}
	
	public String get_local_path(){
		return local_path;
	}
	
	public void set_local_path(String str){
		local_path = str;
	}
	
	public String get_localfoldersetted(){
		return localfoldersetted;
	}
	
	public void set_localfoldersetted(String str){
		localfoldersetted = str;
	}
	
	public int get_syncinterval(){
		return syncinterval;
	}
	
	public void set_syncinterval(String str){
		syncinterval = Integer.parseInt(str);
	}
	
	public String get_synctimesetted(){
		return synctimesetted;
	}
	
	public void set_synctimesetted(String str){
		synctimesetted = str;
	}
	
	public boolean get_syncornot(){
		return syncornot;
	}
	
	public void set_syncornot(boolean b){
		syncornot = b;
	}
	
	public CloseableHttpClient get_httpclient(){
		return httpclient;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}