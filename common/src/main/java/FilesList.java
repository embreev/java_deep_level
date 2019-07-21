import java.util.Set;

public class FilesList extends AbstractMessage {
    private Set<String> filesList;

    public Set<String> getFilesList() {
        return filesList;
    }

    public FilesList(Set<String> filesList) {
        this.filesList = filesList;
    }
}
