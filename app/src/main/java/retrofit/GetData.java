package retrofit;

import gson.WelfareGson;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by dell88 on 2018/3/7 0007.
 */

public interface GetData {
    @GET("福利/{number}/{page}")
    Observable<WelfareGson> getWelfare(@Path("number") String number, @Path("page") Integer page);
}
