import java.util.Set;

public class FilesListRequest extends AbstractMessage {
    private Set<String> filesList;

    public Set<String> getFilesList() {
        return filesList;
    }

    public FilesListRequest(Set<String> filesList) {
        this.filesList = filesList;
    }
}
