package org.dolphinemu.dolphinemu.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dolphinemu.dolphinemu.BuildConfig;
import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.adapters.PlatformPagerAdapter;
import org.dolphinemu.dolphinemu.ui.platform.Platform;
import org.dolphinemu.dolphinemu.ui.platform.PlatformGamesView;

/**
 * The main UI of the phone/tablet UI. Manages several PlatformGamesFragments, which
 * individually display a grid of available games for each Fragment, in a tabbed layout.
 */
public final class PhoneMainFragment extends MainFragment
{
	private MainActivity mMainActivity;
	private ViewPager mViewPager;
	private Toolbar mToolbar;
	private TabLayout mTabLayout;
	private FloatingActionButton mFab;

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		if (context instanceof MainActivity)
		{
			mMainActivity = (MainActivity) context;
		}
		else
		{
			throw new IllegalStateException("PhoneMainFragment must have MainActivity parent");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View layout = inflater.inflate(R.layout.fragment_phone_main, container, false);
		mToolbar = layout.findViewById(R.id.toolbar_main);
		mViewPager = layout.findViewById(R.id.pager_platforms);
		mTabLayout = layout.findViewById(R.id.tabs_platforms);
		mFab =  layout.findViewById(R.id.button_add_directory);

		mToolbar.setSubtitle(BuildConfig.VERSION_NAME);
		((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);


		PlatformPagerAdapter platformPagerAdapter = new PlatformPagerAdapter(
				getFragmentManager(), getContext());
		mViewPager.setAdapter(platformPagerAdapter);

		mTabLayout.setupWithViewPager(mViewPager);

		// Set up the FAB.
		mFab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mMainActivity.launchFileListActivity();
			}
		});

		return layout;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_game_grid, menu);
	}

	/*
	 * MainFragment methods
	 */

	@Override
	public void refreshFragmentScreenshot(int fragmentPosition)
	{
		// Invalidate Picasso image so that the new screenshot is animated in.
		Platform platform = Platform.fromPosition(mViewPager.getCurrentItem());
		PlatformGamesView fragment = getPlatformGamesView(platform);
		if (fragment != null)
		{
			fragment.refreshScreenshotAtPosition(fragmentPosition);
		}
	}

	@Override
	public void showGames(Platform platform, Cursor cursor)
	{
		PlatformGamesView fragment = getPlatformGamesView(platform);
			if (fragment != null)
			{
				fragment.showGames(cursor);
			}

	}

	@Nullable
	private PlatformGamesView getPlatformGamesView(Platform platform)
	{
		String fragmentTag = "android:switcher:" + mViewPager.getId() + ":" + platform;

		return (PlatformGamesView) getFragmentManager().findFragmentByTag(fragmentTag);
	}
}
