package org.dolphinemu.dolphinemu.ui.main;

/** MainActivity implementation for leanback (Android TV). */

public final class TvMainActivity extends MainActivity
{
	public MainFragment createNewMainFragment()
	{
		return new TvMainFragment();
	}
}
