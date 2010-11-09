package ch.arrenbrecht.jcite;

public final class BlockMarker extends FragmentMarker {

	public BlockMarker(String _prefix, String _suffix) {
		super(_prefix, _suffix);
	}

	public BlockMarker(String _prefixAndSuffix) {
		super(_prefixAndSuffix);
	}

	@Override protected void adjustFragment(String _in, FragmentLocator _locator) {
		_locator.beginPrefix = Util.scanBackTo(_in, '\n', _locator.beginPrefix) + 1;
		_locator.endFragment = Util.scanBackTo(_in, '\n', _locator.endFragment) + 1;
	}

}
