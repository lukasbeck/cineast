package org.vitrivr.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.QueryConfig.Distance;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogram;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogramCalculator;

public class MedianFuzzyHist extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public MedianFuzzyHist(){
		super("features_MedianFuzzyHist", 2f / 4f);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			FuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getHistogramNormalized(shot.getMedianImg().getBufferedImage());
			persist(shot.getId(), fch);
		}
		LOGGER.exit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized(sc.getMedianImg().getBufferedImage());
		return getSimilar(query.toArray(null), qc);
	}
	
  @Override
  protected QueryConfig setQueryConfig(QueryConfig qc) {
    return QueryConfig.clone(qc).setDistanceIfEmpty(Distance.chisquared);
  }

}
