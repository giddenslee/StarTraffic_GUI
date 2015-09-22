package modeling.model.traffic;

import java.util.List;

import org.jfree.data.xy.XYSeriesCollection;

import modeling.model.distribution.Distribution;
import modeling.packet.PacketInfo;
import modeling.tools.PolyAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class TrafficModel {
	public abstract void buildModelFromPackets(List<PacketInfo> packets);

	// 生成lastEndTime到endTime之间的packet
	public abstract List<PacketInfo> generatePackets(double endTimeSecond);

	// 清除生成队列
	public abstract void reset();

	public abstract void setStartTime(double timeSecond);

	public abstract int getParameterNumber();
	
	public abstract XYSeriesCollection createChartDataset();

	public String toString() {
		Gson gson = new GsonBuilder()
				.excludeFieldsWithoutExposeAnnotation()
				.serializeNulls()
				.registerTypeAdapter(TrafficModel.class,
						new PolyAdapter<TrafficModel>())
				.registerTypeAdapter(Distribution.class,
						new PolyAdapter<Distribution>()).create();
		return gson.toJson(this, TrafficModel.class);
	}

	public static TrafficModel fromJson(String jsonString) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(TrafficModel.class,
						new PolyAdapter<TrafficModel>())
				.registerTypeAdapter(Distribution.class,
						new PolyAdapter<Distribution>()).create();
		return gson.fromJson(jsonString, TrafficModel.class);
	}

	public String toPrettyString() {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.setPrettyPrinting().serializeNulls().create();
		return gson.toJson(this);
	}

	public static TrafficModel createModel(String level, int precisionSize,
			int precisionTime) {
		TrafficModel model = null;
		if ("packet".equals(level))
			model = new TrafficModelPacket(precisionSize, precisionTime);
		if ("session".equals(level))
			model = new TrafficModelSession(precisionSize, precisionTime);

		return model;
	}
}
