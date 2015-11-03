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
import java.awt.Frame;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
    private static float tp;
    private static float tr;
    private static float ts;
    private static float mp;
    private static float ess;
    private static float p_master;
    private static float i_master;
    private static float d_master;
    private static float p_slave;
    private static float i_slave;
    private static float d_slave;
    private static iPump view;

    private static ClienteSocket cliente;//  = new ClienteSocket();

    public static void main(String[] args) {

        view = new iPump();
        view.setExtendedState(Frame.MAXIMIZED_BOTH);
        //view.setUndecorated(true);
        view.setVisible(true);
        view.setEnabled(true);

        // gerando log no prompt de log
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String datetime = dateFormat.format(date);
        String log_start = datetime + "\n - A aplicação cliente foi iniciada!\n\n -------";

        view.setLog(log_start);

//        String input_ip = JOptionPane.showInputDialog("IP do Middleware:");
        String input_ip = (String) JOptionPane.showInputDialog(null, "IP do Middleware:",
//                "IP do Middleware", JOptionPane.QUESTION_MESSAGE, null, null, "10.13.100.83");
                "IP do Middleware", JOptionPane.QUESTION_MESSAGE, null, null, "localhost");

//        int input_port = Integer.parseInt(JOptionPane.showInputDialog("Port do Middleware:"));
        int input_port = Integer.parseInt(JOptionPane.showInputDialog(null, "Port do Middleware:",
                "Port do Middleware", JOptionPane.QUESTION_MESSAGE, null, null, 8002).toString());

        cliente = new ClienteSocket(input_ip, input_port);

        //gerando log no prompt de log
        date = new Date();
        datetime = dateFormat.format(date);
        if (cliente.getStatus()) {
            log_start = "\n" + datetime + "\n - Conectou ao Middleware!\n" + input_ip + ":" + input_port + "\n\n -------\n";
            view.setLog(log_start);
        } else {
            log_start = "\n" + datetime + "\n - FALHA ao conectar o Middleware!\n\n -------\n";
            view.setLog(log_start);
        }

        view.setConnVars(input_ip, input_port);

        //---------------------------------------------JSON
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("comando", 0);

        JsonObject json_final = json.build();

        StringWriter stWriter = new StringWriter();

        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(json_final);
        }

        String jsonData = stWriter.toString();
        //---------------------------------------------

        Runnable cliente_run = () -> {
            try {
                while (true) {
                    System.out.println("ENVIADO : " + jsonData);
                    String sendData = cliente.sendData(jsonData);
                    //gerando log no prompt de log
                    view.setLog("# RECEBIDO \n" + sendData + "\n-----------------\n");
                    //---------
                    parseJSON(sendData);
                    view.setDataTQ1(sptq1, mvtq1, pvtq1, p_master, i_master, d_master);
                    view.setDataTQ2(sptq2, mvtq2, pvtq2, p_slave, i_slave, d_slave);
                    view.setDataCalc(tr, tp, ts, mp, ess);
                    Thread.sleep(100L);
                }
            } catch (InterruptedException iex) {
            }
        };

        Thread thr1 = new Thread(cliente_run);

        thr1.start();

    }

    private static void parseJSON(String jsonData) {

        System.out.println("RECEBIDO : " + jsonData);

        if (!jsonData.contains("ack")) {
            try {
                JSONObject obj = (JSONObject) new JSONParser().parse(jsonData);

                if (jsonData.contains("sptq_1")) {
                    sptq1 = Float.parseFloat(obj.get("sptq_1").toString());
                }

                if (jsonData.contains("sptq_2")) {
                    sptq2 = Float.parseFloat(obj.get("sptq_2").toString());
                }

                if (jsonData.contains("mvtq_1")) {
                    mvtq1 = Float.parseFloat(obj.get("mvtq_1").toString());
                }

                if (jsonData.contains("mvtq_2")) {
                    mvtq2 = Float.parseFloat(obj.get("mvtq_2").toString());
                }

                if (jsonData.contains("pvtq_1")) {
                    pvtq1 = Float.parseFloat(obj.get("pvtq_1").toString());
                }

                if (jsonData.contains("pvtq_2")) {
                    pvtq2 = Float.parseFloat(obj.get("pvtq_2").toString());
                }

                if (jsonData.contains("tp")) {
                    tp = Float.parseFloat(obj.get("tp").toString());
                }

                if (jsonData.contains("tr")) {
                    tr = Float.parseFloat(obj.get("tr").toString());
                }

                if (jsonData.contains("ts")) {
                    ts = Float.parseFloat(obj.get("ts").toString());
                }

                if (jsonData.contains("mp")) {
                    mp = Float.parseFloat(obj.get("mp").toString());
                }

                if (jsonData.contains("ess")) {
                    ess = Float.parseFloat(obj.get("ess").toString());
                }
                
                if (jsonData.contains("P_master")) {
                    p_master = Float.parseFloat(obj.get("P_master").toString());
                }
                
                if (jsonData.contains("I_master")) {
                    i_master = Float.parseFloat(obj.get("I_master").toString());
                }
                
                if (jsonData.contains("D_master")) {
                    d_master = Float.parseFloat(obj.get("D_master").toString());
                }
                
                if (jsonData.contains("P_slave")) {
                    p_slave = Float.parseFloat(obj.get("P_slave").toString());
                }
                
                if (jsonData.contains("I_slave")) {
                    i_slave = Float.parseFloat(obj.get("I_slave").toString());
                }
                
                if (jsonData.contains("D_slave")) {
                    d_slave = Float.parseFloat(obj.get("D_slave").toString());
                }

            } catch (ParseException ex) {

                view.setLog("\n\nERRO NO FORMATO DO JSON RECEBIDO\n\n");

                JOptionPane.showMessageDialog(null, "Erro no parser do Json!!!");
                Logger.getLogger(SuperIPump.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            view.setLog("\n\nConexão Teste Funcionou!!!\n\n");
        }

    }
}
