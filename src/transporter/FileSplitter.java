package transporter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSplitter {
    public static Path[] split(Path filepath, long chunkSizeInBytes) throws FileNotFoundException, IOException
    {
        File file = filepath.toFile();
        // open the file
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

        // get the file length
        long fileSize = file.length();
        long numberOfSubfileWithFullChunkSize = fileSize / chunkSizeInBytes;

        Path[] partPaths = new Path[(int)numberOfSubfileWithFullChunkSize + 1];

            // loop for each full chunk
        int subfile;
        for (subfile = 0; subfile < numberOfSubfileWithFullChunkSize; subfile++)
        {
            String subfilePath = filepath + "." + subfile;
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(subfilePath));

            // write the right amount of bytes
            for (int currentByte = 0; currentByte < chunkSizeInBytes; currentByte++)
            {
                // load one byte from the input file and write it to the output file
                out.write(in.read());
            }

            // close the file
            out.close();

            partPaths[subfile] = Paths.get(subfilePath);
        }

        // loop for the last chunk (which may be smaller than the chunk size)
        if (fileSize != chunkSizeInBytes * (subfile - 1))
        {
            
            String subfilePath = filepath + "." + subfile;
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(subfilePath));

            // write the rest of the file
            int b;
            while ((b = in.read()) != -1)
                out.write(b);

            // close the file
            out.close();

            partPaths[partPaths.length - 1] = Paths.get(subfilePath);
        }

        // close the file
        in.close();

        return partPaths;
    }
}
