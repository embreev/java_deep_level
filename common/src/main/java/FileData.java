import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileData extends AbstractMessage {
    private String filename;
    private byte[] data;

    public String getFileName() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public FileData(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
