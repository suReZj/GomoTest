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
    private String mError;

    @SerializedName("results")
    @Expose
    private List<ResultGson> mResults;

    public String getmError() {
        return mError;
    }

    public void setmError(String mError) {
        this.mError = mError;
    }

    public List<ResultGson> getmResults() {
        return mResults;
    }

    public void setmResults(List<ResultGson> mResults) {
        this.mResults = mResults;
    }
}
