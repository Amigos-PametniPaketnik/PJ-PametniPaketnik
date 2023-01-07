package com.example.pametni_paketnik.matrixTSP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class TSP {

    enum DistanceType {EUCLIDEAN, WEIGHTED}

    public class City {
        public int index;
        public double x, y;
    }

    public class Tour {

        double distance;
        int dimension;
        City[] path;

        public Tour(Tour tour) {
            distance = tour.distance;
            dimension = tour.dimension;
            path = tour.path.clone();
        }

        public Tour(int dimension) {
            this.dimension = dimension;
            path = new City[dimension];
            distance = Double.MAX_VALUE;
        }

        public Tour clone() {
            return new Tour(this);
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public City[] getPath() {
            return path;
        }

        public void setPath(City[] path) {
            this.path = path.clone();
        }

        public void setCity(int index, City city) {
            path[index] = city;
            distance = Double.MAX_VALUE;
        }
    }

    String name;
    City start;
    List<City> cities = new ArrayList<>();
    int numberOfCities;
    double[][] weights;
    DistanceType distanceType = DistanceType.EUCLIDEAN;
    int numberOfEvaluations, maxEvaluations;


    public TSP(String path, int maxEvaluations,List<City> cities2,double[][] matrix) {
        loadData(path);
        numberOfEvaluations = 0;
        this.maxEvaluations = maxEvaluations;
        cities=cities2;
        weights=matrix;
        start=cities2.get(0);
        numberOfCities=cities2.size();
    }

    public TSP() {
    }

    public void evaluate(Tour tour) {
        double distance = 0;
        distance += calculateDistance(start, tour.getPath()[0]);
        for (int index = 0; index < numberOfCities; index++) {
            if (index + 1 < numberOfCities){
                distance += calculateDistance(tour.getPath()[index], tour.getPath()[index + 1]);
                }
            else
                distance += calculateDistance(tour.getPath()[index], start);
        }
        tour.setDistance(distance);
        numberOfEvaluations++;
    }

    private double calculateDistance(City from, City to) {
        //TODO implement
        switch (distanceType) {
            case EUCLIDEAN:
                return Math.sqrt(Math.pow((from.x - to.x), 2) + Math.pow((from.y - to.y), 2));
            case WEIGHTED:
                return weights[from.index - 1][to.index - 1];
            default:
                return Double.MAX_VALUE;
        }
    }

    public Tour generateTour() {
        //TODO generate random tour, use RandomUtils
        Tour randomTour=new Tour(numberOfCities);
        randomTour.path[0]= start;
        List<City> tempCitys= new ArrayList<>(cities); // Need to create copy
        tempCitys.remove(start);
        int index=1;
        while(tempCitys.size()>0){
            int random = RandomUtils.nextInt(tempCitys.size());
            randomTour.path[index] = tempCitys.get(random);
            tempCitys.remove(random);
            index++;
        }

     //   System.out.println("done ");
        return randomTour;
    }

    private void loadData(String path) {
        //TODO set starting city, which is always at index 0
        // start=new City();
        // start.index=0;
/*
        InputStream inputStream = TSP.class.getClassLoader().getResourceAsStream(path);
        if(inputStream == null) {
            System.err.println("File "+path+" not found!");
            return;
        }

        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String line = br.readLine();
            while (line != null) {
                lines.add(line);
               line = br.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(lines);
        //TODO parse data
        String edgeWeightFormat = null;
        String displayDataType = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.equals("EOF")) {
                this.start = cities.get(0);
                return;
            }
            String keyword = line.split("[:]", 0)[0].trim();
            String value = "";
            if (line.split("[:]", 0).length > 1)
                value = line.split("[:]", 0)[1].trim();
            switch (keyword) {
                case "NAME":
                    this.name = value;
                    break;
                case "TYPE":
                    if (!value.equals("TSP"))
                        System.out.println("Given problem is not TSP problem!");
                    break;
                case "COMMENT":
                    //System.out.println(keyword + " : " + value);
                    break;
                case "DIMENSION":
                    this.numberOfCities = Integer.parseInt(value);
                    break;
                case "EDGE WEIGHT TYPE":
                    switch (value) {
                        case "EXPLICIT":
                            this.distanceType = DistanceType.WEIGHTED;
                            this.weights = new double[numberOfCities][numberOfCities];
                            break;
                        case "EUC_2D":
                            this.distanceType = DistanceType.EUCLIDEAN;
                            break;
                    }
                    break;
                case "EDGE_WEIGHT_FORMAT":
                    switch (value) {
                        case "FULL_MATRIX":
                            edgeWeightFormat = value;
                            this.distanceType = DistanceType.WEIGHTED;
                            break;
                    }
                    break;
                case "DISPLAY_DATA_TYPE":
                    displayDataType = value;
                    break;
                case "NODE_COORD_SECTION":
                    // Preberemo podatke (data section) v obliki koordinat mest
                    for (int j = i + 1; j < lines.size(); j++) {
                        if (lines.get(j).equals("EOF")) {
                            this.start = cities.get(0);
                            return;
                        }
                        //System.out.println(lines.get(j));
                        String[] cityCoordinates = lines.get(j).replaceAll("^\\s+", "").split("\\s+", 0);
                        //System.out.println(cityCoordinates[0] + " " + cityCoordinates[1] + " " + cityCoordinates[2]);
                        City newCity = new City();
                        newCity.index = Integer.parseInt(cityCoordinates[0]);
                        newCity.x = Double.parseDouble(cityCoordinates[1]);
                        newCity.y = Double.parseDouble(cityCoordinates[2]);
                        this.cities.add(newCity);
                    }
                    break;
                case "EDGE_WEIGHT_SECTION":
                    // Preberemo podatke (data section)
                    if (this.weights == null)
                        this.weights = new double[numberOfCities][numberOfCities];
                    int n = 0;
                    int j;
                    for (j = i + 1; j < (i + 1) + numberOfCities; j++) {
                        String [] weightsInLine = lines.get(j).split("[ ]");
                        int l = 0;
                        for (int k = 0; k < weightsInLine.length; k++) {
                            if (!weightsInLine[k].isEmpty()) {
                                if (weightsInLine[k] != " ") {
                                    this.weights[n][l] = Integer.parseInt(weightsInLine[k]);
                                    l++;
                                }
                            }
                        }
                        n++;
                    }
                    i = j - 1;
                    break;
                case "DISPLAY_DATA_SECTION":
                    switch (displayDataType) {
                        case "TWOD_DISPLAY":
                            int l = 0;
                            for (j = i + 1; j < (i + 1) + numberOfCities; j++) {
                                String [] nodeInLine = lines.get(j).split("[ ]");
                                City newCity = new City();
                                l = 0;
                                for (int k = 0; k < nodeInLine.length; k++) {
                                    if (!nodeInLine[k].isEmpty()) {
                                        if (nodeInLine[k] != " ") {
                                            switch (l) {
                                                case 0:
                                                    newCity.index = Integer.parseInt(nodeInLine[k]);
                                                    break;
                                                case 1:
                                                    newCity.x = Double.parseDouble(nodeInLine[k]);
                                                    break;
                                                case 2:
                                                    newCity.y = Double.parseDouble(nodeInLine[k]);
                                                    break;
                                            }
                                            l++;
                                        }
                                    }
                                }
                                this.cities.add(newCity);
                            }
                            i = j - 1;
                            break;
                    }
                    break;
            }
        }

        */
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public int getNumberOfEvaluations() {
        return numberOfEvaluations;
    }
}
