package fr.free.nrw.commons.contributions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;
import fr.free.nrw.commons.Media;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.contributions.model.DisplayableContribution;
import fr.free.nrw.commons.media.MediaSource;
import fr.free.nrw.commons.media.MediaViewPagerActivity;
import fr.free.nrw.commons.upload.UploadService;
import fr.free.nrw.commons.utils.NetworkUtils;
import fr.free.nrw.commons.utils.ViewUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static fr.free.nrw.commons.contributions.Contribution.STATE_FAILED;

/**
 * Displays images for a particular category with load more on scrolling incorporated
 */
public class ContributionListFragment extends DaggerFragment {

    private static int TIMEOUT_SECONDS = 15;

    @BindView(R.id.contributionsList)
    RecyclerView recyclerView;
    @BindView(R.id.loadingContributionsProgressBar) ProgressBar progressBar;
    @BindView(R.id.fab_plus)
    FloatingActionButton fabPlus;
    @BindView(R.id.fab_camera) FloatingActionButton fabCamera;
    @BindView(R.id.fab_gallery) FloatingActionButton fabGallery;
    @BindView(R.id.waitingMessage) TextView waitingMessage;

    @Inject ContributionListController contributionListController;
    @Inject ContributionController controller;
    @Inject ContributionDao contributionDao;

    private UploadService uploadService;

    private ContributionListAdapter contributionListAdapter;
    private boolean hasMoreImages = true;
    private boolean isLoading = true;
    private int offset = 0;

    private Animation fab_close;
    private Animation fab_open;
    private Animation rotate_forward;
    private Animation rotate_backward;

    private boolean isFabOpen = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contributions_list, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    public void setUploadService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    private void initializeAnimations() {
        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);
    }

    private void setListeners() {
        fabPlus.setOnClickListener(view -> animateFAB(isFabOpen));
        fabCamera.setOnClickListener(view -> controller.initiateCameraPick(getActivity()));
        fabGallery.setOnClickListener(view -> controller.initiateGalleryPick(getActivity(), true));
    }

    private void animateFAB(boolean isFabOpen) {
        this.isFabOpen = !isFabOpen;
        if (fabPlus.isShown()) {
            if (isFabOpen) {
                fabPlus.startAnimation(rotate_backward);
                fabCamera.startAnimation(fab_close);
                fabGallery.startAnimation(fab_close);
                fabCamera.hide();
                fabGallery.hide();
            } else {
                fabPlus.startAnimation(rotate_forward);
                fabCamera.startAnimation(fab_open);
                fabGallery.startAnimation(fab_open);
                fabCamera.show();
                fabGallery.show();
            }
            this.isFabOpen = !isFabOpen;
        }
    }

    /**
     * Initializes the UI elements for the fragment
     * Setup the grid view to and scroll listener for it
     */
    private void initViews() {
        initRecyclerView();
        setScrollListener();
        initializeAnimations();
        setListeners();
        initList();
    }

    private void initRecyclerView() {
        contributionListAdapter = new ContributionListAdapter(getActivity());
        contributionListAdapter.setListener(item -> {
            Timber.d("Contribution clicked %s", item.toString());
            MediaViewPagerActivity.startYourself(getContext(), MediaSource.CONTRIBUTIONS, item.getPosition());
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contributionListAdapter);
    }

    /**
     * Checks for internet connection and then initializes the grid view with first 10 images of that category
     */
    @SuppressLint("CheckResult")
    private void initList() {
        fetchImages();
    }

    /**
     * Handles the UI updates for no internet scenario
     */
    private void handleNoInternet() {
        progressBar.setVisibility(GONE);
        if (contributionListAdapter == null || contributionListAdapter.isEmpty()) {
            waitingMessage.setVisibility(VISIBLE);
            waitingMessage.setText(getString(R.string.no_internet));
        } else {
            ViewUtil.showShortSnackbar(recyclerView, R.string.no_internet);
        }
    }

    /**
     * Logs and handles API error scenario
     *
     * @param throwable
     */
    private void handleError(Throwable throwable) {
        Timber.e(throwable, "Error occurred while loading images inside a category");
        try {
            ViewUtil.showShortSnackbar(recyclerView, R.string.error_loading_images);
            initErrorView();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Handles the UI updates for a error scenario
     */
    private void initErrorView() {
        progressBar.setVisibility(GONE);
        if (contributionListAdapter == null || contributionListAdapter.isEmpty()) {
            waitingMessage.setVisibility(VISIBLE);
            waitingMessage.setText(getString(R.string.no_images_found));
        } else {
            waitingMessage.setVisibility(GONE);
        }
    }

    /**
     * Sets the scroll listener for the grid view so that more images are fetched when the user scrolls down
     * Checks if the category has more images before loading
     * Also checks whether images are currently being fetched before triggering another request
     */
    private void setScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (hasMoreImages && !isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition()
                            == contributionListAdapter.getItemCount() - 1) {
                        fetchMoreImages();
                    }
                }
                if (!hasMoreImages) {
                    progressBar.setVisibility(GONE);
                }
            }
        });
    }

    /**
     * Fetches more images for the category and adds it to the grid view adapter
     */
    @SuppressLint("CheckResult")
    private void fetchMoreImages() {
        if (!NetworkUtils.isInternetConnectionEstablished(getContext())) {
            handleNoInternet();
            return;
        }

        fetchImages();
    }

    @SuppressLint("CheckResult")
    private void fetchImages() {
        isLoading = true;
        progressBar.setVisibility(VISIBLE);
        contributionListController.getMediaList(offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .subscribe(this::handleSuccess, this::handleError);
    }

    /**
     * Handles the success scenario
     * On first load, it initializes the grid view. On subsequent loads, it adds items to the adapter
     *
     * @param collection List of new Media to be displayed
     */
    private void handleSuccess(List<? extends Media> collection) {
        if (collection == null || collection.isEmpty()) {
            initErrorView();
            hasMoreImages = false;
            return;
        }

        if (collection.size() < 10) {
            hasMoreImages = false;
            return;
        }

        List<DisplayableContribution> displayableContributions = new ArrayList<>();
        int counter = 0;
        for (Media media : collection) {
            Contribution contribution = (Contribution) media;
            DisplayableContribution displayableContribution = new DisplayableContribution(contribution, offset + counter,
                    new DisplayableContribution.ContributionActions() {
                        @Override
                        public void retryUpload() {
                            ContributionListFragment.this.retryUpload(contribution);
                        }

                        @Override
                        public void deleteUpload() {
                            ContributionListFragment.this.deleteUpload(contribution);
                        }

                        @Override
                        public void onClick() {

                        }
                    });
            displayableContributions.add(displayableContribution);
            counter++;
        }

        offset += collection.size();
        contributionListAdapter.addAll(displayableContributions);
        contributionListAdapter.notifyDataSetChanged();

        progressBar.setVisibility(GONE);
        isLoading = false;
        waitingMessage.setVisibility(GONE);
        Timber.d("Count is %d", recyclerView.getAdapter().getItemCount());
    }

    /**
     * Retry upload when it is failed
     *
     * @param contribution contribution to be retried
     */
    private void retryUpload(Contribution contribution) {
        if (NetworkUtils.isInternetConnectionEstablished(getContext())) {
            if (contribution.getState() == STATE_FAILED
                    && uploadService != null) {
                uploadService.queue(UploadService.ACTION_UPLOAD_FILE, contribution);
                Timber.d("Restarting for %s", contribution.toString());
            } else {
                Timber.d("Skipping re-upload for non-failed %s", contribution.toString());
            }
        } else {
            ViewUtil.showLongToast(getContext(), R.string.this_function_needs_network_connection);
        }
    }

    /**
     * Delete a failed upload attempt
     *
     * @param contribution contribution to be deleted
     */
    private void deleteUpload(Contribution contribution) {
        if (NetworkUtils.isInternetConnectionEstablished(getContext())) {
            if (contribution.getState() == STATE_FAILED) {
                Timber.d("Deleting failed contrib %s", contribution.toString());
                contributionDao.delete(contribution);
            } else {
                Timber.d("Skipping deletion for non-failed contrib %s", contribution.toString());
            }
        } else {
            ViewUtil.showLongToast(getContext(), R.string.this_function_needs_network_connection);
        }
    }
}