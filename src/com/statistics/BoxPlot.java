/* (c) Copyright 2001 and following years, Yann-Gal Guhneuc,
 * University of Montreal.
 *
 * Use and copying of this software and preparation of derivative works
 * based upon this software are permitted. Any copy of this software or
 * of any derivative work must include the above copyright notice of
 * the author, this paragraph and the one after it.
 *
 * This software is made available AS IS, and THE AUTHOR DISCLAIMS
 * ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, AND NOT WITHSTANDING ANY OTHER PROVISION CONTAINED HEREIN,
 * ANY LIABILITY FOR DAMAGES RESULTING FROM THE SOFTWARE OR ITS USE IS
 * EXPRESSLY DISCLAIMED, WHETHER ARISING IN CONTRACT, TORT (INCLUDING
 * NEGLIGENCE) OR STRICT LIABILITY, EVEN IF THE AUTHOR IS ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * All Rights Reserved.
 */

package com.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.operators.Operator;
import com.operators.OperatorFactory;

/**
 * This class represents a boxplot. We use the TUKEY definition of the boxplot
 * because it enables to detect extreme values.
 *
 * @author Naouel Moha
 * @version 2.0
 * @since 2006/02/14
 */

public class BoxPlot {

    public final static double TUKEY = 1.5;

    protected Map<String, Double> mapOfEntities = new HashMap<String, Double>();
    private Double[] sortedValues;
    
    private double fuzziness;
    private int nbValues;
    private double median;
    private double lowerQuartile;
    private double upperQuartile;
    private double interQuartileRange;
    private double minBound;
    private double maxBound;

    private String name;

    public BoxPlot(String name) {
        this.name = name;
    }

    public Map<String, Double> getEntries() {
        return this.mapOfEntities;
    }

    public void addEntry(String name, Double value) {
        this.mapOfEntities.put(name, value);
    }

    public void init(Map<String, Double> mapOfEntities, double fuzziness) {
        this.mapOfEntities = mapOfEntities;
        this.fuzziness = fuzziness;
        init();
    }

    public void init(){
        if (this.mapOfEntities.size() < 4) {
            for (int i = this.mapOfEntities.size(); i < 5; i++) {
                this.mapOfEntities.put("FakeValue", 0.0D);
            }
        }

        this.sortedValues = this.sortEntities(mapOfEntities);
        this.nbValues = this.sortedValues.length;
        this.median = getMedian();
        this.lowerQuartile = getLowerQuartile();
        this.upperQuartile = getUpperQuartile();
        this.interQuartileRange = getInterQuartileRange();
        this.minBound = getMinBound();
        this.maxBound = getMaxBound();

        // Calculate fuzziness value
        double minValue = this.sortedValues[0];
        double maxValue = this.sortedValues[this.sortedValues.length - 1];

        double range = maxValue - minValue;
        this.fuzziness = this.fuzziness * range / 100;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("# Results of the Boxplot " + name);
        s.append(" ###### ");
        s.append(this.nbValues + " values : \n");
        for (String el : this.mapOfEntities.keySet()){
        	s.append(el + " " + this.mapOfEntities.get(el) + "\n");
        }
        s.append("\n ###### ");
        s.append("\n Median        : " + this.median + " ");
        s.append("\n LowerQuartile : " + this.lowerQuartile + " ");
        s.append("\n UpperQuartile : " + this.upperQuartile + " ");
        s.append("\n InterQuartile : " + this.interQuartileRange + " ");
        s.append("\n MinBound      : " + this.minBound + " ");
        s.append("\n MaxBound      : " + this.maxBound + " ");
        s.append("\n\n Fuzziness   : " + this.fuzziness + " \n");

        return s.toString();
    }

    /**
     * Find value of pth sample percentile; can use to get median(p = 0.5)
     *
     * Defintion: A percentile is a value at or below which a given percentage
     * or fraction of the variable values lie. For a set of measurements
     * arranged in order of magnitude, the p-th percentile is the value that has
     * p% of the measurements below it and (100-p)% above it.
     *
     * Here the percentile is calculated according to SAS Method 4 For this
     * method, we use (n+1) instead of n. In that case, the p-th percentile is
     * defined by: y = (1-g)*x(j) +g*x(j+1), where (n+1)*p= j + g (and x(n+1) is
     * taken to be x(n)).
     */
    public double getPercentile(double p) {

        double percentile;

        double a = (this.nbValues + 1) * p;

        int j = (int) a;
        double g = a - j;

        if (j == this.nbValues) {
            percentile = (1 - g) * this.sortedValues[j - 1] + g * this.sortedValues[j - 1];
        } else if (j == 0) {
            percentile = (1 - g) * this.sortedValues[j] + g * this.sortedValues[j];
        } else {
            percentile = (1 - g) * this.sortedValues[j - 1] + g * this.sortedValues[j];
        }

        return percentile;
    }

    public double getMedian() {
        return this.getPercentile(0.5);
    }
    
    public double getLowerOutlier(){
    	return this.sortedValues[0];
    }
    
    public double getHigherOutlier(){
    	return this.sortedValues[this.nbValues-1];
    }

    // Do not remove!
    // Minitab method
    public double getLowerQuartile() {
        int lowerQuartile = (this.nbValues + 1) / 4;
        return this.sortedValues[lowerQuartile];
    }

    // Do not remove!
    // Another method
    // Minitab method
    // cf. http://mathworld.wolfram.com/Quartile.html
    public double getUpperQuartile() {
        // TODO: This computations do not work in the case where there are only
        // two classes... For example, using APSEC Example1.ptidej
        int upperQuartile = (3 * this.nbValues + 3) / 4;
        return (upperQuartile < this.sortedValues.length) ? this.sortedValues[upperQuartile] : 0.0;
    }

    public double getInterQuartileRange() {
        return getUpperQuartile() - getLowerQuartile();
    }

    public double getMinBound() {
        double min = this.lowerQuartile - (BoxPlot.TUKEY * this.interQuartileRange);

        // exception : if min is lower than the minimal value tolerated (here 0)
        // than min = minimal value tolerated
        return (min < 0) ? 0.0 : min ;
    }

    public double getMaxBound() {
        return (this.upperQuartile + (BoxPlot.TUKEY * this.interQuartileRange));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sort the values of the map and return a array of double
     */
    private Double[] sortEntities(Map<String, Double> mapOfEntities) {

        Double[] tab = new Double[mapOfEntities.size()];
        mapOfEntities.values().toArray(tab);
        Arrays.sort(tab);
        
        return tab;
    }
    
    public Map<String, Double> getValues (String opString, Double compare, Map<String, Double> initialMap) {
    	
    	Map<String, Double> entitiesMap = (initialMap == null) ? this.mapOfEntities : initialMap;
    	Map<String, Double> resultMap = new HashMap<String, Double>();
    	Operator op = OperatorFactory.getOperator(opString);
    	
    	for (String key : entitiesMap.keySet()){
        	Double value = entitiesMap.get(key);
        	if (op.exec(value, compare)){
        		resultMap.put(key, value);
        	}
        }
    	
    	return resultMap;
    }

    /**
     * Get high outliers (> max bound)
     */
    public Map<String, Double> getHighOutliers() {
        return this.getValues(">", this.maxBound - this.fuzziness, null);
    }

    /**
     * Get high values (> 75th percentile or upper quartile) (no high outliers)
     */
    public Map<String, Double> getHighValues() {
    	Map<String, Double> map = this.getValues(">=", this.upperQuartile - this.fuzziness, null);
    	return this.getValues("<=", this.maxBound + this.fuzziness, map);
    }

    /**
     * Get low outliers (< min bound)
     */
    public Map<String, Double> getLowOutliers() {
        return this.getValues("<=", this.minBound + this.fuzziness, null);
    }

    /**
     * Get low outliers (< 25th percentile or lower quartile) (no low outliers)
     */
    public Map<String, Double> getLowValues() {
    	Map<String, Double> map = this.getValues("<=", this.lowerQuartile + this.fuzziness, null);
        return this.getValues(">", this.minBound - this.fuzziness, map);
    }

    /**
     * Get normal values (> 25th and < 75th percentile of lower quartile) + low
     * outliers
     */
    public Map<String, Double> getNormalValues() {
    	Map<String, Double> map = this.getValues(">", this.lowerQuartile - this.fuzziness, null);
        return this.getValues("<", this.upperQuartile + this.fuzziness, map);
    }

    public Map<String, Double> getEqual(double threshold) {
    	Map<String, Double> map = this.getValues(">=", threshold - this.fuzziness, null);
        return this.getValues("<=", threshold + this.fuzziness, map);
    }

    public Map<String, Double> getGreater(double threshold) {
    	return this.getValues(">", threshold - this.fuzziness, null);
    }

    public Map<String, Double> getGreaterOrEqual(double threshold) {
    	return this.getValues(">=", threshold - this.fuzziness, null);
    }

    public Map<String, Double> getLess(double threshold) {
    	return this.getValues("<", threshold + this.fuzziness, null);
    }

    public Map<String, Double> getLessOrEqual(double threshold) {
    	return this.getValues("<=", threshold + this.fuzziness, null);
    }

}
