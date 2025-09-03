import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class Project2_Codebase {
    static final float [][] gaussianKernel = {
            {0.0625f, 0.125f, 0.0625f},
            {0.125f, 0.25f, 0.125f},
            {0.0625f, 0.125f, 0.0625f}
    };

    //references to some variables we may want to access in a global context
    static int WIDTH = 500; //width of the image
    static int HEIGHT = 500; //height of the image
    static BufferedImage Display; //the image we are displaying
    static JFrame window; //the frame containing our window
    static float [][] terrain;
    static int terreno_Size = 20;

    public static void main(String[] args) {
        //run the GUI on the special event dispatch thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                //Create the window and set options
                //The window
                window = new JFrame("TerrainGenerator");
                window.setPreferredSize(new Dimension(WIDTH + 100, HEIGHT + 50)); //sets the "ideal" window size
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //terminate program when "x" is clicked
                window.setVisible(true); //show the window
                window.pack(); //make window the preferred size

                //Display panel/image
                JPanel DisplayPanel = new JPanel();
                Display = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                DisplayPanel.add(new JLabel(new ImageIcon(Display)));
                window.add(DisplayPanel, BorderLayout.CENTER);

                //Config panel
                JPanel Configuration = new JPanel();
                Configuration.setBackground(new Color(230, 230, 230));
                Configuration.setPreferredSize(new Dimension(100, 500));
                Configuration.setLayout(new FlowLayout());

                //Step count input
                JLabel StepCountLabel = new JLabel("Step Count:");
                Configuration.add(StepCountLabel);

                JTextField StepCount = new JTextField("500");
                StepCount.setPreferredSize(new Dimension(100, 25));
                Configuration.add(StepCount);

                //Walker type input
                JLabel WalkerType = new JLabel("Walker Type:");
                Configuration.add(WalkerType);

                ButtonGroup WalkerTypes = new ButtonGroup(); //group of buttons
                JRadioButton Standard = new JRadioButton("Standard"); //creates a radio button. in a ButtonGroup, only one can be selected at a time
                Standard.setActionCommand("standard"); //can be grabbed to see which button is active
                Standard.setSelected(true); //set this one as selected by default
                JRadioButton Skippy = new JRadioButton("Skippy");
                Skippy.setActionCommand("skippy");
                WalkerTypes.add(Standard); //set as part of group
                WalkerTypes.add(Skippy);
                Configuration.add(Standard); //add to panel
                Configuration.add(Skippy);





                JLabel Geometry = new JLabel("World Geometry:");
                Configuration.add(Geometry);
                ButtonGroup Geometries = new ButtonGroup();
                JRadioButton Bounded = new JRadioButton("Bounded");
                Bounded.setActionCommand("bounded");
                Bounded.setSelected(true);
                JRadioButton Toroidal = new JRadioButton("Toroidal");
                Toroidal.setActionCommand("toroidal");
                Geometries.add(Bounded);
                Geometries.add(Toroidal);
                Configuration.add(Bounded);
                Configuration.add(Toroidal);


                JLabel RenderModeLabel = new JLabel("Render Style:");
                Configuration.add(RenderModeLabel);
                ButtonGroup RenderModes = new ButtonGroup();
                JRadioButton Satellite = new JRadioButton("Satellite");
                Satellite.setActionCommand("satellite");
                Satellite.setSelected(true);
                JRadioButton HeightMap = new JRadioButton("Height Map");
                HeightMap.setActionCommand("height");
                RenderModes.add(Satellite);
                RenderModes.add(HeightMap);
                Configuration.add(Satellite);
                Configuration.add(HeightMap);

                Satellite.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        BufferedImage img = terrainRendering(terrain, "satellite");
                        UpdateDisplay(img);
                        window.repaint();
                    }
                });

                HeightMap.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        BufferedImage img = terrainRendering(terrain, "height");
                        UpdateDisplay(img);
                        window.repaint();
                    }
                });



                JLabel dimensionsLabel = new JLabel("Dimensions");
                Configuration.add(dimensionsLabel);
                JTextField dimensionsin = new JTextField("10");
                dimensionsin.setPreferredSize(new Dimension(100, 25));
                Configuration.add(dimensionsin);

                //=====THIS IS THE MOST RELEVANT SECTION FOR PROJECT 1=====
                //Create the Walk button
                JButton Walk = new JButton("Walk");

                //Assign a behavior to run when the Walk button is pressed
                Walk.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int count = Integer.parseInt(StepCount.getText()); //gets the string from a TextField, and read it as an int
                        String walk_type = WalkerTypes.getSelection().getActionCommand(); //gets the action command of which radio button is selected, a String describing the type of Walk
                        terreno_Size = Integer.parseInt(dimensionsin.getText());
                        terrain = new float[terreno_Size][terreno_Size];
                        boolean is_toro = Geometries.getSelection().getActionCommand().equals("toroidal");


                        //do the stuff
                        if (walk_type.equals("standard")) {
                            standardWalker(terrain, count, terreno_Size,is_toro);
                        } else if (walk_type.equals("skippy")) {
                            skippyWalker(terrain, count, terreno_Size, is_toro);
                        }

                        //2. Update the display with the generated image
                        //===Walk, Update Display, repaint===
                        //1. Generate a Buffered image in the specified style using the data from above
                        BufferedImage GeneratedImage = terrainRendering(terrain, RenderModes.getSelection().getActionCommand());

                        UpdateDisplay(GeneratedImage);
                        window.repaint();
                    }
                });

                Configuration.add(Walk);

                //Divide button
                JButton Divide = new JButton("Divide");
                Divide.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        terreno_Size *= 2;
                        dimensionsin.setText(String.valueOf(terreno_Size));

                       // terreno_Size = Integer.parseInt(dimensionsin.getText());
                        terrain = Divide(terrain);
                        terrain = KernelProcess(terrain, gaussianKernel); // this smooths
                        BufferedImage img = terrainRendering(terrain, RenderModes.getSelection().getActionCommand());
                        UpdateDisplay(img);
                        window.repaint();
                    }
                });
                Configuration.add(Divide);

                window.add(Configuration, BorderLayout.EAST);
            }
        });
    }

    //A method to update the display image to match one generated by you
    static void UpdateDisplay(BufferedImage img) {
        //Below 4 lines draws the input image on the display image
        Graphics2D g = (Graphics2D) Display.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT); //draw over the old image with white
        g.drawImage(img, 0, 0, null); //draw over the old image with the one you generated from the walk.

        //forces the window to redraw its components (i.e., the image)
        window.repaint();
    }

    static void standardWalker(float[][] terrain, int steps, int number, boolean is_toro) {
        int x = (int) (Math.random() * number);
        int y = (int) (Math.random() * number);

        //for loop that goes through every step and chooses a random direction for each
        for (int i = 0; i < steps; i++) {
            int[] translations = {-1, 0, 1};
            int x2 = translations[(int) (Math.random() * 3)];
            int y2 = translations[(int) (Math.random() * 3)];
            if(is_toro){
                x = (x + x2 + number) % number;
                y = (y + y2 + number) % number;
            }else{

            x = Math.min(Math.max(x + x2, 0), number - 1);
            y = Math.min(Math.max(y + y2, 0), number - 1);}

            for (int xa = -1; xa <= 1; xa++) {
                for (int ya = -1; ya <= 1; ya++) {
                    int xb = Math.min(Math.max(x + xa, 0), number - 1);
                    int yb = Math.min(Math.max(y + ya, 0), number - 1);
                    terrain[xb][yb] += 1.0f;
                }
            }
        }
    }

    static void skippyWalker(float[][] terrain, int steps, int number, boolean is_toro) {
        int x = number / 2;
        int y = number / 2;
        int countingSteps = 0;
        int skippingSteps = (int) (Math.random() * 400) + 100; //500
        boolean rendering = true;

        for (int i = 0; i < steps; i++) {
            if (countingSteps == skippingSteps) {
                rendering = !rendering;
                countingSteps = 0;
                skippingSteps = (int) (Math.random() * 400) + 100; //500
            }
            countingSteps++;

            int[] translations = {-1, 0, 1};
            int x2 = translations[(int) (Math.random() * 3)];
            int y2 = translations[(int) (Math.random() * 3)];
            if(is_toro){
                x = (x + x2 + number) % number;
                y = (y + y2 + number) % number;

            }else{
                x = Math.min(Math.max(x + x2, 0), number - 1);
                y = Math.min(Math.max(y + y2, 0), number - 1);
            }






            if (rendering) {
                for (int xa = -1; xa <= 1; xa++) {
                    for (int ya = -1; ya <= 1; ya++) {
                        int xb = Math.min(Math.max(x + xa, 0), number - 1);
                        int yb = Math.min(Math.max(y + ya, 0), number - 1);
                        terrain[xb][yb] += 1.0f;
                    }
                }
            }
        }
    }
    static BufferedImage terrainRendering(float[][] terrain, String render_mode) {
        int number = terrain.length;
        BufferedImage imagen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g_imagen = imagen.createGraphics();

        float max_height = getMaxHeight(terrain);

        int tile_tamano_x = (int) Math.ceil((float) WIDTH / number);  //weird gaps cover up
        int tile_tamano_y = (int) Math.ceil((float) HEIGHT / number);

        int ultimatileancho = WIDTH - (tile_tamano_x * (number - 1));
        int ultimatilelargo = HEIGHT - (tile_tamano_y * (number - 1));

        for (int i = 0; i < number; i++) {
            for (int j = 0; j < number; j++) {
                float heightValue = terrain[i][j] / max_height;
                Color color;

                if (render_mode.equals("satellite")) {
                    color = getSateColor(heightValue);
                } else { //height !!!
                    int gray = (int) (heightValue * 255);
                    color = new Color(gray, gray, gray);}

                g_imagen.setColor(color);
                int currentTileWidth = (i == number - 1) ? ultimatileancho : tile_tamano_x;
                int currentTileHeight = (j == number - 1) ? ultimatilelargo : tile_tamano_y;

                g_imagen.fillRect(i * tile_tamano_x, j * tile_tamano_y, currentTileWidth, currentTileHeight);
            }
        }

        return imagen;
    }

    static float getMaxHeight(float[][] terrain) {
        float maxval = 0;
        for (float[] row : terrain) {
            for (float val : row) {
                maxval = Math.max(maxval, val);
            }
        }
        return maxval;
    }

    static float[][] Divide(float[][] input) {
        int var = input.length;
        float[][] sendout = new float[var * 2][var * 2];

        for (int i = 0; i < var; i++) {
            for (int j = 0; j < var; j++) {
                sendout[2 * i][2 * j] = input[i][j];
                sendout[2 * i + 1][2 * j] = input[i][j];
                sendout[2 * i][2 * j + 1] = input[i][j];
                sendout[2 * i + 1][2 * j + 1] = input[i][j];
            }
        }
        return sendout;
    }

    static Color getSateColor(float heightVal) {
        if (heightVal < 0.2f) {//for the water
            return new Color(0, 0, 255);
        } else if (heightVal < 0.4f) { //beige for the sandy
            return new Color(240, 230, 140);
        } else if (heightVal < 0.6f) {//grassy green
            return new Color(34, 139, 34);
        } else if (heightVal < 0.8f) {//montañas
            return new Color(139, 69, 19);
        } else {//la snow
            return new Color(255, 250, 250);
        }
    }

    static float[][] KernelProcess(float[][] input, float[][] kernel) {
        int vari = input.length;
        float[][] sendout = new float[vari][vari];

        for (int i = 0; i < vari; i++) {
            for (int j = 0; j < vari; j++) {
                float countsum = 0;
                for (int a = -1; a <= 1; a++) {
                    for (int b = -1; b <= 1; b++) {
                        int x = Math.min(Math.max(i + a, 0), vari - 1);
                        int y = Math.min(Math.max(j + b, 0), vari - 1);
                        countsum += input[x][y] * kernel[a + 1][b + 1];
                    }
                }
                sendout[i][j] = countsum;
            }
        }
        return sendout;
    }
}









