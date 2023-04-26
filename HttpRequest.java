import java.io.* ;
import java.net.* ;
import java.util.* ;

import javax.script.ScriptContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileReader;

public class HttpRequest implements Runnable {

    final static String CRLF = "\r\n";
    Socket socket;
    // Constructor
    public HttpRequest(Socket socket) throws Exception
    {
        this.socket = socket;
    }


    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
    {

    byte[] buffer = new byte[1024];
    int bytes = 0;

        while((bytes = fis.read(buffer)) != -1 ) {
            os.write(buffer, 0, bytes);

        }
    }

    private static String contentType(String fileName)
    {
        if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if(fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
    
    public void run()
    {
        try {
            processRequest();
            } catch (Exception e) {
            System.out.println(e);
            }
    }
    private void processRequest() throws Exception
    {
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(os);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String requestLine = br.readLine();
        System.out.println(requestLine);
        // Get and display the header lines.
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // skip over the method, which should be "GET"
        String fileName = tokens.nextToken();
        // Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;

        // Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        // Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {

            statusLine = "HTTP/1.0 200 0K" + CRLF;
            contentTypeLine = "Content-type: " +
            contentType( fileName ) + CRLF;


        } 
        else {
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: " +
            contentType( fileName ) + CRLF;
            entityBody = "<HTML>" +
            "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
            "<BODY><img src=\"404.jpeg\"></BODY></HTML>";

        }

        dataOutputStream.writeBytes(statusLine);

        dataOutputStream.writeBytes(contentTypeLine);

        dataOutputStream.writeBytes(CRLF);

        String httpResponse = null;

        if (fileExists) {
            sendBytes(fis, dataOutputStream);
            fis.close();
        } else {

            File htmlFile = new File("./error.html");
            File cssFile = new File("./error.css");
            File jssFile = new File("./error.jss");

            String htmlContent = "";
            String cssContent = "";
            String jssContent = "";

            try {
                htmlContent = new String(Files.readAllBytes(htmlFile.toPath()));
                cssContent = new String(Files.readAllBytes(cssFile.toPath()));
                jssContent = new String(Files.readAllBytes(jssFile.toPath()));
            } catch (IOException e) {
                // Handle the exception appropriately
            }

             httpResponse = "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<style>" +
                                cssContent +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                htmlContent +
                                "<script><script>" +
                                jssContent +
                                "</body>" +
                                "</html>";

            dataOutputStream.writeBytes(httpResponse);

        }

        

        os.close();
        br.close();
        socket.close();
    }


}