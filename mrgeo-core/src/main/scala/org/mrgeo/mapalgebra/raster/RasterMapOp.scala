package org.mrgeo.mapalgebra.raster

import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.mrgeo.data.DataProviderFactory.AccessMode
import org.mrgeo.data.raster.RasterWritable
import org.mrgeo.data.tile.TileIdWritable
import org.mrgeo.data.{DataProviderFactory, ProviderProperties}
import org.mrgeo.data.image.MrsImageDataProvider
import org.mrgeo.data.rdd.RasterRDD
import org.mrgeo.image.MrsImagePyramidMetadata
import org.mrgeo.mapalgebra.MapOp
import org.mrgeo.utils.{Bounds, SparkUtils}

object RasterMapOp {
  def isNodata(value:Double, nodata:Double):Boolean = {
    if (nodata.isNaN) {
      value.isNaN
    }
    else {
      nodata == value
    }
  }
  def isNotNodata(value:Double, nodata:Double):Boolean = {
    if (nodata.isNaN) {
      !value.isNaN
    }
    else {
      nodata != value
    }
  }

}
abstract class RasterMapOp extends MapOp {

  private var meta:MrsImagePyramidMetadata = null

  def rdd():Option[RasterRDD]

  def metadata():Option[MrsImagePyramidMetadata] =  Option(meta)
  def metadata(meta:MrsImagePyramidMetadata) = { this.meta = meta}

  def save(output: String, providerProperties:ProviderProperties, context:SparkContext) = {
    rdd() match {
    case Some(rdd) =>
      val provider: MrsImageDataProvider =
        DataProviderFactory.getMrsImageDataProvider(output, AccessMode.OVERWRITE, providerProperties)
      metadata() match {
      case Some(metadata) =>
        val meta = metadata

        SparkUtils.saveMrsPyramid(rdd, provider,
          meta.getMaxZoomLevel, meta.getTilesize, meta.getDefaultValues,
          context.hadoopConfiguration, providerproperties =  providerProperties)
      case _ =>
      }
    case _ =>
    }
  }

}
