
/**
 * SCOPO: Progettazione di un sistema multi-agente cooperante incentrato su comunicazione.
 *
 * PROBLEMA:
 * Implementazione di una regina su di una scacchiera, possiede conoscenza dell'ambiente 
 * in cui muoversi e ne ha una completa visione. L'ambiente scacchiera possiede 
 * le seguenti caratteristiche:
 * - accessibile( in un array segno le posizioni di tutte le regine )
 * - dinamico( poiche le 8 regine sono autonome )
 * - discreto( ogni regina segna la propria posizione e quando tutte hanno scritto l'ambiente si aggiorna )
 * - non-episodico( ricordare per ogni regina tutti gli stati del mondo incontrati saturerebbe le risorse
 *                  in breve tempo e non avrebbe molta utilità nella scelta poichè le regine vengono 
 *                  modellate con completa autonomia)
 * - non deterministico ( per l'autonomia delle altre regine )
 * PRIMA IMPLEMENTAZIONE:
 *  Le regine comunicano implicitamente mediante blackbourne, utilizzata dal sistema
 * grafico per la coordinazione. Ne segue che ogni agente ha conoscenza di tutti gli
 * altri.
 * @author  Skyman
 */
public class ReginaAgent extends Thread{
    
    private int id; // il nome
    private Environment c; // l'ambiente in cui agire
    private boolean min; // risponde alla domanda è minacciata?
    private int riga; // posizione = ( riga, id )
    private int other; // quanti altri egenti coesistono
    private boolean found; // legata alla presenza di una posizione !minacciata
    public boolean prepotente; // tipo di regina
    public boolean go = true;
    
    /** Creates a new instance of ReginaAgent */
    public ReginaAgent( int name, boolean type )
    {
       super();
       prepotente = type;
       id = name;
       min = true;       
       found = false;
    }
    
    /** una regina può cambiare nel tempo */
    public void setEnvironment( Environment e )
    {
       this.c = e;
       riga = c.blackbourne[id];
       other = c.getAgentsNumber();
       //c.out.boxes[id].setSelected(prepotente);
    }
        
    /** run method */
    public synchronized void run()
    {
       while ( go )
       {
           while ( !c.avail ||           // non è possibile
                   c.semaphore[id] )    // non è il mio turno
           {
              try
              {
//                 System.out.println("regina "+id+" wait");
                 wait( 500 );
              }catch (Exception e){ e.printStackTrace(); }
           }
           found = false;
//           System.out.println("regina "+id+" search");
           cercoRiga(); // ogni regina pensa a se stessa senza creare richieste 
           notifyAll();
       }
    }
    
    /** Ceck sulla possibile minaccia da parte di altre regine */
    public boolean sonoMinacciata()
    {
//        boolean min = false;
//        if (isDangerPos(id, riga))
//        {
//                min = true;                
//        }                
//        System.out.println("\nRegina "+id+" minacciata ");
//        return min;
					return isDangerPos(this.id, riga);
    }
    
    /** Controlla se la posizione indicata è minacciata da una qualche regina */
    private boolean isDangerPos(int asker, int rowToCheck )
    {
        boolean ret = false;
        for( int queen=0; queen<other ; queen++)
        {
            if (asker!=queen)
                if (c.blackbourne[queen] == rowToCheck || // è sulla riga
                    isOnMyDiag(queen) )
                {// è sulla diagonale
                        ret = true;
                }
        }
        return ret;
    }
    
    /** Controlla se la regina è sulla mia diagonale */
    private boolean isOnMyDiag( int queen )
    {
        boolean ret = false;
        //int riga = c.blackbourne[asker];
        if ( riga == c.blackbourne[queen]-Math.abs(id-queen) ||
             riga == c.blackbourne[queen]+Math.abs(id-queen)) ret = true;
        /*int delta = queen - id;
        if( (riga+delta) == c.blackbourne[queen] )
        {
            ret=true;
        }
        if( (riga-delta) == c.blackbourne[queen] )
        {
            ret=true;
        }*/
        return ret;
    }
    
    /** Ricerca di una riga disponibile */
    private void cercoRiga()
    {  
//        System.out.println("regina "+id+" cerca...");
        int miaRiga = riga;
        for( int r=0; r<other ; r++)// su tutte le righe       
        {
          riga = r;
            if (!isDangerPos(id, r)) // se la riga r è sicura ci vado altrimenti ne cerco un altra
            {
                if ( miaRiga == r ) c.changes[id] = false;
                
                    fixRiga(r); // scelgo la riga                 
                    // esco dal metodo     
                    r = other;
                
            }            
        }
        if ( !found && prepotente ) // non muoio io, lascio che un'altra trovi la pos sicura
        {
//            System.out.print(" prepotentemente ");
            fixRiga( ((int)(Math.random()*10))%other ); // ne scelgo una a caso e si spostano le altre
        }
        if ( !found )  fixRiga(miaRiga); // sono morta
    }
    
    /** Prendo la posizione */
    private void fixRiga( int riga )
    {   
        
        this.riga = riga;
        min = false; 
        found = true;
//        System.out.println("\t La regina "+id+" ha scelto la riga "+riga+" !");
        c.oneLess( id, riga );        
        
    }
}
