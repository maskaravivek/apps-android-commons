package fr.free.nrw.commons.contributions;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import fr.free.nrw.commons.Media;
import fr.free.nrw.commons.auth.SessionManager;
import fr.free.nrw.commons.contributions.ContributionsListContract.UserActionListener;
import fr.free.nrw.commons.di.CommonsApplicationModule;
import fr.free.nrw.commons.media.MediaClient;
import fr.free.nrw.commons.utils.NetworkUtils;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import timber.log.Timber;

/**
 * The presenter class for Contributions
 */
public class ContributionsListPresenter implements UserActionListener {

  private final ContributionsRepository repository;
  private final Scheduler mainThreadScheduler;
  private final Scheduler ioThreadScheduler;
  private final SessionManager sessionManager;
  private final MediaClient mediaClient;

  private CompositeDisposable compositeDisposable;
  private ContributionsListContract.View view;
  private LifecycleOwner lifeCycleOwner;
  private String user;
  private static final int PAGE_SIZE = 10;
  private static final int START_LOADING_SIZE = 5;
  private boolean isLoading;
  private boolean isLastPage;

  public final LiveData<PagedList<Contribution>> contributionList;

  @Inject
  ContributionsListPresenter(ContributionsRepository repository,
      @Named(CommonsApplicationModule.MAIN_THREAD) Scheduler mainThreadScheduler,
      @Named(CommonsApplicationModule.IO_THREAD) Scheduler ioThreadScheduler,
      SessionManager sessionManager,
      MediaClient mediaClient) {
    this.repository = repository;
    this.mainThreadScheduler = mainThreadScheduler;
    this.ioThreadScheduler = ioThreadScheduler;
    this.sessionManager = sessionManager;
    this.mediaClient = mediaClient;
    contributionList = new LivePagedListBuilder<>(repository.fetchContributions(), 50).build();
    compositeDisposable = new CompositeDisposable();
  }

  @Override
  public void onAttachView(ContributionsListContract.View view) {
    this.view = view;
  }

  public void setLifeCycleOwner(LifecycleOwner lifeCycleOwner) {
    this.lifeCycleOwner = lifeCycleOwner;
  }

  public OnScrollListener getScrollListener(LinearLayoutManager layoutManager,
      Context context) {
    return new OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
      }

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        final int visibleItemCount = layoutManager.getChildCount();
        final int totalItemCount = layoutManager.getItemCount();
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        if (!isLoading && !isLastPage) {
          if ((visibleItemCount + firstVisibleItemPosition + START_LOADING_SIZE) >= totalItemCount
              && firstVisibleItemPosition >= 0
              && totalItemCount >= PAGE_SIZE) {
            loadMoreItems(context);
          }
        }
      }
    };
  }

  public void fetchContributions(Context context) {
    if (NetworkUtils.isInternetConnectionEstablished(context)) {
      user = sessionManager.getUserName();
      compositeDisposable.add(mediaClient.getMediaListForUser(user)
          .map(mediaList -> {
            List<Contribution> contributions = new ArrayList<>();
            for (Media media : mediaList) {
              contributions.add(new Contribution(media, Contribution.STATE_COMPLETED));
            }
            return contributions;
          })
          .subscribe(results -> {
            isLoading = false;
            saveContributionsToDB(results);
          }, error -> {
            Timber.e("Failed to fetch contributions: %s", error.getMessage());
          }));
    }
  }

  private void loadMoreItems(Context context) {
    isLoading = true;
    fetchContributions(context);
  }

  void setupLiveData() {
    if (null != lifeCycleOwner) {
      contributionList.observe(lifeCycleOwner, contributions -> showContributions(contributions));
    }
  }

  private void showContributions(@NonNull final List<Contribution> contributions) {
    view.showProgress(false);
    if (contributions.isEmpty()) {
      view.showWelcomeTip(true);
      view.showNoContributionsUI(true);
    } else {
      view.showWelcomeTip(false);
      view.showNoContributionsUI(false);
      view.showContributions(contributions);
    }
  }

  private void saveContributionsToDB(final List<Contribution> contributions) {
    repository.save(contributions);
    repository.set("last_fetch_timestamp", System.currentTimeMillis());
  }

  @Override
  public void onDetachView() {
    this.view = null;
    compositeDisposable.clear();
  }

  /**
   * Delete a failed contribution from the local db
   *
   * @param contribution
   */
  @Override
  public void deleteUpload(Contribution contribution) {
    compositeDisposable.add(repository.deleteContributionFromDB(contribution)
        .subscribeOn(ioThreadScheduler)
        .subscribe());
  }

}
