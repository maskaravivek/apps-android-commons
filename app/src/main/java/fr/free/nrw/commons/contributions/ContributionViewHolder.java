package fr.free.nrw.commons.contributions;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.free.nrw.commons.MediaWikiImageView;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.contributions.model.DisplayableContribution;
import fr.free.nrw.commons.di.ApplicationlessInjection;
import fr.free.nrw.commons.widget.RecyclerItemClickListener;
import fr.free.nrw.commons.widget.RecyclerViewHolder;

public class ContributionViewHolder extends
        RecyclerViewHolder<DisplayableContribution, RecyclerItemClickListener<DisplayableContribution>> {

    @BindView(R.id.contributionImage) MediaWikiImageView imageView;
    @BindView(R.id.contributionTitle) TextView titleView;
    @BindView(R.id.contributionState) TextView stateView;
    @BindView(R.id.contributionSequenceNumber) TextView seqNumView;
    @BindView(R.id.contributionProgress) ProgressBar progressView;
    @BindView(R.id.failed_image_options) LinearLayout failedImageOptions;

    private View itemView;
    private DisplayableContribution contribution;

    ContributionViewHolder(View parent) {
        super(parent);
        this.itemView = parent;
        ApplicationlessInjection
                .getInstance(parent.getContext()
                        .getApplicationContext())
                .getCommonsApplicationComponent()
                .inject(this);

        ButterKnife.bind(this, parent);
    }

    @Override
    public void onBind(DisplayableContribution contribution,
                       @Nullable RecyclerItemClickListener<DisplayableContribution> listener) {
        this.contribution = contribution;
        imageView.setMedia(contribution);
        titleView.setText(contribution.getDisplayTitle());

        seqNumView.setText(String.valueOf(contribution.getPosition() + 1));
        seqNumView.setVisibility(View.VISIBLE);

        switch (contribution.getState()) {
            case Contribution.STATE_COMPLETED:
                stateView.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);
                failedImageOptions.setVisibility(View.GONE);
                stateView.setText("");
                break;
            case Contribution.STATE_QUEUED:
                stateView.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.GONE);
                stateView.setText(R.string.contribution_state_queued);
                failedImageOptions.setVisibility(View.GONE);
                break;
            case Contribution.STATE_IN_PROGRESS:
                stateView.setVisibility(View.GONE);
                progressView.setVisibility(View.VISIBLE);
                failedImageOptions.setVisibility(View.GONE);
                long total = contribution.getDataLength();
                long transferred = contribution.getTransferred();
                if (transferred == 0 || transferred >= total) {
                    progressView.setIndeterminate(true);
                } else {
                    progressView.setProgress((int) (((double) transferred / (double) total) * 100));
                }
                break;
            case Contribution.STATE_FAILED:
                stateView.setVisibility(View.VISIBLE);
                stateView.setText(R.string.contribution_state_failed);
                progressView.setVisibility(View.GONE);
                failedImageOptions.setVisibility(View.VISIBLE);
                break;
        }

        if (listener != null) {
            itemView.setOnClickListener(v -> listener.onItemClicked(contribution));
        }
    }

    /**
     * Retry upload when it is failed
     */
    @OnClick(R.id.retryButton)
    public void retryUpload() {
        DisplayableContribution.ContributionActions actions = contribution.getContributionActions();
        if (actions != null) {
            actions.retryUpload();
        }
    }

    /**
     * Delete a failed upload attempt
     */
    @OnClick(R.id.cancelButton)
    public void deleteUpload() {
        DisplayableContribution.ContributionActions actions = contribution.getContributionActions();
        if (actions != null) {
            actions.deleteUpload();
        }
    }

    @OnClick(R.id.contributionImage)
    public void imageClicked(){
        DisplayableContribution.ContributionActions actions = contribution.getContributionActions();
        if (actions != null) {
            actions.onClick();
        }
    }
}
