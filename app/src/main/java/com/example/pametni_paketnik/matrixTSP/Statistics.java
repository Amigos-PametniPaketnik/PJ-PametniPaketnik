package com.example.pametni_paketnik.matrixTSP;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Statistics {
    private Statistics() {
    }
    static public double average(ArrayList<TSP.Tour> tours) {
        double[] fitnesses = new double[tours.size()];

        for (int i = 0; i < tours.size(); i++)
            fitnesses[i] = tours.get(i).getDistance();

        return Arrays.stream(fitnesses).average().getAsDouble();
    }
    static public double min(ArrayList<TSP.Tour> tours) {
            double[] fitnesses = new double[tours.size()];

            for (int i = 0; i < tours.size(); i++)
                fitnesses[i] = tours.get(i).getDistance();

            return Arrays.stream(fitnesses).min().getAsDouble();
        }
    static public double std(ArrayList<TSP.Tour> tours) {
        double[] fitnesses = new double[tours.size()];

        for (int i = 0; i < tours.size(); i++)
            fitnesses[i] = tours.get(i).getDistance();

        double standardDeviation = 0.0;
        for (int i = 0; i < tours.size(); i++)
            standardDeviation += Math.pow(fitnesses[i] - average(tours), 2.0);

        return Math.sqrt(standardDeviation / tours.size());
    }
}
