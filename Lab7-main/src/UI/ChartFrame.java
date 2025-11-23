package UI;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChartFrame extends JFrame {
    
    public ChartFrame(String title, Map<String, Double> data, String chartType) {
        setTitle(title);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        ChartPanel chartPanel = new ChartPanel(data, chartType);
        add(chartPanel);
    }
}

class ChartPanel extends JPanel {
    private Map<String, Double> data;
    private String chartType;
    
    public ChartPanel(Map<String, Double> data, String chartType) {
        this.data = data;
        this.chartType = chartType;
        setBackground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (data == null || data.isEmpty()) {
            g.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;
        
        // Draw axes
        g.setColor(Color.BLACK);
        g.drawLine(padding, height - padding, padding, padding); // Y-axis
        g.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
        
        // Find maximum value for scaling
        double maxValue = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(100);
        
        String[] labels = data.keySet().toArray(new String[0]);
        double[] values = data.values().stream().mapToDouble(Double::doubleValue).toArray();
        
        if ("bar".equals(chartType)) {
            drawBarChart(g, labels, values, padding, chartWidth, chartHeight, maxValue);
        } else if ("line".equals(chartType)) {
            drawLineChart(g, labels, values, padding, chartWidth, chartHeight, maxValue);
        }
    }
    
    private void drawBarChart(Graphics g, String[] labels, double[] values, 
                            int padding, int chartWidth, int chartHeight, double maxValue) {
        int barWidth = chartWidth / labels.length;
        
        for (int i = 0; i < labels.length; i++) {
            int barHeight = (int) ((values[i] / maxValue) * chartHeight);
            int x = padding + i * barWidth;
            int y = getHeight() - padding - barHeight;
            
            // Draw bar
            g.setColor(new Color(70, 130, 180));
            g.fillRect(x + 5, y, barWidth - 10, barHeight);
            
            // Draw value on top of bar
            g.setColor(Color.BLACK);
            String valueText = String.format("%.1f", values[i]);
            g.drawString(valueText, x + barWidth/2 - 10, y - 5);
            
            // Draw label
            g.drawString(labels[i], x + barWidth/2 - 15, getHeight() - padding + 20);
        }
    }
    
    private void drawLineChart(Graphics g, String[] labels, double[] values,
                             int padding, int chartWidth, int chartHeight, double maxValue) {
        int pointWidth = chartWidth / (labels.length - 1);
        
        g.setColor(Color.RED);
        for (int i = 0; i < labels.length - 1; i++) {
            int x1 = padding + i * pointWidth;
            int y1 = getHeight() - padding - (int)((values[i] / maxValue) * chartHeight);
            
            int x2 = padding + (i + 1) * pointWidth;
            int y2 = getHeight() - padding - (int)((values[i + 1] / maxValue) * chartHeight);
            
            g.drawLine(x1, y1, x2, y2);
            g.fillOval(x1 - 3, y1 - 3, 6, 6);
        }
        
        // Draw last point
        int lastX = padding + (labels.length - 1) * pointWidth;
        int lastY = getHeight() - padding - (int)((values[values.length - 1] / maxValue) * chartHeight);
        g.fillOval(lastX - 3, lastY - 3, 6, 6);
        
        // Draw labels and values
        g.setColor(Color.BLACK);
        for (int i = 0; i < labels.length; i++) {
            int x = padding + i * pointWidth;
            g.drawString(labels[i], x - 10, getHeight() - padding + 20);
        }
    }
}
