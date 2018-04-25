package gson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by dell88 on 2018/3/7 0007.
 */

public class WelfareGson {
    @SerializedName("error")
    @Expose
    private String error;

    @SerializedName("results")
    @Expose
    private List<ResultGson> results;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<ResultGson> getResults() {
        return results;
    }

    public void setResults(List<ResultGson> results) {
        this.results = results;
    }
}
