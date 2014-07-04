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
		this._key = value;
	}

	public void changeTo(String value) {
		if (this._key instanceof LinkedKey) {
			((LinkedKey) this._key).changeTo(value);
		} else if (this._key instanceof Key) {
			((Key<?>) this._key).changeTo(value);
		}
	}

	@Override
	public String toString() {
		if (this._key == this)
			return "circular reference";
		return this._key.toString();
	}

	@Override
	public boolean equals(Object key) {
		if (this._key == null || key == null)
			return false;
		return this._key.toString().equals(key.toString());
	}

	@Override
	public int hashCode() {
		return this._key.hashCode();
	}

	@Override
	public int compareTo(IKey o) {
		if (o == null || this._key == null)
			return 0;
		return this._key.compareTo(o);
	}

	@Override
	public boolean isEmpty() {
		return this._key == null || this._key.isEmpty();
	}

	@Override
	public boolean isNotEmpty() {
		return this._key != null && this._key.isNotEmpty();
	}

}
