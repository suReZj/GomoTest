package gson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dell88 on 2018/3/7 0007.
 */

public class ResultGson {
    @SerializedName("_id")
    @Expose
    private String mId;

    @SerializedName("createdAt")
    @Expose
    private String mCreatedAt;

    @SerializedName("desc")
    @Expose
    private String mDesc;

    @SerializedName("publishedAt")
    @Expose
    private String mPublishedAt;

    @SerializedName("source")
    @Expose
    private String mSource;

    @SerializedName("type")
    @Expose
    private String mType;

    @SerializedName("url")
    @Expose
    private String mUrl;

    @SerializedName("used")
    @Expose
    private boolean mUsed;

    @SerializedName("who")
    @Expose
    private String mWho;

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmCreatedAt() {
        return mCreatedAt;
    }

    public void setmCreatedAt(String mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public String getmDesc() {
        return mDesc;
    }

    public void setmDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public String getmPublishedAt() {
        return mPublishedAt;
    }

    public void setmPublishedAt(String mPublishedAt) {
        this.mPublishedAt = mPublishedAt;
    }

    public String getmSource() {
        return mSource;
    }

    public void setmSource(String mSource) {
        this.mSource = mSource;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public boolean ismUsed() {
        return mUsed;
    }

    public void setmUsed(boolean mUsed) {
        this.mUsed = mUsed;
    }

    public String getmWho() {
        return mWho;
    }

    public void setmWho(String mWho) {
        this.mWho = mWho;
    }
}
