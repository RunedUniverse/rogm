package net.runeduniverse.libs.rogm.querying;

import java.io.Serializable;

public interface IIdentified<ID extends Serializable> extends IFilter {

	ID getId();

	public default <T extends Serializable> boolean checkType(Class<T> type) {
		return type.isInstance(this.getId());
	}

	public static <T extends Serializable> void checkType(Class<T> type, IFilter filter) throws Exception {
		if (!identify(filter))
			return;
		IIdentified<?> idf = (IIdentified<?>) filter;
		if (!idf.checkType(type))
			throw new Exception("IFilter ID <" + idf.getId().getClass().toString() + "> not supported");
	}
	
	public static boolean identify(IFilter filter) {
		if (filter == null || !(filter instanceof IIdentified) || ((IIdentified<?>) filter).getId() == null)
			return false;
		return true;
	}
}
