package com.example.pametni_paketnik.matrixTSP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GA {

    int popSize;
    double cr; //crossover probability
    double pm; //mutation probability

    ArrayList<TSP.Tour> population;
    ArrayList<TSP.Tour> offspring;

    public GA(int popSize, double cr, double pm) {
        this.popSize = popSize;
        this.cr = cr;
        this.pm = pm;
    }

    public TSP.Tour execute(TSP problem) {
        population = new ArrayList<>();
        offspring = new ArrayList<>();
        TSP.Tour best = null;

        for (int i = 0; i < popSize; i++) {
            TSP.Tour newTour = problem.generateTour();
            problem.evaluate(newTour);
            population.add(newTour);

            if (best == null) {
                best = newTour.clone();

            }
            else {
                if (newTour.distance < best.distance) {
                    best = newTour.clone();

                }
            }
        }

        while (problem.getNumberOfEvaluations() < problem.getMaxEvaluations()) {

            //elitizem - poišči najboljšega in ga dodaj v offspring in obvezno uporabi clone()
            while (offspring.size() < popSize) {
                TSP.Tour parent1 = tournamentSelection();
                TSP.Tour parent2 = tournamentSelection();

                while (parent1.equals(parent2))
                    parent2 = tournamentSelection();

                if (RandomUtils.nextDouble() < cr) {
                    TSP.Tour[] children = pmx(parent1, parent2);
                    offspring.add(children[0]);
                    if (offspring.size() < popSize)
                        offspring.add(children[1]);
                } else {
                    offspring.add(parent1.clone());
                    if (offspring.size() < popSize)
                        offspring.add(parent2.clone());
                }
            }

            for (TSP.Tour off : offspring) {
                if (RandomUtils.nextDouble() < pm) {
                    swapMutation(off);
                }
            }

            //TODO ovrednoti populacijo in shrani najboljšega (best) - Nisem ziher ali je prav tako!
            //implementacijo lahko naredimo bolj učinkovito tako, da overdnotimo samo tiste, ki so se spremenili (mutirani in križani potomci)
            population = new ArrayList<>(offspring);
            for (int i = 0; i < population.size(); i++) {
                problem.evaluate(population.get(i));
                if (population.get(i).getDistance() < best.getDistance()) {
                    best = population.get(i).clone();

                }
            }
            offspring.clear();
        }
        return best;
    }

    private void swapMutation(TSP.Tour off) {
        //izvedi mutacijo

        int random1 = RandomUtils.nextInt(off.path.length);
        int random2 = RandomUtils.nextInt(off.path.length);

        while (random1 == random2)
            random2 = RandomUtils.nextInt(off.path.length);

        TSP.City temp = off.path[random1];
        off.path[random1] = off.path[random2];
        off.path[random2] = temp;
    }

    private TSP.Tour[] pmx(TSP.Tour parent1, TSP.Tour parent2) {
        //izvedi pmx križanje, da ustvariš dva potomca
        int cutpoint1=0;
        int cutpoint2=0;
        TSP.Tour[] newTours = new TSP.Tour[2];

        while(cutpoint1 > cutpoint2 || cutpoint1 == cutpoint2){
            cutpoint1 = RandomUtils.nextInt(parent1.path.length);
            cutpoint2 = RandomUtils.nextInt(parent2.path.length);
        }

        Map<TSP.City, TSP.City> map1 = new HashMap<>();
        Map<TSP.City, TSP.City> map2 = new HashMap<>();
        TSP.Tour parent1Copy = parent1.clone();
        TSP.Tour parent2Copy = parent2.clone();
        TSP.Tour offspring1 = parent1.clone();
        TSP.Tour offspring2 = parent2.clone();

        for (int i = cutpoint1; i < cutpoint2; i++) {
            offspring1.path[i] = parent2Copy.path[i];
            map1.put(parent2Copy.path[i], parent1Copy.path[i]);

            offspring2.path[i] = parent1Copy.path[i];
            map2.put(parent1Copy.path[i], parent2Copy.path[i]);
        }

        // Make changes in first part
        for (int i = 0; i < cutpoint1; i++) {
            while (map1.containsKey(offspring1.path[i]))
                offspring1.path[i] = map1.get(offspring1.path[i]);
            while (map2.containsKey(offspring2.path[i]))
                offspring2.path[i] = map2.get(offspring2.path[i]);
        }
        // Make changes in second part
        for (int i = cutpoint2; i < offspring1.path.length; i++) {
            while (map1.containsKey(offspring1.path[i]))
                offspring1.path[i] = map1.get(offspring1.path[i]);
            while (map2.containsKey(offspring2.path[i]))
                offspring2.path[i] = map2.get(offspring2.path[i]);
        }

        newTours[0] = offspring1;
        newTours[1] = offspring2;

        return newTours;
    }

    private TSP.Tour tournamentSelection() {
        // naključno izberi dva RAZLIČNA posameznika in vrni boljšega
        TSP.Tour firstCandidate = population.get(RandomUtils.nextInt(population.size())).clone();
        TSP.Tour secondCandidate = population.get(RandomUtils.nextInt(population.size())).clone();

        while (firstCandidate.equals(secondCandidate))
            secondCandidate = population.get(RandomUtils.nextInt(population.size())).clone();

        if (firstCandidate.getDistance() < secondCandidate.getDistance())
            return firstCandidate;
        else
            return secondCandidate;
    }
}
