package modeling.model.distribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class Distribution {

	HashMap<Double, Integer> valueCountMap; // 记录每个区间数值的出现次数
	@Expose
	double precision = 100;
	@Expose
	public ArrayList<CDFEntry> cdf; // 积累概率密度分布
	@Expose
	double minValue = Double.MAX_VALUE;
	@Expose
	double maxValue = Double.MIN_VALUE;
	@Expose
	int numValue = 0;

	Random rand = null;

	public static class CDFEntry implements Comparable<CDFEntry> {
		@Expose
		@SerializedName("v")
		double value;

		@Expose
		@SerializedName("p")
		double probability;

		public CDFEntry() {
		}

		public CDFEntry(double value, double probability) {
			this.value = value;
			this.probability = probability;
		}

		@Override
		public int compareTo(CDFEntry o) {
			return Double.compare(this.value, o.value);
		}

		@Override
		public String toString() {
			return "(v:" + value + ", p:" + probability + ")";
		}
	}

	public Distribution() {
		valueCountMap = new HashMap<Double, Integer>();
		valueCountMap.put(0.0, 0);
	}

	public abstract void addValue(double value, int times);

	public abstract void buildModel();

	public abstract double predictValue();

	public void addValue(double value) {
		addValue(value, 1);
	}

	public abstract int getParameterNumber();

	void cdfBuild() {
		cdf = new ArrayList<CDFEntry>();

		int totalCount = 0;

		for (Integer x : valueCountMap.values())
			totalCount += x;

		// 计算各个区间的概率密度
		for (Entry<Double, Integer> e : valueCountMap.entrySet()) {
			double p = e.getValue() * 1.0 / totalCount;
			cdf.add(new CDFEntry(e.getKey(), p));
		}

		// 按区间排序
		Collections.sort(cdf);

		// 计算积累概率密度
		for (int i = 1; i < cdf.size(); ++i) {
			cdf.get(i).probability += cdf.get(i - 1).probability;
		}

		// System.out.println(cdf);
		if (cdf.size() == 0)
			cdf = null;
		else
			cdf.get(cdf.size() - 1).probability = 1.0;
	}

	protected void changeMinMax(double value) {
		minValue = Math.min(value, minValue);
		maxValue = Math.max(value, maxValue);
	}

	private int cdfUpperBound(double target) {
		int first = 0, last = cdf.size() - 1;
		int middle, pos = 0;

		while (first < last) {
			middle = (first + last) / 2;
			if (cdf.get(middle).probability > target) { // 当中位数大于key时，last不动，让first不断逼近last
				last = middle;
				pos = last;
			} else {
				first = middle + 1; // 当中位数小于等于key时，将first递增，并记录新的位置
				pos = first;
			}
		}
		return pos;
	}

	double cdfPredictValue() {
		if (rand == null)
			rand = new Random();
		double rp = rand.nextDouble();
		int pos = cdfUpperBound(rp);

		if (pos >= cdf.size())
			return maxValue;
		else if (pos <= 0)
			return minValue;

		double gap = cdf.get(pos).value - cdf.get(pos - 1).value;
		if (gap > Integer.MAX_VALUE)
			gap = Integer.MAX_VALUE;

		return cdf.get(pos).value - rand.nextDouble() * gap;
	}

	public XYSeries createChartSeries(String name) {
		XYSeries series = new XYSeries(name, false);
		if (cdf != null)
			for (CDFEntry entry : cdf)
				series.add(entry.value, entry.probability);
		return series;
	}

	public String toString() {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.setPrettyPrinting().create();
		return gson.toJson(this);
	}

	public String toPrettyString(boolean useInt, double scale) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\n");

		sb.append("\"precision\": ");
		sb.append(Double.toString(precision));
		sb.append("\n");

		sb.append("\"minValue\": ");
		if (scale == 0)
			sb.append(Double.toString(minValue));
		else
			sb.append(String.format("%.9f", minValue * scale));
		sb.append("\n");

		sb.append("\"maxValue\": ");
		if (scale == 0)
			sb.append(Double.toString(maxValue));
		else
			sb.append(String.format("%.9f", maxValue * scale));
		sb.append("\n");

		sb.append("\"numValue\": ");
		sb.append(Integer.toString(numValue));
		sb.append("\n");

		sb.append("\"cdf\": [");
		sb.append("\n");

		if (cdf != null)
			for (int i = 0; i < cdf.size(); ++i) {
				double v = cdf.get(i).value;
				double p = cdf.get(i).probability;
				sb.append("{");
				if (useInt)
					sb.append(Long.toString((long) v));
				else {
					if (scale != 0)
						v *= scale;
					sb.append(String.format("%.9f", v));
				}
				sb.append(",");
				sb.append(String.format("%.9f", p));
				sb.append("}\n");
			}

		sb.append("]");
		sb.append("\n");

		sb.append("}");
		sb.append("\n");

		return sb.toString();
	}

	public static void countMapIncrease(Map<Double, Integer> map, double key,
			int inc) {
		Integer v = map.get(key);
		if (v == null)
			map.put(key, inc);
		else
			map.put(key, v + inc);
	}
}
