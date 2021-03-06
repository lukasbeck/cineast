package org.vitrivr.cineast.core.data;

import org.vitrivr.cineast.core.decode.subtitle.SubTitle;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;

public class QuerySubTitleItem implements SubtitleItem {

	private final String text;
	
	public QuerySubTitleItem(String text){
		this.text = text;
	}
	
	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public String getRawText() {
		return text;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public int getStartFrame() {
		return 0;
	}

	@Override
	public int getEndFrame() {
		return 0;
	}

	@Override
	public SubTitle getSubTitle() {
		return null;
	}

}
