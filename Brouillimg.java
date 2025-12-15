import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Brouillimg {

    public static void main(String[] args) throws IOException {

    if (args.length < 3) {
        System.err.println("Usage: java Brouillimg <image_claire> <clé> <process> [image_sortie]");
        System.exit(1);
    }

    String inPath = args[0];
    // Masque 0x7FFF pour garantir que la clé ne dépasse pas les 15 bits
    int key = Integer.parseInt(args[1]) & 0x7FFF;
    String process = args[2];
    String outPath = (args.length >= 4) ? args[3] : "out.png";


    BufferedImage inputImage = ImageIO.read(new File(inPath));
    if (inputImage == null) {
        throw new IOException("Format d’image non reconnu: " + inPath);
    }

    final int height = inputImage.getHeight();
    final int width = inputImage.getWidth();
    System.out.println("Dimensions de l'image : " + width + "x" + height);

    // Pré‑calcul des lignes en niveaux de gris pour accélérer le calcul du critère
    int[][] inputImageGL = rgb2gl(inputImage);

    int[] perm = generatePermutation(height, key);

    if (process.equals("scramble")) {
        BufferedImage scrambledImage = scrambleLines(inputImage, perm);
        ImageIO.write(scrambledImage, "png", new File(outPath));
    }

    else if (process.equals("unscramble")) {
        BufferedImage unscrambledImage = unScrambleLines(inputImage, perm);
        ImageIO.write(unscrambledImage, "png", new File(outPath));
    }

    System.out.println("Image écrite: " + outPath);
    }

    /**
    * Convertit une image RGB en niveaux de gris (GL).
    * @param inputRGB image d'entrée en RGB
    * @return tableau 2D des niveaux de gris (0-255)
    */
    public static int[][] rgb2gl(BufferedImage inputRGB) {
        final int height = inputRGB.getHeight();
        final int width = inputRGB.getWidth();
        int[][] outGL = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = inputRGB.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                // luminance simple (évite float)
                int gray = (r * 299 + g * 587 + b * 114) / 1000;
                outGL[y][x] = gray;
            }
        }
        return outGL;
    }

    /**
    * Génère une permutation des entiers 0..size-1 en fonction d'une clé.
    * @param size taille de la permutation
    * @param key clé de génération (15 bits)
    * @return tableau de taille 'size' contenant une permutation des entiers 0..size-1
    */
    public static int[] generatePermutation(int size, int key) {
        int[] permutation = new int[size];
        for (int i = 0; i < size; i++) permutation[i] = scrambledId(i, size, key);
        return permutation;
    }

    /**
     * Mélange les lignes d'une image selon une permutation donnée.
     * @param image image d'entrée
     * @param permutation permutation des lignes taille = hauteur de l'image
     * @return image avec lignes mélangées
     */
    public static BufferedImage scrambleLines(BufferedImage image, int[] permutation) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();
        if (permutation.length != hauteur) throw new IllegalArgumentException("Taille d'image <> taille permutation");

        BufferedImage out = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < hauteur; y++) {
            int yDestination = permutation[y];
            for (int x = 0; x < largeur; x++) {
                out.setRGB(x, yDestination, image.getRGB(x, y));
            }
        }
        return out;
    }

    /**
     * Déchiffre les lignes d'une image selon une permutation donnée.
     * @param image image d'entrée
     * @param permutation permutation des lignes taille = hauteur de l'image
     * @return image dé-mélangée
     */
    public static BufferedImage unScrambleLines(BufferedImage image, int[] permutation) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();
        if (permutation.length != hauteur) throw new IllegalArgumentException("Taille d'image <> taille permutation");

        BufferedImage out = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        int[] inverse = new int[hauteur];
        for (int i = 0; i < hauteur; i++) inverse[permutation[i]] = i;

        for (int y = 0; y < hauteur; y++) {
            int ySource = inverse[y];
            for (int x = 0; x < largeur; x++) {
                out.setRGB(x, ySource, image.getRGB(x, y));
            }
        }
        return out;
    }

    /**
     * Calcule l'indice de la ligne brouillée correspondant à la ligne claire.
     * @param id indice de la ligne dans l'image claire
     * @param size hauteur de l'image
     * @param key clé de chiffrement (15 bits)
     * @return indice de la ligne dans l'image brouillée
     */
    public static int scrambledId(int id, int size, int key) {
        int r = key & 0xFF;
        int s = (key >> 8) & 0x7F;
        return (r + (2 * s + 1) * id) % size;
    }

    /**
     * Calcule la distance euclidienne entre deux lignes.
     * @param imageGL image en niveaux de gris
     * @param ligne1 indice de la première ligne
     * @param ligne2 indice de la seconde ligne
     * @return distance euclidienne
     */
    public static double distanceEuclidian(int[][] imageGL, int ligne1, int ligne2) {
        double somme = 0.0;
        int largeur = imageGL[0].length;
        for (int i = 0; i < largeur; i++) {
            double diff = imageGL[ligne1][i] - imageGL[ligne2][i];
            somme += diff * diff;
        }
        return Math.sqrt(somme);
    }

    /**
     * Calcule le score total euclidien pour une image selon une permutation.
     * @param gl image en niveaux de gris
     * @param permutation permutation des lignes
     * @return score total
     */
    public static double scoreEuclidian(int[][] gl, int[] permutation) {
        double score = 0.0;
        for (int i = 0; i < permutation.length - 1; i++)
            score += distanceEuclidian(gl, permutation[i], permutation[i + 1]);
        return score;
    }


    /**
    * Calcule le coefficient de corrélation de Pearson entre deux lignes.
    * @param gl image en niveaux de gris
    * @param ligneX indice de la première ligne
    * @param ligneY indice de la seconde ligne
    * @return coefficient de Pearson (-1 à 1)
    */
    public static double correlationPearson(int[][] gl, int ligneX, int ligneY) {
        int largeur = gl[0].length;
        double moyenneX = 0.0;
        double moyenneY = 0.0;

        for (int i = 0; i < largeur; i++) {
            moyenneX += gl[ligneX][i];
            moyenneY += gl[ligneY][i];
            }

        moyenneX /= largeur;
        moyenneY /= largeur;

        double numerateur = 0.0;
        double denominateurX = 0.0;
        double denominateurY = 0.0;

        for (int i = 0; i < largeur; i++) {
            double ecartX = gl[ligneX][i] - moyenneX;
            double ecartY = gl[ligneY][i] - moyenneY;

            numerateur += ecartX * ecartY;
            denominateurX += ecartX * ecartX;
            denominateurY += ecartY * ecartY;
        }

        return numerateur / (Math.sqrt(denominateurX) * Math.sqrt(denominateurY) + 1e-10);
    }

    /**
    * Calcule le score total Pearson pour une image selon une permutation.
    * @param gl image en niveaux de gris
    * @param permutation permutation des lignes
    * @return score total Pearson
    */
    public static double scorePearson(int[][] gl, int[] permutation) {
        double score = 0.0;
        for (int i = 0; i < permutation.length - 1; i++)
            score += correlationPearson(gl, permutation[i], permutation[i + 1]);
        return score;
    }

    /**
     * Recherche la meilleure clé par force brute selon le critère Pearson ou Euclidien.
     * @param gl image en niveaux de gris
     * @param type "pearson" ou "euclid"
     * @return top 3 [score, clé]
     */
    public static double[][] breakKey(int[][] gl, String type){
        int h = gl.length;
        double[][] top3 = new double[3][2];
        for(int i=0;i<3;i++){
            top3[i][0] = type.equals("pearson") ? -Double.MAX_VALUE : Double.MAX_VALUE;
            top3[i][1] = -1;
        }
        for(int k=0;k<32768;k++){
            int[] perm = generatePermutation(h, k);
            double score = type.equals("pearson") ? scorePearson(gl, perm) : scoreEuclidian(gl, perm);
            for(int i=0;i<3;i++){
                boolean better = type.equals("pearson") ? score>top3[i][0] : score<top3[i][0];
                if(better){
                    for(int j=2;j>i;j--){top3[j][0]=top3[j-1][0]; top3[j][1]=top3[j-1][1];}
                    top3[i][0]=score; top3[i][1]=k; break;
                }
            }
        }
        return top3;
    }
}
