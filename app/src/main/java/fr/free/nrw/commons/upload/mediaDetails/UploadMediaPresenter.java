package fr.free.nrw.commons.upload.mediaDetails;

import static fr.free.nrw.commons.utils.ImageUtils.EMPTY_TITLE;
import static fr.free.nrw.commons.utils.ImageUtils.FILE_NAME_EXISTS;
import static fr.free.nrw.commons.utils.ImageUtils.IMAGE_KEEP;
import static fr.free.nrw.commons.utils.ImageUtils.IMAGE_OK;

import fr.free.nrw.commons.R;
import fr.free.nrw.commons.filepicker.UploadableFile;
import fr.free.nrw.commons.nearby.Place;
import fr.free.nrw.commons.repository.UploadRepository;
import fr.free.nrw.commons.upload.SimilarImageInterface;
import fr.free.nrw.commons.upload.UploadModel.UploadItem;
import fr.free.nrw.commons.upload.mediaDetails.UploadMediaDetailsContract.UserActionListener;
import fr.free.nrw.commons.upload.mediaDetails.UploadMediaDetailsContract.View;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.lang.reflect.Proxy;
import javax.inject.Inject;
import timber.log.Timber;

public class UploadMediaPresenter implements UserActionListener, SimilarImageInterface {

    private static final UploadMediaDetailsContract.View DUMMY = (UploadMediaDetailsContract.View) Proxy
            .newProxyInstance(
                    UploadMediaDetailsContract.View.class.getClassLoader(),
                    new Class[]{UploadMediaDetailsContract.View.class},
                    (proxy, method, methodArgs) -> null);

    private final UploadRepository repository;
    private UploadMediaDetailsContract.View view = DUMMY;

    private CompositeDisposable compositeDisposable;

    @Inject
    public UploadMediaPresenter(UploadRepository uploadRepository) {
        this.repository = uploadRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onAttachView(View view) {
        this.view = view;
    }

    @Override
    public void onDetachView() {
        this.view = DUMMY;
        compositeDisposable.clear();
    }

    @Override
    public void receiveImage(UploadableFile uploadableFile, String source, Place place) {
        view.showProgress(true);

        Disposable uploadItemDisposable = repository
                .preProcessImage(uploadableFile, place, source, this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uploadItem ->
                        {
                            view.onImageProcessed(uploadItem, place);
                            view.showProgress(false);
                        },
                        throwable -> Timber.e(throwable, "Error occurred in processing images"));
        compositeDisposable.add(uploadItemDisposable);
    }

    @Override
    public void verifyImageQuality(UploadItem uploadItem, boolean validateTitle) {
        view.showProgress(true);
        Disposable imageQualityDisposable = repository
                .getImageQuality(uploadItem, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageResult -> {
                            view.showProgress(false);
                            handleImageResult(imageResult);
                        },
                        throwable -> {
                            view.showProgress(false);
                            view.showMessage("" + throwable.getLocalizedMessage(),
                                    R.color.color_error);
                            Timber.e(throwable, "Error occurred while handling image");
                        });

        compositeDisposable.add(imageQualityDisposable);
    }

    @Override
    public void setUploadItem(int index, UploadItem uploadItem) {
        repository.updateUploadItem(index, uploadItem);
    }

    private void handleImageResult(Integer imageResult) {
        if (imageResult == IMAGE_KEEP || imageResult == IMAGE_OK) {
            view.onImageValidationSuccess();
        } else {
            handleBadImage(imageResult);
        }
    }

    private void handleBadImage(Integer errorCode) {
        Timber.d("Handle bad picture with error code %d", errorCode);
        if (errorCode
                >= 8) { // If location of image and nearby does not match, then set shared preferences to disable wikidata edits
            repository.saveInDirectKvStore("Picture_Has_Correct_Location", false);
        }

        switch (errorCode) {
            case EMPTY_TITLE:
                Timber.d("Title is empty. Showing toast");
                view.showMessage(R.string.add_title_toast, R.color.color_error);
                break;
            case FILE_NAME_EXISTS:
                Timber.d("Trying to show duplicate picture popup");
                view.showDuplicatePicturePopup();
                break;
            default:
                view.showBadImagePopup(errorCode);
        }
    }

    @Override
    public void showSimilarImageFragment(String originalFilePath, String possibleFilePath) {
        view.showSimilarImageFragment(originalFilePath, possibleFilePath);
    }
}