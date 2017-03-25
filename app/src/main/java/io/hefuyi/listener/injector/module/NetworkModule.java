package io.hefuyi.listener.injector.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.Constants;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.injector.scope.PerApplication;
import io.hefuyi.listener.respository.RepositoryImpl;
import io.hefuyi.listener.respository.interfaces.Repository;
import io.hefuyi.listener.util.FileUtil;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hefuyi on 2016/11/1.
 */
@Module
public class NetworkModule {
    private final ListenerApp mListenerApp;

    public NetworkModule(ListenerApp listenerApp) {
        this.mListenerApp = listenerApp;
    }

    @Provides
    @PerApplication
    Repository provideRepository(@Named("kugou") Retrofit kugou, @Named("lastfm") Retrofit lastfm) {
        return new RepositoryImpl(mListenerApp, kugou, lastfm);
    }

    @Provides
    @Named("lastfm")
    @PerApplication
    Retrofit provideLastFMRetrofit(){
        String endpointUrl = Constants.BASE_API_URL_LASTFM;
        Gson gson = new GsonBuilder().create();
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(FileUtil.getHttpCacheDir(mListenerApp.getApplicationContext()), Constants.HTTP_CACHE_SIZE))
                .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
//        OkHttpClient newClient = client.newBuilder().addInterceptor(loggingInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpointUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit;
    }

    @Provides
    @Named("kugou")
    @PerApplication
    Retrofit provideKuGouRetrofit(){
        String endpointUrl = Constants.BASE_API_URL_KUGOU;
        Gson gson = new GsonBuilder().create();
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(new Cache(FileUtil.getHttpCacheDir(mListenerApp.getApplicationContext()), Constants.HTTP_CACHE_SIZE))
                .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
//        OkHttpClient newClient = client.newBuilder().addInterceptor(loggingInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpointUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit;
    }

}
