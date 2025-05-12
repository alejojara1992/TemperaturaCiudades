import entidades.RegistroTemperatura;
import servicios.ServicioTemperatura;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import datechooser.beans.DateChooserCombo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class FrmTemperaturas extends JFrame {

    private DateChooserCombo fechaDesde, fechaHasta, fechaExtremos;
    private JPanel pnlGrafica, pnlEstadisticas;
    private JTextArea txtResultadoCiudadFecha;
    private List<RegistroTemperatura> datos;
    private JPanel panelGraficaTop;

    public FrmTemperaturas() {
        setTitle("Temperaturas por Ciudad");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        fechaDesde = new DateChooserCombo();
        fechaHasta = new DateChooserCombo();
        fechaExtremos = new DateChooserCombo();

        pnlGrafica = new JPanel(new BorderLayout());
        panelGraficaTop = new JPanel(new FlowLayout());
        panelGraficaTop.add(new JLabel("Desde:"));
        panelGraficaTop.add(fechaDesde);
        panelGraficaTop.add(new JLabel("Hasta:"));
        panelGraficaTop.add(fechaHasta);
        JButton btnGraficar = new JButton("Graficar promedio");
        panelGraficaTop.add(btnGraficar);
        pnlGrafica.add(panelGraficaTop, BorderLayout.NORTH);

        pnlEstadisticas = new JPanel(new BorderLayout());
        JPanel panelEstadisticasTop = new JPanel(new FlowLayout());
        panelEstadisticasTop.add(new JLabel("Fecha:"));
        panelEstadisticasTop.add(fechaExtremos);
        JButton btnExtremos = new JButton("Mostrar extremos de temperatura en la fecha");
        panelEstadisticasTop.add(btnExtremos);
        pnlEstadisticas.add(panelEstadisticasTop, BorderLayout.NORTH);

        txtResultadoCiudadFecha = new JTextArea();
        txtResultadoCiudadFecha.setEditable(false);
        txtResultadoCiudadFecha.setBackground(null);
        txtResultadoCiudadFecha.setFont(new Font("Arial", Font.PLAIN, 14));
        txtResultadoCiudadFecha.setWrapStyleWord(true);
        txtResultadoCiudadFecha.setLineWrap(true);
        txtResultadoCiudadFecha.setOpaque(false);
        txtResultadoCiudadFecha.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtResultadoCiudadFecha.setAlignmentY(Component.CENTER_ALIGNMENT);
        txtResultadoCiudadFecha.setPreferredSize(new Dimension(500, 100));


        JPanel pnlResultadoCentro = new JPanel(new GridBagLayout());
        pnlResultadoCentro.add(txtResultadoCiudadFecha);
        pnlEstadisticas.add(pnlResultadoCentro, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Gráfica", pnlGrafica);
        tabs.addTab("Estadísticas", new JScrollPane(pnlEstadisticas));
        add(tabs, BorderLayout.CENTER);

        datos = ServicioTemperatura.getDatos("src/datos/Temperaturas.csv");

        btnGraficar.addActionListener((ActionEvent e) -> mostrarGrafica());
        btnExtremos.addActionListener((ActionEvent e) -> mostrarTemperaturaCiudadFecha());
    }

    private void mostrarGrafica() {
        LocalDate desde = fechaDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = fechaHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Map<String, Double> promedio = ServicioTemperatura.promedioPorCiudad(datos, desde, hasta);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        promedio.forEach((ciudad, temp) -> dataset.addValue(temp, "Temperatura", ciudad));

        JFreeChart chart = ChartFactory.createBarChart(
                "Temperatura Promedio por Ciudad",
                "Ciudad",
                "Temperatura (°C)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        pnlGrafica.removeAll();
        pnlGrafica.add(panelGraficaTop, BorderLayout.NORTH);
        pnlGrafica.add(new ChartPanel(chart), BorderLayout.CENTER);
        pnlGrafica.revalidate();
    }

    private void mostrarTemperaturaCiudadFecha() {
        LocalDate fecha = fechaExtremos.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Map<String, Double> tempFecha = ServicioTemperatura.temperaturasPorFecha(datos, fecha);

        if (tempFecha.isEmpty()) {
            txtResultadoCiudadFecha.setText("No hay datos disponibles para la fecha: " + fecha);
            return;
        }

        String ciudadMax = ServicioTemperatura.ciudadMasCalurosa(tempFecha);
        String ciudadMin = ServicioTemperatura.ciudadMenosCalurosa(tempFecha);
        double tempMax = tempFecha.get(ciudadMax);
        double tempMin = tempFecha.get(ciudadMin);

        StringBuilder sb = new StringBuilder();
        sb.append("Fecha: ").append(fecha).append("\n");
        sb.append("Ciudad más calurosa: ").append(ciudadMax)
                .append(" (").append(String.format("%.2f", tempMax)).append(" °C)\n");
        sb.append("Ciudad menos calurosa: ").append(ciudadMin)
                .append(" (").append(String.format("%.2f", tempMin)).append(" °C)");

        String mensaje = sb.toString();
        txtResultadoCiudadFecha.setText(mensaje);
        pnlEstadisticas.revalidate();
        pnlEstadisticas.repaint();
        
    }
}
