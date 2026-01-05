// Lecoq Brian Bittiger Bryan groupe de SAE n°4

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class BrouillimgTest {
    static Profiler p = new Profiler();
    static MemoireProfiler m = new MemoireProfiler();
    public static void main(String[] args) throws Exception {

        BufferedImage img = ImageIO.read(new File("/home/shadowlrsp/Documents/S1/SAE/SAE_1.01_et_1.02/Images/4k_scramble.png"));
        // a la place de shadowlrsp merci de mettre votre user ainsi que la bonne route vers le repertoire
        int[][] gl = Brouillimg.rgb2gl(img);

        m.debut();
        p.debut();
        double[][] rE = Brouillimg.breakKey(gl, "euclid");
        p.fin();
        m.fin();
        p.print("Temps euclidien : ", p.tempsEcouleNs());
        System.out.println("Euclid mémoire : " + m.getDeltaKB() + " KB");

        m.debut();
        p.debut();
        double[][] rP = Brouillimg.breakKey(gl, "pearson");
        p.fin();
        m.fin();
        p.print("Temps Pearson : ", p.tempsEcouleNs());
        System.out.println("Pearson mémoire : " + m.getDeltaKB() + " KB");

        m.debut();
        p.debut();
        double[][] rT = Brouillimg.breakKey(gl, "tv");
        p.fin();
        m.fin();
        p.print("Temps TV : ", p.tempsEcouleNs());
        System.out.println("TV mémoire : " + m.getDeltaKB() + " KB");

        System.out.println("Meilleure clé Euclid : " + (int) rE[0][1]);
        System.out.println("Meilleure clé Pearson : " + (int) rP[0][1]);
        System.out.println("Meilleure clé TV : " + (int) rT[0][1]);

        int bestKey = (int) rT[0][1];
        int[] perm = Brouillimg.generatePermutation(img.getHeight(), bestKey);
        BufferedImage ok = Brouillimg.unScrambleLines(img, perm);

        ImageIO.write(ok, "png", new File("image_dechiffree.png"));
    }
}
