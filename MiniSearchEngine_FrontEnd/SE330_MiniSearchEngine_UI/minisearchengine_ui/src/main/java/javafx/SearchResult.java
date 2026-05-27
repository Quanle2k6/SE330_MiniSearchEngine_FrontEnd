package javafx;

public class SearchResult {
    private String context;
    private String summary;
    private String title;
    private String url;

    private Long rank;
    private Long index;

    public SearchResult(String context, String summary, String title, String url, Long rank, Long index) {
        this.context = context;
        this.summary = summary;
        this.title = title;
        this.url = url;
        this.rank = rank;
        this.index = index;
    }
    //Getters
    public String getContext() {
        return context;
    }
    public String getSummary() {
        return summary;
    }
    public String getTitle() {
        return title;
    }
    public String getUrl() {
        return url;
    }
    public Long getRank() {
        return rank;
    }
    public Long getIndex() {
        return index;
    }


}

