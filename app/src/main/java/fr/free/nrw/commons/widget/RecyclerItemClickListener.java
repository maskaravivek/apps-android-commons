package fr.free.nrw.commons.widget;

public interface RecyclerItemClickListener<T> extends BaseRecyclerListener {
    /**
     * Item has been clicked.
     *
     * @param item object associated with the clicked item.
     */
    void onItemClicked(T item);
}