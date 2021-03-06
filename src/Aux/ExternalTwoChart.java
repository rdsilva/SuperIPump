/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Aux;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Rodrigo
 */
public class ExternalTwoChart extends javax.swing.JFrame {

    public ExternalTwoChart() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel_chart_top = new javax.swing.JPanel();
        btn_ok = new javax.swing.JButton();
        panel_chart_bottom = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gráfico - Tanque XX");
        setAlwaysOnTop(true);

        panel_chart_top.setBorder(javax.swing.BorderFactory.createTitledBorder("Tanque XX"));

        javax.swing.GroupLayout panel_chart_topLayout = new javax.swing.GroupLayout(panel_chart_top);
        panel_chart_top.setLayout(panel_chart_topLayout);
        panel_chart_topLayout.setHorizontalGroup(
            panel_chart_topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_chart_topLayout.setVerticalGroup(
            panel_chart_topLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 217, Short.MAX_VALUE)
        );

        btn_ok.setText("OK");
        btn_ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_okActionPerformed(evt);
            }
        });

        panel_chart_bottom.setBorder(javax.swing.BorderFactory.createTitledBorder("Tanque XX"));
        panel_chart_bottom.setPreferredSize(new java.awt.Dimension(12, 245));

        javax.swing.GroupLayout panel_chart_bottomLayout = new javax.swing.GroupLayout(panel_chart_bottom);
        panel_chart_bottom.setLayout(panel_chart_bottomLayout);
        panel_chart_bottomLayout.setHorizontalGroup(
            panel_chart_bottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel_chart_bottomLayout.setVerticalGroup(
            panel_chart_bottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 222, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel_chart_top, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 801, Short.MAX_VALUE)
                        .addComponent(btn_ok))
                    .addComponent(panel_chart_bottom, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel_chart_top, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panel_chart_bottom, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(btn_ok)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_okActionPerformed
        dispose();
    }//GEN-LAST:event_btn_okActionPerformed

    private JFreeChart createChart(final XYDataset dataset) {

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

    private XYDataset setDataset(TimeSeries ts_1, TimeSeries ts_2, TimeSeries ts_3) {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        dataset.addSeries(ts_1);
        dataset.addSeries(ts_2);
        dataset.addSeries(ts_3);

        return dataset;
    }

    public void setChart_top(TimeSeries ts_1, TimeSeries ts_2, TimeSeries ts_3, String title) {
        final XYDataset dataset = setDataset(ts_1, ts_2, ts_3);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(95, 25));
        chartPanel.setMouseZoomable(true, false);
        panel_chart_top.setLayout(new java.awt.BorderLayout());
        panel_chart_top.add(chartPanel, BorderLayout.CENTER);
        panel_chart_top.setBorder(new TitledBorder(title));
        panel_chart_top.validate();
    }

    public void setChart_bottom(TimeSeries ts_1, TimeSeries ts_2, TimeSeries ts_3, String title) {
        final XYDataset dataset = setDataset(ts_1, ts_2, ts_3);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(95, 25));
        chartPanel.setMouseZoomable(true, false);
        panel_chart_bottom.setLayout(new java.awt.BorderLayout());
        panel_chart_bottom.add(chartPanel, BorderLayout.CENTER);
        panel_chart_bottom.setBorder(new TitledBorder(title));
        panel_chart_bottom.validate();
    }

    public void setBorderTitle(String title) {
        panel_chart_top.setBorder(new TitledBorder(title));
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
            java.util.logging.Logger.getLogger(ExternalTwoChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExternalTwoChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExternalTwoChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExternalTwoChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ExternalTwoChart().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_ok;
    private javax.swing.JPanel panel_chart_bottom;
    private javax.swing.JPanel panel_chart_top;
    // End of variables declaration//GEN-END:variables
}
