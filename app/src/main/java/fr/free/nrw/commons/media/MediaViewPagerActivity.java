package fr.free.nrw.commons.media;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.free.nrw.commons.Media;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.Utils;
import fr.free.nrw.commons.bookmarks.Bookmark;
import fr.free.nrw.commons.bookmarks.pictures.BookmarkPicturesDao;
import fr.free.nrw.commons.contributions.Contribution;
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment;
import fr.free.nrw.commons.explore.SearchActivity;
import fr.free.nrw.commons.explore.categories.ExploreActivity;
import fr.free.nrw.commons.kvstore.JsonKvStore;
import fr.free.nrw.commons.mwapi.MediaWikiApi;
import fr.free.nrw.commons.di.CommonsDaggerAppCompatActivity;
import fr.free.nrw.commons.utils.ImageUtils;
import fr.free.nrw.commons.utils.NetworkUtils;
import fr.free.nrw.commons.utils.ViewUtil;
import timber.log.Timber;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MediaViewPagerActivity extends CommonsDaggerAppCompatActivity implements ViewPager.OnPageChangeListener {

    @Inject
    BookmarkPicturesDao bookmarkDao;

    @BindView(R.id.mediaDetailsPager)
    ViewPager pager;

    MediaDetailAdapter adapter;
    private Boolean editable;
    private boolean isFeaturedImage;
    private Bookmark bookmark;

    public MediaViewPagerActivity() {
    }

    /**
     * Creates a way to change current activity to WelcomeActivity
     *
     * @param context Activity context
     */
    public static void startYourself(Context context,
                                     MediaSource source,
                                     int position) {
        Intent intent = new Intent(context, MediaViewPagerActivity.class);
        intent.putExtra("source", source);
        intent.putExtra("position", position);
        context.startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current-page", pager.getCurrentItem());
        outState.putBoolean("editable", editable);
        outState.putBoolean("isFeaturedImage", isFeaturedImage);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view_pager);
        ButterKnife.bind(this);
        initViewPager(savedInstanceState);
    }

    private void initViewPager(Bundle savedInstanceState) {
        pager.addOnPageChangeListener(this);

        adapter = new MediaDetailAdapter(getSupportFragmentManager());

        if (savedInstanceState != null) {
            final int pageNumber = savedInstanceState.getInt("current-page");
            pager.setAdapter(adapter);
            pager.setCurrentItem(pageNumber, false);

            supportInvalidateOptionsMenu();
            adapter.notifyDataSetChanged();
        } else {
            pager.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!editable) {
            getMenuInflater().inflate(R.menu.menu_image_detail, menu);
            if (pager != null) {
                Media m = getMediaAtPosition();
                if (m != null) {
                    // Enable default set of actions, then re-enable different set of actions only if it is a failed contrib
                    menu.findItem(R.id.menu_browser_current_image).setEnabled(true).setVisible(true);
                    menu.findItem(R.id.menu_share_current_image).setEnabled(true).setVisible(true);
                    menu.findItem(R.id.menu_download_current_image).setEnabled(true).setVisible(true);
                    menu.findItem(R.id.menu_bookmark_current_image).setEnabled(true).setVisible(true);

                    // Initialize bookmark object
                    bookmark = new Bookmark(
                            m.getFilename(),
                            m.getCreator()
                    );
                    updateBookmarkState(menu.findItem(R.id.menu_bookmark_current_image));

                    if (m instanceof Contribution) {
                        Contribution c = (Contribution) m;
                        switch (c.getState()) {
                            case Contribution.STATE_FAILED:
                                menu.findItem(R.id.menu_browser_current_image).setEnabled(false).setVisible(false);
                                menu.findItem(R.id.menu_share_current_image).setEnabled(false).setVisible(false);
                                menu.findItem(R.id.menu_download_current_image).setEnabled(false).setVisible(false);
                                menu.findItem(R.id.menu_bookmark_current_image).setEnabled(false).setVisible(false);
                                break;
                            case Contribution.STATE_IN_PROGRESS:
                            case Contribution.STATE_QUEUED:
                                menu.findItem(R.id.menu_browser_current_image).setEnabled(false).setVisible(false);
                                menu.findItem(R.id.menu_share_current_image).setEnabled(false).setVisible(false);
                                menu.findItem(R.id.menu_download_current_image).setEnabled(false).setVisible(false);
                                menu.findItem(R.id.menu_bookmark_current_image).setEnabled(false).setVisible(false);
                                break;
                            case Contribution.STATE_COMPLETED:
                                // Default set of menu items works fine. Treat same as regular media object
                                break;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Media m = getMediaAtPosition();
        switch (item.getItemId()) {
            case R.id.menu_bookmark_current_image:
                bookmarkDao.updateBookmark(bookmark);
                updateBookmarkState(item);
                return true;
            case R.id.menu_share_current_image:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, m.getDisplayTitle() + " \n" + m.getFilePageTitle().getCanonicalUri());
                startActivity(Intent.createChooser(shareIntent, "Share image via..."));
                return true;
            case R.id.menu_browser_current_image:
                // View in browser
                Utils.handleWebUrl(this, m.getFilePageTitle().getMobileUri());

                return true;
            case R.id.menu_download_current_image:
                // Download
                if (!NetworkUtils.isInternetConnectionEstablished(this)) {
                    ViewUtil.showShortSnackbar(getWindow().getDecorView(), R.string.no_internet);
                    return false;
                }
                downloadMedia(m);
                return true;
            case R.id.menu_set_as_wallpaper:
                // Set wallpaper
                setWallpaper(m);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Media getMediaAtPosition() {
        return null;
    }

    private void requestMoreImages() {

    }

    private int getTotalMediaCount() {
        return 0;
    }

    /**
     * Set the media as the device's wallpaper if the imageUrl is not null
     * Fails silently if setting the wallpaper fails
     *
     * @param media
     */
    private void setWallpaper(Media media) {
        if (media.getImageUrl() == null || media.getImageUrl().isEmpty()) {
            Timber.d("Media URL not present");
            return;
        }
        ImageUtils.setWallpaperFromImageUrl(this, Uri.parse(media.getImageUrl()));
    }

    /**
     * Start the media file downloading to the local SD card/storage.
     * The file can then be opened in Gallery or other apps.
     *
     * @param m Media file to download
     */
    private void downloadMedia(Media m) {
        String imageUrl = m.getImageUrl(), fileName = m.getFilename();

        if (imageUrl == null || fileName == null) {
            Timber.d("Skipping download media as either imageUrl %s or filename %s activity is null", imageUrl, fileName);
            return;
        }

        // Strip 'File:' from beginning of filename, we really shouldn't store it
        fileName = fileName.replaceFirst("^File:", "");

        Uri imageUri = Uri.parse(imageUrl);

        DownloadManager.Request req = new DownloadManager.Request(imageUri);
        //These are not the image title and description fields, they are download descs for notifications
        req.setDescription(getString(R.string.app_name));
        req.setTitle(m.getDisplayTitle());
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // Modern Android updates the gallery automatically. Yay!
        req.allowScanningByMediaScanner();
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            Snackbar.make(getWindow().getDecorView(), R.string.read_storage_permission_rationale,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok,
                    view -> ActivityCompat.requestPermissions(this,
                            new String[]{READ_EXTERNAL_STORAGE}, 1)).show();
        } else {
            DownloadManager systemService = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (systemService != null) {
                systemService.enqueue(req);
            }
        }
    }

    private void updateBookmarkState(MenuItem item) {
        boolean isBookmarked = bookmarkDao.findBookmark(bookmark);
        int icon = isBookmarked ? R.drawable.ic_round_star_filled_24px : R.drawable.ic_round_star_border_24px;
        item.setIcon(icon);
    }

    public void showImage(int i) {
        Handler handler = new Handler();
        handler.postDelayed(() -> pager.setCurrentItem(i), 5);
    }

    /**
     * The method notify the viewpager that number of items have changed.
     */
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        if (i + 1 >= adapter.getCount()) {
            requestMoreImages();
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onPageSelected(int i) {
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    //FragmentStatePagerAdapter allows user to swipe across collection of images (no. of images undetermined)
    private class MediaDetailAdapter extends FragmentStatePagerAdapter {

        public MediaDetailAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                // See bug https://code.google.com/p/android/issues/detail?id=27526
                pager.postDelayed(MediaViewPagerActivity.this::invalidateOptionsMenu, 5);
            }
            return MediaDetailFragment.forMedia(i, editable, isFeaturedImage);
        }

        @Override
        public int getCount() {
            return getTotalMediaCount();
        }
    }
}