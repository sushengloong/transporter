package transporter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileJoiner {
    public static Path join(Path[] subfilePaths) throws IOException {
        String firstFileName = subfilePaths[0].getFileName().toString();
        String[] firstFileNameElements = firstFileName.split("\\.");
        String[] outputFileNameElements = Arrays.copyOfRange(firstFileNameElements, 0, firstFileNameElements.length - 3);
        StringBuffer outputFileName = new StringBuffer("");
        for (int i = 0; i < outputFileNameElements.length; i++) {
            if (i != 0) outputFileName.append('.');
            String element = outputFileNameElements[i];
            outputFileName.append(element);
        }

        Path outputPath = Paths.get(subfilePaths[0].getParent() + "/" + outputFileName.toString());
        System.out.println("Output file: " + outputPath);

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath.toFile()));

        // now, assume that the files are correctly numbered in order (that some joker didn't delete any part)
        for (Path subfilePath: subfilePaths) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(subfilePath.toFile()));

            int b;
            while ( (b = in.read()) != -1 )
                out.write(b);

            in.close();
        }
        out.close();

        return outputPath;
    }
}
