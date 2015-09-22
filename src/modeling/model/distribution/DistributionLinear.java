package modeling.model.distribution;

public class DistributionLinear extends Distribution{
	
	public DistributionLinear(){
		this.precision = 10;
	}
	
	public DistributionLinear(double bucketsize) {
		this.precision = bucketsize;
	}

	@Override
	public void addValue(double value, int times) {
		//区间定义[0],(0,gap],(gap,2*gap]...
		if (value>0)
		{
			double curValue = Math.ceil(value/precision)*precision;
			double preValue = curValue - precision;
			countMapIncrease(valueCountMap, curValue, times);
			countMapIncrease(valueCountMap, preValue, 0);			
		}
		else
		{
			countMapIncrease(valueCountMap, 0.0, times);
		}
		changeMinMax(value);
		++numValue;
	}

	@Override
	public double predictValue() {
		if (cdf==null)
			buildModel();
		return cdfPredictValue();
	}

	@Override
	public void buildModel() {
		cdfBuild();
	}
	
	public static void main(String[] args)
	{
		Distribution model = new DistributionLinear(10);
		
		model.addValue(200);
		model.addValue(210);
		model.addValue(400);
		
		for (int i=0;i<1000;++i)
		{
			System.out.println(model.predictValue());
		}
		
		System.out.println(model.toString());
	}

	@Override
	public int getParameterNumber() {
		// TODO Auto-generated method stub
		return cdf.size()*2+3;
	}
}
