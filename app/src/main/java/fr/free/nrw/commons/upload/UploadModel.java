package fr.free.nrw.commons.upload;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import fr.free.nrw.commons.CommonsApplication;
import fr.free.nrw.commons.auth.SessionManager;
import fr.free.nrw.commons.contributions.Contribution;
import fr.free.nrw.commons.settings.Prefs;
import fr.free.nrw.commons.utils.ImageUtils;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class UploadModel {
    private static UploadItem DUMMY = new UploadItem(Uri.EMPTY, "", "", GPSExtractor.DUMMY);
    private final SharedPreferences prefs;
    private final List<String> licenses;
    private String license;
    private final Map<String, String> licensesByName;
    private List<UploadItem> items = Collections.emptyList();
    private boolean topCardState = true;
    private boolean bottomCardState = true;
    private int currentStepIndex = 0;
    private Context context;
    private ContentResolver contentResolver;
    private boolean useExtStorage;


    @Inject
    SessionManager sessionManager;

    @Inject
    UploadModel(@Named("licenses") List<String> licenses,
                @Named("default_preferences") SharedPreferences prefs,
                @Named("licenses_by_name") Map<String, String> licensesByName,
                Context context) {
        this.licenses = licenses;
        this.prefs = prefs;
        this.license = Prefs.Licenses.CC_BY_SA_3;
        this.licensesByName = licensesByName;
        this.context = context;
        this.contentResolver = context.getContentResolver();
        useExtStorage = prefs.getBoolean("useExternalStorage", true);
    }

    @SuppressLint("CheckResult")
    public void receive(List<Uri> mediaUri, String mimeType, String source) {
        currentStepIndex = 0;
        Observable<UploadItem> itemObservable = Observable.fromIterable(mediaUri)
                .map(this::cacheFileUpload)
                .map(uri->{
                    FileProcessor fp = new FileProcessor(uri, context.getContentResolver(), context);
                    return new UploadItem(uri, mimeType, source, fp.processFileCoordinates(false));
                });
        items=itemObservable.toList().blockingGet();
        items.get(0).selected = true;
        items.get(0).first = true;

//        Observable.fromIterable(items
//        ).observeOn(Schedulers.io()
//        ).subscribe(item -> item.imageQuality.onNext(ImageUtils.checkIfImageIsTooDark(item.mediaUri)));
    }

    public boolean isPreviousAvailable() {
        return currentStepIndex > 0;
    }

    public boolean isNextAvailable() {
        return currentStepIndex < (items.size() + 1);
    }

    public boolean isSubmitAvailable() {
        int count = items.size();
        boolean hasError = license == null;
        for (int i = 0; i < count; i++) {
            UploadItem item = items.get(i);
            hasError |= item.error;
        }
        return !hasError;
    }

    public int getCurrentStep() {
        return currentStepIndex + 1;
    }

    public int getStepCount() {
        return items.size() + 2;
    }

    public int getCount() {
        return items.size();
    }

    public List<UploadItem> getUploads() {
        return items;
    }

    public boolean isTopCardState() {
        return topCardState;
    }

    public void setTopCardState(boolean topCardState) {
        this.topCardState = topCardState;
    }

    public boolean isBottomCardState() {
        return bottomCardState;
    }

    public void setBottomCardState(boolean bottomCardState) {
        this.bottomCardState = bottomCardState;
    }

    public void next() {
        markCurrentUploadVisited();
        if (currentStepIndex < items.size() + 1) {
            currentStepIndex++;
        }
        updateItemState();
    }

    public void previous() {
        markCurrentUploadVisited();
        if (currentStepIndex > 0) {
            currentStepIndex--;
        }
        updateItemState();
    }

    public void jumpTo(UploadItem item) {
        currentStepIndex = items.indexOf(item);
        item.visited = true;
        updateItemState();
    }

    public UploadItem getCurrentItem() {
        return isShowingItem() ? items.get(currentStepIndex) : DUMMY;
    }

    public boolean isShowingItem() {
        return currentStepIndex < items.size();
    }

    private void updateItemState() {
        int count = items.size();
        for (int i = 0; i < count; i++) {
            UploadItem item = items.get(i);
            item.selected = (currentStepIndex >= count || i == currentStepIndex);
            item.error = item.title == null || item.title.trim().isEmpty();
        }
    }

    private void markCurrentUploadVisited() {
        if (currentStepIndex < items.size() && currentStepIndex >= 0) {
            items.get(currentStepIndex).visited = true;
        }
    }

    public List<String> getLicenses() {
        return licenses;
    }

    public String getSelectedLicense() {
        return license;
    }

    public void setSelectedLicense(String licenseName) {
        this.license = licensesByName.get(licenseName);
    }

    //When the EXIF modification UI is added, the modifications will be done here
    public Observable<Contribution> toContributions() {
        return Observable.fromIterable(items).map(item ->
                new Contribution(item.mediaUri, null, item.title, item.description, -1,
                        null, null, sessionManager.getCurrentAccount().name,
                        CommonsApplication.DEFAULT_EDIT_SUMMARY, item.gpsCoords.getCoords()));
    }

    private Uri cacheFileUpload(Uri media) {
        //Copy files into local storage and return URI
        try {
            String copyPath;
            ParcelFileDescriptor descriptor = contentResolver.openFileDescriptor(media, "r");
            if (descriptor != null) {
                if (useExtStorage)
                    copyPath=FileUtils.createExternalCopyPathAndCopy(descriptor);
                else
                    copyPath=FileUtils.createCopyPathAndCopy(descriptor);
                Timber.i("Parsed Uri is "+Uri.parse(copyPath).toString());
                return Uri.fromFile(new File(copyPath));
            }
        } catch (IOException e) {
        Timber.w(e, "Error in copying URI " + media.getPath());
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    static class UploadItem {
        public final Uri mediaUri;
        public final String mimeType;
        public final String source;
        public final GPSExtractor gpsCoords;

        public boolean selected = false;
        public boolean first = false;
//        public BehaviorSubject<ImageUtils.Result> imageQuality;
        public String title;
        public String description;
        public boolean visited;
        public boolean error;

        @SuppressLint("CheckResult")
        UploadItem(Uri mediaUri, String mimeType, String source, GPSExtractor gpsCoords) {
            this.mediaUri = mediaUri;
            this.mimeType = mimeType;
            this.source = source;
            this.gpsCoords = gpsCoords;
//            imageQuality=BehaviorSubject.createDefault(ImageUtils.Result.IMAGE_WAIT);
//            imageQuality.subscribe(iq->Timber.i("New value of imageQuality:"+ImageUtils.Result.IMAGE_OK));
        }
    }
}
