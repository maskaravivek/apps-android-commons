package fr.free.nrw.commons.widget;

import android.view.View;

import javax.annotation.Nullable;

import androidx.recyclerview.widget.RecyclerView;

public abstract class RecyclerViewHolder<T, L extends BaseRecyclerListener> extends RecyclerView.ViewHolder {

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * Bind data to the item and set listener if needed.
     *
     * @param item     object, associated with the item.
     * @param listener listener a listener {@link BaseRecyclerListener} which has to b set at the item (if not `null`).
     */
    public abstract void onBind(T item, @Nullable L listener);
}