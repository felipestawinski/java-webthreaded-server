import java.io.* ;
import java.net.* ;
import java.util.* ;
public final class webServer
{
public static void main(String argv[]) throws Exception
{
    int port = 6789;

    try {
        ServerSocket serverSocket = new ServerSocket(port);


        while (true) {

            Socket socket = serverSocket.accept();
            HttpRequest request = new HttpRequest(socket);
            Thread thread = new Thread(request);
            thread.start();
        }

    }
    catch (Exception e) {
        e.printStackTrace();
    }

}
}
