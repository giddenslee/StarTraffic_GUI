package modeling.model.distribution;

public class DistributionExponential extends Distribution {
		
	public DistributionExponential(){
		this.precision = 100;
	}
	
	public DistributionExponential(Double precision) {
		this.precision = 100;
		
		for (int i=1;i<=100 && i<=precision;++i)
		{
			if (100%i==0)
				this.precision = (double) i;
		}
	}

	@Override
	public void addValue(double value, int times) {
		//区间定义[0] | (0,1],(1,2]...(99,100]} | {(100,110],(110,120],(120,130]...(990,1000]},... ||precision = 100
		if (value > 0) {
			double base = Math.pow(10, 1 + Math.ceil(Math.log10(value / 10)));
			if (base<100)
				base = 100;
			double bucketsize = base / precision;
			double curValue = Math.ceil(value / bucketsize) * bucketsize;
			double preValue = curValue - bucketsize;
			
			//System.out.println("value:"+value+"  base:"+base+"  preValue:"+preValue+"  curValue:"+curValue);
			
			countMapIncrease(valueCountMap, curValue, times);
			countMapIncrease(valueCountMap, preValue, 0);
		} else {
			countMapIncrease(valueCountMap, 0.0, times);
		}
		changeMinMax(value);
		++numValue;
	}

	@Override
	public void buildModel() {
		cdfBuild();
	}

	@Override
	public double predictValue() {
		if (cdf==null)
			buildModel();
		return cdfPredictValue();
	}
	
	public static void main(String[] args)
	{
		Distribution model = new DistributionExponential();
		

		model.addValue(10);
		model.addValue(88);
		model.addValue(200);
		model.addValue(210);
		model.addValue(400);
		model.addValue(1912);
		model.addValue(4832904802422.0);
		
		for (int i=0;i<1000;++i)
		{
			System.out.println(model.predictValue());
		}

		System.out.println(model.toPrettyString(false, 1.0));
	}

	@Override
	public int getParameterNumber() {
		return cdf.size()*2 + 2;
	}
}
