import javax.swing.*;
import java.io.*;

/**
 * SCOPO: Progettazione di un sistema multi-agente cooperante incentrato su comunicazione.
 *
 * PROBLEMA:
 *   Implementazione di un ambiente adatto ad ospitare N regine senza obbligarle all'assunzione
 * di una posizione minacciata da un'altra.
 * Per le caratteristiche di una regina ( pezzo degli scacchi ) è suficiente modellare una 
 * matrice N x N. Per la coordinazione delle stesse è sufficiente un semplice modello di lettori 
 * e scrittori su blackbourne, dove una variabile setta lo start per gli agenti. 
 *   Come blackbourne si intende un array di N short associato alle colonne della scacchiera
 * ed il contenuto indica la riga occupata in quella colonna.
 *   Una booleana pubblica ( avail ) permette alle regine di deliberare, al momento in cui tutte
 * hanno comunicato la posizione l'ambiente ne inverte il valore e procede ad aggiornasi lo stato.
 * Fatto cio controlla se qualche regina è in difficolta, nel qual caso riparte la race, altrimenti
 * termina la ricerca.
 *
 * @author  Skyman
 */
public class Environment
{
    public boolean avail;      // false blocca gli agenti in esso.
    private boolean[][] campo; // il posto dedicato agli agenti.
    private int n;             //il numero degli agenti.
    public int[] blackbourne;  // le posizioni degli agenti.
    public boolean[] semaphore; // semaforo per gli agenti
    private final int NUM_MAX_MOSSE = 50;
    private int counter = 0;
    private ReginaAgent[] agenti;
    public boolean changes[];   // segno cambiamenti avvenuti dall'ultimo ciclo agenti
    PrintWriter out = null;
    //public Output out;
    int mosse = 0;
    int prova = 0;
    ReginaAgent[] regine;
    String conf;
    Launch16Regine caller;


    /** Creates a new instance of Environmet */
    public Environment( int n, ReginaAgent[] a, int pr, String configurazione , Launch16Regine caller)
    {
    	  this.caller = caller;
        conf = configurazione;
        prova = pr;
        changes = new boolean[n];
        //out = new Output();
        agenti = a;
        this.n = n;
        blackbourne = new int[n];
        avail = false;
//        try{
//            out = new PrintWriter(new BufferedWriter(new FileWriter("responso"+ configurazione +".txt")));
//            out.append( "=========================================================" );
//            out.append( "=========================================================" );
//            out.append("\n\nPROVA # "+prova);
//            out.flush();
//        }catch(Exception e)
//        {
//            e.printStackTrace();
//        }
        initCampo();
        go();
    }

    /** Aggiornamento dello stato */
    private synchronized void update()
    {
          mosse++;
        boolean finito = true;

        for( int i=0; i<n ; i++)
        {
            System.out.print(blackbourne[i]+" ");
            if ( agenti[i].sonoMinacciata() )
            {
              finito = false;

            }
        }
        System.out.println("  posizioni al turno "+mosse);
        counter = 0;
        if ( finito || mosse >= NUM_MAX_MOSSE ) finish();
        else go();
    }

    /** termina tutto */
    private void finish()
    {
        avail = false;

        System.out.println("---- SOLUZIONE ----");
        for( int i=0; i<n ; i++)
        {
            System.out.print(blackbourne[i]+" ");
        }
        try{
            for( int i=0; i<n ; i++)
            {
                out.append(blackbourne[i]+" ");
            }
            out.append("   in "+mosse+" mosse.");
            out.flush();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("   in "+mosse+" mosse.");

        if(prova < 50 )restart();
//        else System.exit(0);
				else caller.next();
    }

    /** ricomincio  */
    public void restart(){
        prova++;
        // sospendo gli agenti
        for ( int i = 0; i<agenti.length ;i++ ){
            agenti[i].go = false;
        }         /*
        // creo 8 nuovi agenti
        regine = new ReginaAgent[n];
        for( int i=0; i<n ; i++)
        {
            if (i%2 == 0)
                regine[i] = new ReginaAgent( i, false);
            else
                regine[i] = new ReginaAgent( i, true);
        }
        avail = false;
        mosse = 0;
        // azzero lo stato
        initCampo();
        for( int i=0; i<n ; i++)
        {

        }
        // partenza
        for( int i=0; i<n ; i++)
        {
            regine[i].setEnvironment( this );
            regine[i].start();
        }

        // partenza
        //go(); */
        new Main(prova, conf, caller);
    }

    /** Inizializzatore di campo */
    private void initCampo()
    {
        int x = (int)n;
        campo = new boolean[x][x];
        semaphore = new boolean[x];
        for( int i=0; i<x ; i++)
        {
            blackbourne[i] = ((int)(Math.random()*10))%n;
            semaphore[i] = true; // tutti fermi
            changes[i] = true;
            for( int ii=0; ii<x ; ii++)
            {
                campo[i][ii] = false;
            }
        }
        System.out.println("=========================================================");
        System.out.println("=========================================================");
        System.out.println("\n\nPROVA # "+prova);
        System.out.println("");
        for( int i=0; i<n ; i++)
        {
            System.out.print(blackbourne[i]+" ");
        }
        System.out.println("  è la disposizione iniziale.");
        System.out.println("");

        for( int i=0; i<n ; i++)
        {
            if( agenti[i].prepotente )
                System.out.print("0 ");
            else
                System.out.print("  ");
        }
        System.out.println("  è la disposizione delle regine squilibrate");
    }

    /** The starter. */
    private synchronized void go()
    {
        avail = true;

        semaphore[counter] = false; // da il via all'agente
        notifyAll();
//        System.out.println("regina numero "+counter+" parte ");
    }

    /** Controllo deliberazioni avvenute */
    private boolean isQueensTerminate()
    {
        boolean ret = true;
        if (counter<n) ret = false;
        return ret;
    }

    /** Recupero numero agenti */
    public int getAgentsNumber()
    {
        return n;
    }
    
    /** vhiamato degli agenti avvisano l'ambiente */
    public synchronized void oneLess( int id, int riga )
    {
        counter = id+1;
        blackbourne[id] = riga;
        semaphore[id] = true;
        if ( isQueensTerminate() )
        {
            avail = false;
//            System.out.println("\t\tENVIRONMENT dice tutti a nanna");            
            update();
        }
//        System.out.println("\tDELIB: "+delib);        
        go();
    }
}
