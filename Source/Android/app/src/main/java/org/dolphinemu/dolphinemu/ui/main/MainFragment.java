package org.dolphinemu.dolphinemu.ui.main;

import android.database.Cursor;
import android.support.v4.app.Fragment;

import org.dolphinemu.dolphinemu.ui.platform.Platform;

/** Interface for the Fragments to be displayed by the MainActivity. */
public abstract class MainFragment extends Fragment
{
	public abstract void refreshFragmentScreenshot(int fragmentPosition);

	public abstract void showGames(Platform platform, Cursor cursor);
}
