package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class AverageColorGrid8Normalized extends AverageColorGrid8 {

	public AverageColorGrid8Normalized(){
		super("features_AverageColorGrid8Normalized", 12595f / 4f);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		if (!phandler.idExists(shot.getId())) {
			MultiImage avgimg = ImageHistogramEqualizer.getEqualized(shot.getAvgImg());
			
			persist(shot.getId(), partition(avgimg).first);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(sc.getAvgImg()));
		return getSimilar(p.first.toArray(null), new QueryConfig(qc).setDistanceWeights(p.second));
	}

}
