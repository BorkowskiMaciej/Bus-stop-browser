package model;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;

public class Model {

    private ArrayList<BusStop> busStops;
    private LinkedHashSet<BusStop> chosenBusStops;
    private LinkedHashSet<BusStop> currentChosenBusStops;

    public Model() {
        this.busStops = DataLoader.generateBusStopsList();
        this.chosenBusStops = new LinkedHashSet<>();
        this.currentChosenBusStops = new LinkedHashSet<>();
    }

    public void setCurrentChosenBusStops(String name, String lineNumber, String zone) {

        currentChosenBusStops.clear();
        busStops.stream().filter(busStop -> {
            if (!name.equals("Wszystkie")) {
                return busStop.getName().equalsIgnoreCase(name);
            }
            return true;
        }).filter(busStop -> {
            if (!lineNumber.equals("Wszystkie")) {
                return Arrays.stream(busStop.getBusLines()).anyMatch(n -> String.valueOf(n).equals(lineNumber));
            }
            return true;
        }).filter(busStop -> {
            if (!zone.equals("Wszystkie")) {
                return busStop.getZone().equals(zone);
            }
            return true;
        }).sorted(Comparator.comparing(BusStop::getName)).forEach(busStop -> currentChosenBusStops.add(busStop));
    }

    public void downloadBusStops(boolean isTxt) {

        JFileChooser fileChooser = new JFileChooser();
        int ans = fileChooser.showSaveDialog(null);

        if (ans == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile == null) {
                return;
            }
            if (isTxt & !selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".txt");
            }
            if (!isTxt & !selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".csv");
            }
            try (PrintWriter printWriter = new PrintWriter(selectedFile, StandardCharsets.UTF_8)) {
                if (isTxt) {
                    for (BusStop busStop: chosenBusStops) {
                        printWriter.println(busStop.getDescription());
                    }
                } else {
                    printWriter.println("Nazwa;Id;Strefa;Numery linii");
                    for (BusStop busStop: chosenBusStops) {
                        printWriter.println(busStop.getName() + ";" + busStop.getId() + ";" + busStop.getZone() + ";" +
                                busStop.getHeadsigns());
                    }
                }
                JOptionPane.showMessageDialog(null,
                        "Plik " + selectedFile.getName() + " zapisano do " + selectedFile.getParent() + ".", "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Błąd zapisu.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public DefaultTableModel setTableModel(Collection<BusStop> busStops) {

        DefaultTableModel defaultTableModel = new DefaultTableModel();
        defaultTableModel.addColumn("Nazwa");
        defaultTableModel.addColumn("Id");
        defaultTableModel.addColumn("Strefa");
        defaultTableModel.addColumn("Numery linii");

        busStops.forEach(busStop -> {

            Object[] data = {busStop.getName(), busStop.getId(), busStop.getZone(), busStop.getHeadsigns()};
            defaultTableModel.addRow(data);
        });

        return defaultTableModel;
    }

    public LinkedHashSet<BusStop> getChosenBusStops() {
        return chosenBusStops;
    }

    public LinkedHashSet<BusStop> getCurrentChosenBusStops() {
        return currentChosenBusStops;
    }

    public Stream<String> getNamesOfBusStops() {
        return busStops.stream().map(BusStop::getName).distinct().sorted(String::compareTo);
    }

    public Stream<String> getNumbersOfLines() {
        return busStops.stream().flatMapToInt(busStop -> Arrays.stream(busStop.getBusLines())).distinct().sorted()
                .mapToObj(String::valueOf);
    }

    public Stream<String> getZones() {
        return busStops.stream().map(BusStop::getZone).distinct().sorted(String::compareTo);
    }

    public String findNearestBusStop(Double[] coords) {
        BusStop busStop = busStops.stream().min(Comparator.comparingDouble(s -> sqrt(
                        Math.pow(s.getCoordinates()[0] - coords[1], 2) + Math.pow(s.getCoordinates()[1] - coords[0], 2))))
                .orElseThrow();
        chosenBusStops.add(busStop);
        return busStop.toString();
    }

}
