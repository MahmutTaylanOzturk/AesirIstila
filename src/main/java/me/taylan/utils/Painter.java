package me.taylan.utils;


import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Painter {
    public static String paint(String s) {

        return ChatColor.translateAlternateColorCodes('&', s);
    }



    public static String intToNumeral(int i) {
        String str = null;
        //   15 20 25 30 35
        if (i == 15 || i == 20 || i == 25 || i == 30 || i == 35) {
            str = "I";
        } else if (i == 16 || i == 21 || i == 26 || i == 31 || i == 36 || i == 41) {
            str = "II";
        } else if (i == 17 || i == 22 || i == 27 || i == 32 || i == 37 || i == 42) {
            str = "III";
        } else if (i == 18 || i == 23 || i == 28 || i == 33 || i == 38 || i == 43) {
            str = "IV";
        } else if (i == 19 || i == 24 || i == 29 || i == 34 || i == 39 || i == 44) {
            str = "V";
        }
        return str;
    }

    public static void createSphereParticle(Location l) {
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / 15) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 30) {
                double r = 1.5;
                double x = r * Math.cos(theta) * Math.sin(phi);
                double y = r * Math.cos(phi) + 1.5;
                double z = r * Math.sin(theta) * Math.sin(phi);

                l.add(x, y, z);
                l.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, l, 1, 0F, 0F, 0F, 0.001);
                l.subtract(x, y, z);
            }
        }
    }

}
