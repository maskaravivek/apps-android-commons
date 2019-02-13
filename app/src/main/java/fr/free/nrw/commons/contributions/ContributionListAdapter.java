package fr.free.nrw.commons.contributions;

import android.content.Context;
import android.view.ViewGroup;

import fr.free.nrw.commons.R;
import fr.free.nrw.commons.contributions.model.DisplayableContribution;
import fr.free.nrw.commons.widget.RecyclerAdapter;
import fr.free.nrw.commons.widget.RecyclerItemClickListener;

public class ContributionListAdapter extends RecyclerAdapter<DisplayableContribution,
        RecyclerItemClickListener<DisplayableContribution>, ContributionViewHolder> {

    /**
     * Base constructor.
     * Allocate adapter-related objects here if needed.
     *
     * @param context Context needed to retrieve LayoutInflater
     */
    public ContributionListAdapter(Context context) {
        super(context);
    }

    /**
     * To be implemented in as specific adapter
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @Override
    public ContributionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContributionViewHolder(inflate(R.layout.layout_contribution, parent));
    }
}
