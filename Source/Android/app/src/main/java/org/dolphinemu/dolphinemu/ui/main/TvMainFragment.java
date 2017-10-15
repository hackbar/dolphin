package org.dolphinemu.dolphinemu.ui.main;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.database.CursorMapper;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dolphinemu.dolphinemu.BuildConfig;
import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.activities.EmulationActivity;
import org.dolphinemu.dolphinemu.adapters.GameRowPresenter;
import org.dolphinemu.dolphinemu.adapters.SettingsRowPresenter;
import org.dolphinemu.dolphinemu.model.Game;
import org.dolphinemu.dolphinemu.model.TvSettingsItem;
import org.dolphinemu.dolphinemu.ui.platform.Platform;
import org.dolphinemu.dolphinemu.viewholders.TvGameViewHolder;

import java.util.HashMap;
import java.util.Map;

public final class TvMainFragment extends MainFragment
{
	private static final String BROWSE_FRAGMENT_TAG = "browse_fragment";

	private MainActivity mMainActivity;
	private BrowseSupportFragment mBrowseFragment;
	private ArrayObjectAdapter mRowsAdapter;
	private Map<Platform, Integer> mRowsAdapterPlatformMapping;

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
			throw new IllegalStateException("TvMainFragment must have MainActivity parent");
		}

		mRowsAdapterPlatformMapping = new HashMap<>();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View layout = inflater.inflate(R.layout.fragment_tv_main, container, false);

		mBrowseFragment = new BrowseSupportFragment();

		// Set display parameters for the BrowseFragment
		mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_ENABLED);
		mBrowseFragment.setBrandColor(ContextCompat.getColor(getContext(), R.color.dolphin_blue_dark));
		mBrowseFragment.setTitle(BuildConfig.VERSION_NAME);
		buildRowsAdapter();

		mBrowseFragment.setOnItemViewClickedListener(
				new OnItemViewClickedListener()
				{
					@Override
					public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row)
					{
						// Special case: user clicked on a settings row item.
						// Delegate up to the MainActivity's onOptionsItemSelected
						if (item instanceof TvSettingsItem)
						{
							// Build a synthetic MenuItem for the param
							TvSettingsItem settingsItem = (TvSettingsItem) item;
							mMainActivity.onOptionsItemSelected(settingsItem.getItemId());
						}
						else
						{
							TvGameViewHolder holder = (TvGameViewHolder) itemViewHolder;

							// Start the emulation activity and send the path of the clicked ISO to it.
							EmulationActivity.launch(mMainActivity,
									holder.path,
									holder.title,
									holder.screenshotPath,
									-1,
									holder.imageScreenshot);
						}
					}
				});
		// Add the BrowseFragment
		getFragmentManager().beginTransaction()
				.replace(R.id.content, mBrowseFragment, BROWSE_FRAGMENT_TAG)
				.commit();

		return layout;
	}


	/*
	 * MainFragment methods
	 */

	@Override
	public void refreshFragmentScreenshot(int fragmentPosition)
	{
		mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
	}

	@Override
	public synchronized void showGames(Platform platform, Cursor games)
	{
		ListRow row = buildGamesRow(platform, games);

		// Add row to the adapter only if it is not empty.
		if (row != null)
		{
			if (mRowsAdapterPlatformMapping.containsKey(platform))
			{
				mRowsAdapter.replace(mRowsAdapterPlatformMapping.get(platform), row);
			}
			else
			{
				Integer index = mRowsAdapter.size();
				mRowsAdapterPlatformMapping.put(platform, index);
				mRowsAdapter.add(index, row);
			}
		}
	}

	private void buildRowsAdapter()
	{
		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

		mRowsAdapter.add(buildSettingsRow());

		mBrowseFragment.setAdapter(mRowsAdapter);
	}

	@Nullable
	private ListRow buildGamesRow(Platform platform, Cursor games)
	{
		// Create an adapter for this row.
		CursorObjectAdapter row = new CursorObjectAdapter(new GameRowPresenter());

		// If cursor is empty, don't return a Row.
		if (!games.moveToFirst())
		{
			return null;
		}

		row.changeCursor(games);
		row.setMapper(new CursorMapper()
		{
			@Override
			protected void bindColumns(Cursor cursor)
			{
				// No-op? Not sure what this does.
			}

			@Override
			protected Object bind(Cursor cursor)
			{
				return Game.fromCursor(cursor);
			}
		});

		// Create a header for this row.
		HeaderItem header = new HeaderItem(platform.toInt(), platform.getHeaderName());

		// Create the row, passing it the filled adapter and the header, and give it to the master adapter.
		return new ListRow(header, row);
	}

	private ListRow buildSettingsRow()
	{
		ArrayObjectAdapter rowItems = new ArrayObjectAdapter(new SettingsRowPresenter());

		rowItems.add(new TvSettingsItem(R.id.menu_settings_core,
				R.drawable.ic_settings_core_tv,
				R.string.grid_menu_core_settings));

		rowItems.add(new TvSettingsItem(R.id.menu_settings_video,
				R.drawable.ic_settings_graphics_tv,
				R.string.grid_menu_video_settings));

		rowItems.add(new TvSettingsItem(R.id.menu_settings_gcpad,
				R.drawable.ic_settings_gcpad,
				R.string.grid_menu_gcpad_settings));

		rowItems.add(new TvSettingsItem(R.id.menu_settings_wiimote,
				R.drawable.ic_settings_wiimote,
				R.string.grid_menu_wiimote_settings));

		rowItems.add(new TvSettingsItem(R.id.button_add_directory,
				R.drawable.ic_add_tv,
				R.string.add_directory_title));

		rowItems.add(new TvSettingsItem(R.id.menu_refresh,
				R.drawable.ic_refresh_tv,
				R.string.grid_menu_refresh));

		// Create a header for this row.
		HeaderItem header = new HeaderItem(R.string.preferences_settings, getString(R.string.preferences_settings));

		return new ListRow(header, rowItems);
	}
}
