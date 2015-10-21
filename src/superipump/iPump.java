/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package superipump;

import Aux.*;
import Testes.ClienteSocket;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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
    private TimeSeries mv_tq_01 = new TimeSeries("MV");
    private TimeSeries mv_tq_02 = new TimeSeries("MV");
    private TimeSeries sp_tq_01 = new TimeSeries("SP");
    private TimeSeries sp_tq_02 = new TimeSeries("SP");
    private TimeSeries pv_tq_01 = new TimeSeries("PV");
    private TimeSeries pv_tq_02 = new TimeSeries("PV");
    private TimeSeries p_tq_01 = new TimeSeries("P");
    private TimeSeries i_tq_01 = new TimeSeries("I");
    private TimeSeries d_tq_01 = new TimeSeries("D");
    private TimeSeries p_tq_02 = new TimeSeries("P");
    private TimeSeries i_tq_02 = new TimeSeries("I");
    private TimeSeries d_tq_02 = new TimeSeries("D");
    private JFreeChart chart_tq1;
    private JFreeChart chart_tq2;
    private JFreeChart chart_calculados_tq1;
    private JFreeChart chart_calculados_tq2;

    // Variaveis de controle da planta
    private boolean malha_aberta = true;
    private boolean anti_windup = false;
    private double amplitude_min = 0;
    private double amplitude_max = 0;
    private double periodo_min = 0;
    private double periodo_max = 0;
    private double offset = 0;
    private double Kp_ctrl1 = 0;
    private double Ki_ctrl1 = 0;
    private double Kd_ctrl1 = 0;
    private double Kp_ctrl2 = 0;
    private double Ki_ctrl2 = 0;
    private double Kd_ctrl2 = 0;
    private int Tr = 0;
    private int Mp = 0;
    private int Ts = 0;
    private int tanque = 0;  // 1 - tq_sup (default) | 2 - tq_inf | 3 - ambos
    private String controle_ctrl1;
    private String controle_ctrl2;
    private String tipo_controlador = "simples";
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

        log_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        DefaultCaret caret = (DefaultCaret) log_area.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        //Criando o gráfcio referentes ao tanque 1
        createChartTQ1();
        createChartCalculadosTQ1();
        //Criando o gráfcio referentes ao tanque 2
        createChartTQ2();
        createChartCalculadosTQ2();

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
                case 2: // controlar ambos os tanques
                    tanque = 3;
                    break;
            }
        });

        //listener para o combobox dos sinais de controle
        select_sinal.addActionListener((ActionEvent e) -> {
            int index = select_sinal.getSelectedIndex();

            switch (index) {
                case 1: // sinal degrau
                    input_amplitude_min.setEnabled(false);
                    input_amplitude_max.setEnabled(true);
                    input_periodo_min.setEnabled(false);
                    input_periodo_max.setEnabled(false);
                    input_offset.setEnabled(false);
                    sinal = "degrau";
                    break;
                case 2: // sinal senoidal
                    input_amplitude_min.setEnabled(false);
                    input_amplitude_max.setEnabled(true);
                    input_periodo_min.setEnabled(false);
                    input_periodo_max.setEnabled(true);
                    input_offset.setEnabled(true);
                    sinal = "senoidal";
                    break;
                case 3: // sinal quadrado
                    input_amplitude_min.setEnabled(false);
                    input_amplitude_max.setEnabled(true);
                    input_periodo_min.setEnabled(false);
                    input_periodo_max.setEnabled(true);
                    input_offset.setEnabled(true);
                    sinal = "quadrado";
                    break;
                case 4: // sinal dente de serra
                    input_amplitude_min.setEnabled(false);
                    input_amplitude_max.setEnabled(true);
                    input_periodo_min.setEnabled(false);
                    input_periodo_max.setEnabled(true);
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
        select_controle_ctrl1.addActionListener((ActionEvent e) -> {
            int index = select_controle_ctrl1.getSelectedIndex();

            switch (index) {
                case 1: // tipo P
                    input_kp_ctrl1.setEnabled(true);
                    input_ki_ctrl1.setEnabled(false);
                    input_kd_ctrl1.setEnabled(false);
                    controle_ctrl1 = "P";
                    break;
                case 2: // tipo PI
                    input_kp_ctrl1.setEnabled(true);
                    input_ki_ctrl1.setEnabled(true);
                    input_kd_ctrl1.setEnabled(false);
                    controle_ctrl1 = "PI";
                    break;
                case 3: // tipo PD
                    input_kp_ctrl1.setEnabled(true);
                    input_ki_ctrl1.setEnabled(false);
                    input_kd_ctrl1.setEnabled(true);
                    controle_ctrl1 = "PD";
                    break;
                case 4: // tipo PID
                    input_kp_ctrl1.setEnabled(true);
                    input_ki_ctrl1.setEnabled(true);
                    input_kd_ctrl1.setEnabled(true);
                    controle_ctrl1 = "PID";
                    break;
                case 5: // tipo PI-D
                    input_kp_ctrl1.setEnabled(true);
                    input_ki_ctrl1.setEnabled(true);
                    input_kd_ctrl1.setEnabled(true);
                    controle_ctrl1 = "PI-D";
                    break;
                default: // nenhum tipo escolhido
                    input_kp_ctrl1.setEnabled(false);
                    input_ki_ctrl1.setEnabled(false);
                    input_kd_ctrl1.setEnabled(false);
                    controle_ctrl1 = "";
                    break;
            }
        });

        select_controle_ctrl2.addActionListener((ActionEvent e) -> {
            int index = select_controle_ctrl2.getSelectedIndex();

            switch (index) {
                case 1: // tipo P
                    input_kp_ctrl2.setEnabled(true);
                    input_ki_ctrl2.setEnabled(false);
                    input_kd_ctrl2.setEnabled(false);
                    controle_ctrl2 = "P";
                    break;
                case 2: // tipo PI
                    input_kp_ctrl2.setEnabled(true);
                    input_ki_ctrl2.setEnabled(true);
                    input_kd_ctrl2.setEnabled(false);
                    controle_ctrl2 = "PI";
                    break;
                case 3: // tipo PD
                    input_kp_ctrl2.setEnabled(true);
                    input_ki_ctrl2.setEnabled(false);
                    input_kd_ctrl2.setEnabled(true);
                    controle_ctrl2 = "PD";
                    break;
                case 4: // tipo PID
                    input_kp_ctrl2.setEnabled(true);
                    input_ki_ctrl2.setEnabled(true);
                    input_kd_ctrl2.setEnabled(true);
                    controle_ctrl2 = "PID";
                    break;
                case 5: // tipo PI-D
                    input_kp_ctrl2.setEnabled(true);
                    input_ki_ctrl2.setEnabled(true);
                    input_kd_ctrl2.setEnabled(true);
                    controle_ctrl2 = "PI-D";
                    break;
                default: // nenhum tipo escolhido
                    input_kp_ctrl2.setEnabled(false);
                    input_ki_ctrl2.setEnabled(false);
                    input_kd_ctrl2.setEnabled(false);
                    controle_ctrl2 = "";
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

        //listener para o combobox do Tipo de Controlador
        select_tipo_controle_mf.addActionListener((ActionEvent e) -> {
            int index = select_tipo_controle_mf.getSelectedIndex();

            switch (index) {
                case 0:
                    tipo_controlador = "simples";
                    select_controle_ctrl2.setEnabled(false);
                    break;
                case 1:
                    tipo_controlador = "cascata";
                    select_controle_ctrl2.setEnabled(true);
                    break;
            }
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

        jMenuItem3 = new javax.swing.JMenuItem();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenu3 = new javax.swing.JMenu();
        jLabel1 = new javax.swing.JLabel();
        pbar_tq1 = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        pbar_tq2 = new javax.swing.JProgressBar();
        lbl_nivel_tq1 = new javax.swing.JLabel();
        lbl_nivel_tq2 = new javax.swing.JLabel();
        btn_reset = new javax.swing.JButton();
        btn_malha = new javax.swing.JToggleButton();
        btn_windup = new javax.swing.JToggleButton();
        btn_enviar = new javax.swing.JButton();
        panel_chart_tq1 = new javax.swing.JPanel();
        panel_chart_tq2 = new javax.swing.JPanel();
        select_tanque = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        log_pane = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        log_area = new javax.swing.JTextArea();
        btn_limpar_log = new javax.swing.JButton();
        btn_salvar_log = new javax.swing.JButton();
        select_tipo_controle_mf = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        select_controle_ctrl1 = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        input_kd_ctrl1 = new javax.swing.JTextField();
        input_ki_ctrl1 = new javax.swing.JTextField();
        input_kp_ctrl1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        select_tr = new javax.swing.JComboBox();
        select_mp = new javax.swing.JComboBox();
        select_ts = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        lbl_ts = new javax.swing.JLabel();
        lbl_mp = new javax.swing.JLabel();
        lbl_ess = new javax.swing.JLabel();
        lbl_tp = new javax.swing.JLabel();
        lbl_tr = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        select_controle_ctrl2 = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        input_kd_ctrl2 = new javax.swing.JTextField();
        input_ki_ctrl2 = new javax.swing.JTextField();
        input_kp_ctrl2 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        select_sinal = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        input_amplitude_min = new javax.swing.JTextField();
        input_periodo_min = new javax.swing.JTextField();
        input_offset = new javax.swing.JTextField();
        input_periodo_max = new javax.swing.JTextField();
        input_amplitude_max = new javax.swing.JTextField();
        panel_chart_tq3 = new javax.swing.JPanel();
        panel_chart_tq4 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menu_conectar = new javax.swing.JMenuItem();
        menu_desconectar = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        menu_salvar_log = new javax.swing.JMenuItem();
        menu_limpar_log = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        menu_graficos = new javax.swing.JMenu();
        menu_grafico_externo_tq1 = new javax.swing.JMenuItem();
        menu_grafico_externo_tq2 = new javax.swing.JMenuItem();
        menu_grafico_externo_calc_tq1 = new javax.swing.JMenuItem();
        menu_grafico_externo_calc_tq2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        menu_sobre_ipump = new javax.swing.JMenuItem();
        menu_sobre_grupo = new javax.swing.JMenuItem();
        menu_sobre_protocolo = new javax.swing.JMenuItem();

        jMenuItem3.setText("jMenuItem3");

        jMenu3.setText("jMenu3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("iPump Supervisório - Turma 3 | Grupo 3");

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel1.setText("Tanque 1");

        pbar_tq1.setMaximum(30);
        pbar_tq1.setOrientation(1);
        pbar_tq1.setToolTipText("Nível do Tanque Superior");
        pbar_tq1.setValue(23);
        pbar_tq1.setPreferredSize(new java.awt.Dimension(60, 145));
        pbar_tq1.setSize(new java.awt.Dimension(60, 145));
        pbar_tq1.setStringPainted(true);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("Tanque 2");

        pbar_tq2.setMaximum(30);
        pbar_tq2.setOrientation(1);
        pbar_tq2.setToolTipText("Nível do Tanque Inferior");
        pbar_tq2.setValue(15);
        pbar_tq2.setPreferredSize(new java.awt.Dimension(60, 145));
        pbar_tq2.setSize(new java.awt.Dimension(60, 145));
        pbar_tq2.setStringPainted(true);

        lbl_nivel_tq1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_nivel_tq1.setText("XX cm");

        lbl_nivel_tq2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_nivel_tq2.setText("XX cm");

        btn_reset.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        btn_reset.setForeground(java.awt.Color.red);
        btn_reset.setLabel("RESET");
        btn_reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_resetActionPerformed(evt);
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
            .addGap(0, 0, Short.MAX_VALUE)
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
            .addGap(0, 0, Short.MAX_VALUE)
        );

        select_tanque.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tanque 1", "Tanque 2", "Ambos" }));

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel3.setText("Qual tanque controlar ?");

        log_pane.setViewportBorder(javax.swing.BorderFactory.createTitledBorder("Log :"));

        log_area.setColumns(20);
        log_area.setLineWrap(true);
        log_area.setRows(5);
        log_area.setWrapStyleWord(true);
        log_area.setEnabled(false);
        jScrollPane1.setViewportView(log_area);

        log_pane.setViewportView(jScrollPane1);

        btn_limpar_log.setText("Limpar Log");
        btn_limpar_log.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_limpar_logActionPerformed(evt);
            }
        });

        btn_salvar_log.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        btn_salvar_log.setText("Salvar Log");
        btn_salvar_log.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_salvar_logActionPerformed(evt);
            }
        });

        select_tipo_controle_mf.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Simples", "Cascata" }));
        select_tipo_controle_mf.setEnabled(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Mestre"));

        select_controle_ctrl1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Controle...", "P", "PI", "PD", "PID", "PI-D" }));
        select_controle_ctrl1.setEnabled(false);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Kp");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Ki");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Kd");

        input_kd_ctrl1.setEnabled(false);

        input_ki_ctrl1.setEnabled(false);

        input_kp_ctrl1.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(select_controle_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(input_kd_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(input_ki_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(input_kp_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(34, 34, 34))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(select_controle_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel8))
                    .addComponent(input_kp_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel9))
                    .addComponent(input_ki_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel10))
                    .addComponent(input_kd_ctrl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Forma de Calculo"));

        jLabel11.setText("Selecione Tr");

        jLabel12.setText("Selecione Mp");

        jLabel13.setText("Selecione Ts");

        select_tr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0% <-> 100%", "5% <-> 95%", "10% <-> 90%" }));

        select_mp.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Porcentagem (%)", "Absoluto" }));

        select_ts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2%", "5%", "7%", "10%" }));
        select_ts.setSelectedIndex(1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel11)
                    .addComponent(select_tr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(select_mp, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(select_ts, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addGap(4, 4, 4)
                .addComponent(select_tr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addGap(4, 4, 4)
                .addComponent(select_mp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(4, 4, 4)
                .addComponent(select_ts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Variavéis Calculadas"));

        lbl_ts.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_ts.setText("<html>Ts : <b style=\"color:red\">0.0</b></html>");

        lbl_mp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_mp.setText("<html>Mp : <b style=\"color:red\">0.0</b></html>");

        lbl_ess.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_ess.setText("<html>Ess : <b style=\"color:red\">0.0</b></html>");

        lbl_tp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_tp.setText("<html>Tp : <b style=\"color:red\">0.0</b></html>");

        lbl_tr.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_tr.setText("<html>Tr : <b style=\"color:red\">0.0</b></html>");
        lbl_tr.setToolTipText("");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_ess, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                    .addComponent(lbl_tp)
                    .addComponent(lbl_ts)
                    .addComponent(lbl_mp)
                    .addComponent(lbl_tr))
                .addGap(45, 45, 45))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lbl_tr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(lbl_mp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_ts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_tp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_ess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Escravo"));

        select_controle_ctrl2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Controle...", "P", "PI", "PD", "PID", "PI-D" }));
        select_controle_ctrl2.setEnabled(false);

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Kp");

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Ki");

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("Kd");

        input_kd_ctrl2.setEnabled(false);

        input_ki_ctrl2.setEnabled(false);

        input_kp_ctrl2.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(select_controle_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(input_kd_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(input_ki_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(input_kp_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(34, 34, 34))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(select_controle_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel19))
                    .addComponent(input_kp_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel20))
                    .addComponent(input_ki_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel21))
                    .addComponent(input_kd_ctrl2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Sinal de Entrada"));

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

        input_periodo_max.setEnabled(false);

        input_amplitude_max.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(select_sinal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(input_periodo_min, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(input_amplitude_min, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(input_offset, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(input_periodo_max, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(input_amplitude_max, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(select_sinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(input_amplitude_min, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5))
                    .addComponent(input_amplitude_max, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel6))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(input_periodo_min, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(input_periodo_max, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel7))
                    .addComponent(input_offset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        panel_chart_tq3.setBorder(javax.swing.BorderFactory.createTitledBorder("Calculados - Controlador 1"));
        panel_chart_tq3.setPreferredSize(new java.awt.Dimension(12, 185));

        javax.swing.GroupLayout panel_chart_tq3Layout = new javax.swing.GroupLayout(panel_chart_tq3);
        panel_chart_tq3.setLayout(panel_chart_tq3Layout);
        panel_chart_tq3Layout.setHorizontalGroup(
            panel_chart_tq3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_chart_tq3Layout.setVerticalGroup(
            panel_chart_tq3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        panel_chart_tq4.setBorder(javax.swing.BorderFactory.createTitledBorder("Calculados - Controlador 2"));
        panel_chart_tq4.setPreferredSize(new java.awt.Dimension(12, 185));

        javax.swing.GroupLayout panel_chart_tq4Layout = new javax.swing.GroupLayout(panel_chart_tq4);
        panel_chart_tq4.setLayout(panel_chart_tq4Layout);
        panel_chart_tq4Layout.setHorizontalGroup(
            panel_chart_tq4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_chart_tq4Layout.setVerticalGroup(
            panel_chart_tq4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jMenu1.setText("Supervisório");

        menu_conectar.setText("Conectar Servidor");
        menu_conectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_conectarActionPerformed(evt);
            }
        });
        jMenu1.add(menu_conectar);

        menu_desconectar.setText("Desconectar Servidor");
        menu_desconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_desconectarActionPerformed(evt);
            }
        });
        jMenu1.add(menu_desconectar);
        jMenu1.add(jSeparator4);

        menu_salvar_log.setText("Salvar Log");
        menu_salvar_log.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_salvar_logActionPerformed(evt);
            }
        });
        jMenu1.add(menu_salvar_log);

        menu_limpar_log.setText("Limpar Log");
        menu_limpar_log.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_limpar_logActionPerformed(evt);
            }
        });
        jMenu1.add(menu_limpar_log);
        jMenu1.add(jSeparator5);

        menu_graficos.setText("Abrir Gráficos");

        menu_grafico_externo_tq1.setText("Tanque 1");
        menu_grafico_externo_tq1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_grafico_externo_tq1ActionPerformed(evt);
            }
        });
        menu_graficos.add(menu_grafico_externo_tq1);

        menu_grafico_externo_tq2.setText("Tanque 2");
        menu_grafico_externo_tq2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_grafico_externo_tq2ActionPerformed(evt);
            }
        });
        menu_graficos.add(menu_grafico_externo_tq2);

        menu_grafico_externo_calc_tq1.setText("Calculado - Controlador 1");
        menu_grafico_externo_calc_tq1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_grafico_externo_calc_tq1ActionPerformed(evt);
            }
        });
        menu_graficos.add(menu_grafico_externo_calc_tq1);

        menu_grafico_externo_calc_tq2.setText("Calculado - Controlador 2");
        menu_grafico_externo_calc_tq2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_grafico_externo_calc_tq2ActionPerformed(evt);
            }
        });
        menu_graficos.add(menu_grafico_externo_calc_tq2);

        jMenu1.add(menu_graficos);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Sobre");

        menu_sobre_ipump.setText("iPump");
        menu_sobre_ipump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_sobre_ipumpActionPerformed(evt);
            }
        });
        jMenu2.add(menu_sobre_ipump);

        menu_sobre_grupo.setText("Grupo de Desenvolvimento");
        menu_sobre_grupo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_sobre_grupoActionPerformed(evt);
            }
        });
        jMenu2.add(menu_sobre_grupo);

        menu_sobre_protocolo.setText("Protocolo de Comunicação");
        menu_sobre_protocolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menu_sobre_protocoloActionPerformed(evt);
            }
        });
        jMenu2.add(menu_sobre_protocolo);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(35, 35, 35)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pbar_tq1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35)
                                .addComponent(pbar_tq2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbl_nivel_tq1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35)
                                .addComponent(lbl_nivel_tq2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(select_tanque, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_reset, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_windup, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(select_tipo_controle_mf, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_malha, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_enviar, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(panel_chart_tq2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                            .addComponent(panel_chart_tq1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panel_chart_tq3, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                            .addComponent(panel_chart_tq4, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                        .addGap(2, 2, 2)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(log_pane, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_limpar_log, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btn_salvar_log, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pbar_tq1, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                                    .addComponent(pbar_tq2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbl_nivel_tq1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lbl_nivel_tq2, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addGap(6, 6, 6)
                                .addComponent(select_tanque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_reset, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btn_malha, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(select_tipo_controle_mf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_windup, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_enviar, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(log_pane)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btn_limpar_log)
                                    .addComponent(btn_salvar_log)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panel_chart_tq3, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .addComponent(panel_chart_tq1, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panel_chart_tq2, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .addComponent(panel_chart_tq4, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_resetActionPerformed
        // Este botão reseta todas as configurações do Cliente e do Middleware

        // Resetando as variaveis do cliente
        btn_malha.setSelected(true);
        btn_malha.setText("Malha ABERTA");
        select_controle_ctrl1.setEnabled(false);
        select_controle_ctrl1.setSelectedIndex(0);
        select_controle_ctrl2.setEnabled(false);
        select_controle_ctrl2.setSelectedIndex(0);
        select_tipo_controle_mf.setEnabled(true);
        select_tipo_controle_mf.setSelectedIndex(0);
        select_sinal.setSelectedIndex(0);
        select_tanque.setSelectedIndex(0);
        input_kp_ctrl1.setText("");
        input_ki_ctrl1.setText("");
        input_kd_ctrl1.setText("");
        input_kp_ctrl2.setText("");
        input_ki_ctrl2.setText("");
        input_kd_ctrl2.setText("");
        input_amplitude_max.setText("");
        input_amplitude_min.setText("");
        input_periodo_max.setText("");
        input_periodo_min.setText("");
        input_offset.setText("");
        malha_aberta = true;
        amplitude_min = 0;
        amplitude_max = 0;
        periodo_min = 0;
        periodo_max = 0;
        offset = 0;
        Kp_ctrl1 = 0;
        Ki_ctrl1 = 0;
        Kd_ctrl1 = 0;
        Kp_ctrl2 = 0;
        Ki_ctrl2 = 0;
        Kd_ctrl2 = 0;
        Tr = 0;
        Mp = 0;
        Ts = 0;
        tanque = 0;

        // Resentando os gráficos
        clearChart();
        
        // Limpando o LOG
        log_area.setText("");

        // Enviando o comando para o middleware
        JsonObjectBuilder json = Json.createObjectBuilder();
        JsonObjectBuilder json_sinal = Json.createObjectBuilder();
        json_sinal.add("tipo", sinal)
                .add("amp_max", 0)
                .add("amp_min", 0)
                .add("periodo_max", 0)
                .add("periodo_min", 0)
                .add("offset", 0);

        json.add("comando", 1)
                .add("malha_aberta", true)
                .add("tanque", tanque)
                .add("Tr", 0)
                .add("Ts", 0)
                .add("Mp", 0);

        json.add("sinal", json_sinal);

        JsonObject json_final = json.build();

        StringWriter stWriter = new StringWriter();

        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(json_final);
        }

        String jsonData = stWriter.toString();

        //gerando log no prompt de log
        setLog("$ RESETADO \n" + jsonData + "\n-----------------\n");
        //---------

        String sendData = cliente.sendData(jsonData);
        System.out.println(sendData);
    }//GEN-LAST:event_btn_resetActionPerformed

    private void btn_enviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_enviarActionPerformed

        int index_sinal = select_sinal.getSelectedIndex();
        int index_ctrl = select_controle_ctrl1.getSelectedIndex();

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

                //gerando log no prompt de log
                setLog("$ ENVIADO \n" + jsonData + "\n-----------------\n");
                //---------

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
                    JsonObjectBuilder json_ctrl_1 = Json.createObjectBuilder();
                    JsonObjectBuilder json_ctrl_2 = Json.createObjectBuilder();
                    JsonObjectBuilder json_controlador = Json.createObjectBuilder();
                    json_sinal.add("tipo", sinal)
                            .add("amp_max", amplitude_max)
                            .add("amp_min", amplitude_min)
                            .add("periodo_max", periodo_max)
                            .add("periodo_min", periodo_min)
                            .add("offset", offset);

                    json.add("comando", 1)
                            .add("malha_aberta", false)
                            .add("tanque", tanque)
                            .add("Tr", Tr)
                            .add("Ts", Ts)
                            .add("Mp", Mp);

                    json.add("sinal", json_sinal);

                    //Controlador Tipo 1
                    json_ctrl_1.add("tipo", controle_ctrl1)
                            .add("Kp", Kp_ctrl1)
                            .add("Ki", Ki_ctrl1)
                            .add("Kd", Kd_ctrl1);

                    //Controlador Tipo 2
                    if (select_tipo_controle_mf.getSelectedIndex() == 1) {
                        json_ctrl_2.add("tipo", controle_ctrl2)
                                .add("Kp", Kp_ctrl2)
                                .add("Ki", Ki_ctrl2)
                                .add("Kd", Kd_ctrl2);

                        json_controlador.add("tipo", tipo_controlador)
                                .add("controle_1", json_ctrl_1)
                                .add("controle_2", json_ctrl_2);
                    } else {
                        json_controlador.add("tipo", tipo_controlador)
                                .add("controle_1", json_ctrl_1);
                    }

                    json.add("controlador", json_controlador);

                    JsonObject json_final = json.build();

                    StringWriter stWriter = new StringWriter();

                    try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                        jsonWriter.writeObject(json_final);
                    }

                    String jsonData = stWriter.toString();

                    //gerando log no prompt de log
                    setLog("$ ENVIADO \n" + jsonData + "\n-----------------\n");
                    //---------

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
            btn_malha.setText("Malha ABERTA");
            select_controle_ctrl1.setEnabled(true);
            select_tipo_controle_mf.setEnabled(true);
        } else {
            btn_malha.setText("Malha FECHADA");
            select_controle_ctrl1.setEnabled(false);
            select_controle_ctrl2.setEnabled(false);
            input_kp_ctrl1.setEnabled(false);
            input_ki_ctrl1.setEnabled(false);
            input_kd_ctrl1.setEnabled(false);
            select_tipo_controle_mf.setEnabled(false);
        }

        malha_aberta = !btn_malha.isSelected();
    }//GEN-LAST:event_btn_malhaActionPerformed

    private void btn_limpar_logActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_limpar_logActionPerformed
        log_area.setText("");
    }//GEN-LAST:event_btn_limpar_logActionPerformed

    private void btn_salvar_logActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_salvar_logActionPerformed

        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Arquivos de texto", "txt");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Salvar Arquivo de Log");

        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            System.out.println("Salvar Arquivo Como: " + fileToSave.getAbsolutePath());

            try {

                String content = log_area.getText();

//                File file = new File("/users/mkyong/filename.txt");
                // if file doesnt exists, then create it
                if (!fileToSave.exists()) {
                    fileToSave.createNewFile();
                }

                FileWriter fw = new FileWriter(fileToSave.getAbsoluteFile() + ".txt");
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();

                // gerando log no prompt de log
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                String datetime = dateFormat.format(date);
                String log = datetime + "\n\n\n ------ > LOG SALVO : " + fileToSave.getAbsoluteFile() + ".txt < ------ \n\n\n";

                setLog(log);

//                System.out.println("Salvo");
            } catch (IOException e) {
            }

        }


    }//GEN-LAST:event_btn_salvar_logActionPerformed

    private void menu_grafico_externo_tq1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_grafico_externo_tq1ActionPerformed
        ExternalChart ext_chart = new ExternalChart();
        ext_chart.setChart(mv_tq_01, sp_tq_01, pv_tq_01);
        ext_chart.setBorderTitle("Tanque 01");
        ext_chart.setTitle("Gráfico - Tanque 01");
        ext_chart.setVisible(true);
    }//GEN-LAST:event_menu_grafico_externo_tq1ActionPerformed

    private void menu_grafico_externo_tq2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_grafico_externo_tq2ActionPerformed
        ExternalChart ext_chart = new ExternalChart();
        ext_chart.setChart(mv_tq_02, sp_tq_02, pv_tq_02);
        ext_chart.setBorderTitle("Tanque 02");
        ext_chart.setTitle("Gráfico - Tanque 02");
        ext_chart.setVisible(true);
    }//GEN-LAST:event_menu_grafico_externo_tq2ActionPerformed

    private void menu_sobre_ipumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_sobre_ipumpActionPerformed
        SobreIPump about = new SobreIPump();
        about.setVisible(true);
    }//GEN-LAST:event_menu_sobre_ipumpActionPerformed

    private void menu_sobre_grupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_sobre_grupoActionPerformed
        SobreEquipe equipe = new SobreEquipe();
        equipe.setVisible(true);
    }//GEN-LAST:event_menu_sobre_grupoActionPerformed

    private void menu_sobre_protocoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_sobre_protocoloActionPerformed
        SobreProtocolo protocolo = new SobreProtocolo();
        protocolo.setVisible(true);
    }//GEN-LAST:event_menu_sobre_protocoloActionPerformed

    private void menu_salvar_logActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_salvar_logActionPerformed
        btn_salvar_logActionPerformed(evt);
    }//GEN-LAST:event_menu_salvar_logActionPerformed

    private void menu_limpar_logActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_limpar_logActionPerformed
        btn_limpar_logActionPerformed(evt);
    }//GEN-LAST:event_menu_limpar_logActionPerformed

    private void menu_desconectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_desconectarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menu_desconectarActionPerformed

    private void menu_conectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_conectarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menu_conectarActionPerformed

    private void menu_grafico_externo_calc_tq1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_grafico_externo_calc_tq1ActionPerformed
        ExternalChart ext_chart = new ExternalChart();
        ext_chart.setChart(p_tq_01, i_tq_01, d_tq_01);
        ext_chart.setBorderTitle("Calculado - Controlador 01");
        ext_chart.setTitle("Gráfico - Calculado - Controlador 01");
        ext_chart.setVisible(true);
    }//GEN-LAST:event_menu_grafico_externo_calc_tq1ActionPerformed

    private void menu_grafico_externo_calc_tq2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menu_grafico_externo_calc_tq2ActionPerformed
        ExternalChart ext_chart = new ExternalChart();
        ext_chart.setChart(p_tq_02, i_tq_02, d_tq_02);
        ext_chart.setBorderTitle("Calculado - Controlador 02");
        ext_chart.setTitle("Gráfico - Calculado - Controlador 02");
        ext_chart.setVisible(true);
    }//GEN-LAST:event_menu_grafico_externo_calc_tq2ActionPerformed

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

        //Kp's
        if (input_kp_ctrl1.isEnabled()) {
            if (!input_kp_ctrl1.getText().isEmpty()) {
                Kp_ctrl1 = Double.parseDouble(input_kp_ctrl1.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Kp no Controlador 1!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Kp_ctrl1 = 0;
        }

        if (input_kp_ctrl2.isEnabled()) {
            if (!input_kp_ctrl2.getText().isEmpty()) {
                Kp_ctrl2 = Double.parseDouble(input_kp_ctrl2.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Kp no Controlador 2!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Kp_ctrl2 = 0;
        }

        //Ki's
        if (input_ki_ctrl1.isEnabled()) {
            if (!input_ki_ctrl1.getText().isEmpty()) {
                Ki_ctrl1 = Double.parseDouble(input_ki_ctrl1.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Ki no Controlador 1!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Ki_ctrl1 = 0;
        }

        if (input_ki_ctrl2.isEnabled()) {
            if (!input_ki_ctrl2.getText().isEmpty()) {
                Ki_ctrl2 = Double.parseDouble(input_ki_ctrl2.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Ki no Controlador 2!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Ki_ctrl2 = 0;
        }

        //Kd's
        if (input_kd_ctrl1.isEnabled()) {
            if (!input_kd_ctrl1.getText().isEmpty()) {
                Kd_ctrl1 = Double.parseDouble(input_kd_ctrl1.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Kd no Controlador 1!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Kd_ctrl1 = 0;
        }

        if (input_kd_ctrl2.isEnabled()) {
            if (!input_kd_ctrl2.getText().isEmpty()) {
                Kd_ctrl2 = Double.parseDouble(input_kd_ctrl2.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Você deve informar um valor de Kd no Controlador 2!",
                        "Cuidado!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Kd_ctrl2 = 0;
        }

    }

    // SET DATAS
    public void setDataTQ1(float sp, float mv, float pv, float p, float i, float d) {
        pbar_tq1.setValue((int) pv);
        lbl_nivel_tq1.setText("<html><b>" + String.format("%.2f", pv) + "</b> cm</html>");

        // TANQUE
        mv_tq_01.addOrUpdate(new Millisecond(), mv);
        sp_tq_01.addOrUpdate(new Millisecond(), sp);
        pv_tq_01.addOrUpdate(new Millisecond(), pv);

        // VARIAVEIS CALCULADAS
        p_tq_01.addOrUpdate(new Millisecond(), p);
        i_tq_01.addOrUpdate(new Millisecond(), i);
        d_tq_01.addOrUpdate(new Millisecond(), d);

    }

    public void setDataTQ2(float sp, float mv, float pv, float p, float i, float d) {
        pbar_tq2.setValue((int) pv);
        lbl_nivel_tq2.setText("<html><b>" + String.format("%.2f", pv) + "</b> cm</html>");

        // TANQUE
        mv_tq_02.addOrUpdate(new Millisecond(), mv);
        sp_tq_02.addOrUpdate(new Millisecond(), sp);
        pv_tq_02.addOrUpdate(new Millisecond(), pv);

        // VARIAVEIS CALCULADAS
        p_tq_02.addOrUpdate(new Millisecond(), p);
        i_tq_02.addOrUpdate(new Millisecond(), i);
        d_tq_02.addOrUpdate(new Millisecond(), d);

    }

    public void setDataCalc(float tr, float tp, float ts, float mp, float ess) {
        lbl_tr.setText("<html>Tr : <b style=\"color:red\">" + String.format("%.2f", tr) + "</b></html>");
        lbl_tp.setText("<html>Tp : <b style=\"color:red\">" + String.format("%.2f", tp) + "</b></html>");
        lbl_ts.setText("<html>Ts : <b style=\"color:red\">" + String.format("%.2f", ts) + "</b></html>");
        lbl_mp.setText("<html>Mp : <b style=\"color:red\">" + String.format("%.2f", mp) + "</b></html>");
        lbl_ess.setText("<html>Ess : <b style=\"color:red\">" + String.format("%.2f", ess) + "</b></html>");
    }

    // GRÁFICOS
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

    private void createChartCalculadosTQ1() {
        final XYDataset dataset = createDatasetCalculadosTQ1();
        final JFreeChart chart = createChartTQ(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(95, 25));
        chartPanel.setMouseZoomable(true, false);

        panel_chart_tq3.setLayout(new java.awt.BorderLayout());
        panel_chart_tq3.add(chartPanel, BorderLayout.CENTER);
        panel_chart_tq3.validate();
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

    private void createChartCalculadosTQ2() {
        final XYDataset dataset = createDatasetCalculadosTQ2();
        final JFreeChart chart = createChartTQ(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(95, 25));
        chartPanel.setMouseZoomable(true, false);
        panel_chart_tq4.setLayout(new java.awt.BorderLayout());
        panel_chart_tq4.add(chartPanel, BorderLayout.CENTER);
        panel_chart_tq4.validate();
    }

    // SERIES
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

    private XYDataset createDatasetCalculadosTQ1() {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        p_tq_01.add(new Millisecond(), 0.0);
        d_tq_01.add(new Millisecond(), 0.0);
        i_tq_01.add(new Millisecond(), 0.0);

        dataset.addSeries(p_tq_01);
        dataset.addSeries(i_tq_01);
        dataset.addSeries(d_tq_01);

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

    private XYDataset createDatasetCalculadosTQ2() {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        p_tq_02.add(new Millisecond(), 0.0);
        d_tq_02.add(new Millisecond(), 0.0);
        i_tq_02.add(new Millisecond(), 0.0);

        dataset.addSeries(p_tq_02);
        dataset.addSeries(i_tq_02);
        dataset.addSeries(d_tq_02);

        return dataset;
    }

    //GRÁFICO GENERICO
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

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(false);

        XYItemRenderer r = plot.getRenderer();

        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;

            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, Color.BLUE);
            renderer.setSeriesPaint(2, Color.black);

            renderer.setSeriesVisibleInLegend(0, true, true);
            renderer.setSeriesVisibleInLegend(1, true, true);
            renderer.setSeriesVisibleInLegend(2, true, true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("k:mm:s"));

        return chart;

    }

    private void clearChart() {

        this.mv_tq_01.clear();
        this.mv_tq_02.clear();
        this.sp_tq_01.clear();
        this.sp_tq_02.clear();
        this.pv_tq_01.clear();
        this.pv_tq_02.clear();
        this.p_tq_01.clear();
        this.i_tq_01.clear();
        this.d_tq_01.clear();
        this.p_tq_02.clear();
        this.i_tq_02.clear();
        this.d_tq_02.clear();

    }

    public void setConnVars(String ip, int port) {
        this.Ip = ip;
        this.Port = port;

        cliente = new ClienteSocket(Ip, Port);

    }

    public void setLog(String text) {
        log_area.append(text);

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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(iPump.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new iPump().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_enviar;
    private javax.swing.JButton btn_limpar_log;
    private javax.swing.JToggleButton btn_malha;
    private javax.swing.JButton btn_reset;
    private javax.swing.JButton btn_salvar_log;
    private javax.swing.JToggleButton btn_windup;
    private javax.swing.JTextField input_amplitude_max;
    private javax.swing.JTextField input_amplitude_min;
    private javax.swing.JTextField input_kd_ctrl1;
    private javax.swing.JTextField input_kd_ctrl2;
    private javax.swing.JTextField input_ki_ctrl1;
    private javax.swing.JTextField input_ki_ctrl2;
    private javax.swing.JTextField input_kp_ctrl1;
    private javax.swing.JTextField input_kp_ctrl2;
    private javax.swing.JTextField input_offset;
    private javax.swing.JTextField input_periodo_max;
    private javax.swing.JTextField input_periodo_min;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JLabel lbl_ess;
    private javax.swing.JLabel lbl_mp;
    private javax.swing.JLabel lbl_nivel_tq1;
    private javax.swing.JLabel lbl_nivel_tq2;
    private javax.swing.JLabel lbl_tp;
    private javax.swing.JLabel lbl_tr;
    private javax.swing.JLabel lbl_ts;
    private javax.swing.JTextArea log_area;
    private javax.swing.JScrollPane log_pane;
    private javax.swing.JMenuItem menu_conectar;
    private javax.swing.JMenuItem menu_desconectar;
    private javax.swing.JMenuItem menu_grafico_externo_calc_tq1;
    private javax.swing.JMenuItem menu_grafico_externo_calc_tq2;
    private javax.swing.JMenuItem menu_grafico_externo_tq1;
    private javax.swing.JMenuItem menu_grafico_externo_tq2;
    private javax.swing.JMenu menu_graficos;
    private javax.swing.JMenuItem menu_limpar_log;
    private javax.swing.JMenuItem menu_salvar_log;
    private javax.swing.JMenuItem menu_sobre_grupo;
    private javax.swing.JMenuItem menu_sobre_ipump;
    private javax.swing.JMenuItem menu_sobre_protocolo;
    private javax.swing.JPanel panel_chart_tq1;
    private javax.swing.JPanel panel_chart_tq2;
    private javax.swing.JPanel panel_chart_tq3;
    private javax.swing.JPanel panel_chart_tq4;
    private javax.swing.JProgressBar pbar_tq1;
    private javax.swing.JProgressBar pbar_tq2;
    private javax.swing.JComboBox select_controle_ctrl1;
    private javax.swing.JComboBox select_controle_ctrl2;
    private javax.swing.JComboBox select_mp;
    private javax.swing.JComboBox select_sinal;
    private javax.swing.JComboBox select_tanque;
    private javax.swing.JComboBox select_tipo_controle_mf;
    private javax.swing.JComboBox select_tr;
    private javax.swing.JComboBox select_ts;
    // End of variables declaration//GEN-END:variables

}
