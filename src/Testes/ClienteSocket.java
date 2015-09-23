package Testes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteSocket {

    private Socket socket;
    private String host = "localhost";
    private int port = 8002;
    private InetAddress address;

    public ClienteSocket(String host, int port) {
//    public ClienteSocket() {
        this.host = host;
        this.port = port;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClienteSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String sendData(String jsonData) {
        try {

            socket = new Socket(address, port);

            //---------------------------------------------JSON
//            JsonObject json = Json.createObjectBuilder().add("comando", 0).build();
//            StringWriter stWriter = new StringWriter();
//
//            try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
//                jsonWriter.writeObject(json);
//            }
//
//            String jsonData = stWriter.toString();
            System.out.println(jsonData);

            //-------------------------------------------------
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(jsonData);
            bw.flush();
                //System.out.println("Message sent to the server : " + jsonData);

            //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            //System.out.println("Message received from the server : " + message);

            return message;
        } catch (IOException exception) {
        } finally {
            //Closing the socket
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
}
