import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
	private static Socket socket;

	public static void main(String args[]) {
		String host = System.getenv("HOST_IP");
		String port = System.getenv("HOST_PORT");
		
		System.out.println("posting messages on " + host +":" + port);
		
		for (int i = 0; i < 10; i++) {
			try {								
				InetAddress address = InetAddress.getByName(host);
				socket = new Socket(address, Integer.parseInt(port));
				// Send the message to the server
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

				String msg = "Hello from XOR";

				String sendMessage = msg + "_" + i;
				bw.write(sendMessage);
				bw.flush();

				try {
					// sending the actual Thread of execution to sleep X milliseconds
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println("Exception : " + e.getMessage());
				}
				System.out.println("Message sent to the server : " + sendMessage);
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				// Closing the socket
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}