/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package superipump;

import Testes.ClienteSocket;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Rodrigo
 */
public class iPump extends javax.swing.JFrame {

    // Variaveis dos graficos
    private final TimeSeries mv_tq_01 = new TimeSeries("MV");
    private final TimeSeries mv_tq_02 = new TimeSeries("MV");
    private final TimeSeries sp_tq_01 = new TimeSeries("SP");
    private final TimeSeries sp_tq_02 = new TimeSeries("SP");
    private final TimeSeries pv_tq_01 = new TimeSeries("PV");
    private final TimeSeries pv_tq_02 = new TimeSeries("PV");
    private JFreeChart chart_tq1;
    private JFreeChart chart_tq2;

    // Variaveis de controle da planta
    private boolean malha_aberta = true;
    private boolean anti_windup = false;
    private double amplitude_min = 0;
    private double amplitude_max = 0;
    private double periodo_min = 0;
    private double periodo_max = 0;
    private double offset = 0;
    private double Kp = 0;
    private double Ki = 0;
    private double Kd = 0;
    private int Tr = 0;
    private int Mp = 0;
    private int Ts = 0;
    private int tanque = 0;  // 1 - tq_sup (default) | 2 - tq_inf | 3 - ambos
    private String controle;
    private String sinal;

    // Variaveis de conexão
    private String Ip = "localhost";
    private int Port = 8001;
    private static ClienteSocket cliente;// = new ClienteSocket();

    /**
     * Creates new form iPump
     */
    public iPump() {
        initComponents();

        //Criando o gráfcio do tanque 1
        createChartTQ1();
        //Criando o gráfcio do tanque 2
        createChartTQ2();

        revalidate();

        //listener para o combobox do tanque a ser controlado
        select_tanque.addActionListener((ActionEvent e) -> {
            int index = select_tanque.getSelectedIndex();

            switch (index) {
                case 0: // controlar o tanque superior | default
                    tanque = 0;
                    break;
                case 1: // controlar o tanque inferior
                    tanque = 1;
                    break;
                //case 2: // controlar ambos os tanques
                   // tanque = 3;
//                    break;
            }
        });

        //listener para o combobox dos sinais de controle
        select_sinal.addActionListener((ActionEvent e) -> {
            int index = select_sinal.getSelectedIndex();

            switch (index) {
                case 1: // sinal degrau
                    input_amplitude_min.setEnabled(true);
                    input_amplitude_max.setEnabled(false);
                    input_periodo_min.setEnabled(false);
                    input_periodo_max.setEnabled(false);
                    input_offset.setEnabled(false);
                    sinal = "degrau";
                    break;
                case 2: // sinal senoidal
                    input_amplitude_min.setEnabled(true);
                    input_amplitude_max.setEnabled(false);
                    input_periodo_min.setEnabled(true);
                    input_periodo_max.setEnabled(false);
                    input_offset.setEnabled(true);
                    sinal = "senoidal";
                    break;
                case 3: // sinal quadrado
                    input_amplitude_min.setEnabled(true);
                    input_amplitude_max.setEnabled(false);
                    input_periodo_min.setEnabled(true);
                    input_periodo_max.setEnabled(false);
                    input_offset.setEnabled(true);
                    sinal = "quadrado";
                    break;
                case 4: // sinal dente de serra
                    input_amplitude_min.setEnabled(true);
                    input_amplitude_max.setEnabled(false);
                    input_periodo_min.setEnabled(true);
                    input_periodo_max.setEnabled(false);
                    input_offset.setEnabled(true);
                    sinal = "serra";
                    break;
                case 5: // sinal aleatório
                    input_amplitude_min.setEnabled(true);
                    input_amplitude_max.setEnabled(true);
                    input_periodo_min.setEnabled(true);
                    input_periodo_max.setEnabled(true);
                    input_offset.setEnabled(true);
                    sinal = "aleatório";
                    break;
                default: // nenhum sinal escolhido
                    input_amplitude_min.setEnabled(false);
                    input_amplitude_max.setEnabled(false);
                    input_periodo_min.setEnabled(false);
                    input_periodo_max.setEnabled(false);
                    input_offset.setEnabled(false);
                    sinal = "";
                    break;
            }
        });

        //listener para o combobox dos tipos de controle
        select_controle.addActionListener((ActionEvent e) -> {
            int index = select_controle.getSelectedIndex();

            switch (index) {
                case 1: // tipo P
                    input_kp.setEnabled(true);
                    input_ki.setEnabled(false);
                    input_kd.setEnabled(false);
                    controle = "P";
                    break;
                case 2: // tipo PI
                    input_kp.setEnabled(true);
                    input_ki.setEnabled(true);
                    input_kd.setEnabled(false);
                    controle = "PI";
                    break;
                case 3: // tipo PD
                    input_kp.setEnabled(true);
                    input_ki.setEnabled(false);
                    input_kd.setEnabled(true);
                    controle = "PD";
                    break;
                case 4: // tipo PID
                    input_kp.setEnabled(true);
                    input_ki.setEnabled(true);
                    input_kd.setEnabled(true);
                    controle = "PID";
                    break;
                case 5: // tipo PI-D
                    input_kp.setEnabled(true);
                    input_ki.setEnabled(true);
                    input_kd.setEnabled(true);
                    controle = "PI-D";
                    break;
                default: // nenhum tipo escolhido
                    input_kp.setEnabled(false);
                    input_ki.setEnabled(false);
                    input_kd.setEnabled(false);
                    controle = "";
                    break;
            }
        });

        //listener para o combobox do calculo de Mp
        select_mp.addActionListener((ActionEvent e) -> {
            Mp = select_mp.getSelectedIndex() + 1;
        });

        //listener para o combobox do calculo de Tr
        select_tr.addActionListener((ActionEvent e) -> {
            Tr = select_tr.getSelectedIndex() + 1;
        });

        //listener para o combobox do calculo de Ts
        select_ts.addActionListener((ActionEvent e) -> {
            Ts = select_ts.getSelectedIndex() + 1;
        });

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        pbar_tq1 = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        pbar_tq2 = new javax.swing.JProgressBar();
        lbl_nivel_tq1 = new javax.swing.JLabel();
        lbl_nivel_tq2 = new javax.swing.JLabel();
        btn_emergencia = new javax.swing.JButton();
        btn_malha = new javax.swing.JToggleButton();
        btn_windup = new javax.swing.JToggleButton();
        btn_enviar = new javax.swing.JButton();
        select_sinal = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        input_amplitude_min = new javax.swing.JTextField();
        input_periodo_min = new javax.swing.JTextField();
        input_offset = new javax.swing.JTextField();
        select_controle = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        input_kd = new javax.swing.JTextField();
        input_ki = new javax.swing.JTextField();
        input_kp = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        select_tr = new javax.swing.JComboBox();
        select_mp = new javax.swing.JComboBox();
        select_ts = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        input_ts = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        input_mp = new javax.swing.JTextField();
        input_tp = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        input_tr = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        input_ess = new javax.swing.JTextField();
        panel_chart_tq1 = new javax.swing.JPanel();
        panel_chart_tq2 = new javax.swing.JPanel();
        input_periodo_max = new javax.swing.JTextField();
        input_amplitude_max = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        select_tanque = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("iPump Supervisório - Turma 3 | Grupo 3");

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel1.setText("Tanque 1");

        pbar_tq1.setMaximum(30);
        pbar_tq1.setOrientation(1);
        pbar_tq1.setToolTipText("Nível do Tanque Superior");
        pbar_tq1.setValue(23);
        pbar_tq1.setPreferredSize(new java.awt.Dimension(60, 145));
        pbar_tq1.setSize(new java.awt.Dimension(145, 60));
        pbar_tq1.setStringPainted(true);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("Tanque 2");

        pbar_tq2.setMaximum(30);
        pbar_tq2.setOrientation(1);
        pbar_tq2.setToolTipText("Nível do Tanque Inferior");
        pbar_tq2.setValue(15);
        pbar_tq2.setPreferredSize(new java.awt.Dimension(60, 145));
        pbar_tq2.setSize(new java.awt.Dimension(145, 60));
        pbar_tq2.setStringPainted(true);

        lbl_nivel_tq1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_nivel_tq1.setText("XX cm");

        lbl_nivel_tq2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_nivel_tq2.setText("XX cm");

        btn_emergencia.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        btn_emergencia.setForeground(java.awt.Color.red);
        btn_emergencia.setText("EMERGÊNCIA");
        btn_emergencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_emergenciaActionPerformed(evt);
            }
        });

        btn_malha.setText("Malha FECHADA");
        btn_malha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_malhaActionPerformed(evt);
            }
        });

        btn_windup.setText("Anti - WindUP (off)");
        btn_windup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_windupActionPerformed(evt);
            }
        });

        btn_enviar.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        btn_enviar.setText("ENVIAR");
        btn_enviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_enviarActionPerformed(evt);
            }
        });

        select_sinal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sinal...", "Degrau", "Senoidal", "Quadrado", "Dente de Serra", "Aleatório" }));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Amplitude (V)");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Período (s)");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Off Set (s)");

        input_amplitude_min.setEnabled(false);

        input_periodo_min.setEnabled(false);

        input_offset.setEnabled(false);

        select_controle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Controle...", "P", "PI", "PD", "PID", "PI-D" }));
        select_controle.setEnabled(false);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Kp");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Ki");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Kd");

        input_kd.setEnabled(false);

        input_ki.setEnabled(false);

        input_kp.setEnabled(false);

        jLabel11.setText("Selecione Tr");

        jLabel12.setText("Selecione Mp");

        jLabel13.setText("Selecione Ts");

        select_tr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0% <-> 100%", "5% <-> 95%", "10% <-> 90%" }));

        select_mp.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Porcentagem (%)", "Absoluto" }));

        select_ts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2%", "5%", "7%", "10%" }));
        select_ts.setSelectedIndex(1);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Ts");

        input_ts.setEnabled(false);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Mp");

        input_mp.setEnabled(false);

        input_tp.setEnabled(false);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Tp");

        input_tr.setEnabled(false);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Tr");
        jLabel17.setToolTipText("");

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Ess");

        input_ess.setEnabled(false);

        panel_chart_tq1.setBorder(javax.swing.BorderFactory.createTitledBorder("Gráfico - Tanque 1"));
        panel_chart_tq1.setPreferredSize(new java.awt.Dimension(12, 185));

        javax.swing.GroupLayout panel_chart_tq1Layout = new javax.swing.GroupLayout(panel_chart_tq1);
        panel_chart_tq1.setLayout(panel_chart_tq1Layout);
        panel_chart_tq1Layout.setHorizontalGroup(
            panel_chart_tq1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_chart_tq1Layout.setVerticalGroup(
            panel_chart_tq1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );

        panel_chart_tq2.setBorder(javax.swing.BorderFactory.createTitledBorder("Gráfico - Tanque 2"));
        panel_chart_tq2.setPreferredSize(new java.awt.Dimension(12, 185));

        javax.swing.GroupLayout panel_chart_tq2Layout = new javax.swing.GroupLayout(panel_chart_tq2);
        panel_chart_tq2.setLayout(panel_chart_tq2Layout);
        panel_chart_tq2Layout.setHorizontalGroup(
            panel_chart_tq2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_chart_tq2Layout.setVerticalGroup(
            panel_chart_tq2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );

        input_periodo_max.setEnabled(false);

        input_amplitude_max.setEnabled(false);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        select_tanque.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tanque 1", "Tanque 2", "Ambos" }));

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel3.setText("Qual tanque controlar ?");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_emergencia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lbl_nivel_tq1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pbar_tq1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pbar_tq2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_nivel_tq2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(btn_malha, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_windup, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_enviar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(select_tanque, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(input_offset, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(input_periodo_min)
                                    .addComponent(input_amplitude_min))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(input_amplitude_max, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(input_periodo_max)))
                            .addComponent(select_sinal, 0, 209, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(input_ki, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                    .addComponent(input_kp)
                                    .addComponent(input_kd)))
                            .addComponent(select_controle, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(select_tr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(select_mp, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addGap(18, 18, 18)
                                .addComponent(select_ts, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(input_tr)
                            .addComponent(input_mp)
                            .addComponent(input_ts, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(input_tp)
                            .addComponent(input_ess, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(panel_chart_tq1, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE)
                    .addComponent(panel_chart_tq2, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel_chart_tq1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panel_chart_tq2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(select_controle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8)
                                .addComponent(input_kp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(input_ki, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(input_kd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(select_sinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(input_amplitude_max, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(input_periodo_max, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(input_amplitude_min, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(input_periodo_min, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(input_offset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(select_tr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(select_mp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(select_ts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(14, 14, 14))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(input_tr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel17)
                                .addComponent(input_tp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel16))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(input_mp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15)
                                .addComponent(input_ess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel18))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(input_ts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14))
                            .addGap(12, 12, 12))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pbar_tq1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pbar_tq2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_nivel_tq1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbl_nivel_tq2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(select_tanque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_emergencia, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_malha, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_windup, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_enviar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_emergenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_emergenciaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_emergenciaActionPerformed

    private void btn_enviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_enviarActionPerformed

        int index_sinal = select_sinal.getSelectedIndex();
        int index_ctrl = select_controle.getSelectedIndex();

        readVars();

        if (malha_aberta) {
            if (index_sinal == 0) {
                JOptionPane.showMessageDialog(this,
                        "Você deve selecionar um sinal!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                //---------------------------------------------JSON

                JsonObjectBuilder json = Json.createObjectBuilder();
                JsonObjectBuilder json_sinal = Json.createObjectBuilder();
                json_sinal.add("tipo", sinal)
                        .add("amp_max", amplitude_max)
                        .add("amp_min", amplitude_min)
                        .add("periodo_max", periodo_max)
                        .add("periodo_min", periodo_min)
                        .add("offset", offset);

                json.add("comando", 1)
                        .add("malha_aberta", true)
                        .add("tanque", tanque)
                        .add("Tr", Tr)
                        .add("Ts", Ts)
                        .add("Mp", Mp);

                json.add("sinal", json_sinal);

                JsonObject json_final = json.build();

                StringWriter stWriter = new StringWriter();

                try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                    jsonWriter.writeObject(json_final);
                }

                String jsonData = stWriter.toString();
                String sendData = cliente.sendData(jsonData);
                System.out.println(sendData);
                //---------------------------------------------

            }
        } else {
            if (index_sinal == 0) {
                JOptionPane.showMessageDialog(this,
                        "Você deve selecionar um sinal!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                if (index_ctrl == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Você deve selecionar um controle!",
                            "Cuidado!",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    //---------------------------------------------JSON

                    JsonObjectBuilder json = Json.createObjectBuilder();
                    JsonObjectBuilder json_sinal = Json.createObjectBuilder();
                    JsonObjectBuilder json_ctrl = Json.createObjectBuilder();
                    json_sinal.add("tipo", sinal)
                            .add("amp_max", amplitude_max)
                            .add("amp_min", amplitude_min)
                            .add("periodo_max", periodo_max)
                            .add("periodo_min", periodo_min)
                            .add("offset", offset);

                    json_ctrl.add("tipo", controle)
                            .add("Kp", Kp)
                            .add("Ki", Ki)
                            .add("Kd", Kd);

                    json.add("comando", 1)
                            .add("malha_aberta", false)
                            .add("tanque", tanque)
                            .add("Tr", Tr)
                            .add("Ts", Ts)
                            .add("Mp", Mp);

                    json.add("sinal", json_sinal);
                    json.add("controle", json_ctrl);

                    JsonObject json_final = json.build();

                    StringWriter stWriter = new StringWriter();

                    try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                        jsonWriter.writeObject(json_final);
                    }

                    String jsonData = stWriter.toString();
                    String sendData = cliente.sendData(jsonData);
                    System.out.println(sendData);
                    //---------------------------------------------

                }
            }
        }


    }//GEN-LAST:event_btn_enviarActionPerformed

    private void btn_windupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_windupActionPerformed
        if (btn_windup.isSelected()) {
            btn_windup.setText("Anti - WindUP (on)");
            anti_windup = true;
        } else {
            btn_windup.setText("Anti - WindUP (off)");
            anti_windup = false;
        }
    }//GEN-LAST:event_btn_windupActionPerformed

    private void btn_malhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_malhaActionPerformed
        if (btn_malha.isSelected()) {
            select_controle.setEnabled(true);
            btn_malha.setText("Malha ABERTA");
        } else {
            btn_malha.setText("Malha FECHADA");
            select_controle.setEnabled(false);
            input_kp.setEnabled(false);
            input_ki.setEnabled(false);
            input_kd.setEnabled(false);
        }

        malha_aberta = !btn_malha.isSelected();
    }//GEN-LAST:event_btn_malhaActionPerformed

    private void readVars() {
        //Amplitude Maxima
        if (input_amplitude_max.isEnabled()) {
            if (!input_amplitude_max.getText().isEmpty()) {
                amplitude_max = Double.parseDouble(input_amplitude_max.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Amplitude Máxima!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            amplitude_max = 0;
        }

        //Amplitude Minima
        if (input_amplitude_min.isEnabled()) {
            if (!input_amplitude_min.getText().isEmpty()) {
                amplitude_min = Double.parseDouble(input_amplitude_min.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Amplitude Mínima!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            amplitude_min = 0;
        }

        //Período Máximo
        if (input_periodo_max.isEnabled()) {
            if (!input_periodo_max.getText().isEmpty()) {
                periodo_max = Double.parseDouble(input_periodo_max.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Período Máximo!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            periodo_max = 0;
        }

        //Período Mínimo
        if (input_periodo_min.isEnabled()) {
            if (!input_periodo_min.getText().isEmpty()) {
                periodo_min = Double.parseDouble(input_periodo_min.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Período Mínimo!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            periodo_min = 0;
        }

        //Offset
        if (input_offset.isEnabled()) {
            if (!input_offset.getText().isEmpty()) {
                offset = Double.parseDouble(input_offset.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Período Mínimo!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            offset = 0;
        }

        //Kp
        if (input_kp.isEnabled()) {
            if (!input_kp.getText().isEmpty()) {
                Kp = Double.parseDouble(input_kp.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Kp!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Kp = 0;
        }

        //Ki
        if (input_ki.isEnabled()) {
            if (!input_ki.getText().isEmpty()) {
                Ki = Double.parseDouble(input_ki.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Ki!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Ki = 0;
        }

        //Kd
        if (input_kd.isEnabled()) {
            if (!input_kd.getText().isEmpty()) {
                Kd = Double.parseDouble(input_kd.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Kd!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Kd = 0;
        }

    }

    public void setDataTQ1(float sp, float mv, float pv) {
        pbar_tq1.setValue((int) sp);
        lbl_nivel_tq1.setText(sp + " cm");

        mv_tq_01.addOrUpdate(new Millisecond(), mv);
        sp_tq_01.addOrUpdate(new Millisecond(), sp);
        pv_tq_01.addOrUpdate(new Millisecond(), pv);

    }

    public void setDataTQ2(float sp, float mv, float pv) {
        pbar_tq2.setValue((int) sp);
        lbl_nivel_tq2.setText(sp + " cm");

        mv_tq_02.addOrUpdate(new Millisecond(), mv);
        sp_tq_02.addOrUpdate(new Millisecond(), sp);
        pv_tq_02.addOrUpdate(new Millisecond(), pv);

    }

    public void setDataCalc(float tr, float tp, float ts, float mp, float ess) {
        input_tr.setText(tr + "");
        input_tp.setText(tp + "");
        input_ts.setText(ts + "");
        input_mp.setText(mp + "");
        input_ess.setText(ess + "");
    }

    private void createChartTQ1() {
        final XYDataset dataset = createDatasetTQ1();
        final JFreeChart chart = createChartTQ(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(95, 25));
        chartPanel.setMouseZoomable(true, false);
        panel_chart_tq1.setLayout(new java.awt.BorderLayout());
        panel_chart_tq1.add(chartPanel, BorderLayout.CENTER);
        panel_chart_tq1.validate();
    }

    private void createChartTQ2() {
        final XYDataset dataset = createDatasetTQ2();
        final JFreeChart chart = createChartTQ(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(95, 25));
        chartPanel.setMouseZoomable(true, false);
        panel_chart_tq2.setLayout(new java.awt.BorderLayout());
        panel_chart_tq2.add(chartPanel, BorderLayout.CENTER);
        panel_chart_tq2.validate();
    }

    private XYDataset createDatasetTQ1() {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        mv_tq_01.add(new Millisecond(), 0.0);
        sp_tq_01.add(new Millisecond(), 0.0);
        pv_tq_01.add(new Millisecond(), 0.0);

        dataset.addSeries(mv_tq_01);
        dataset.addSeries(sp_tq_01);
        dataset.addSeries(pv_tq_01);

        return dataset;
    }

    private XYDataset createDatasetTQ2() {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        mv_tq_02.add(new Millisecond(), 0.0);
        sp_tq_02.add(new Millisecond(), 0.0);
        pv_tq_02.add(new Millisecond(), 0.0);

        dataset.addSeries(mv_tq_02);
        dataset.addSeries(sp_tq_02);
        dataset.addSeries(pv_tq_02);

        return dataset;
    }

    private JFreeChart createChartTQ(final XYDataset dataset) {

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                " ",
                " ",
                " ",
                dataset,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.white);

//        final StandardLegend sl = (StandardLegend) chart.getLegend();
        //      sl.setDisplaySeriesShapes(true);
        final XYPlot plot = chart.getXYPlot();
        //plot.setOutlinePaint(null);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(false);

        final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
//            rr.setPlotShapes(true);
            rr.setShapesFilled(true);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        }

//        final DateAxis axis = (DateAxis) plot.getDomainAxis();
//        axis.setDateFormatOverride(new SimpleDateFormat("mm:ss a"));
        return chart;

    }

    public void setConnVars(String ip, int port) {
        this.Ip = ip;
        this.Port = port;

        cliente = new ClienteSocket(Ip, Port);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(iPump.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(iPump.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(iPump.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(iPump.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new iPump().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_emergencia;
    private javax.swing.JButton btn_enviar;
    private javax.swing.JToggleButton btn_malha;
    private javax.swing.JToggleButton btn_windup;
    private javax.swing.JTextField input_amplitude_max;
    private javax.swing.JTextField input_amplitude_min;
    private javax.swing.JTextField input_ess;
    private javax.swing.JTextField input_kd;
    private javax.swing.JTextField input_ki;
    private javax.swing.JTextField input_kp;
    private javax.swing.JTextField input_mp;
    private javax.swing.JTextField input_offset;
    private javax.swing.JTextField input_periodo_max;
    private javax.swing.JTextField input_periodo_min;
    private javax.swing.JTextField input_tp;
    private javax.swing.JTextField input_tr;
    private javax.swing.JTextField input_ts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbl_nivel_tq1;
    private javax.swing.JLabel lbl_nivel_tq2;
    private javax.swing.JPanel panel_chart_tq1;
    private javax.swing.JPanel panel_chart_tq2;
    private javax.swing.JProgressBar pbar_tq1;
    private javax.swing.JProgressBar pbar_tq2;
    private javax.swing.JComboBox select_controle;
    private javax.swing.JComboBox select_mp;
    private javax.swing.JComboBox select_sinal;
    private javax.swing.JComboBox select_tanque;
    private javax.swing.JComboBox select_tr;
    private javax.swing.JComboBox select_ts;
    // End of variables declaration//GEN-END:variables

}
