package model;

import java.io.Serializable;
import java.util.Arrays;

public class BusStop implements Serializable {

    private Geometry geometry;
    private String id;
    private String type;
    private Properties properties;

    public String getId() {
        return id;
    }

    public int[] getBusLines() {
        return Arrays.stream(properties.getHeadsigns().split(", ")).mapToInt(Integer::parseInt).toArray();
    }

    public String getName() {return properties.stop_name;}

    public String getZone() {
        return properties.zone;
    }

    public String getHeadsigns() {
        return properties.headsigns;
    }

    @Override
    public String toString() {
        return "Nazwa przystanku: "+ properties.stop_name +"\n"+
                "Identyfikator: "+id+"\n"+
                "Strefa: "+properties.zone+"\n"+
                "Linie odjeżdżające: "+properties.headsigns;

    }

    public String getDescription() {
        return "BusStop{" + "geometry=" + geometry + ", id='" + id + '\'' + ", type='" + type + '\'' + ", properties=" +
                properties + ", busLines=" + Arrays.toString(getBusLines()) + ", name='" + getName() + '\'' +
                ", zone='" + getZone() + '\'' + ", headsigns='" + getHeadsigns() + '\'' + ", coordinates=" +
                Arrays.toString(getCoordinates()) + '}';
    }

    public Double[] getCoordinates() {
        return geometry.coordinates;
    }

    static class Properties {

        private String zone;
        private int route_type;
        private String headsigns;
        private String stop_name;

        public String getHeadsigns() {
            return headsigns;
        }

        @Override
        public String toString() {
            return "Properties{" + "zone='" + zone + '\'' + ", route_type=" + route_type + ", headsigns='" + headsigns +
                    '\'' + ", stop_name='" + stop_name + '\'' + '}';
        }

    }

    static class Geometry {

        private Double[] coordinates;
        private String type;

        @Override
        public String toString() {
            return "Geometry{" + "coordinates=" + Arrays.toString(coordinates) + ", type='" + type + '\'' + '}';
        }

    }

}
