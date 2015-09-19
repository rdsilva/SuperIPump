package Testes;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

public class Client_tst {

    private static Socket socket;

    public static void main(String args[]) {
        try {
            String host = "localhost";
            int port = 8002;
            InetAddress address = InetAddress.getByName(host);
            
//            while (true) {
            socket = new Socket(address, port);

            //---------------------------------------------JSON
            JsonObject json = Json.createObjectBuilder().add("comando", 1).build();
            StringWriter stWriter = new StringWriter();

            try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                jsonWriter.writeObject(json);
            }

            String jsonData = stWriter.toString();
//            System.out.println(jsonData);

            //-------------------------------------------------
                //Send the message to the server
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);

                bw.write(jsonData);
                bw.flush();
                System.out.println("Message sent to the server : " + jsonData);

                //Get the return message from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message = br.readLine();
                System.out.println("Message received from the server : " + message);
                
//                Thread.sleep(1000);
//            }
        } catch (IOException exception) {
        } finally {
            //Closing the socket
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
}
