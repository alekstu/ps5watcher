package fi.alekstu.ps5watch;

public class ListedConsole {

    private final String name;
    private final String url;

    public ListedConsole(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ConsoleLink{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
