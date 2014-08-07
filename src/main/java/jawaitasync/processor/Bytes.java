package jawaitasync.processor;

class Bytes {
	static public int indexOf(byte[] array, byte[] subarray) {
		outer: for (int n = 0; n < array.length - subarray.length; n++) {
			for (int m = 0; m < subarray.length; m++) {
				if (array[n + m] != array[m]) continue outer;
			}
			return n;
		}
		return -1;
	}

	static public boolean contains(byte[] array, byte[] subarray) {
		return Bytes.indexOf(array, subarray) >= 0;
	}
}
