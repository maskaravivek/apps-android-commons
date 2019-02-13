package fr.free.nrw.commons.media;

import java.util.List;

import fr.free.nrw.commons.Media;
import io.reactivex.Single;

public interface MediaController {
    Single<List<? extends Media>> getMediaList(int offset);

    Single<Media> getMediaAtPosition(int position);
}
