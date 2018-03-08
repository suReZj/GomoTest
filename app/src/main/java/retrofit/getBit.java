package retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by zhangzijian on 2018/03/08.
 */

public interface getBit {
    @GET
    @Streaming
    Call<ResponseBody> download(@Url String url);
}
