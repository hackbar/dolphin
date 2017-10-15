package org.dolphinemu.dolphinemu.ui.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import org.dolphinemu.dolphinemu.DolphinApplication;
import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.activities.AddDirectoryActivity;
import org.dolphinemu.dolphinemu.model.GameDatabase;
import org.dolphinemu.dolphinemu.model.GameProvider;
import org.dolphinemu.dolphinemu.ui.platform.Platform;
import org.dolphinemu.dolphinemu.ui.settings.SettingsActivity;
import org.dolphinemu.dolphinemu.utils.PermissionsHandler;
import org.dolphinemu.dolphinemu.utils.SettingsFile;
import org.dolphinemu.dolphinemu.utils.StartupHandler;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * The main Activity holding non form-factor specific logic.
 *
 * Clients are expected to subclass this, and set the correct theme in the manifest.
 *
 * Normally we couldn't use AppCompatActivity with the leanback theme, but we cheat a bit (see the
 * style) to make the object composition much simpler.
 */
public abstract class MainActivity extends AppCompatActivity
{
	public static final int REQUEST_ADD_DIRECTORY = 1;
	public static final int REQUEST_EMULATE_GAME = 2;

	private static final String MAIN_FRAGMENT_TAG = "main_fragment";
	private MainFragment mMainFragment;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Stuff in this block only happens when this activity is newly created (not a rotation)
		// TODO Split some of this stuff into Application.onCreate()
		if (savedInstanceState == null)
			StartupHandler.HandleInit(this);

		if (PermissionsHandler.hasWriteAccess(this))
		{
			setupMainFragment(savedInstanceState == null);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		scanAndUpdateGames();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_ADD_DIRECTORY:
				// If the user picked a file, as opposed to just backing out.
				if (resultCode == RESULT_OK)
				{
					getContentResolver().insert(GameProvider.URI_REFRESH, null);
					scanAndUpdateGames();
				}
				break;

			case REQUEST_EMULATE_GAME:
				mMainFragment.refreshFragmentScreenshot(resultCode);
				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PermissionsHandler.REQUEST_CODE_WRITE_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// If we just got permissions granted, always create the fragment.
					setupMainFragment(true);
				} else {
					Toast.makeText(this, R.string.write_permission_needed, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return onOptionsItemSelected(item.getItemId());
	}

	public boolean onOptionsItemSelected(int itemId)
	{
		switch (itemId)
		{
			case R.id.menu_settings_core:
				launchSettingsActivity(SettingsFile.FILE_NAME_DOLPHIN);
				return true;

			case R.id.menu_settings_video:
				launchSettingsActivity(SettingsFile.FILE_NAME_GFX);
				return true;

			case R.id.menu_settings_gcpad:
				launchSettingsActivity(SettingsFile.FILE_NAME_GCPAD);
				return true;

			case R.id.menu_settings_wiimote:
				launchSettingsActivity(SettingsFile.FILE_NAME_WIIMOTE);
				return true;

			case R.id.menu_refresh:
				scanAndUpdateGames();
				return true;

			case R.id.button_add_directory:
				launchFileListActivity();
				return true;
		}

		return false;
	}

	public void launchFileListActivity()
	{
		AddDirectoryActivity.launch(this);
	}

	public void launchSettingsActivity(String menuTag)
	{
		SettingsActivity.launch(this, menuTag);
	}

	private void setupMainFragment(boolean createFragment)
	{
		StartupHandler.copyAssetsIfNeeded(this);

		if (createFragment)
		{
			mMainFragment = createNewMainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.frame_main, mMainFragment, MAIN_FRAGMENT_TAG)
					.commit();
		}
		else
		{
			mMainFragment = (MainFragment) getSupportFragmentManager()
					.findFragmentByTag(MAIN_FRAGMENT_TAG);
			if (mMainFragment == null)
			{
				throw new IllegalStateException("Cannot find existing MainFragment");
			}
		}
	}

	private void scanAndUpdateGames()
	{
		GameDatabase databaseHelper = DolphinApplication.databaseHelper;
		databaseHelper.scanLibrary(databaseHelper.getWritableDatabase());

		for (final Platform platform : Platform.values())
		{
			databaseHelper.getGamesForPlatform(platform)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Action1<Cursor>()
							   {
								   @Override
								   public void call(Cursor games)
								   {
									   mMainFragment.showGames(platform, games);
								   }
							   }
					);
		}}

	public abstract MainFragment createNewMainFragment();
}