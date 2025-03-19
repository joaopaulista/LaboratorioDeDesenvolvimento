package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Baralho {
    private List<Carta> cartas;

    public Baralho() {
        cartas = new ArrayList<>();
        String[] naipes = {"Copas", "Ouros", "Espadas", "Paus"};
        String[] nomes = {"Ás", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Valete", "Dama", "Rei"};

    
        for (String naipe : naipes) {
            for (String nome : nomes) {
                cartas.add(new Carta(nome, naipe));
            }
        }

        for (int i = 0; i < 4; i++) {
            cartas.add(new Carta("Coringa", "Coringa"));
        }
    }

    public void embaralhar() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int pos1 = random.nextInt(cartas.size());
            int pos2 = random.nextInt(cartas.size());


            Carta temp = cartas.get(pos1);
            cartas.set(pos1, cartas.get(pos2));
            cartas.set(pos2, temp);
        }
    }

    public Carta distribuirCarta() {
        if (!cartas.isEmpty()) {
            return cartas.remove(0);
        }
        return null;
    }


    public boolean hasCarta() {
        return !cartas.isEmpty();
    }

    public void imprimirBaralho() {
        for (Carta carta : cartas) {
            System.out.println(carta);
        }
    }
}
