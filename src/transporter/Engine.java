package transporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class Engine {
    final static long MAX_SUBFILE_SIZE_IN_BYTES = 24 * 1024 * 1024;

    public Path reduce(String filepath, String password) throws Exception {
        long startTime = System.nanoTime();

        Path inputPath = Paths.get(filepath);

        Path tmp = createTempDirectoryDeleteOnExit();
        Path tempInputFile = FileSystems.getDefault().getPath(tmp.toString(), inputPath.getFileName().toString());
        Files.copy(inputPath, tempInputFile);
        long originalFileSize = tempInputFile.toFile().length();
        System.out.println("Original file size: " + FileSizeFormatter.format(originalFileSize));
        System.out.println("[DONE] Clone temp: " + tempInputFile);

        Path[] subfilePaths = splitIntoSubfiles(tempInputFile);
        Files.delete(tempInputFile);
        System.out.println("[DONE] Split");

        Path[] encryptedSubfilePaths = encryptSubfiles(subfilePaths, password);
        for (Path subfilePath: subfilePaths) {
            Files.delete(subfilePath);
        }
        System.out.println("[DONE] Encryption");

        long stopTime = System.nanoTime();
        System.out.printf("Runtime: %s seconds\n\n", TimeUnit.SECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS));

        return encryptedSubfilePaths[0].getParent();
    }

    public Path consolidate(String directoryPath, String password) throws Exception {
        long startTime = System.nanoTime();

        Path inputPath = Paths.get(directoryPath);
        File[] encryptedSubfiles = inputPath.toFile().listFiles();
        Path[] encryptedSubfilePaths = new Path[encryptedSubfiles.length];
        for (int i = 0; i < encryptedSubfilePaths.length; i++) {
            File subfile = encryptedSubfiles[i];
            encryptedSubfilePaths[i] = subfile.toPath();
        }

        Path[] decryptedSubfilePaths = decryptSubfiles(encryptedSubfilePaths, password);
        for (Path encryptedSubfilePath: encryptedSubfilePaths) {
            Files.delete(encryptedSubfilePath);
        }
        System.out.println("[DONE] Decryption");

        Path outputPath = joinSubfiles(decryptedSubfilePaths);
        for (Path subfilePath: decryptedSubfilePaths) {
            Files.delete(subfilePath);
        }
        System.out.println("[DONE] Join");

        long stopTime = System.nanoTime();
        System.out.printf("Runtime: %s seconds\n\n", TimeUnit.SECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS));

        return outputPath;
    }

    private Path[] splitIntoSubfiles(Path tempInputFile) throws IOException {
        return FileSplitter.split(tempInputFile, MAX_SUBFILE_SIZE_IN_BYTES);
    }

    private Path joinSubfiles(Path[] subfilePaths) throws IOException {
        return FileJoiner.join(subfilePaths);
    }

    private Path[] encryptSubfiles(Path[] subfilePaths, String password) throws Exception {
        Path[] encryptedSubfilePaths = new Path[subfilePaths.length];

        for (int i = 0; i < subfilePaths.length; i++) {
            Path subfilePath = subfilePaths[i];
            byte[] encryptedFileBytes = FileEncryptor.encryptFile(subfilePath, password);
            encryptedSubfilePaths[i] = Paths.get(subfilePath.toString() + ".enc");
            Files.write(encryptedSubfilePaths[i], encryptedFileBytes);
        }

        return encryptedSubfilePaths;
    }

    private Path[] decryptSubfiles(Path[] encryptedSubfilePaths, String password) throws Exception {
        Path[] decryptedSubfilePaths = new Path[encryptedSubfilePaths.length];

        for (int i = 0; i < encryptedSubfilePaths.length; i++) {
            Path encryptedSubfilePath = encryptedSubfilePaths[i];
            byte[] decryptedFileBytes = FileDecryptor.decryptFile(encryptedSubfilePath, password);
            decryptedSubfilePaths[i] = Paths.get(encryptedSubfilePath.toString() + ".dec");
            Files.write(decryptedSubfilePaths[i], decryptedFileBytes);
        }

        return decryptedSubfilePaths;
    }

    private int numberOfSubfiles(long originalSize) {
        return (int) Math.ceil(originalSize / MAX_SUBFILE_SIZE_IN_BYTES);
    }

    private Path createTempDirectoryDeleteOnExit() throws IOException {
        Path tempFilePath = Files.createTempDirectory("transporter");
//        recursiveDeleteOnShutdownHook(tempFilePath);
        return tempFilePath;
    }

    private void recursiveDeleteOnShutdownHook(final Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file,
                                                                 @SuppressWarnings("unused") BasicFileAttributes attrs)
                                        throws IOException {
                                    Files.delete(file);
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                                        throws IOException {
                                    if (e == null) {
                                        Files.delete(dir);
                                        return FileVisitResult.CONTINUE;
                                    }
                                    // directory iteration failed
                                    throw e;
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    }
                }));
    }
}
