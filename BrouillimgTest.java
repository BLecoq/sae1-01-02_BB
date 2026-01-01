
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class BrouillimgTest {

    public static void main(String[] args) throws Exception {

        BufferedImage img = ImageIO.read(new File("/home/shadowlrsp/Documents/S1/SAE/SAE_1.01_et_1.02/Images/image_grec2_2.png"));
        // a la place de shadowlrsp merci de mettre votre user ainsi que la bonne route vers le repertoire
        int[][] gl = Brouillimg.rgb2gl(img);

        long t1 = System.currentTimeMillis();
        double[][] rE = Brouillimg.breakKey(gl, "euclid");
        long t2 = System.currentTimeMillis();

        long t3 = System.currentTimeMillis();
        double[][] rP = Brouillimg.breakKey(gl, "pearson");
        long t4 = System.currentTimeMillis();

        long t5 = System.currentTimeMillis();
        double[][] rT = Brouillimg.breakKey(gl, "tv");
        long t6 = System.currentTimeMillis();

        System.out.println("Temps Euclidien : " + (t2 - t1) + " ms");
        System.out.println("Temps Pearson   : " + (t4 - t3) + " ms");
        System.out.println("Temps TV : " + (t6 - t5) + " ms");

        System.out.println("Meilleure clé Euclid : " + (int) rE[0][1]);
        System.out.println("Meilleure clé Pearson : " + (int) rP[0][1]);
        System.out.println("Meilleure clé TV : " + (int) rT[0][1]);

        int bestKey = (int) rT[0][1];
        int[] perm = Brouillimg.generatePermutation(img.getHeight(), bestKey);
        BufferedImage ok = Brouillimg.unScrambleLines(img, perm);

        ImageIO.write(ok, "png", new File("image_dechiffree.png"));
    }
}
