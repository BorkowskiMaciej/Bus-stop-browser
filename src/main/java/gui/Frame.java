package gui;

import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
import com.teamdev.jxbrowser.js.JsAccessible;
import com.teamdev.jxbrowser.js.JsArray;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import googlemap.GoogleMap;
import model.Model;

import javax.swing.*;
import java.util.*;

public class Frame extends JFrame {

    private JPanel mainPanel;
    private JPanel selectionPanel;
    private JPanel searchPanel;
    private JPanel resultPanel;
    private JPanel mapPanel;
    private JScrollPane tablePanel;
    private JTable leftTable;
    private JTable rightTable;
    private JComboBox<String> nameComboBox;
    private JComboBox<String> numberOfLineComboBox;
    private JComboBox<String> zoneComboBox;
    private JButton searchButton;
    private JButton resetButton;
    private JButton addButton;
    private JButton addSelectedButton;
    private JButton csvDownloadButton;
    private JButton txtDownloadButton;
    private JButton clearButton;
    private JTextArea busStopInformation;

    private GoogleMap googleMap;
    private Model model;

    public Frame(Model model) {

        this.model = model;

        setContentPane(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Wyszukiwarka przystanków ZTM w Poznaniu");

        setGoogleMap();
        setSearchComboBoxes();
        setButtons();
        setDefaultSearch();

        pack();
        setVisible(true);
    }

    private void setLeftTable(String name, String lineNumber, String zone) {

        model.setCurrentChosenBusStops(name, lineNumber, zone);

        if (model.getCurrentChosenBusStops().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Brak przystanków spełniających kryteria.", "Uwaga",
                    JOptionPane.WARNING_MESSAGE);

            resetButton.doClick();
        } else {
            leftTable.setModel(model.setTableModel(model.getCurrentChosenBusStops()));
            leftTable.getColumnModel().getColumn(1).setPreferredWidth(1);
            leftTable.getColumnModel().getColumn(2).setPreferredWidth(1);
        }
    }

    public void setRightTable() {

        model.getChosenBusStops().forEach(
                busStop -> googleMap.setMarker(busStop.getCoordinates(), "'" + busStop.getName() + "'"));

        rightTable.setModel(model.setTableModel(model.getChosenBusStops()));
        rightTable.getColumnModel().getColumn(1).setPreferredWidth(1);
        rightTable.getColumnModel().getColumn(2).setPreferredWidth(1);
        rightTable.setRowSelectionAllowed(false);
    }

    private void setSearchComboBoxes() {

        nameComboBox.addItem("Wszystkie");
        model.getNamesOfBusStops().forEach(busStop -> nameComboBox.addItem(busStop));
        nameComboBox.setEditable(true);

        numberOfLineComboBox.addItem("Wszystkie");
        model.getNumbersOfLines().forEach(n -> numberOfLineComboBox.addItem(n));

        zoneComboBox.addItem("Wszystkie");
        model.getZones().forEach(zone -> zoneComboBox.addItem(zone));
    }

    private void setDefaultSearch() {
        setLeftTable("Wszystkie", "Wszystkie", "Wszystkie");
        nameComboBox.setSelectedItem("Wszystkie");
        numberOfLineComboBox.setSelectedItem("Wszystkie");
        zoneComboBox.setSelectedItem("Wszystkie");
    }

    private void setButtons() {

        searchButton.addActionListener(e -> setLeftTable((String) nameComboBox.getSelectedItem(),
                (String) numberOfLineComboBox.getSelectedItem(), (String) zoneComboBox.getSelectedItem()));
        resetButton.addActionListener(e -> setDefaultSearch());

        addButton.addActionListener(e -> {
            model.getChosenBusStops().addAll(model.getCurrentChosenBusStops());
            setRightTable();
        });
        addSelectedButton.addActionListener(e -> {
            Arrays.stream(leftTable.getSelectedRows()).forEach(
                    i -> model.getChosenBusStops().add(model.getCurrentChosenBusStops().stream().toList().get(i)));
            setRightTable();
        });

        csvDownloadButton.addActionListener(e -> model.downloadBusStops(false));
        txtDownloadButton.addActionListener(e -> model.downloadBusStops(true));
        clearButton.addActionListener(e -> {
            model.getChosenBusStops().clear();
            googleMap.clear();
            busStopInformation.setText("");
            setRightTable();
        });
    }

    public void setGoogleMap() {
        this.googleMap = new GoogleMap();
        googleMap.getBrowser().set(InjectJsCallback.class, params -> {
            JsObject window = params.frame().executeJavaScript("window");
            Objects.requireNonNull(window).putProperty("java", new JavaCallback());
            return InjectJsCallback.Response.proceed();
        });
        mapPanel.add(BrowserView.newInstance(googleMap.getBrowser()));
    }

    public class JavaCallback {

        @JsAccessible
        public void getCoords(JsArray coords) {
            Double[] coordinations = new Double[]{coords.get(0), coords.get(1)};
            busStopInformation.setText(model.findNearestBusStop(coordinations));
            setRightTable();
        }

    }

}
