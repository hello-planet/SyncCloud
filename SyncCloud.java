package syncto;

import javax.swing.JFrame;

public class SyncCloud {

	public static void main(String[] args) {
		JFrame signinpage = new SignInPage(400,200,"Welcome");
		signinpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		signinpage.setResizable(false);
		signinpage.setVisible(true);
		signinpage.setLocationRelativeTo(null);
	}
}