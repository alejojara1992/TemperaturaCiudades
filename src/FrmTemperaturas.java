
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

    private DateChooserCombo dccDesde, dccHasta, dccEspecifica;
    private JComboBox<String> cmbCiudadEstadistica;
    private DateChooserCombo dccCiudadFecha;
    private JButton btnBuscarCiudadFecha;
    private JPanel pnlGrafica, pnlEstadisticas;
    private JLabel lblResultadoCiudadFecha;
    private List<RegistroTemperatura> datos;

    public FrmTemperaturas() {
        setTitle("Temperaturas por Ciudad");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new FlowLayout());
        dccDesde = new DateChooserCombo();
        dccHasta = new DateChooserCombo();
        dccEspecifica = new DateChooserCombo();
        JButton btnGraficar = new JButton("Graficar promedio");
        JButton btnEstadisticas = new JButton("Mostrar extremos");

        top.add(new JLabel("Desde:"));
        top.add(dccDesde);
        top.add(new JLabel("Hasta:"));
        top.add(dccHasta);
        top.add(btnGraficar);

        top.add(new JLabel("Fecha específica:"));
        top.add(dccEspecifica);
        top.add(btnEstadisticas);

        pnlGrafica = new JPanel(new BorderLayout());
        pnlEstadisticas = new JPanel();
        pnlEstadisticas.setLayout(new BoxLayout(pnlEstadisticas, BoxLayout.Y_AXIS));

        cmbCiudadEstadistica = new JComboBox<>();
        btnBuscarCiudadFecha = new JButton("Ver promedio de ciudad en periodo");
        lblResultadoCiudadFecha = new JLabel("", SwingConstants.CENTER);
        lblResultadoCiudadFecha.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelCiudadFecha = new JPanel();
        panelCiudadFecha.add(new JLabel("Ciudad:"));
        panelCiudadFecha.add(cmbCiudadEstadistica);
        panelCiudadFecha.add(btnBuscarCiudadFecha);

        pnlEstadisticas.add(panelCiudadFecha);
        JPanel pnlResultadoCentro = new JPanel(new BorderLayout());
        pnlResultadoCentro.add(lblResultadoCiudadFecha, BorderLayout.CENTER);
        pnlEstadisticas.add(pnlResultadoCentro);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Gráfica", pnlGrafica);
        tabs.addTab("Estadísticas", new JScrollPane(pnlEstadisticas));

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        datos = ServicioTemperatura.getDatos(System.getProperty("user.dir") + "/src/datos/Temperaturas.csv");
        ServicioTemperatura.getCiudades(datos).forEach(cmbCiudadEstadistica::addItem);

        btnGraficar.addActionListener((ActionEvent e) -> mostrarGrafica());
        btnEstadisticas.addActionListener((ActionEvent e) -> mostrarExtremos());
        btnBuscarCiudadFecha.addActionListener((ActionEvent e) -> mostrarTemperaturaCiudadFecha());
    }

    private void mostrarGrafica() {
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

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
        pnlGrafica.add(new ChartPanel(chart), BorderLayout.CENTER);
        pnlGrafica.revalidate();
    }

    private void mostrarExtremos() {
        LocalDate fecha = dccEspecifica.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Map<String, Double> tempFecha = ServicioTemperatura.temperaturasPorFecha(datos, fecha);

        String ciudadMax = ServicioTemperatura.ciudadMasCalurosa(tempFecha);
        String ciudadMin = ServicioTemperatura.ciudadMenosCalurosa(tempFecha);

        pnlEstadisticas.add(new JLabel("Ciudad más calurosa: " + ciudadMax));
        pnlEstadisticas.add(new JLabel("Ciudad menos calurosa: " + ciudadMin));
        pnlEstadisticas.revalidate();
        pnlEstadisticas.repaint();
    }

    private void mostrarTemperaturaCiudadFecha() {
        String ciudad = (String) cmbCiudadEstadistica.getSelectedItem();
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        double promedio = datos.stream()
                .filter(r -> r.getCiudad().equals(ciudad))
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .mapToDouble(RegistroTemperatura::getTemperatura)
                .average()
                .orElse(Double.NaN);

        String mensaje = Double.isNaN(promedio)
                ? "No hay datos para " + ciudad + " entre " + desde + " y " + hasta
                : "Promedio de " + ciudad + " entre " + desde + " y " + hasta + ": " + String.format("%.2f", promedio) + " °C";

        lblResultadoCiudadFecha.setText(mensaje);
    }
}
