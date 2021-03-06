package org.vitrivr.cineast.art.modules;

import com.eclipsesource.json.JsonObject;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationMedianColorSunburst extends AbstractVisualizationModule {
  public VisualizationMedianColorSunburst() {
    super();
    tableNames.put("MedianColor", "features_MedianColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationMedianColorSunburst";
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int[][][] colors = ArtUtil.createColorDistribution3();

    int[][] data = new int[3][216];
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[] pixel = ArtUtil.shotToRGB(feature.get("feature").getFloatArray(), 1, 1)[0][0];
      int min = ArtUtil.minDistance(colors[2], 216, pixel);
      data[0][min/36]++;
      data[1][min/6]++;
      data[2][min]++;
    }

    JsonObject graph = new JsonObject();
    graph.add("name", "VisualizationMedianColorSunburst");
    graph.add("children", ArtUtil.getSunburstChildren(data, colors, 0, 0));

    System.out.println(graph.toString());

    return graph.toString();
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), multimediaobjectId));
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.GRAPH_SUNBURST;
  }
}
