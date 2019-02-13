package fr.free.nrw.commons.category;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import butterknife.ButterKnife;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.auth.AuthenticatedActivity;
import fr.free.nrw.commons.explore.SearchActivity;
import fr.free.nrw.commons.media.MediaSource;
import fr.free.nrw.commons.media.MediaViewPagerActivity;
import fr.free.nrw.commons.theme.NavigationBaseActivity;

/**
 * This activity displays pictures of a particular category
 * Its generic and simply takes the name of category name in its start intent to load all images in
 * a particular category. This activity is currently being used to display a list of featured images,
 * which is nothing but another category on wikimedia commons.
 */

public class CategoryImagesActivity
        extends AuthenticatedActivity
        implements FragmentManager.OnBackStackChangedListener,
                    AdapterView.OnItemClickListener{


    private FragmentManager supportFragmentManager;
    private CategoryImagesListFragment categoryImagesListFragment;

    @Override
    protected void onAuthCookieAcquired(String authCookie) {

    }

    @Override
    protected void onAuthFailure() {

    }

    /**
     * This method is called on backPressed of anyFragment in the activity.
     * We are changing the icon here from back to hamburger icon.
     */
    @Override
    public void onBackPressed() {
        initDrawer();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_images);
        ButterKnife.bind(this);

        // Activity can call methods in the fragment by acquiring a
        // reference to the Fragment from FragmentManager, using findFragmentById()
        supportFragmentManager = getSupportFragmentManager();
        setCategoryImagesFragment();
        supportFragmentManager.addOnBackStackChangedListener(this);
        requestAuthToken();
        initDrawer();
        setPageTitle();
    }

    /**
     * Gets the categoryName from the intent and initializes the fragment for showing images of that category
     */
    private void setCategoryImagesFragment() {
        categoryImagesListFragment = new CategoryImagesListFragment();
        String categoryName = getIntent().getStringExtra("categoryName");
        if (getIntent() != null && categoryName != null) {
            Bundle arguments = new Bundle();
            arguments.putString("categoryName", categoryName);
            categoryImagesListFragment.setArguments(arguments);
            FragmentTransaction transaction = supportFragmentManager.beginTransaction();
            transaction
                    .add(R.id.fragmentContainer, categoryImagesListFragment)
                    .commit();
        }
    }

    /**
     * Gets the passed title from the intents and displays it as the page title
     */
    private void setPageTitle() {
        if (getIntent() != null && getIntent().getStringExtra("title") != null) {
            setTitle(getIntent().getStringExtra("title"));
        }
    }

    @Override
    public void onBackStackChanged() {
    }

    /**
     * This method is called onClick of media inside category details (CategoryImageListFragment).
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        MediaViewPagerActivity.startYourself(this, MediaSource.CATEGORY, i);
        forceInitBackButton();
    }

    /**
     * Consumers should be simply using this method to use this activity.
     * @param context A Context of the application package implementing this class.
     * @param title Page title
     * @param categoryName Name of the category for displaying its images
     */
    public static void startYourself(Context context, String title, String categoryName) {
        Intent intent = new Intent(context, CategoryImagesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("title", title);
        intent.putExtra("categoryName", categoryName);
        context.startActivity(intent);
    }

    /**
     * This method inflates the menu in the toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method handles the logic on ItemSelect in toolbar menu
     * Currently only 1 choice is available to open search page of the app
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
                NavigationBaseActivity.startActivityWithFlags(this, SearchActivity.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
