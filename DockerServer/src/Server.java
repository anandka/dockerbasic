import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static Socket socket;

	public static void main(String[] args) {
		try {
			String port = System.getenv("HOST_PORT");// 25000;
			ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
			System.out.println("Server Started and listening to the port : " + port);

			// Server is running always. This is done using this while(true)
			// loop
			while (true) {
				// Reading the message from the client
				socket = serverSocket.accept();
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String message = br.readLine();
				System.out.println("Message received : " + message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}
}