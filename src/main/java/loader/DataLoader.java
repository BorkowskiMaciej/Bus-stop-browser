package loader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;

import busstop.BusStop;
import com.google.gson.reflect.TypeToken;
import org.json.*;
import com.google.gson.*;

import javax.swing.*;

public class DataLoader {

    public static ArrayList<BusStop> generateBusStopsList() {

        String uri = "https://www.poznan.pl/mim/plan/map_service.html?mtype=pub_transport&co=cluster";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

        HttpResponse<String> response;

        while (true) {

            try {
                response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                break;
            } catch (IOException | InterruptedException e) {

                Object[] options = {"Odśwież", "Zamknij"};
                int res = JOptionPane.showOptionDialog(null, "Brak połączenia z internetem", "Błąd",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                        null, options, options[0]);
                if (res == JOptionPane.CLOSED_OPTION | res == 1) {
                    System.exit(0);
                }
            }

        }

        JSONArray jsonArray = new JSONObject(response.body()).getJSONArray("features");
        Type listType = new TypeToken<ArrayList<BusStop>>() {}.getType();

        return new Gson().fromJson(jsonArray.toString(), listType);

    }

}
