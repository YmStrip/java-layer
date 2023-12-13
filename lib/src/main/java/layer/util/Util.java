package layer.util;

import sun.misc.Unsafe;

public class Util {
	public static <T> T newInstance(String name, Class<T> data) {
		try {
			//Unsafe.
			//不安全创建
			//return (T) Unsafe.getUnsafe().allocateInstance(data);
			return data.newInstance();
		} catch (Exception e) {
			System.out.println(e);
			throw new Error(String.format("[Constructor][%s] constructor error\n%s", name, e));
		}
	}
	
	public static <T> T newInstance(Class<T> data) {
		return newInstance(data.getSimpleName(), data);
	}
}
