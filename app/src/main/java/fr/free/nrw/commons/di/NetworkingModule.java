package fr.free.nrw.commons.di;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.wikipedia.dataclient.ServiceFactory;
import org.wikipedia.dataclient.WikiSite;
import org.wikipedia.dataclient.okhttp.HttpStatusException;
import org.wikipedia.json.GsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fr.free.nrw.commons.BuildConfig;
import fr.free.nrw.commons.CommonsApplication;
import fr.free.nrw.commons.kvstore.JsonKvStore;
import fr.free.nrw.commons.mwapi.ApacheHttpClientMediaWikiApi;
import fr.free.nrw.commons.mwapi.MediaWikiApi;
import fr.free.nrw.commons.mwapi.OkHttpJsonApiClient;
import fr.free.nrw.commons.review.ReviewInterface;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

@Module
@SuppressWarnings({"WeakerAccess", "unused"})
public class NetworkingModule {
    private static final String WIKIDATA_SPARQL_QUERY_URL = "https://query.wikidata.org/sparql";
    private static final String TOOLS_FORGE_URL = "https://tools.wmflabs.org/urbanecmbot/commonsmisc";

    private static final String TEST_TOOLS_FORGE_URL = "https://tools.wmflabs.org/commons-android-app/tool-commons-android-app";

    private static final String CACHE_DIR_NAME = "okhttp-cache";
    private static final long NET_CACHE_SIZE = 64 * 1024 * 1024;
    @NonNull
    private static final Cache NET_CACHE = new Cache(new File(CommonsApplication.getInstance().getCacheDir(),
            CACHE_DIR_NAME), NET_CACHE_SIZE);

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(Context context,
                                            HttpLoggingInterceptor httpLoggingInterceptor) {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .cache(NET_CACHE)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new UnsuccessfulResponseInterceptor())
                .addInterceptor(new CommonHeaderRequestInterceptor())
                .build();
    }

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> {
            Timber.tag("OkHttp").v(message);
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    private static class CommonHeaderRequestInterceptor implements Interceptor {
        @Override
        @NonNull
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                    .header("User-Agent", CommonsApplication.getInstance().getUserAgent())
                    .build();
            return chain.proceed(request);
        }
    }

    public static class UnsuccessfulResponseInterceptor implements Interceptor {
        @Override
        @NonNull
        public Response intercept(@NonNull Chain chain) throws IOException {
            Response rsp = chain.proceed(chain.request());
            if (rsp.isSuccessful()) {
                return rsp;
            }
            throw new HttpStatusException(rsp);
        }
    }

    @Provides
    @Singleton
    public MediaWikiApi provideMediaWikiApi(Context context,
                                            @Named("default_preferences") JsonKvStore defaultKvStore,
                                            Gson gson) {
        return new ApacheHttpClientMediaWikiApi(context, BuildConfig.WIKIMEDIA_API_HOST, BuildConfig.WIKIDATA_API_HOST, defaultKvStore, gson);
    }

    @Provides
    @Singleton
    public OkHttpJsonApiClient provideOkHttpJsonApiClient(OkHttpClient okHttpClient,
                                                          @Named("tools_forge") HttpUrl toolsForgeUrl,
                                                          @Named("default_preferences") JsonKvStore defaultKvStore,
                                                          Gson gson) {
        return new OkHttpJsonApiClient(okHttpClient,
                toolsForgeUrl,
                WIKIDATA_SPARQL_QUERY_URL,
                BuildConfig.WIKIMEDIA_CAMPAIGNS_URL,
                BuildConfig.WIKIMEDIA_API_HOST,
                defaultKvStore,
                gson);
    }

    @Provides
    @Named("wikimedia_api_host")
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public String provideMwApiUrl() {
        return BuildConfig.WIKIMEDIA_API_HOST;
    }

    @Provides
    @Named("tools_forge")
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public HttpUrl provideToolsForgeUrl() {
        return HttpUrl.parse(TOOLS_FORGE_URL);
    }

    /**
     * Gson objects are very heavy. The app should ideally be using just one instance of it instead of creating new instances everywhere.
     * @return returns a singleton Gson instance
     */
    @Provides
    @Singleton
    public Gson provideGson() {
        return GsonUtil.getDefaultGson();
    }

    @Provides
    @Singleton
    @Named("commons-wikisite")
    public WikiSite provideCommonsWikiSite() {
        return new WikiSite(BuildConfig.COMMONS_URL);
    }

    @Provides
    @Singleton
    public ReviewInterface provideReviewInterface(@Named("commons-wikisite") WikiSite commonsWikiSite) {
        return ServiceFactory.get(commonsWikiSite, BuildConfig.COMMONS_URL, ReviewInterface.class);
    }
}
