package util;

import java.util.ArrayList;
import java.util.List;

import gson.gson_result;
import gson.gson_welfare;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit.getData;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dell88 on 2018/2/7 0007.
 */

public class RetrofitUtil {
    public static List<String> path=new ArrayList<>();
    public static Retrofit getRetrofit(String url,String page){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
//        getData getData=retrofit.create(getData.class);
//        getData.getWelfare("10",page)
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Observer<gson_welfare>() {
//                    private Disposable disposable;
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        disposable = d;
//                    }
//
//                    @Override
//                    public void onNext(gson_welfare value) {
//                        List<String> images=new ArrayList<>();
//                        List<gson_result> results=new ArrayList<>();
//                        results=value.getResults();
//                        for(int i=0;i<results.size();i++){
//                            images.add(results.get(i).getUrl());
//                        }
//                        path=images;
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        disposable.dispose();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
        return retrofit;
    }
}
