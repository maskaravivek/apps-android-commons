package fr.free.nrw.commons;

import android.annotation.SuppressLint;

import org.wikipedia.csrf.CsrfTokenClient;
import org.wikipedia.dataclient.Service;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import fr.free.nrw.commons.upload.FileUtils;
import fr.free.nrw.commons.wikidata.WikidataClient;
import io.reactivex.Observable;
import timber.log.Timber;

import static fr.free.nrw.commons.di.NetworkingModule.NAMED_COMMONS_CSRF;

@Singleton
public class SampleService {

    public final WikidataClient wikidataClient;
    public final Service service;
    public final CsrfTokenClient csrfTokenClient;

    @Inject
    public SampleService(WikidataClient wikidataClient,
                         @Named("commons-service") Service service,
                         @Named(NAMED_COMMONS_CSRF) CsrfTokenClient csrfTokenClient) {
        this.wikidataClient = wikidataClient;
        this.service = service;
        this.csrfTokenClient = csrfTokenClient;
    }


    @SuppressLint("CheckResult")
    public Observable<String> sampleMethod1() throws IOException {
        String fileContent = FileUtils.readFromResource("/queries/nearby_query.rq");
        return wikidataClient.createClaim("Q1", fileContent)
                .flatMap(result -> {
                    try {
                        return service.thank("test", "commons app",
                                csrfTokenClient.getTokenBlocking(),
                                String.valueOf(result));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return null;
                    }
                }).map(mwPostResponse -> mwPostResponse.getOptions());
    }

    @SuppressLint("CheckResult")
    public void sampleMethod2(String fileContent) throws IOException {
        wikidataClient.createClaim("Q1", "test value")
                .flatMap(result -> {
                    try {
                        return service.thank("test", fileContent,
                                csrfTokenClient.getTokenBlocking(),
                                String.valueOf(result));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return null;
                    }
                }).subscribe(mwPostResponse -> Timber.d("Response is %s", mwPostResponse.getOptions()));
    }

    @SuppressLint("CheckResult")
    public void sampleMethod3() throws IOException {
        int result = 500 / 0;
        Timber.d("Result is %d", result);
    }

    @SuppressLint("CheckResult")
    public String sampleMethod4() throws Throwable {
        return csrfTokenClient.getTokenBlocking();
    }
}
