package servicios;

import entidades.RegistroTemperatura;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ServicioTemperatura {

    public static List<RegistroTemperatura> getDatos(String archivo) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            return Files.lines(Paths.get(archivo))
                    .skip(1)
                    .map(l -> l.split(","))
                    .map(p -> new RegistroTemperatura(p[0], LocalDate.parse(p[1], formato), Double.parseDouble(p[2])))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<String> getCiudades(List<RegistroTemperatura> datos) {
        return datos.stream()
                .map(RegistroTemperatura::getCiudad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static Map<String, Double> promedioPorCiudad(List<RegistroTemperatura> datos, LocalDate desde, LocalDate hasta) {
        return datos.stream()
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .collect(Collectors.groupingBy(RegistroTemperatura::getCiudad,
                        Collectors.averagingDouble(RegistroTemperatura::getTemperatura)));
    }

    public static Map<String, Double> temperaturasPorFecha(List<RegistroTemperatura> datos, LocalDate fecha) {
        return datos.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .collect(Collectors.toMap(
                        RegistroTemperatura::getCiudad,
                        RegistroTemperatura::getTemperatura));
    }

    public static String ciudadMasCalurosa(Map<String, Double> datos) {
        return datos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");
    }

    public static String ciudadMenosCalurosa(Map<String, Double> datos) {
        return datos.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");
    }

    public static OptionalDouble temperaturaDeCiudadEnFecha(List<RegistroTemperatura> datos, String ciudad, LocalDate fecha) {
        return datos.stream()
                .filter(r -> r.getCiudad().equals(ciudad) && r.getFecha().equals(fecha))
                .mapToDouble(RegistroTemperatura::getTemperatura)
                .findFirst();
    }
}