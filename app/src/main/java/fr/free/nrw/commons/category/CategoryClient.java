package fr.free.nrw.commons.category;

import org.wikipedia.dataclient.mwapi.GeoSearchItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.free.nrw.commons.location.LatLng;
import io.reactivex.Observable;

@Singleton
public class CategoryClient {

    private final CategoryInterface categoryInterface;

    @Inject
    public CategoryClient(CategoryInterface categoryInterface) {
        this.categoryInterface = categoryInterface;
    }

    public Observable<List<String>> getGpsCategorySuggestions(LatLng latLng) {
        return categoryInterface.getGpsCategories(latLng.getLatitude() + "|" + latLng.getLatitude())
                .map(mwQueryResponse -> {
                    List<String> categories = new ArrayList<>();
                    List<GeoSearchItem> geoSearchItems = mwQueryResponse.query().geoSearch();
                    for (GeoSearchItem item : geoSearchItems) {
                        categories.add(item.getTitle().replace("Category:", ""));
                    }
                    return categories;
                });
    }
}
