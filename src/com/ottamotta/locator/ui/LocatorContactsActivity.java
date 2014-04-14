package com.ottamotta.locator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.BaseLocatorActionExecutor;
import com.ottamotta.locator.application.LocatorApplication;
import com.ottamotta.locator.application.LocatorSettings;
import com.ottamotta.locator.contacts.TrustedContact;
import com.ottamotta.locator.ui.dialogs.InfoDialogFragment;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class LocatorContactsActivity extends BaseActivity implements SlidingPaneLayout.PanelSlideListener, PaneHost {

    private static final int OPEN_CONTACT_ON_CLICK_DELAY = 200;

    protected final int AUTO_ANSWER_ON = LocatorSettings.AUTO_ANSWER_ON;
    protected final int AUTO_ANSWER_OFF = LocatorSettings.AUTO_ANSWER_OFF;
    protected final int AUTO_ANSWER_HELP = 2;

    protected int autoAnswerSelection;
    ShareActionProvider shareActionProvider;

    @Inject
    private EventBus bus;

    @Inject
    private LocatorSettings settings;

    private SlidingPaneLayout pane;
    private TrustedContact contactSelected;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locator_pager_activity);
        if (savedInstanceState == null) {
            LocatorApplication.initRate(this);
            setupContactsFragment();
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        pane = (SlidingPaneLayout) findViewById(R.id.sp);
        pane.setPanelSlideListener(this);
        pane.setParallaxDistance(100);
        openPane();
        setupIntroFragment();
        setNavigation(getSupportActionBar());
        selectContactIfNeed();
    }

    @Override
    public void openPane() {
        if (pane != null) pane.openPane();
    }

    @Override
    public void closePane() {
        if (pane != null) pane.closePane();
    }

    private void selectContactIfNeed() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(BaseLocatorActionExecutor.EXTRA_CONTACT)) {
            pane.openPane();
            selectContact((TrustedContact) getIntent().getExtras().getParcelable(BaseLocatorActionExecutor.EXTRA_CONTACT));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        selectContactIfNeed();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) item.getActionProvider();
        shareActionProvider.setShareIntent(getShareIntent());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                switchToFragment(IntroFragment.newInstance());
                closePane();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupActionBarBackground(boolean enabled) {
        int bg = enabled ? R.drawable.enabled_bg : R.drawable.disabled_bg;
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(bg));
    }

    private void setNavigation(final ActionBar bar) {
        settings.setAutoAnswerSetting(LocatorSettings.AUTO_ANSWER_ON);
        /*final String[] actions = getResources().getStringArray(R.array.auto_answer_options);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        int savedSelection = settings.getAutoAnswerSetting();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getBaseContext(),
                R.layout.actionbar_spinner_item, actions);
        bar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                switch (itemPosition) {
                    case AUTO_ANSWER_ON:
                        setupActionBarBackground(true);
                        settings.setAutoAnswerSetting(itemPosition);
                        autoAnswerSelection = AUTO_ANSWER_ON;
                        break;
                    case (AUTO_ANSWER_OFF):
                        setupActionBarBackground(false);
                        settings.setAutoAnswerSetting(itemPosition);
                        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.disabled_bg));
                        autoAnswerSelection = AUTO_ANSWER_OFF;
                        break;
                    case (AUTO_ANSWER_HELP):
                        showInfoDialog(getString(R.string.auto_answer_help), adapter.getItem(itemPosition));
                        break;
                    default:
                        return false;
                }
                bar.setSelectedNavigationItem(autoAnswerSelection);
                return true;
            }
        });

        autoAnswerSelection = savedSelection;
        settings.setAutoAnswerSetting(savedSelection);
        bar.setSelectedNavigationItem(autoAnswerSelection);*/

    }

    protected void showInfoDialog(String message, String title) {
        DialogFragment dialog = InfoDialogFragment.newInstance(title, message, getString(R.string.dialog_ok));
        dialog.show(getSupportFragmentManager(), "tag_message");
    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    public void onEvent(final TrustedContactSelectedEvent event) {
        selectContact(event.contact);
    }

    private void selectContact(TrustedContact contact) {
        contactSelected = contact;
        setupJournalFragment(contact);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closePane();
            }
        }, OPEN_CONTACT_ON_CLICK_DELAY);
    }

    private void setupJournalFragment(TrustedContact contact) {
        switchToFragment(JournalFragment.newInstance(contact));
    }

    private void setupIntroFragment() {
        switchToFragment(IntroFragment.newInstance());
    }

    private void switchToFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.journal, fragment);
        ft.commit();
    }

    private void setupContactsFragment() {
        Fragment fragment = new TrustedContactsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.contacts, fragment);
        ft.commit();
    }

    @Override
    public void onPanelSlide(View view, float v) {
    }

    @Override
    public void onPanelOpened(View view) {
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    @Override
    public void onPanelClosed(View view) {
        String title = contactSelected == null ? getString(R.string.all_requests) : contactSelected.getName();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (pane.isOpen()) {
            super.onBackPressed();
        } else {
            pane.openPane();
        }
    }


    private Intent getShareIntent() {
        Intent sendIntent = new Intent();
        //String marketUrl = AppRater.getMarket().getMarketURI(this).toString();
        // sendIntent.putExtra(Intent.EXTRA_TEXT, "TEXT here");
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, LocatorApplication.PROMO_SITE_URL)); //"Please install 'People locator' to find me by SMS at any time");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject)); //"Find me");
        sendIntent.setType("text/plain");
        return sendIntent;
    }
}
