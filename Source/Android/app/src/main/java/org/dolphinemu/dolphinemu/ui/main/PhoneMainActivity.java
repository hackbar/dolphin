package org.dolphinemu.dolphinemu.ui.main;

/** MainActivity implementation for phones and tablets. */

public final class PhoneMainActivity extends MainActivity
{
	public MainFragment createNewMainFragment()
	{
		return new PhoneMainFragment();
	}
}
