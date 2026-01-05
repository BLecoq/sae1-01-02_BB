
public class Profiler {

    private long tempsDebut;
    private long tempsFin;

    public void debut (){
        tempsDebut = System.nanoTime();
    }

    public void fin (){
        tempsFin = System.nanoTime();
    }

    public long tempsEcouleNs() {
        return tempsFin - tempsDebut;
    }

    public double tempsEcouleMs(){
        return (tempsFin - tempsDebut)/1_000_000.0;
    }

    public void print(String texte,long ns){
        System.out.println(texte + ":" + (ns/1_000_000.0) + " ms");
    }

}