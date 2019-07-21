public class Command extends AbstractMessage {
    private String command;
    private String itemName;

    public String getCommand() {
        return command;
    }

    public String getItemName() {
        return itemName;
    }

    public Command(String command) {
        this.command = command;
    }

    public Command(String command, String itemName) {
        this.command = command;
        this.itemName = itemName;
    }
}
