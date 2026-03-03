package discord.util;

import java.io.File;
import java.net.URISyntaxException;

public class PathHelper {
    public static String getJarDirectory() {
        try {
            String path = PathHelper.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            File jarFile = new File(path);
            return jarFile.getParent();
        } catch (URISyntaxException e) {
            return System.getProperty("user.dir");
        }
    }
}
