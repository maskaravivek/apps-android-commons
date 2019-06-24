package fr.free.nrw.commons.category;

import org.wikipedia.dataclient.mwapi.MwQueryResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CategoryInterface {
    @GET("w/api.php?action=query&format=json&formatversion=2&list=geosearch&gsprimary=all&gsnamespace=14&gsradius=10000")
    Observable<MwQueryResponse> getGpsCategories(@Query("gscoord") String coords);
}
