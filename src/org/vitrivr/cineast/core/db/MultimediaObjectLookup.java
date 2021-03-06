package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.setup.EntityCreator;

public class MultimediaObjectLookup {

  private static final Logger LOGGER = LogManager.getLogger();

  private final DBSelector selector;

  public MultimediaObjectLookup() {
    this.selector = Config.getDatabaseConfig().getSelectorSupplier().get();
    this.selector.open(EntityCreator.CINEAST_MULTIMEDIAOBJECT);
  }

  public MultimediaObjectDescriptor lookUpObjectById(String objectId) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", objectId);

    if (result.isEmpty()) {
      return new MultimediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }

  private MultimediaObjectDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map) {
    PrimitiveTypeProvider idProvider = map.get("id");
    PrimitiveTypeProvider nameProvider = map.get("name");
    PrimitiveTypeProvider pathProvider = map.get("path");
    PrimitiveTypeProvider widthProvider = map.get("width");
    PrimitiveTypeProvider heightProvider = map.get("height");
    PrimitiveTypeProvider framecountProvider = map.get("framecount");
    PrimitiveTypeProvider typeProvider = map.get("type");
    PrimitiveTypeProvider durationProvider = map.get("duration");

    if (!checkProvider("id", idProvider, ProviderDataType.STRING)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider("name", nameProvider, ProviderDataType.STRING)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider("path", pathProvider, ProviderDataType.STRING)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider("width", widthProvider, ProviderDataType.INT)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider("height", heightProvider, ProviderDataType.INT)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider("framecount", framecountProvider, ProviderDataType.INT)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider("type", typeProvider, ProviderDataType.INT)) {
      return new MultimediaObjectDescriptor();
    }

    /**
     * This is because the current setup produces a float-field as duration in the DB, but on old
     * versions of Cineast it is a double So the code has to accomodate for both options
     */
    if (!checkProvider("duration", durationProvider, ProviderDataType.FLOAT)) {
      if (!checkProvider("duration", durationProvider, ProviderDataType.DOUBLE)) {
        return new MultimediaObjectDescriptor();
      }
      LOGGER.info("Duration is a double, returning valid Multimediadescriptor");
    }

    return new MultimediaObjectDescriptor(idProvider.getString(), nameProvider.getString(),
        pathProvider.getString(), typeProvider.getInt(), widthProvider.getInt(),
        heightProvider.getInt(), framecountProvider.getInt(), (float) durationProvider.getDouble(),
        true);

  }

  private boolean checkProvider(String name, PrimitiveTypeProvider provider,
      ProviderDataType expectedType) {
    if (provider == null) {
      LOGGER.error("no {} in multimedia object", name);
      return false;
    }

    if (provider.getType() != expectedType) {
      LOGGER.error("invalid data type for field {} in multimedia object, expected {}, got {}", name,
          expectedType, provider.getType());
      return false;
    }
    return true;
  }

  public MultimediaObjectDescriptor lookUpObjectByName(String name) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("name", name);

    if (result.isEmpty()) {
      return new MultimediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }

  public Map<String, MultimediaObjectDescriptor> lookUpVideos(String... videoIds) {
    if (videoIds == null || videoIds.length == 0) {
      return new HashMap<>();
    }

    HashMap<String, MultimediaObjectDescriptor> _return = new HashMap<>();

    List<Map<String, PrimitiveTypeProvider>> results = selector.getRows("id", videoIds);

    if (results.isEmpty()) {
      return new HashMap<>();
    }

    for (Map<String, PrimitiveTypeProvider> map : results) {
      MultimediaObjectDescriptor d = mapToDescriptor(map);
      _return.put(d.getId(), d);
    }

    return _return;

  }

  public Map<String, MultimediaObjectDescriptor> lookUpVideos(Iterable<String> videoIds) {
    if (videoIds == null) {
      return new HashMap<>();
    }

    HashMap<String, MultimediaObjectDescriptor> _return = new HashMap<>();

    List<Map<String, PrimitiveTypeProvider>> results = selector.getRows("id", videoIds);

    if (results.isEmpty()) {
      return new HashMap<>();
    }

    for (Map<String, PrimitiveTypeProvider> map : results) {
      MultimediaObjectDescriptor d = mapToDescriptor(map);
      _return.put(d.getId(), d);
    }

    return _return;

  }

  public void close() {
    this.selector.close();
  }

  @Override
  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

  public List<MultimediaObjectDescriptor> getAllVideos() {
    DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();
    selector.open(EntityCreator.CINEAST_MULTIMEDIAOBJECT);
    List<Map<String, PrimitiveTypeProvider>> all = selector.getAll();
    List<MultimediaObjectDescriptor> _return = new ArrayList<>(all.size());
    for (Map<String, PrimitiveTypeProvider> map : all) {
      _return.add(mapToDescriptor(map));
    }
    return _return;
  }

  public List<String> lookUpVideoIds() {
    DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();
    selector.open(EntityCreator.CINEAST_MULTIMEDIAOBJECT);
    List<PrimitiveTypeProvider> ids = selector.getAll("id");
    Set<String> uniqueIds = new HashSet<>();
    for (PrimitiveTypeProvider l : ids) {
      uniqueIds.add(l.getString());
    }
    selector.close();

    List<String> multimediaobjectIds = new ArrayList<>();
    for (String id : uniqueIds) {
      multimediaobjectIds.add(id);
    }

    return multimediaobjectIds;
  }

  public static class MultimediaObjectDescriptor implements ExistenceCheck {

    private final String objectId;
    private final int width, height, framecount;
    private final MediaType type;
    private final float seconds;
    private final String name, path;
    private final boolean exists;

    public static MultimediaObjectDescriptor makeVideoDescriptor(String objectId, String name,
        String path, int width, int height, int framecount, float duration) {
      return new MultimediaObjectDescriptor(objectId, name, path, MediaType.VIDEO.getId(), width, height, framecount,
          duration, true);
    }

//    public static MultimediaObjectDescriptor makeImageDescriptor(String objectId, String name,
//        String path, int width, int height) {
//      return new MultimediaObjectDescriptor(objectId, name, path, 1, width, height, 1, 0, true);
//    }

    private MultimediaObjectDescriptor(String objectId, String name, String path, int type,
        int width, int height, int framecount, float duration, boolean exists) {
      this.objectId = objectId;
      this.name = name;
      this.path = path;
      this.type = MediaType.fromId(type);
      this.width = width;
      this.height = height;
      this.framecount = framecount;
      this.seconds = duration;
      this.exists = exists;
    }

    public MultimediaObjectDescriptor() {
      this("", "", "", 0, 0, 0, 0, 0, false);
    }

    public String getId() {
      return objectId;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public int getFramecount() {
      return framecount;
    }

    public float getSeconds() {
      return seconds;
    }

    public String getName() {
      return name;
    }

    public String getPath() {
      return path;
    }

    public float getFPS() {
      return this.framecount / this.seconds;
    }

    @Override
    public String toString() {
      return "MultimediaObjectDescriptor(" + objectId + ")";
    }

    public MediaType getType(){
      return this.type;
    }
    
    @Override
    public boolean exists() {
      return this.exists;
    }
  }
}
