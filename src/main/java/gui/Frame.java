package gui;

import busstop.BusStop;
import googlemap.GoogleMap;
import loader.DataLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Frame extends JFrame {

    private JButton addButton;
    private JComboBox<String> nameComboBox;
    private JPanel mainPanel;
    private JPanel selectionPanel;
    private JPanel searchPanel;
    private JPanel resultPanel;
    private JScrollPane tablePanel;
    private JTable table;
    private JButton resetButton;
    private JComboBox<String> numberOfLineComboBox;
    private JComboBox<String> zoneComboBox;
    private JTable table2;
    private JButton txtDownloadButton;
    private JButton clearButton;
    private JButton csvDownloadButton;
    private JButton searchButton;
    private JButton addSelectedButton;
    private ArrayList<BusStop> busStops = DataLoader.generateBusStopsList();
    private LinkedHashSet<BusStop> chosenBusStops = new LinkedHashSet<>();
    private LinkedHashSet<BusStop> currentChosenBusStops = new LinkedHashSet<>();
    private GoogleMap googleMap;
    private JPanel mapPanel;
    private JTextArea busStopInformation;

    public Frame() {

        this.googleMap = new GoogleMap(this, mapPanel, busStops, busStopInformation, chosenBusStops);

        setContentPane(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Wyszukiwarka przystanków ZTM w Poznaniu");

        setSearchComboBoxes();
        setButtons();
        setDefaultSearch();
        setTable2();

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void setTable(String name, String lineNumber, String zone) {

        DefaultTableModel defaultTableModel = setTableModel();

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
        }).sorted(Comparator.comparing(BusStop::getName)).forEach(busStop -> {

            Object[] data = {busStop.getName(), busStop.getId(), busStop.getZone(), busStop.getHeadsigns()};
            defaultTableModel.addRow(data);
            currentChosenBusStops.add(busStop);
        });

        if (currentChosenBusStops.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Brak przystanków spełniających kryteria.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);

            resetButton.doClick();
        } else {
            table.setModel(defaultTableModel);
            table.getColumnModel().getColumn(1).setPreferredWidth(1);
            table.getColumnModel().getColumn(2).setPreferredWidth(1);
        }
    }

    public void setTable2() {

        DefaultTableModel defaultTableModel = setTableModel();
        chosenBusStops.forEach(busStop -> {

            Object[] data = {busStop.getName(), busStop.getId(), busStop.getZone(), busStop.getHeadsigns()};
            defaultTableModel.addRow(data);
        });

        chosenBusStops.forEach(busStop -> googleMap.setMarker(busStop.getCoordinates(), "'" + busStop.getName() + "'"));
        table2.setModel(defaultTableModel);
        table2.getColumnModel().getColumn(1).setPreferredWidth(1);
        table2.getColumnModel().getColumn(2).setPreferredWidth(1);
        table2.setRowSelectionAllowed(false);
    }

    private DefaultTableModel setTableModel() {

        DefaultTableModel defaultTableModel = new DefaultTableModel();
        defaultTableModel.addColumn("Nazwa");
        defaultTableModel.addColumn("Id");
        defaultTableModel.addColumn("Strefa");
        defaultTableModel.addColumn("Numery linii");

        return defaultTableModel;
    }

    private void setSearchComboBoxes() {

        nameComboBox.addItem("Wszystkie");
        busStops.stream().map(BusStop::getName).distinct().sorted(String::compareTo).forEach(
                busStop -> nameComboBox.addItem(busStop));
//        nameComboBox.setEditable(true);

        numberOfLineComboBox.addItem("Wszystkie");
        busStops.stream().flatMapToInt(busStop -> Arrays.stream(busStop.getBusLines())).distinct().sorted().forEach(
                n -> numberOfLineComboBox.addItem(String.valueOf(n)));

        zoneComboBox.addItem("Wszystkie");
        busStops.stream().map(BusStop::getZone).distinct().sorted(String::compareTo).forEach(
                busStop -> zoneComboBox.addItem(busStop));
    }

    private void setDefaultSearch() {
        setTable("Wszystkie", "Wszystkie", "Wszystkie");
        nameComboBox.setSelectedItem("Wszystkie");
        numberOfLineComboBox.setSelectedItem("Wszystkie");
        zoneComboBox.setSelectedItem("Wszystkie");
    }

    private void setButtons() {

        searchButton.addActionListener(
                e -> setTable((String) nameComboBox.getSelectedItem(), (String) numberOfLineComboBox.getSelectedItem(),
                        (String) zoneComboBox.getSelectedItem()));
        resetButton.addActionListener(e -> {
            setDefaultSearch();
        });

        addButton.addActionListener(e -> {
            chosenBusStops.addAll(currentChosenBusStops);
            setTable2();
        });
        addSelectedButton.addActionListener(e -> {
            Arrays.stream(table.getSelectedRows()).forEach(
                    i -> chosenBusStops.add(currentChosenBusStops.stream().toList().get(i)));
            setTable2();
        });

        csvDownloadButton.addActionListener(e -> downloadBusStops(false));
        txtDownloadButton.addActionListener(e -> downloadBusStops(true));
        clearButton.addActionListener(e -> {
            chosenBusStops.clear();
            googleMap.clear();
            busStopInformation.setText("");
            setTable2();
        });
    }

    private void downloadBusStops(boolean isTxt) {

        JFileChooser fileChooser = new JFileChooser();
        int ans = fileChooser.showSaveDialog(this);

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
                JOptionPane.showMessageDialog(this,
                        "Plik " + selectedFile.getName() + " zapisano do " + selectedFile.getParent() + ".", "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Błąd zapisu.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
