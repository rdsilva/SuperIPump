/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package superipump;

import Testes.ClienteSocket;
import com.orsoncharts.util.json.JSONObject;
import com.orsoncharts.util.json.parser.JSONParser;
import com.orsoncharts.util.json.parser.ParseException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.swing.JOptionPane;

/**
 *
 * @author Rodrigo
 */
public class SuperIPump {

    private static float sptq1;
    private static float mvtq1;
    private static float pvtq1;
    private static float sptq2;
    private static float mvtq2;
    private static float pvtq2;

    private static ClienteSocket cliente;//  = new ClienteSocket();

    public static void main(String[] args) {
        
        iPump view = new iPump();
        view.setVisible(true);
        view.setEnabled(true);

        String input_ip = JOptionPane.showInputDialog("IP do Middleware:");
        int input_port = Integer.parseInt(JOptionPane.showInputDialog("Port do Middleware:"));
        
        cliente = new ClienteSocket(input_ip, input_port);
        
        view.setConnVars(input_ip, input_port);
        
        
        //---------------------------------------------JSON
        JsonObject json = Json.createObjectBuilder().add("comando", 0).build();
        StringWriter stWriter = new StringWriter();

        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(json);
        }

        String jsonData = stWriter.toString();
        //---------------------------------------------

        Runnable cliente_run = () -> {
            try {
                while (true) {
                    parseJSON(cliente.sendData(jsonData));
                    view.setDataTQ1(sptq1, mvtq1, pvtq1);
                    view.setDataTQ2(sptq2, mvtq2, pvtq2);
                    Thread.sleep(1000L);
                }
            } catch (InterruptedException iex) {
            }
        };

        Thread thr1 = new Thread(cliente_run);

        thr1.start();

    }

    private static void parseJSON(String jsonData) {

        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(jsonData);

            sptq1 = Float.parseFloat(obj.get("sptq_1").toString());
            sptq2 = Float.parseFloat(obj.get("sptq_2").toString());
            mvtq1 = Float.parseFloat(obj.get("mvtq_1").toString());
            mvtq2 = Float.parseFloat(obj.get("mvtq_2").toString());
            pvtq1 = Float.parseFloat(obj.get("pvtq_1").toString());
            pvtq2 = Float.parseFloat(obj.get("pvtq_2").toString());

        } catch (ParseException ex) {
            Logger.getLogger(SuperIPump.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
