package main;

import model.Baralho;
import model.Carta;

public class Main {
    public static void main(String[] args) {
        Baralho baralho = new Baralho();

        System.out.println("📜 Baralho original:");
        baralho.imprimirBaralho();

        System.out.println("\n🔀 Embaralhando...");
        baralho.embaralhar();

        System.out.println("\n📜 Baralho embaralhado:");
        baralho.imprimirBaralho();

        System.out.println("\n🃏 Distribuindo 5 cartas:");
        for (int i = 0; i < 5; i++) {
            Carta carta = baralho.distribuirCarta();
            System.out.println("Carta retirada: " + carta);
        }

        System.out.println("\n📌 Ainda tem cartas no baralho? " + baralho.hasCarta());
    }
}
