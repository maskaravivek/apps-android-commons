package fr.free.nrw.commons.contributions;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.free.nrw.commons.Media;
import fr.free.nrw.commons.media.MediaController;
import io.reactivex.Single;

@Singleton
public class ContributionListController implements MediaController {

    private ContributionDao contributionDao;

    @Inject
    ContributionListController(ContributionDao contributionDao) {
        this.contributionDao = contributionDao;
    }

    /**
     * Takes a category name as input and calls the API to get a list of images for that category
     *
     * @param offset
     * @return
     */
    public Single<List<? extends Media>> getMediaList(int offset) {
        return Single.fromCallable(() -> contributionDao.loadContributions(offset));
    }

    @Override
    public Single<Media> getMediaAtPosition(int position) {
        return null;
    }
}