public class Project2_Functions {
    //Required function: Combine four bytes representing the red, green, blue, and alpha channels of a color into a single integer representing the color.
    static public int ARGBtoInt(int a, int r, int g, int b){
        return (a<<24) | (r<<16) | (g<<8) | b; //or operator |, note to self, research more on this java
    }
    //Required function: From a single integer representing a color, create a byte array of size 4 where each entry is the corresponding value for each color channel.
    static public int[] InttoARGB(int color){
        int mask= 0xFF;

        int alpha= (color>>24)&mask;
        int red= (color>>16)&mask;
        int green= (color>>8)&mask;
        int blue= (color)&mask;
        return new int [] {alpha, red, green, blue};
    }

    static public int[] CreateGradient(int GradientStartColor, int GradientEndColor, int steps){
        int[] interpolation= new int [steps];
        int[] endColor= InttoARGB(GradientEndColor);
        int[] startColor= InttoARGB(GradientStartColor);

        //float alphachanges = (float) (endColor[0]-startColor[0])/(steps-1);
        float redchanges = (float) (endColor[1]-startColor[1])/(steps-1);
        float greenchanges = (float) (endColor[2]-startColor[2])/(steps-1);
        float bluechanges = (float) (endColor[3]-startColor[3])/(steps-1);

        //gradient starrts here
        for (int i = 0; i < steps; i++) {
            int uncleAL = 0xff;
            int uncleRD = Math.round(startColor[1]+(redchanges*i));
            int uncleGR = Math.round(startColor[2]+(greenchanges*i));
            int uncleBL = Math.round(startColor[3]+(bluechanges*i));
            interpolation[i] = ARGBtoInt(uncleAL, uncleRD, uncleGR, uncleBL);
        }
        return interpolation;


    }
}
