package ch.arrenbrecht.jcite;

public final class InlineMarker extends FragmentMarker {

	public InlineMarker(String _prefix, String _suffix) {
		super(_prefix, _suffix);
	}

	public InlineMarker(String _prefixAndSuffix) {
		super(_prefixAndSuffix);
	}

	@Override protected void adjustFragment(String _in, FragmentLocator _locator) {}

}
