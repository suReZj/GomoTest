package retrofit;

import gson.gson_welfare;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by dell88 on 2018/3/7 0007.
 */

public interface getData {
    @GET("福利/{number}/{page}")
    Observable<gson_welfare> getWelfare(@Path("number") String number, @Path("page") String page);
}
