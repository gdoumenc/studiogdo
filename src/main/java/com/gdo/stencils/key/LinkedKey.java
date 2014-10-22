package com.gdo.stencils.key;

public class LinkedKey implements IKey {

	private IKey _key; // original key

	public LinkedKey(IKey value) {
		if (value == null) {
			throw new NullPointerException("no value for key");
		}
		if (value == this) {
			throw new NullPointerException("circular reference");
		}
		_key = value;
	}

	public void changeTo(String value) {
		if (_key instanceof LinkedKey) {
			((LinkedKey) _key).changeTo(value);
		} else if (_key instanceof Key) {
			((Key<?>) _key).changeTo(value);
		}
	}

	@Override
	public String toString() {
		if (_key == this)
			return "circular reference";
		return _key.toString();
	}

	@Override
	public boolean equals(Object key) {
		if (_key == null || key == null)
			return false;
		return _key.toString().equals(key.toString());
	}

	@Override
	public int hashCode() {
		return _key.hashCode();
	}

	@Override
	public int compareTo(IKey o) {
		if (o == null || _key == null)
			return 0;
		return _key.compareTo(o);
	}

	@Override
	public boolean isEmpty() {
		return _key == null || _key.isEmpty();
	}

	@Override
	public boolean isNotEmpty() {
		return _key != null && _key.isNotEmpty();
	}

}
