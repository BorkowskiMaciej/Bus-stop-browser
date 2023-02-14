package googlemap;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;

import java.io.File;

public class GoogleMap {

    private Browser browser;

    public GoogleMap() {
        this.browser = Engine.newInstance(EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED).licenseKey(
                "1BNDHFSC1G577WNB5G4F1JENWZGASVD16XB54YPTBQXTNNGW5QUQWHTAI92ZBSVNQZKJMB").build()).newBrowser();
        browser.navigation().loadUrl(new File(
                "." + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                        "map.html").getAbsolutePath());
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setMarker(Double[] coordinates, String name) {
        String script = "var myLatlng = new google.maps.LatLng(" + coordinates[1].toString() + ", " +
                coordinates[0].toString() + ");\n" + "var marker = new google.maps.Marker({\n" +
                "    position: myLatlng,\n" + "    map: map,\n" + "    title: " + name + ",\n" + "});";
        browser.mainFrame().ifPresent(f -> f.executeJavaScript(script));
    }

    public void clear() {
        browser.mainFrame().ifPresent(f -> f.executeJavaScript("initMap()"));
    }


}
