package googlemap;

import busstop.BusStop;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.js.JsAccessible;
import com.teamdev.jxbrowser.js.JsArray;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import gui.Frame;

import javax.swing.*;
import java.io.File;
import java.util.*;

import static java.lang.Math.sqrt;

public class GoogleMap {

    private EngineOptions options = EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED).licenseKey(
            "1BNDHFSC1G577WNB5G4F1JENWZGASVD16XB54YPTBQXTNNGW5QUQWHTAI92ZBSVNQZKJMB").build();
    private Engine engine = Engine.newInstance(options);
    private Browser browser = engine.newBrowser();
    private ArrayList<BusStop> busStops;
    private JTextArea label;
    private LinkedHashSet<BusStop> currentChosenBusStops;
    private Frame frame;


    public GoogleMap(Frame frame, JPanel panel, ArrayList<BusStop> busStops, JTextArea label, LinkedHashSet<BusStop> currentChosenBusStops) {
        this.busStops = busStops;
        this.label = label;
        this.currentChosenBusStops = currentChosenBusStops;
        this.frame = frame;
        browser.set(InjectJsCallback.class, params -> {
            JsObject window = params.frame().executeJavaScript("window");
            Objects.requireNonNull(window).putProperty("java", new JavaCallback());
            return InjectJsCallback.Response.proceed();
        });
        BrowserView view = BrowserView.newInstance(browser);
        panel.add(view);
        browser.navigation().loadUrl(new File(
                "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                        "map.html").getAbsolutePath());
    }

    public void setMarker(Double[] coordinates, String name) {
        String script = "var myLatlng = new google.maps.LatLng(" + coordinates[1].toString() + ", " +
                coordinates[0].toString() + ");\n" + "var marker = new google.maps.Marker({\n" +
                "    position: myLatlng,\n" + "    map: map,\n" + "    title: " + name + ",\n" + "});";
        browser.mainFrame().ifPresent(f -> f.executeJavaScript(script));
    }

    public final class JavaCallback {

        @JsAccessible
        public void getCoords(JsArray coords) {
            Double[] coordinations = new Double[]{coords.get(0), coords.get(1)};
            findNearestBusStop(coordinations);
        }

    }

    public void findNearestBusStop(Double[] coords) {
        double x = coords[1];
        double y = coords[0];
        BusStop busStop = busStops.stream().min(Comparator.comparingDouble(
                s -> sqrt(Math.pow(s.getCoordinates()[0] - x, 2) + Math.pow(s.getCoordinates()[1] - y, 2)))).orElse(
                null);
        this.setMarker(Objects.requireNonNull(busStop).getCoordinates(), "'" + busStop.getName() + "'");
        label.setText(busStop.toString());
        currentChosenBusStops.add(busStop);
        frame.setTable2();
    }

    public void clear() {
        browser.mainFrame().ifPresent(f -> f.executeJavaScript("initMap()"));
    }

}
